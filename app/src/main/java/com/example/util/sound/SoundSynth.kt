package com.example.util.sound

import kotlin.math.PI
import kotlin.math.sin

data class SynthNote(
    val frequencyHz: Double,
    val durationMs: Int,
    val amplitude: Double = 1.0
)

object SoundSynth {
    const val SAMPLE_RATE = 22050
    const val PEAK_AMPLITUDE_RATIO = 0.35
    const val FADE_MS = 8.0

    fun shouldPlay(soundEnabled: Boolean, ringerModeNormal: Boolean): Boolean {
        return soundEnabled && ringerModeNormal
    }

    fun getNotes(event: SoundEvent): List<SynthNote> {
        return when (event) {
            SoundEvent.ANSWER_CORRECT -> listOf(
                SynthNote(frequencyHz = 659.0, durationMs = 110, amplitude = 1.0),
                SynthNote(frequencyHz = 880.0, durationMs = 160, amplitude = 1.0)
            )
            SoundEvent.ANSWER_WRONG -> listOf(
                SynthNote(frequencyHz = 330.0, durationMs = 110, amplitude = 0.65),
                SynthNote(frequencyHz = 247.0, durationMs = 180, amplitude = 0.65)
            )
            SoundEvent.OPTION_TICK -> listOf(
                SynthNote(frequencyHz = 800.0, durationMs = 35, amplitude = 0.45)
            )
            SoundEvent.EXAM_FINISHED -> listOf(
                SynthNote(frequencyHz = 523.0, durationMs = 120, amplitude = 1.0),
                SynthNote(frequencyHz = 659.0, durationMs = 120, amplitude = 1.0),
                SynthNote(frequencyHz = 784.0, durationMs = 120, amplitude = 1.0),
                SynthNote(frequencyHz = 1047.0, durationMs = 220, amplitude = 1.0)
            )
            SoundEvent.EXAM_TIMEOUT -> listOf(
                SynthNote(frequencyHz = 440.0, durationMs = 140, amplitude = 0.8),
                SynthNote(frequencyHz = 0.0, durationMs = 60, amplitude = 0.0),
                SynthNote(frequencyHz = 440.0, durationMs = 140, amplitude = 0.8)
            )
        }
    }

    fun noteSampleCount(durationMs: Int): Int {
        return ((SAMPLE_RATE * durationMs + 500) / 1000).coerceAtLeast(1)
    }

    fun generateNotePcm(note: SynthNote): ShortArray {
        val n = noteSampleCount(note.durationMs)
        val pcm = ShortArray(n)
        if (note.frequencyHz <= 0.0 || note.amplitude <= 0.0) {
            return pcm
        }

        val fadeSamples = minOf(n / 2, (SAMPLE_RATE * (FADE_MS / 1000.0)).toInt()).coerceAtLeast(1)
        val maxAmp = (Short.MAX_VALUE * PEAK_AMPLITUDE_RATIO * note.amplitude.coerceIn(0.0, 1.0))

        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val fade = when {
                i < fadeSamples -> i.toDouble() / fadeSamples.toDouble()
                i >= n - fadeSamples -> (n - 1 - i).toDouble() / fadeSamples.toDouble()
                else -> 1.0
            }
            val sampleVal = (sin(2.0 * PI * note.frequencyHz * t) * fade * maxAmp).toInt()
            pcm[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return pcm
    }

    fun generatePcm(notes: List<SynthNote>): ShortArray {
        if (notes.isEmpty()) return ShortArray(0)
        val noteBuffers = notes.map { generateNotePcm(it) }
        val totalSize = noteBuffers.sumOf { it.size }
        val result = ShortArray(totalSize)
        var offset = 0
        for (buf in noteBuffers) {
            System.arraycopy(buf, 0, result, offset, buf.size)
            offset += buf.size
        }
        return result
    }

    fun generatePcm(event: SoundEvent): ShortArray {
        return generatePcm(getNotes(event))
    }
}
