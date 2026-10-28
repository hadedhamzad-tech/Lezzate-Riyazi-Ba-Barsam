package com.example.util

object PersianUtils {
    const val LRI = '\u2066'
    const val PDI = '\u2069'
    private const val MATH_SYMBOLS = "+-−*·×/÷=≠<>≤≥^√π∞→|±∓²³⁴ⁿ⁻⁺⁰¹₂₃₄°~Δαβθλμσω∪∩∠≡∨∧∈"
    private const val MATH_CLOSING_DELIMITERS = ")]}|'\""

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
     * and Pop Directional Isolate (PDI U+2069).
     */
    fun wrapMathFormula(formula: String): String {
        return "$LRI$formula$PDI"
    }

    fun isPersian(c: Char): Boolean {
        val code = c.code
        return (code in 0x0600..0x06FF) || (code in 0x0750..0x077F) || (code in 0x08A0..0x08FF) ||
               (code in 0xFB50..0xFDFF) || (code in 0xFE70..0xFEFF)
    }

    private const val OPENING_BRACKETS = "([{"
    private const val CLOSING_BRACKETS = ")]}"

    fun isCoreMathChar(c: Char): Boolean =
        (c in 'a'..'z') || (c in 'A'..'Z') || (c in '0'..'9') || (c in MATH_SYMBOLS)

    fun isMathAllowedChar(c: Char): Boolean =
        isCoreMathChar(c) || (c in "()[]{}(),.:;!_'\" \t\n\u00A0")

    fun canStartMath(c: Char): Boolean =
        isCoreMathChar(c) || (c in "([{|")

    fun canEndMath(c: Char): Boolean =
        (c in 'a'..'z') || (c in 'A'..'Z') || (c in '0'..'9') ||
        (c in MATH_CLOSING_DELIMITERS) || (c in "²³⁴ⁿ⁻⁺⁰¹₂₃₄°π∞Δαβθλμσω")

    private fun isOpener(c: Char): Boolean = c in OPENING_BRACKETS
    private fun isCloser(c: Char): Boolean = c in CLOSING_BRACKETS

    fun formatMathText(input: String?): String {
        if (input.isNullOrEmpty()) return input ?: ""
        val text = input.replace(LRI.toString(), "").replace(PDI.toString(), "")
        val sb = StringBuilder()
        var i = 0
        val n = text.length
        while (i < n) {
            val c = text[i]
            if (canStartMath(c) && !isPersian(c)) {
                var j = i
                var lastValidEnd = -1
                var hasCoreMath = false
                var depth = 0
                while (j < n) {
                    val cur = text[j]
                    if (isPersian(cur) || !isMathAllowedChar(cur)) break
                    if (isCloser(cur)) {
                        if (depth == 0) {
                            // Unmatched closing bracket: stays outside the candidate run
                            break
                        }
                        depth--
                    } else if (isOpener(cur)) {
                        depth++
                    }
                    if (isCoreMathChar(cur)) hasCoreMath = true
                    if (depth == 0 && canEndMath(cur) && hasCoreMath) {
                        lastValidEnd = j
                    }
                    j++
                }
                if (lastValidEnd >= i && hasCoreMath) {
                    val rawRun = text.substring(i, lastValidEnd + 1)
                    val processedRun = if (rawRun.length <= 40) {
                        rawRun.replace(' ', '\u00A0')
                    } else {
                        rawRun.replace('\u00A0', ' ')
                    }
                    sb.append(LRI).append(processedRun).append(PDI)
                    i = lastValidEnd + 1
                    continue
                }
            }
            sb.append(c)
            i++
        }
        return sb.toString()
    }
}
