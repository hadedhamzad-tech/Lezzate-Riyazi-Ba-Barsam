package com.example

import com.example.util.TransformMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.hypot

class TransformMathTest {

    private val eps = 1e-4f

    @Test
    fun testTranslateAtZeroAndOne() {
        val px = 10f
        val py = 20f
        val vx = 50f
        val vy = -30f

        val atZero = TransformMath.translate(px, py, vx, vy, 0f)
        assertEquals(px, atZero.first, eps)
        assertEquals(py, atZero.second, eps)

        val atOne = TransformMath.translate(px, py, vx, vy, 1f)
        assertEquals(px + vx, atOne.first, eps)
        assertEquals(py + vy, atOne.second, eps)

        val atHalf = TransformMath.translate(px, py, vx, vy, 0.5f)
        assertEquals(px + 0.5f * vx, atHalf.first, eps)
        assertEquals(py + 0.5f * vy, atHalf.second, eps)
    }

    @Test
    fun testReflectVerticalSpecificPoints() {
        val px = 3f
        val py = 2f
        val lineX = 1f

        val t0 = TransformMath.reflectVertical(px, py, lineX, 0f)
        assertEquals(3f, t0.first, eps)
        assertEquals(2f, t0.second, eps)

        val tHalf = TransformMath.reflectVertical(px, py, lineX, 0.5f)
        assertEquals(1f, tHalf.first, eps)
        assertEquals(2f, tHalf.second, eps)

        val t1 = TransformMath.reflectVertical(px, py, lineX, 1f)
        assertEquals(-1f, t1.first, eps)
        assertEquals(2f, t1.second, eps)
    }

    @Test
    fun testReflectionPreservesDistanceToLine() {
        val lineX = 25f
        val py = 40f
        for (x in listOf(5f, 15f, 30f, 50f)) {
            val originalDist = abs(x - lineX)
            val reflected = TransformMath.reflectVertical(x, py, lineX, 1f)
            val reflectedDist = abs(reflected.first - lineX)
            assertEquals("Distance to line must be preserved for x=$x", originalDist, reflectedDist, eps)
            assertEquals("y must not change", py, reflected.second, eps)
        }
    }

    @Test
    fun testRotatePointAboutOrigin() {
        val p = TransformMath.rotate(1f, 0f, 0f, 0f, (PI / 2).toFloat())
        assertEquals(0f, p.first, eps)
        assertEquals(1f, p.second, eps)

        val p180 = TransformMath.rotate(1f, 0f, 0f, 0f, PI.toFloat())
        assertEquals(-1f, p180.first, eps)
        assertEquals(0f, p180.second, eps)
    }

    @Test
    fun testRotationPreservesDistanceToCenter() {
        val cx = 100f
        val cy = 150f
        val px = 140f
        val py = 180f
        val originalRadius = hypot(px - cx, py - cy)

        val testAngles = listOf(0f, 0.3f, (PI / 4).toFloat(), (PI / 2).toFloat(), 2.1f, PI.toFloat(), (1.5 * PI).toFloat())
        for (angle in testAngles) {
            val rotated = TransformMath.rotate(px, py, cx, cy, angle)
            val currentRadius = hypot(rotated.first - cx, rotated.second - cy)
            assertEquals("Radius must be preserved for angle=$angle", originalRadius, currentRadius, eps)
        }
    }

    @Test
    fun testRotationAngle() {
        assertEquals(0f, TransformMath.rotationAngle(0f), eps)
        assertEquals(PI.toFloat(), TransformMath.rotationAngle(1f), eps)
        assertEquals((PI / 2).toFloat(), TransformMath.rotationAngle(0.5f), eps)
    }

    @Test
    fun testProgressAtKeyPhases() {
        assertEquals(0.5f, TransformMath.progressAt(0f), eps)
        assertEquals(1.0f, TransformMath.progressAt((PI / 2).toFloat()), eps)
        assertEquals(0.0f, TransformMath.progressAt((-PI / 2).toFloat()), eps)
    }

    @Test
    fun testPhaseForProgressRoundTrip() {
        val testProgressValues = listOf(0.0f, 0.1f, 0.25f, 0.5f, 0.75f, 0.9f, 1.0f)
        for (t in testProgressValues) {
            val phase = TransformMath.phaseForProgress(t)
            val recoveredT = TransformMath.progressAt(phase)
            assertEquals("Round-trip failed for t=$t", t, recoveredT, eps)
        }
    }

    @Test
    fun testFormatDegrees() {
        assertEquals("90°", TransformMath.formatDegrees((PI / 2).toFloat()))
        assertEquals("0°", TransformMath.formatDegrees(0f))
        assertEquals("180°", TransformMath.formatDegrees(PI.toFloat()))
        assertEquals("45°", TransformMath.formatDegrees((PI / 4).toFloat()))
        assertEquals("135°", TransformMath.formatDegrees((3 * PI / 4).toFloat()))
    }
}
