package com.example.util

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object UnitCircleMath {
    const val TWO_PI: Float = (2.0 * PI).toFloat()

    fun pointAt(angle: Float): Pair<Float, Float> {
        val c = cos(angle.toDouble()).toFloat()
        val s = sin(angle.toDouble()).toFloat()
        return Pair(c, s)
    }

    fun advance(angle: Float, dtSeconds: Float, speedRadPerSec: Float): Float {
        var next = (angle + dtSeconds * speedRadPerSec) % TWO_PI
        if (next < 0f) {
            next += TWO_PI
        }
        return next
    }

    fun clampDt(dt: Float, maxDt: Float): Float {
        return if (dt < 0f) 0f else if (dt > maxDt) maxDt else dt
    }
}
