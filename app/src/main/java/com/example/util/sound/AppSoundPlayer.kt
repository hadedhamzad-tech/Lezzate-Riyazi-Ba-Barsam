package com.example.util.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import com.example.data.settings.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "AppSound"

class AppSoundPlayer(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val pcmCache = ConcurrentHashMap<SoundEvent, ShortArray>()
    private val activeTracks = Collections.synchronizedSet(mutableSetOf<AudioTrack>())

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
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
            var track: AudioTrack? = null
            try {
                val ringerModeNormal = audioManager?.ringerMode == AudioManager.RINGER_MODE_NORMAL
                if (!SoundSynth.shouldPlay(settingsRepository.isSoundEnabled, ringerModeNormal)) return@launch
                val pcm = pcmCache.computeIfAbsent(event) { SoundSynth.generatePcm(it) }
                if (pcm.isEmpty()) return@launch

                val t = AudioTrack(audioAttributes, audioFormat, pcm.size * 2,
                    AudioTrack.MODE_STATIC, AudioManager.AUDIO_SESSION_ID_GENERATE)
                track = t
                // MODE_STATIC: state is STATE_NO_STATIC_DATA until write() is called.
                if (t.state == AudioTrack.STATE_UNINITIALIZED) {
                    Log.w(TAG, "AudioTrack uninitialized for $event"); return@launch
                }
                val written = t.write(pcm, 0, pcm.size)
                if (written != pcm.size || t.state != AudioTrack.STATE_INITIALIZED) {
                    Log.w(TAG, "write failed for $event: written=$written state=${t.state}"); return@launch
                }
                activeTracks.add(t)
                t.play()
                delay(SoundSynth.durationMs(pcm.size) + 150L)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.w(TAG, "play failed for $event", e)   // never crash the UI
            } finally {
                track?.let {
                    activeTracks.remove(it)
                    try { it.stop() } catch (_: Throwable) {}
                    try { it.release() } catch (_: Throwable) {}
                }
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
