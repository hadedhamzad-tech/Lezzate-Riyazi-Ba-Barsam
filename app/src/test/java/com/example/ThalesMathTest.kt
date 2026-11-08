package com.example

import com.example.util.ThalesMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt

class ThalesMathTest {

    private val eps = 1e-4f

    @Test
    fun testPointOnSegment() {
        val ax = 2.0f
        val ay = 4.0f
        val bx = 10.0f
        val by = 16.0f

        val p0 = ThalesMath.pointOnSegment(ax, ay, bx, by, 0.0f)
        assertEquals(2.0f, p0.first, eps)
        assertEquals(4.0f, p0.second, eps)

        val pHalf = ThalesMath.pointOnSegment(ax, ay, bx, by, 0.5f)
        assertEquals(6.0f, pHalf.first, eps)
        assertEquals(10.0f, pHalf.second, eps)

        val p1 = ThalesMath.pointOnSegment(ax, ay, bx, by, 1.0f)
        assertEquals(10.0f, p1.first, eps)
        assertEquals(16.0f, p1.second, eps)
    }

    @Test
    fun testThalesParallelAndRatio() {
        val ax = 0.0f
        val ay = 0.0f
        val bx = 10.0f
        val by = 0.0f
        val cx = 4.0f
        val cy = 8.0f

        val bcX = cx - bx
        val bcY = cy - by
        val bcLen = sqrt((bcX * bcX + bcY * bcY).toDouble()).toFloat()

        val testKs = listOf(0.2f, 0.5f, 0.9f)
        for (k in testKs) {
            val (dx, dy) = ThalesMath.pointOnSegment(ax, ay, bx, by, k)
            val (ex, ey) = ThalesMath.pointOnSegment(ax, ay, cx, cy, k)

            val deX = ex - dx
            val deY = ey - dy

            // 2D Cross product of DE and BC must be ~0 (parallel)
            val cross = (deX * bcY) - (deY * bcX)
            assertEquals("Cross product must be 0 for k=$k", 0.0f, cross, eps)

            // |DE| / |BC| must equal k
            val deLen = sqrt((deX * deX + deY * deY).toDouble()).toFloat()
            val ratio = deLen / bcLen
            assertEquals("Length ratio must equal k for k=$k", k, ratio, eps)
        }
    }

    @Test
    fun testRatioAtPhases() {
        val r0 = ThalesMath.ratioAt(0.0f)
        assertEquals(0.55f, r0, eps)

        val rHalfPi = ThalesMath.ratioAt((PI / 2.0).toFloat())
        assertEquals(0.95f, rHalfPi, eps)

        val rNegHalfPi = ThalesMath.ratioAt((-PI / 2.0).toFloat())
        assertEquals(0.15f, rNegHalfPi, eps)
    }

    @Test
    fun testPhaseForRatioRoundTrip() {
        val testKs = listOf(0.15f, 0.25f, 0.40f, 0.55f, 0.70f, 0.85f, 0.95f)
        for (k in testKs) {
            val phase = ThalesMath.phaseForRatio(k)
            val recovered = ThalesMath.ratioAt(phase)
            assertEquals("Round-trip failed for k=$k", k, recovered, eps)
        }
    }

    @Test
    fun testAreaRatio() {
        assertEquals(0.25f, ThalesMath.areaRatio(0.5f), eps)
        assertEquals(0.04f, ThalesMath.areaRatio(0.2f), eps)
        assertEquals(0.81f, ThalesMath.areaRatio(0.9f), eps)
    }

    @Test
    fun testFormatRatio() {
        assertEquals("0.46", ThalesMath.formatRatio(0.456f))
        assertEquals("0.50", ThalesMath.formatRatio(0.5f))
        assertEquals("0.15", ThalesMath.formatRatio(0.15f))
    }
}
