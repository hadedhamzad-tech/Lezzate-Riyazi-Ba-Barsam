package com.example.util

import java.util.Locale
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object TransformMath {

    fun translate(px: Float, py: Float, vx: Float, vy: Float, t: Float): Pair<Float, Float> {
        return Pair(px + t * vx, py + t * vy)
    }

    fun reflectVertical(px: Float, py: Float, lineX: Float, t: Float): Pair<Float, Float> {
        return Pair(lineX + (px - lineX) * (1f - 2f * t), py)
    }

    fun rotate(px: Float, py: Float, cx: Float, cy: Float, angle: Float): Pair<Float, Float> {
        val dx = px - cx
        val dy = py - cy
        val cosA = cos(angle.toDouble()).toFloat()
        val sinA = sin(angle.toDouble()).toFloat()
        val rx = cx + dx * cosA - dy * sinA
        val ry = cy + dx * sinA + dy * cosA
        return Pair(rx, ry)
    }

    fun rotationAngle(t: Float): Float = t * PI.toFloat()

    fun progressAt(phase: Float): Float {
        return 0.5f + 0.5f * sin(phase.toDouble()).toFloat()
    }

    fun phaseForProgress(t: Float): Float {
        val clamped = (2f * t - 1f).coerceIn(-1f, 1f)
        return asin(clamped.toDouble()).toFloat()
    }

    fun formatDegrees(angleRad: Float): String {
        val deg = (angleRad * 180.0 / PI).roundToInt()
        return String.format(Locale.US, "%d°", deg)
    }
}
