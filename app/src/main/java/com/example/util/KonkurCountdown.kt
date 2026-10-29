package com.example.util

data class CountdownDisplay(
    val bigText: String,
    val message: String,
    val weeksText: String?
)

object KonkurCountdown {
    const val DEFAULT_YEAR = 1406
    const val DEFAULT_MONTH = 4
    const val DEFAULT_DAY = 11

    val DEFAULT: JalaliDate = JalaliDate(DEFAULT_YEAR, DEFAULT_MONTH, DEFAULT_DAY)

    fun daysUntil(todayGy: Int, todayGm: Int, todayGd: Int, target: JalaliDate): Long {
        val (targetGy, targetGm, targetGd) = JalaliCalendarUtils.jalaliToGregorian(target.year, target.month, target.day)
        val targetEpoch = JalaliCalendarUtils.gregorianToEpochDay(targetGy, targetGm, targetGd)
        val todayEpoch = JalaliCalendarUtils.gregorianToEpochDay(todayGy, todayGm, todayGd)
        return targetEpoch - todayEpoch
    }

    fun weeksAndDaysText(days: Long): String? {
        if (days < 7) return null
        val w = days / 7
        val r = days % 7
        return if (r == 0L) {
            "$w هفته"
        } else {
            "$w هفته و $r روز"
        }
    }

    fun display(days: Long): CountdownDisplay {
        val weeksText = if (days >= 7) weeksAndDaysText(days) else null
        return when {
            days >= 2 -> CountdownDisplay(
                bigText = "$days روز",
                message = "تا بزرگترین ماجراجویی! 🚀",
                weeksText = weeksText
            )
            days == 1L -> CountdownDisplay(
                bigText = "فردا",
                message = "فردا روز بزرگه! نفس عمیق بکش و آرامش داشته باش 🌟",
                weeksText = null
            )
            days == 0L -> CountdownDisplay(
                bigText = "امروز",
                message = "امروز روز کنکوره! تو آمادهای؛ با آرامش برو و بدرخش 🏆",
                weeksText = null
            )
            else -> CountdownDisplay(
                bigText = "تاریخ گذشته",
                message = "از تنظیمات، تاریخ جدید کنکور را وارد کن ⚙️",
                weeksText = null
            )
        }
    }

    fun parse(s: String?): JalaliDate {
        if (s == null) return DEFAULT
        val parts = s.split("-")
        if (parts.size != 3) return DEFAULT
        val y = parts[0].toIntOrNull() ?: return DEFAULT
        val m = parts[1].toIntOrNull() ?: return DEFAULT
        val d = parts[2].toIntOrNull() ?: return DEFAULT
        if (!JalaliCalendarUtils.isValidJalaliDate(y, m, d)) return DEFAULT
        return JalaliDate(y, m, d)
    }

    fun serialize(d: JalaliDate): String {
        return "${d.year.toString().padStart(4, '0')}-${d.month.toString().padStart(2, '0')}-${d.day.toString().padStart(2, '0')}"
    }
}
