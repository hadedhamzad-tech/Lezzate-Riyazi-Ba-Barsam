package com.example.util.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.example.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class AppSoundPlayer(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val pcmCache = ConcurrentHashMap<SoundEvent, ShortArray>()
    private val activeTracks = Collections.synchronizedSet(mutableSetOf<AudioTrack>())

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val audioFormat = AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setSampleRate(SoundSynth.SAMPLE_RATE)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .build()

    init {
        // Continuously observe sound enabled setting to keep fast local access updated
        coroutineScope.launch {
            try {
                settingsRepository.userSettingsFlow.collect { settings ->
                    settingsRepository.isSoundEnabled = settings.soundEnabled
                }
            } catch (_: Throwable) {
                // Ignore collection errors
            }
        }
    }

    fun play(event: SoundEvent) {
        coroutineScope.launch {
            try {
                val ringerModeNormal = audioManager?.ringerMode == AudioManager.RINGER_MODE_NORMAL
                val isSoundEnabled = settingsRepository.isSoundEnabled
                if (!SoundSynth.shouldPlay(soundEnabled = isSoundEnabled, ringerModeNormal = ringerModeNormal)) {
                    return@launch
                }

                val pcm = pcmCache.computeIfAbsent(event) { SoundSynth.generatePcm(it) }
                if (pcm.isEmpty()) return@launch

                val sizeInBytes = pcm.size * 2
                val track = AudioTrack(
                    audioAttributes,
                    audioFormat,
                    sizeInBytes,
                    AudioTrack.MODE_STATIC,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
                )

                if (track.state != AudioTrack.STATE_INITIALIZED) {
                    track.release()
                    return@launch
                }

                track.write(pcm, 0, pcm.size)
                activeTracks.add(track)

                track.setNotificationMarkerPosition(pcm.size)
                track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                    override fun onMarkerReached(t: AudioTrack?) {
                        try {
                            t?.stop()
                            t?.release()
                        } catch (_: Throwable) {}
                        activeTracks.remove(t)
                    }

                    override fun onPeriodicNotification(t: AudioTrack?) {}
                })

                track.play()
            } catch (_: Throwable) {
                // Sound failure must never crash the app or affect the UI
            }
        }
    }

    fun release() {
        try {
            synchronized(activeTracks) {
                for (track in activeTracks) {
                    try {
                        track.stop()
                        track.release()
                    } catch (_: Throwable) {}
                }
                activeTracks.clear()
            }
            coroutineScope.cancel()
        } catch (_: Throwable) {}
    }
}
