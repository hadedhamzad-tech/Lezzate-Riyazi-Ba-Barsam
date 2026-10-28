package com.example

import com.example.ui.screens.DashboardTexts
import com.example.util.JalaliCalendarUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardTest {

    @Test
    fun testJalaliDateConversionKnownDates() {
        // Known date 1: 2026-10-28 -> 1405/08/06
        val d1 = JalaliCalendarUtils.gregorianToJalali(2026, 10, 28)
        assertEquals(1405, d1.year)
        assertEquals(8, d1.month)
        assertEquals(6, d1.day)

        // Known date 2: 2026-03-21 -> 1405/01/01
        val d2 = JalaliCalendarUtils.gregorianToJalali(2026, 3, 21)
        assertEquals(1405, d2.year)
        assertEquals(1, d2.month)
        assertEquals(1, d2.day)

        // Known date 3: 2025-03-21 -> 1404/01/01
        val d3 = JalaliCalendarUtils.gregorianToJalali(2025, 3, 21)
        assertEquals(1404, d3.year)
        assertEquals(1, d3.month)
        assertEquals(1, d3.day)

        // Known date 4: 2025-03-20 -> 1403/12/30 (leap year)
        val d4 = JalaliCalendarUtils.gregorianToJalali(2025, 3, 20)
        assertEquals(1403, d4.year)
        assertEquals(12, d4.month)
        assertEquals(30, d4.day)

        // Known date 5: 2026-01-20 -> 1404/10/30
        val d5 = JalaliCalendarUtils.gregorianToJalali(2026, 1, 20)
        assertEquals(1404, d5.year)
        assertEquals(10, d5.month)
        assertEquals(30, d5.day)
    }

    @Test
    fun testWeekdayNameForKnownDate() {
        // Weekday name for 2026-10-28 is چهارشنبه
        val weekday = JalaliCalendarUtils.getWeekdayNameForDate(2026, 10, 28)
        assertEquals("چهارشنبه", weekday)
    }

    @Test
    fun testFormatPersianDate() {
        val d1 = JalaliCalendarUtils.gregorianToJalali(2026, 10, 28)
        val weekday = JalaliCalendarUtils.getWeekdayNameForDate(2026, 10, 28)
        val formatted = d1.formatPersianDate(weekday)
        assertEquals("چهارشنبه 6 آبان 1405", formatted)
    }

    @Test
    fun testGreetingBoundaries() {
        // 04:59 -> شب
        assertTrue("04:59 should contain شب", DashboardTexts.getGreeting(4, 59).contains("شب"))
        // 05:00 -> صبح
        assertTrue("05:00 should contain صبح", DashboardTexts.getGreeting(5, 0).contains("صبح"))
        // 11:59 -> صبح
        assertTrue("11:59 should contain صبح", DashboardTexts.getGreeting(11, 59).contains("صبح"))
        // 12:00 -> ظهر
        assertTrue("12:00 should contain ظهر", DashboardTexts.getGreeting(12, 0).contains("ظهر"))
        // 16:59 -> ظهر
        assertTrue("16:59 should contain ظهر", DashboardTexts.getGreeting(16, 59).contains("ظهر"))
        // 17:00 -> عصر
        assertTrue("17:00 should contain عصر", DashboardTexts.getGreeting(17, 0).contains("عصر"))
        // 19:59 -> عصر
        assertTrue("19:59 should contain عصر", DashboardTexts.getGreeting(19, 59).contains("عصر"))
        // 20:00 -> شب
        assertTrue("20:00 should contain شب", DashboardTexts.getGreeting(20, 0).contains("شب"))
        // 00:00 -> شب
        assertTrue("00:00 should contain شب", DashboardTexts.getGreeting(0, 0).contains("شب"))
    }

    @Test
    fun testWeekdayMessagesAreNonBlankAndAccurate() {
        val weekdays = JalaliCalendarUtils.WEEKDAY_NAMES
        for (w in weekdays) {
            val msg = DashboardTexts.getWeekdayMessage(w)
            assertNotNull(msg)
            assertTrue("Message for $w must not be blank", msg.isNotBlank())
            assertFalse("Message for $w should not be default fallback", msg.contains("با انرژی و تمرکز بالا"))
        }

        // Verify exact prompt strings
        assertEquals("شروع هفته است؛ یک سؤال سخت را امروز شکست بده! 💪", DashboardTexts.getWeekdayMessage("شنبه"))
        assertEquals("پشتکار از استعداد مهم\u200Cتر است؛ امروز هم یک قدم جلوتر برو. 🚶", DashboardTexts.getWeekdayMessage("یکشنبه"))
        assertEquals("اشتباه\u200Cها معلم\u200Cهای خوبی\u200Cاند؛ امروز از یکی\u200Cشان یاد بگیر. 📘", DashboardTexts.getWeekdayMessage("دوشنبه"))
        assertEquals("وسط هفته\u200Cای؛ نفس بگیر و با تمرکز ادامه بده. 🎯", DashboardTexts.getWeekdayMessage(JalaliCalendarUtils.WEEKDAY_TUESDAY))
        assertEquals("یک آزمون کوتاه امتحان کن و خودت را بسنج. 📝", DashboardTexts.getWeekdayMessage("چهارشنبه"))
        assertEquals("مرور کن؛ چیزی که دوباره دیده شود، ماندگار می\u200Cشود. 🔁", DashboardTexts.getWeekdayMessage("پنجشنبه"))
        assertEquals("امروز استراحت هم بخشی از پیشرفت است؛ کمی تفریح کن. 🌿", DashboardTexts.getWeekdayMessage("جمعه"))
    }

    @Test
    fun testSloganListHasAtLeastEightDistinctNonBlankItems() {
        val slogans = DashboardTexts.SLOGANS
        assertTrue("Slogan list must have at least 8 items", slogans.size >= 8)
        val distinct = slogans.toSet()
        assertEquals("All slogans must be distinct", slogans.size, distinct.size)
        for (slogan in slogans) {
            assertTrue("Slogan must not be blank", slogan.isNotBlank())
        }
    }

    @Test
    fun testSloganOfTheDayIsStableAndValid() {
        val slogan1 = DashboardTexts.getSloganOfTheDay(1405, 8, 6)
        val slogan2 = DashboardTexts.getSloganOfTheDay(1405, 8, 6)
        assertEquals("Slogan must be stable on the same date", slogan1, slogan2)
        assertTrue(DashboardTexts.SLOGANS.contains(slogan1))

        // Check across different dates
        for (y in 1400..1406) {
            for (m in 1..12) {
                for (d in listOf(1, 15, 29)) {
                    val s = DashboardTexts.getSloganOfTheDay(y, m, d)
                    assertTrue("Slogan must be valid for date $y/$m/$d", s.isNotBlank())
                    assertTrue(DashboardTexts.SLOGANS.contains(s))
                }
            }
        }
    }
}
