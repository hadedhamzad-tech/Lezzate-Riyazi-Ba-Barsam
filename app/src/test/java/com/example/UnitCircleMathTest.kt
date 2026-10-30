package com.example

import com.example.util.UnitCircleMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class UnitCircleMathTest {

    private val tolerance = 1e-5f

    @Test
    fun testPointAtSpecialAngles() {
        // angle 0: cos = 1, sin = 0
        val p0 = UnitCircleMath.pointAt(0f)
        assertEquals(1f, p0.first, tolerance)
        assertEquals(0f, p0.second, tolerance)

        // angle π/2: cos = 0, sin = 1
        val pHalfPi = UnitCircleMath.pointAt((PI / 2.0).toFloat())
        assertEquals(0f, pHalfPi.first, tolerance)
        assertEquals(1f, pHalfPi.second, tolerance)

        // angle π: cos = -1, sin = 0
        val pPi = UnitCircleMath.pointAt(PI.toFloat())
        assertEquals(-1f, pPi.first, tolerance)
        assertEquals(0f, pPi.second, tolerance)

        // angle 3π/2: cos = 0, sin = -1
        val pThreeHalfPi = UnitCircleMath.pointAt((3.0 * PI / 2.0).toFloat())
        assertEquals(0f, pThreeHalfPi.first, tolerance)
        assertEquals(-1f, pThreeHalfPi.second, tolerance)
    }

    @Test
    fun testAdvanceWrapsCorrectlyAndNeverNegative() {
        val twoPi = UnitCircleMath.TWO_PI

        // advancing past 2π wraps into [0, 2π)
        val initial = twoPi - 0.1f
        val advanced = UnitCircleMath.advance(initial, 0.2f, 1.0f)
        assertTrue("Advanced angle must be >= 0", advanced >= 0f)
        assertTrue("Advanced angle must be < 2π", advanced < twoPi)
        assertEquals(0.1f, advanced, tolerance)

        // multiple wraps
        val bigAdvance = UnitCircleMath.advance(0f, 10f * twoPi + 1.5f, 1.0f)
        assertTrue("Big advance angle must be >= 0", bigAdvance >= 0f)
        assertTrue("Big advance angle must be < 2π", bigAdvance < twoPi)
        assertEquals(1.5f, bigAdvance, tolerance)

        // negative dt or speed never returns a negative value
        val negativeAdvance = UnitCircleMath.advance(0.5f, -1.0f, 1.0f)
        assertTrue("Result must never be negative", negativeAdvance >= 0f)
        assertTrue("Result must be < 2π", negativeAdvance < twoPi)
        assertEquals(twoPi - 0.5f, negativeAdvance, tolerance)
    }

    @Test
    fun testClampDt() {
        val maxDt = 0.05f

        // at limit
        assertEquals(0.05f, UnitCircleMath.clampDt(0.05f, maxDt), tolerance)

        // below limit
        assertEquals(0.016f, UnitCircleMath.clampDt(0.016f, maxDt), tolerance)
        assertEquals(0f, UnitCircleMath.clampDt(0f, maxDt), tolerance)

        // above limit
        assertEquals(0.05f, UnitCircleMath.clampDt(0.1f, maxDt), tolerance)
        assertEquals(0.05f, UnitCircleMath.clampDt(1.0f, maxDt), tolerance)

        // negative input clamped to 0
        assertEquals(0f, UnitCircleMath.clampDt(-0.02f, maxDt), tolerance)
    }
}
