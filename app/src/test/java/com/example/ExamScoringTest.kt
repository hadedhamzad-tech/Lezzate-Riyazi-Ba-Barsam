package com.example

import com.example.ui.screens.ExamScoring
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamScoringTest {

    @Test
    fun testAllCorrect() {
        val result = ExamScoring.score(correct = 10, wrong = 0, total = 10)
        assertEquals(10, result.totalQuestions)
        assertEquals(10, result.correctCount)
        assertEquals(0, result.wrongCount)
        assertEquals(0, result.blankCount)
        assertEquals(30, result.rawScore)
        assertEquals(30, result.maxScore)
        assertEquals(100.0, result.percentage, 0.001)
        assertEquals("100.0%", result.formattedPercentage)
    }

    @Test
    fun testAllWrong() {
        val result = ExamScoring.score(correct = 0, wrong = 10, total = 10)
        assertEquals(10, result.totalQuestions)
        assertEquals(0, result.correctCount)
        assertEquals(10, result.wrongCount)
        assertEquals(0, result.blankCount)
        assertEquals(-10, result.rawScore)
        assertEquals(30, result.maxScore)
        assertEquals(-33.333, result.percentage, 0.01)
        assertEquals("-33.3%", result.formattedPercentage)
    }

    @Test
    fun testAllBlank() {
        val result = ExamScoring.score(correct = 0, wrong = 0, total = 10)
        assertEquals(10, result.totalQuestions)
        assertEquals(0, result.correctCount)
        assertEquals(0, result.wrongCount)
        assertEquals(10, result.blankCount)
        assertEquals(0, result.rawScore)
        assertEquals(30, result.maxScore)
        assertEquals(0.0, result.percentage, 0.001)
        assertEquals("0.0%", result.formattedPercentage)
    }

    @Test
    fun testMixedKonkurExampleFromPrompt() {
        // Prompt requirement: percent for N=10 with 7 correct, 2 wrong, 1 blank (score 19 of 30, 63.3%)
        val result = ExamScoring.score(correct = 7, wrong = 2, total = 10)
        assertEquals(10, result.totalQuestions)
        assertEquals(7, result.correctCount)
        assertEquals(2, result.wrongCount)
        assertEquals(1, result.blankCount)
        assertEquals(19, result.rawScore)
        assertEquals(30, result.maxScore)
        assertEquals(63.333, result.percentage, 0.01)
        assertEquals("63.3%", result.formattedPercentage)
    }

    @Test
    fun testNegativePercentageCase() {
        // N=10 with 1 correct, 9 wrong -> score 3 - 9 = -6 of 30 -> -20.0%
        val result = ExamScoring.score(correct = 1, wrong = 9, total = 10)
        assertEquals(-6, result.rawScore)
        assertEquals(30, result.maxScore)
        assertEquals(-20.0, result.percentage, 0.001)
        assertEquals("-20.0%", result.formattedPercentage)
    }

    @Test
    fun testEdgeCaseZeroQuestions() {
        val result = ExamScoring.score(correct = 0, wrong = 0, total = 0)
        assertEquals(0, result.totalQuestions)
        assertEquals(0, result.rawScore)
        assertEquals(0, result.maxScore)
        assertEquals(0.0, result.percentage, 0.001)
        assertEquals("0.0%", result.formattedPercentage)
    }

    @Test
    fun testEdgeCaseOneQuestionCorrect() {
        val result = ExamScoring.score(correct = 1, wrong = 0, total = 1)
        assertEquals(1, result.totalQuestions)
        assertEquals(3, result.rawScore)
        assertEquals(3, result.maxScore)
        assertEquals(100.0, result.percentage, 0.001)
    }

    @Test
    fun testEdgeCaseOneQuestionWrong() {
        val result = ExamScoring.score(correct = 0, wrong = 1, total = 1)
        assertEquals(1, result.totalQuestions)
        assertEquals(-1, result.rawScore)
        assertEquals(3, result.maxScore)
        assertEquals(-33.333, result.percentage, 0.01)
        assertEquals("-33.3%", result.formattedPercentage)
    }
}
