package com.example

import com.example.util.PersianUtils
import org.junit.Assert.assertEquals
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
}
