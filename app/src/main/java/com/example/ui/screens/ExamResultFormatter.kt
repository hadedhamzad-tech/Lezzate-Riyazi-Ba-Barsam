package com.example.ui.screens

import com.example.util.PersianUtils

object ExamResultFormatter {

    /**
     * Wraps the formatted percentage string (e.g. "-20.0%" or "63.3%") in LRI/PDI isolates
     * so that negative percentages and the percent symbol are displayed in correct LTR order
     * inside RTL text containers without sign inversion.
     */
    fun formatDisplayPercent(formattedPercentage: String): String {
        return PersianUtils.wrapMathFormula(formattedPercentage)
    }

    /**
     * Wraps raw score integer (e.g. -6 or 19) in LRI/PDI isolates so negative scores
     * keep the minus sign on the left inside RTL text.
     */
    fun formatRawScoreNumber(rawScore: Int): String {
        return PersianUtils.wrapMathFormula(rawScore.toString())
    }

    /**
     * Formats the full raw score line for the exam result screen:
     * e.g. "نمره: \u2066-6\u2069 از 30"
     */
    fun formatRawScoreText(rawScore: Int, maxScore: Int): String {
        return "نمره: ${formatRawScoreNumber(rawScore)} از $maxScore"
    }
}
