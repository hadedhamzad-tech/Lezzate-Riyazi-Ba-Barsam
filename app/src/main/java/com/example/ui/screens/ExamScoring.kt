package com.example.ui.screens

import java.util.Locale

data class ExamScoreResult(
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val blankCount: Int,
    val rawScore: Int,
    val maxScore: Int,
    val percentage: Double
) {
    val formattedPercentage: String
        get() = String.format(Locale.US, "%.1f", percentage) + "%"
}

object ExamScoring {
    fun score(correct: Int, wrong: Int, total: Int): ExamScoreResult {
        val blank = maxOf(0, total - correct - wrong)
        val raw = 3 * correct - wrong
        val max = 3 * total
        val percent = if (max == 0) 0.0 else (raw.toDouble() / max.toDouble()) * 100.0
        return ExamScoreResult(
            totalQuestions = total,
            correctCount = correct,
            wrongCount = wrong,
            blankCount = blank,
            rawScore = raw,
            maxScore = max,
            percentage = percent
        )
    }
}
