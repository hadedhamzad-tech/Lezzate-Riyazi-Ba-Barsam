package com.example.ui.screens

import com.example.util.PersianUtils
import java.util.Locale

object ExamFormatters {
    fun formatDuration(totalSeconds: Long): String {
        val s = maxOf(0L, totalSeconds)
        val minutes = s / 60
        val seconds = s % 60
        return if (seconds == 0L) {
            "زمان آزمون: $minutes دقیقه"
        } else if (minutes == 0L) {
            "زمان آزمون: $seconds ثانیه"
        } else {
            "زمان آزمون: $minutes دقیقه و $seconds ثانیه"
        }
    }

    fun formatTimeMmSs(totalSeconds: Long): String {
        val s = maxOf(0L, totalSeconds)
        val minutes = s / 60
        val seconds = s % 60
        val str = "%02d:%02d".format(Locale.US, minutes, seconds)
        return "${PersianUtils.LRI}$str${PersianUtils.PDI}"
    }

    fun formatUsedTime(totalSeconds: Long): String {
        val s = maxOf(0L, totalSeconds)
        val minutes = s / 60
        val seconds = s % 60
        val str = "%02d:%02d".format(Locale.US, minutes, seconds)
        return "${PersianUtils.LRI}$str${PersianUtils.PDI}"
    }
}
