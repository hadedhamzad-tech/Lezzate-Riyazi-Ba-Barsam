package com.example

import com.example.util.PersianUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

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
        val expectedFormula = "${PersianUtils.LRI}f(x)\u00A0=\u00A03x²\u00A0+\u00A01${PersianUtils.PDI}"
        assertTrue("Formatted text should contain LRI...PDI wrapped formula with NBSP", formatted.contains(expectedFormula))
    }

    @Test
    fun testFormulaWithIntersectionAndUnionWrappedAsOneRun() {
        val text = "مقدار n(A ∩ B ∪ C) را بیابید."
        val formatted = PersianUtils.formatMathText(text)
        val expected = "${PersianUtils.LRI}n(A\u00A0∩\u00A0B\u00A0∪\u00A0C)${PersianUtils.PDI}"
        assertTrue("Text with ∩ and ∪ inside formula run should be wrapped as one run", formatted.contains(expected))
    }

    @Test
    fun testUnmatchedClosingParenthesisInPersianContextStaysOutside() {
        // (a) "(مساحت 12)" -> opening and closing parentheses are outside the LRI/PDI run.
        val input = "(مساحت 12)"
        val formatted = PersianUtils.formatMathText(input)
        val expected = "(مساحت ${PersianUtils.LRI}12${PersianUtils.PDI})"
        assertEquals(expected, formatted)
        assertTrue(formatted.startsWith("("))
        assertTrue(formatted.endsWith(")"))
        assertFalse(formatted.contains("${PersianUtils.LRI}("))
        assertFalse(formatted.contains(")${PersianUtils.PDI}"))
    }

    @Test
    fun testUnmatchedOpeningParenthesisStaysOutside() {
        // (b) "(12 متر" -> opening parenthesis is outside.
        val input = "(12 متر"
        val formatted = PersianUtils.formatMathText(input)
        val expected = "(${PersianUtils.LRI}12${PersianUtils.PDI} متر"
        assertEquals(expected, formatted)
        assertTrue(formatted.startsWith("("))
        assertFalse(formatted.contains("${PersianUtils.LRI}("))
    }

    @Test
    fun testBalancedParenthesesArePreservedInSingleMathRun() {
        // (c) "(g° f)(-3)" -> whole thing is one math run (both pairs of parens balanced).
        val input = "(g° f)(-3)"
        val formatted = PersianUtils.formatMathText(input)
        val expected = "${PersianUtils.LRI}(g°\u00A0f)(-3)${PersianUtils.PDI}"
        assertEquals(expected, formatted)
    }

    @Test
    fun testIntervalExpressionWithBracketsIsPreserved() {
        // (d) "[-4, -1) ∪ (-1, +∞)" -> whole interval expression is preserved.
        val input = "[-4, -1) ∪ (-1, +∞)"
        val formatted = PersianUtils.formatMathText(input)
        val expected = "${PersianUtils.LRI}[-4,\u00A0-1)\u00A0∪\u00A0(-1,\u00A0+∞)${PersianUtils.PDI}"
        assertEquals(expected, formatted)
    }

    @Test
    fun testShortFormulaReplacesSpacesWithNbsp() {
        // (e) formula with spaces <= 40 chars gets U+00A0 instead of ' '.
        val input = "(a, b)→(a, -b)"
        assertTrue(input.length <= 40)
        val formatted = PersianUtils.formatMathText(input)
        val innerFormula = formatted.substringAfter(PersianUtils.LRI).substringBefore(PersianUtils.PDI)
        assertTrue("Formula <= 40 chars should contain NBSP", innerFormula.contains('\u00A0'))
        assertFalse("Formula <= 40 chars should not contain ASCII space", innerFormula.contains(' '))
        assertEquals("(a,\u00A0b)→(a,\u00A0-b)", innerFormula)
    }

    @Test
    fun testLongFormulaPreservesAsciiSpaces() {
        // (f) formula with spaces > 40 chars keeps ASCII ' '.
        val input = "f(x) = a_1 x^10 + a_2 x^9 + a_3 x^8 + a_4 x^7 + a_5 x^6"
        assertTrue(input.length > 40)
        val formatted = PersianUtils.formatMathText(input)
        val innerFormula = formatted.substringAfter(PersianUtils.LRI).substringBefore(PersianUtils.PDI)
        assertTrue("Formula > 40 chars should keep ASCII spaces", innerFormula.contains(' '))
        assertFalse("Formula > 40 chars should not contain NBSP", innerFormula.contains('\u00A0'))
        assertEquals(input, innerFormula)
    }

    @Test
    fun testFormatMathTextIsIdempotentOnAllCases() {
        // (g) idempotency check on every test case: formatMathText(formatMathText(s)) == formatMathText(s).
        val cases = listOf(
            "(مساحت 12)",
            "(12 متر",
            "(g° f)(-3)",
            "[-4, -1) ∪ (-1, +∞)",
            "(a, b)→(a, -b)",
            "f(x) = a_1 x^10 + a_2 x^9 + a_3 x^8 + a_4 x^7 + a_5 x^6",
            "اگر تابع در نقطه پیوسته باشد آنگاه حد چپ و راست برابر است.",
            "اگر f(x) = 3x² + 1 باشد، مقدار آن را بیابید.",
            "مقدار n(A ∩ B ∪ C) را بیابید.",
            "فاصلهٔ دو نقطهٔ A(1, -2) و B(6, 10) چقدر است؟"
        )
        for (case in cases) {
            val once = PersianUtils.formatMathText(case)
            val twice = PersianUtils.formatMathText(once)
            assertEquals("formatMathText must be idempotent for case: $case", once, twice)
        }
    }

    @Test
    fun testNoUiFileCallsToPersianDigits() {
        val candidates = listOf(
            File("src/main/java/com/example/ui"),
            File("app/src/main/java/com/example/ui"),
            File("../app/src/main/java/com/example/ui")
        )
        val uiDir = candidates.firstOrNull { it.exists() && it.isDirectory }
        assertNotNull("ui directory must exist for testing", uiDir)
        val violatingFiles = mutableListOf<String>()
        uiDir!!.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { file ->
            val content = file.readText()
            if (content.contains("toPersianDigits")) {
                violatingFiles.add("${file.name} calls toPersianDigits")
            }
        }
        assertTrue(
            "No UI file should call toPersianDigits. Found violations in: $violatingFiles",
            violatingFiles.isEmpty()
        )
    }
}
