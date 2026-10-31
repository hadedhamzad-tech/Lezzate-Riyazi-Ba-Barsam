package com.example

import com.example.util.ConicMath
import com.example.util.ConicType
import com.example.util.UnitCircleMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class ConicMathTest {

    private val eps = 1e-4f

    @Test
    fun testClassify() {
        assertEquals(ConicType.CIRCLE, ConicMath.classify(0.0f))
        assertEquals(ConicType.CIRCLE, ConicMath.classify(0.01f))
        assertEquals(ConicType.ELLIPSE, ConicMath.classify(0.5f))
        assertEquals(ConicType.PARABOLA, ConicMath.classify(0.98f))
        assertEquals(ConicType.PARABOLA, ConicMath.classify(1.0f))
        assertEquals(ConicType.PARABOLA, ConicMath.classify(1.02f))
        assertEquals(ConicType.HYPERBOLA, ConicMath.classify(1.5f))
        assertEquals(ConicType.HYPERBOLA, ConicMath.classify(2.0f))
    }

    @Test
    fun testRadiusAtZeroTheta() {
        val p = 1.0f
        for (e in listOf(0f, 0.5f, 1f, 1.5f, 2f)) {
            val expected = p / (1f + e)
            val actual = ConicMath.radius(0f, e, p)
            assertNotNull(actual)
            assertEquals(expected, actual!!, eps)
        }
    }

    @Test
    fun testCircleRadiusConstant() {
        val p = 2.5f
        val angles = listOf(0f, PI.toFloat() / 4f, PI.toFloat() / 2f, PI.toFloat(), 1.5f * PI.toFloat())
        for (theta in angles) {
            val r = ConicMath.radius(theta, 0f, p)
            assertNotNull(r)
            assertEquals(p, r!!, eps)
        }
    }

    @Test
    fun testRadiusNearZeroDenominatorAndNegativeForHyperbola() {
        // e = 1, theta = PI -> denom = 1 + 1*(-1) = 0 -> null
        val nullResult = ConicMath.radius(PI.toFloat(), 1.0f, 1.0f)
        assertNull(nullResult)

        // e = 2, theta = PI -> denom = 1 + 2*(-1) = -1 -> r = 1/(-1) = -1f (negative)
        val negativeResult = ConicMath.radius(PI.toFloat(), 2.0f, 1.0f)
        assertNotNull(negativeResult)
        assertEquals(-1.0f, negativeResult!!, eps)
        assertTrue(negativeResult < 0f)
    }

    @Test
    fun testEccentricityAtPhase() {
        // at 3PI/2: sin(3PI/2) = -1 -> e = 0
        val e0 = ConicMath.eccentricityAt(1.5f * PI.toFloat())
        assertEquals(0f, e0, eps)

        // at 0: sin(0) = 0 -> e = 1
        val e1 = ConicMath.eccentricityAt(0f)
        assertEquals(1f, e1, eps)

        // at PI/2: sin(PI/2) = 1 -> e = 2
        val e2 = ConicMath.eccentricityAt(0.5f * PI.toFloat())
        assertEquals(2f, e2, eps)
    }

    @Test
    fun testAdvancePhaseWrapsAndNonNegative() {
        val speed = 1.0f
        val p1 = ConicMath.advancePhase(0f, 0.5f, speed)
        assertEquals(0.5f, p1, eps)

        // Wraps over 2PI
        val pWrap = ConicMath.advancePhase(6.0f, 1.0f, speed)
        assertTrue(pWrap >= 0f && pWrap < UnitCircleMath.TWO_PI)

        // Negative input wraps to positive in [0, 2PI)
        val pNeg = ConicMath.advancePhase(-0.5f, 0f, speed)
        assertTrue(pNeg >= 0f && pNeg < UnitCircleMath.TWO_PI)
        assertEquals(UnitCircleMath.TWO_PI - 0.5f, pNeg, eps)
    }

    @Test
    fun testPhaseForEccentricityRoundTrip() {
        val testValues = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        for (e in testValues) {
            val phase = ConicMath.phaseForEccentricity(e)
            val recoveredE = ConicMath.eccentricityAt(phase)
            assertEquals("Round-trip failed for e = $e", e, recoveredE, eps)
        }
    }
}
