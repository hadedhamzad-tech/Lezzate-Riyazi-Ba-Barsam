package com.example.util

object PersianUtils {
    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    /**
     * Converts ASCII digits to Persian digits.
     */
    fun toPersianDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (char in input) {
            if (char in '0'..'9') {
                sb.append(PERSIAN_DIGITS[char - '0'])
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }

    /**
     * Wraps mathematical formulas in Unicode Left-to-Right Isolate (LRI U+2066)
     * and Pop Directional Isolate (PDI U+2069) to ensure formulas render LTR with Latin digits
     * inside RTL Persian paragraphs.
     */
    fun wrapMathFormula(formula: String): String {
        return "\u2066$formula\u2069"
    }
}
