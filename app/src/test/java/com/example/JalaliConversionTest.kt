package com.example

import com.example.util.JalaliCalendarUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JalaliConversionTest {

    @Test
    fun testEpochDayZeroAndRoundTrip() {
        // epoch day of 1970-01-01 is 0
        val epoch1970 = JalaliCalendarUtils.gregorianToEpochDay(1970, 1, 1)
        assertEquals(0L, epoch1970)

        val date1970 = JalaliCalendarUtils.epochDayToGregorian(0L)
        assertEquals(Triple(1970, 1, 1), date1970)

        // epochDay round trip across sample range
        for (day in -10000L..25000L step 37) {
            val (y, m, d) = JalaliCalendarUtils.epochDayToGregorian(day)
            val convertedBack = JalaliCalendarUtils.gregorianToEpochDay(y, m, d)
            assertEquals("Round trip failed for epoch day $day", day, convertedBack)
        }
    }

    @Test
    fun testJalaliToGregorianAnchors() {
        // (1405,1,1)->(2026,3,21)
        val g1 = JalaliCalendarUtils.jalaliToGregorian(1405, 1, 1)
        assertEquals(Triple(2026, 3, 21), g1)

        // (1406,1,1)->(2027,3,21)
        val g2 = JalaliCalendarUtils.jalaliToGregorian(1406, 1, 1)
        assertEquals(Triple(2027, 3, 21), g2)

        // (1406,4,11)->(2027,7,2)
        val g3 = JalaliCalendarUtils.jalaliToGregorian(1406, 4, 11)
        assertEquals(Triple(2027, 7, 2), g3)
    }

    @Test
    fun testRoundTripEveryDayFrom2026To2030() {
        val startEpoch = JalaliCalendarUtils.gregorianToEpochDay(2026, 1, 1)
        val endEpoch = JalaliCalendarUtils.gregorianToEpochDay(2030, 12, 31)

        for (epoch in startEpoch..endEpoch) {
            val (gy, gm, gd) = JalaliCalendarUtils.epochDayToGregorian(epoch)
            val jDate = JalaliCalendarUtils.gregorianToJalali(gy, gm, gd)
            val (gy2, gm2, gd2) = JalaliCalendarUtils.jalaliToGregorian(jDate.year, jDate.month, jDate.day)
            assertEquals("Gregorian year mismatch for $epoch ($gy-$gm-$gd)", gy, gy2)
            assertEquals("Gregorian month mismatch for $epoch ($gy-$gm-$gd)", gm, gm2)
            assertEquals("Gregorian day mismatch for $epoch ($gy-$gm-$gd)", gd, gd2)
        }
    }

    @Test
    fun testValidityRules() {
        // (1405,12,29) valid
        assertTrue(JalaliCalendarUtils.isValidJalaliDate(1405, 12, 29))
        // (1405,12,30) invalid
        assertFalse(JalaliCalendarUtils.isValidJalaliDate(1405, 12, 30))
        // (1403,12,30) valid
        assertTrue(JalaliCalendarUtils.isValidJalaliDate(1403, 12, 30))
        // (1404,12,30) invalid
        assertFalse(JalaliCalendarUtils.isValidJalaliDate(1404, 12, 30))
        // (1408,12,30) valid
        assertTrue(JalaliCalendarUtils.isValidJalaliDate(1408, 12, 30))
        // (1406,6,31) valid
        assertTrue(JalaliCalendarUtils.isValidJalaliDate(1406, 6, 31))
        // (1406,7,31) invalid
        assertFalse(JalaliCalendarUtils.isValidJalaliDate(1406, 7, 31))
        // (1406,13,1) invalid
        assertFalse(JalaliCalendarUtils.isValidJalaliDate(1406, 13, 1))
        // (1406,0,1) invalid
        assertFalse(JalaliCalendarUtils.isValidJalaliDate(1406, 0, 1))
        // (1406,1,0) invalid
        assertFalse(JalaliCalendarUtils.isValidJalaliDate(1406, 1, 0))
    }

    @Test
    fun testDaysInJalaliMonth() {
        assertEquals(31, JalaliCalendarUtils.daysInJalaliMonth(1405, 1))
        assertEquals(31, JalaliCalendarUtils.daysInJalaliMonth(1405, 6))
        assertEquals(30, JalaliCalendarUtils.daysInJalaliMonth(1405, 7))
        assertEquals(30, JalaliCalendarUtils.daysInJalaliMonth(1405, 11))
        assertEquals(29, JalaliCalendarUtils.daysInJalaliMonth(1405, 12)) // non-leap
        assertEquals(30, JalaliCalendarUtils.daysInJalaliMonth(1403, 12)) // leap
        assertEquals(30, JalaliCalendarUtils.daysInJalaliMonth(1408, 12)) // leap
    }
}
