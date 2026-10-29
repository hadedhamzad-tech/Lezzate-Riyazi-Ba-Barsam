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

    fun gregorianToEpochDay(gy: Int, gm: Int, gd: Int): Long {
        var y = gy.toLong()
        val m = gm.toLong()
        val d = gd.toLong()
        y -= if (m <= 2) 1 else 0
        val era = if (y >= 0) y / 400 else (y - 399) / 400
        val yoe = y - era * 400
        val doy = (153 * (if (m > 2) m - 3 else m + 9) + 2) / 5 + d - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097 + doe - 719468
    }

    fun epochDayToGregorian(epochDay: Long): Triple<Int, Int, Int> {
        val z = epochDay + 719468
        val era = if (z >= 0) z / 146097 else (z - 146096) / 146097
        val doe = z - era * 146097
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        var y = yoe + era * 400
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = (doy - (153 * mp + 2) / 5 + 1).toInt()
        val m = (if (mp < 10) mp + 3 else mp - 9).toInt()
        y += if (m <= 2) 1 else 0
        return Triple(y.toInt(), m, d)
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jDayOfYear = if (jm <= 6) (jm - 1) * 31 + jd else 186 + (jm - 7) * 30 + jd
        var curEpoch = gregorianToEpochDay(jy + 621, 3, 21) + (jDayOfYear - 1)

        for (i in 0..50) {
            val (gy, gm, gd) = epochDayToGregorian(curEpoch)
            val curJ = gregorianToJalali(gy, gm, gd)
            if (curJ.year == jy && curJ.month == jm && curJ.day == jd) {
                return Triple(gy, gm, gd)
            }
            val curJDayOfYear = if (curJ.month <= 6) (curJ.month - 1) * 31 + curJ.day else 186 + (curJ.month - 7) * 30 + curJ.day
            val diff = (jy - curJ.year) * 365 + (jDayOfYear - curJDayOfYear)
            if (diff == 0) break
            curEpoch += diff.coerceIn(-5, 5)
        }

        val offsets = intArrayOf(0, 1, -1, 2, -2, 3, -3, 4, -4, 5, -5, 6, -6, 7, -7, 8, -8, 9, -9, 10, -10)
        for (offset in offsets) {
            val testEpoch = curEpoch + offset
            val (gy, gm, gd) = epochDayToGregorian(testEpoch)
            val curJ = gregorianToJalali(gy, gm, gd)
            if (curJ.year == jy && curJ.month == jm && curJ.day == jd) {
                return Triple(gy, gm, gd)
            }
        }
        return epochDayToGregorian(curEpoch)
    }

    fun isValidJalaliDate(jy: Int, jm: Int, jd: Int): Boolean {
        if (jm !in 1..12 || jd < 1) return false
        val (gy, gm, gd) = jalaliToGregorian(jy, jm, jd)
        val roundTrip = gregorianToJalali(gy, gm, gd)
        return roundTrip.year == jy && roundTrip.month == jm && roundTrip.day == jd
    }

    fun daysInJalaliMonth(jy: Int, jm: Int): Int {
        return when (jm) {
            in 1..6 -> 31
            in 7..11 -> 30
            12 -> if (isValidJalaliDate(jy, 12, 30)) 30 else 29
            else -> 30
        }
    }

    fun getWeekdayNameForDate(gy: Int, gm: Int, gd: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.set(gy, gm - 1, gd)
        return getWeekdayName(cal.get(java.util.Calendar.DAY_OF_WEEK))
    }
}
