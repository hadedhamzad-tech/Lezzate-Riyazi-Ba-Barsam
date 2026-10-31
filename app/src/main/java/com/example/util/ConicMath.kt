package com.example.util

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

enum class ConicType {
    CIRCLE,
    ELLIPSE,
    PARABOLA,
    HYPERBOLA
}

object ConicMath {

    fun classify(e: Float): ConicType {
        return when {
            e < 0.02f -> ConicType.CIRCLE
            abs(e - 1f) <= 0.02f -> ConicType.PARABOLA
            e < 1f -> ConicType.ELLIPSE
            else -> ConicType.HYPERBOLA
        }
    }

    fun radius(theta: Float, e: Float, p: Float = 1f): Float? {
        val denom = 1f + e * cos(theta.toDouble()).toFloat()
        return if (abs(denom) < 0.02f) null else p / denom
    }

    fun eccentricityAt(phase: Float): Float {
        return 1f + sin(phase.toDouble()).toFloat()
    }

    fun advancePhase(phase: Float, dtSeconds: Float, speed: Float): Float {
        var next = (phase + dtSeconds * speed) % UnitCircleMath.TWO_PI
        if (next < 0f) {
            next += UnitCircleMath.TWO_PI
        }
        return next
    }

    fun phaseForEccentricity(e: Float): Float {
        val clamped = (e - 1f).coerceIn(-1f, 1f)
        return asin(clamped.toDouble()).toFloat()
    }
}
