package com.example.util

data class JalaliDate(val year: Int, val month: Int, val day: Int) {
    fun formatPersianDate(weekdayName: String): String {
        val monthName = JalaliCalendarUtils.JALALI_MONTH_NAMES.getOrElse(month - 1) { "" }
        return "$weekdayName $day $monthName $year"
    }
}

object JalaliCalendarUtils {
    val JALALI_MONTH_NAMES = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    const val WEEKDAY_TUESDAY = "سه\u200Cشنبه"

    val WEEKDAY_NAMES = listOf(
        "شنبه", "یکشنبه", "دوشنبه", WEEKDAY_TUESDAY, "چهارشنبه", "پنجشنبه", "جمعه"
    )

    private val G_DAYS_IN_MONTH = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    private val J_DAYS_IN_MONTH = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

    fun isGregorianLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    /**
     * Converts a Gregorian date (year, month 1..12, day 1..31)
     * to a Jalali (Shamsi) date (year, month 1..12, day 1..31).
     */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gy2 = gy - 1600
        val gm2 = gm - 1
        val gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400

        for (i in 0 until gm2) {
            gDayNo += G_DAYS_IN_MONTH[i]
        }
        if (gm2 > 1 && isGregorianLeapYear(gy)) {
            gDayNo++
        }
        gDayNo += gd2

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        while (jm < 11 && jDayNo >= J_DAYS_IN_MONTH[jm]) {
            jDayNo -= J_DAYS_IN_MONTH[jm]
            jm++
        }
        val jd = jDayNo + 1
        return JalaliDate(jy, jm + 1, jd)
    }

    fun getWeekdayName(calendarDayOfWeek: Int): String {
        return when (calendarDayOfWeek) {
            java.util.Calendar.SATURDAY -> "شنبه"
            java.util.Calendar.SUNDAY -> "یکشنبه"
            java.util.Calendar.MONDAY -> "دوشنبه"
            java.util.Calendar.TUESDAY -> WEEKDAY_TUESDAY
            java.util.Calendar.WEDNESDAY -> "چهارشنبه"
            java.util.Calendar.THURSDAY -> "پنجشنبه"
            java.util.Calendar.FRIDAY -> "جمعه"
            else -> "شنبه"
        }
    }

    fun getWeekdayNameForDate(gy: Int, gm: Int, gd: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.set(gy, gm - 1, gd)
        return getWeekdayName(cal.get(java.util.Calendar.DAY_OF_WEEK))
    }
}
