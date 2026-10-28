package com.example

import com.example.util.sound.SoundEvent
import com.example.util.sound.SoundSynth
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class SoundSynthTest {

    @Test
    fun testEverySoundEventMapsToNonEmptyNoteList() {
        for (event in SoundEvent.values()) {
            val notes = SoundSynth.getNotes(event)
            assertTrue("Event $event must have notes", notes.isNotEmpty())
        }
    }

    @Test
    fun testBufferLengthMatchesTotalDuration() {
        for (event in SoundEvent.values()) {
            val notes = SoundSynth.getNotes(event)
            val totalDurationMs = notes.sumOf { it.durationMs }
            val pcm = SoundSynth.generatePcm(event)

            val expectedSampleCount = (SoundSynth.SAMPLE_RATE * totalDurationMs) / 1000
            val diff = abs(pcm.size - expectedSampleCount)
            // Error within 1 sample per note due to integer rounding per note
            assertTrue(
                "Buffer length ${pcm.size} should match total duration samples $expectedSampleCount within ${notes.size} samples for $event",
                diff <= notes.size
            )
        }
    }

    @Test
    fun testSamplesWithinRangeAndPeakLimit() {
        val peakLimit = (0.4 * Short.MAX_VALUE).toInt()
        for (event in SoundEvent.values()) {
            val pcm = SoundSynth.generatePcm(event)
            assertTrue("PCM buffer for $event should not be empty", pcm.isNotEmpty())

            var maxObserved = 0
            for (sample in pcm) {
                val absVal = abs(sample.toInt())
                if (absVal > maxObserved) maxObserved = absVal
            }

            assertTrue(
                "Max amplitude $maxObserved for $event must be <= $peakLimit (0.4 of Short.MAX_VALUE)",
                maxObserved <= peakLimit
            )
        }
    }

    @Test
    fun testFadeInAndFadeOutOnNotes() {
        for (event in SoundEvent.values()) {
            val notes = SoundSynth.getNotes(event)
            for (note in notes) {
                if (note.frequencyHz > 0 && note.amplitude > 0) {
                    val notePcm = SoundSynth.generateNotePcm(note)
                    assertTrue("Note buffer should have samples", notePcm.isNotEmpty())
                    // First sample must be ~0 (fade-in starts at 0)
                    assertEquals("First sample must be ~0", 0, notePcm[0].toInt())
                    // Last sample must be ~0 (fade-out ends at 0)
                    assertEquals("Last sample must be ~0", 0, notePcm[notePcm.size - 1].toInt())
                }
            }
        }
    }

    @Test
    fun testNonSilenceOnlyBufferForRealSounds() {
        val audibleEvents = listOf(
            SoundEvent.ANSWER_CORRECT,
            SoundEvent.ANSWER_WRONG,
            SoundEvent.OPTION_TICK,
            SoundEvent.EXAM_FINISHED
        )
        for (event in audibleEvents) {
            val pcm = SoundSynth.generatePcm(event)
            val hasSound = pcm.any { it != 0.toShort() }
            assertTrue("Event $event should produce audible non-zero samples", hasSound)
        }
    }

    @Test
    fun testExamTimeoutContainsSilentGap() {
        val notes = SoundSynth.getNotes(SoundEvent.EXAM_TIMEOUT)
        assertEquals(3, notes.size)
        // Middle note is silent (frequency 0 or amplitude 0)
        val middleNote = notes[1]
        assertTrue(
            "Middle note in EXAM_TIMEOUT must be silent",
            middleNote.frequencyHz == 0.0 || middleNote.amplitude == 0.0
        )
        assertTrue("Silent gap duration should be 60 ms", middleNote.durationMs == 60)

        // Generate individual notes and check middle is all zeros
        val middlePcm = SoundSynth.generateNotePcm(middleNote)
        assertTrue("Silent note buffer must have samples", middlePcm.isNotEmpty())
        assertTrue("Silent note must be all zeros", middlePcm.all { it == 0.toShort() })

        // Check full buffer has non-silent first and third parts
        val fullPcm = SoundSynth.generatePcm(SoundEvent.EXAM_TIMEOUT)
        val firstNoteSize = SoundSynth.noteSampleCount(notes[0].durationMs)
        val secondNoteSize = SoundSynth.noteSampleCount(notes[1].durationMs)

        val firstPart = fullPcm.sliceArray(0 until firstNoteSize)
        val gapPart = fullPcm.sliceArray(firstNoteSize until firstNoteSize + secondNoteSize)
        val thirdPart = fullPcm.sliceArray(firstNoteSize + secondNoteSize until fullPcm.size)

        assertTrue("First beep must have audible sound", firstPart.any { it != 0.toShort() })
        assertTrue("Middle gap in EXAM_TIMEOUT must be all zeros", gapPart.all { it == 0.toShort() })
        assertTrue("Second beep must have audible sound", thirdPart.any { it != 0.toShort() })
    }

    @Test
    fun testGenerationIsDeterministic() {
        for (event in SoundEvent.values()) {
            val pcm1 = SoundSynth.generatePcm(event)
            val pcm2 = SoundSynth.generatePcm(event)
            assertArrayEquals("Generation for $event must be deterministic", pcm1, pcm2)
        }
    }

    @Test
    fun testShouldPlayTruthTable() {
        assertTrue("Both true should be true", SoundSynth.shouldPlay(soundEnabled = true, ringerModeNormal = true))
        assertFalse("Disabled sound should be false", SoundSynth.shouldPlay(soundEnabled = false, ringerModeNormal = true))
        assertFalse("Non-normal ringer should be false", SoundSynth.shouldPlay(soundEnabled = true, ringerModeNormal = false))
        assertFalse("Both false should be false", SoundSynth.shouldPlay(soundEnabled = false, ringerModeNormal = false))
    }

    @Test
    fun testDurationMs() {
        assertEquals(1000L, SoundSynth.durationMs(SoundSynth.SAMPLE_RATE))
        val answerCorrectPcm = SoundSynth.generatePcm(SoundEvent.ANSWER_CORRECT)
        val duration = SoundSynth.durationMs(answerCorrectPcm.size)
        // Expected duration ~270 ms
        assertTrue("ANSWER_CORRECT duration should be around 270 ms", duration in 260L..280L)
    }
}
