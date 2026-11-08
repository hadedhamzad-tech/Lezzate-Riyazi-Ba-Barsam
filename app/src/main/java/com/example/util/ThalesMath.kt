package com.example.util

import java.util.Locale
import kotlin.math.asin
import kotlin.math.sin

object ThalesMath {

    fun pointOnSegment(ax: Float, ay: Float, bx: Float, by: Float, k: Float): Pair<Float, Float> {
        val x = ax + k * (bx - ax)
        val y = ay + k * (by - ay)
        return Pair(x, y)
    }

    fun ratioAt(phase: Float): Float = 0.55f + 0.40f * sin(phase.toDouble()).toFloat()

    fun phaseForRatio(k: Float): Float {
        val clamped = ((k - 0.55f) / 0.40f).coerceIn(-1f, 1f)
        return asin(clamped.toDouble()).toFloat()
    }

    fun areaRatio(k: Float): Float = k * k

    fun formatRatio(k: Float): String = String.format(Locale.US, "%.2f", k)
}
