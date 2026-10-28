package com.example

import com.example.ui.screens.ExamResultFormatter
import com.example.util.PersianUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamResultFormatterTest {

    @Test
    fun testNegativeAndPositivePercentagesWrappedInLriPdi() {
        val neg = ExamResultFormatter.formatDisplayPercent("-20.0%")
        assertEquals("${PersianUtils.LRI}-20.0%${PersianUtils.PDI}", neg)
        assertTrue("Must start with LRI", neg.startsWith(PersianUtils.LRI))
        assertTrue("Must end with PDI", neg.endsWith(PersianUtils.PDI))
        assertEquals("-20.0%", neg.substring(1, neg.length - 1))

        val pos = ExamResultFormatter.formatDisplayPercent("63.3%")
        assertEquals("${PersianUtils.LRI}63.3%${PersianUtils.PDI}", pos)
        assertTrue("Must start with LRI", pos.startsWith(PersianUtils.LRI))
        assertTrue("Must end with PDI", pos.endsWith(PersianUtils.PDI))
        assertEquals("63.3%", pos.substring(1, pos.length - 1))
    }

    @Test
    fun testRawScoreNegativeAndPositiveWrapping() {
        val negNumber = ExamResultFormatter.formatRawScoreNumber(-6)
        assertEquals("${PersianUtils.LRI}-6${PersianUtils.PDI}", negNumber)

        val negLine = ExamResultFormatter.formatRawScoreText(-6, 30)
        assertEquals("نمره: ${PersianUtils.LRI}-6${PersianUtils.PDI} از 30", negLine)

        val posNumber = ExamResultFormatter.formatRawScoreNumber(19)
        assertEquals("${PersianUtils.LRI}19${PersianUtils.PDI}", posNumber)

        val posLine = ExamResultFormatter.formatRawScoreText(19, 30)
        assertEquals("نمره: ${PersianUtils.LRI}19${PersianUtils.PDI} از 30", posLine)
    }
}
