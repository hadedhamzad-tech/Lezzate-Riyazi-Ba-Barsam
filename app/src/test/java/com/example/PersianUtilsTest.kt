package com.example

import com.example.util.PersianUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersianUtilsTest {

    @Test
    fun testToPersianDigits() {
        val input = "Test 123"
        val expected = "Test ۱۲۳"
        assertEquals(expected, PersianUtils.toPersianDigits(input))
    }

    @Test
    fun testWrapMathFormula() {
        val formula = "x^2 + y^2 = r^2"
        val expected = "\u2066$formula\u2069"
        assertEquals(expected, PersianUtils.wrapMathFormula(formula))
    }

    @Test
    fun testPersianOnlyTextIsUnchanged() {
        val persianText = "اگر تابع در نقطه پیوسته باشد آنگاه حد چپ و راست برابر است."
        val formatted = PersianUtils.formatMathText(persianText)
        assertEquals(persianText, formatted)
    }

    @Test
    fun testFormulaInsidePersianTextIsWrappedInLriPdi() {
        val text = "اگر f(x) = 3x² + 1 باشد، مقدار آن را بیابید."
        val formatted = PersianUtils.formatMathText(text)
        val expectedFormula = "${PersianUtils.LRI}f(x) = 3x² + 1${PersianUtils.PDI}"
        assertTrue("Formatted text should contain LRI...PDI wrapped formula", formatted.contains(expectedFormula))
    }

    @Test
    fun testFormatMathTextIsIdempotent() {
        val text = "اگر f(x) = 3x² + x - 8 باشد، f'(1) چند است؟"
        val once = PersianUtils.formatMathText(text)
        val twice = PersianUtils.formatMathText(once)
        assertEquals("formatMathText must be idempotent", once, twice)
    }

    @Test
    fun testFormulaWithIntersectionAndUnionWrappedAsOneRun() {
        val text = "مقدار n(A ∩ B ∪ C) را بیابید."
        val formatted = PersianUtils.formatMathText(text)
        val expected = "${PersianUtils.LRI}n(A ∩ B ∪ C)${PersianUtils.PDI}"
        assertTrue("Text with ∩ and ∪ inside formula run should be wrapped as one run", formatted.contains(expected))
    }
}
