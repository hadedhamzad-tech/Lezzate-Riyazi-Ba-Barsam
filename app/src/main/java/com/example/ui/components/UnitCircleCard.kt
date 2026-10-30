package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.util.UnitCircleMath
import kotlinx.coroutines.isActive
import kotlin.math.sin

private val ColorBackground = Color(0xFF0B132B)
private val ColorCyan = Color(0xFF00E5FF)
private val ColorPurple = Color(0xFF7C4DFF)
private val ColorAmber = Color(0xFFFFB300)
private val ColorGreen = Color(0xFF00E676)
private val ColorGrid = Color(0xFF334155)

private const val ANGULAR_SPEED_RAD_PER_SEC = 1.0f
private const val MAX_DT_SECONDS = 0.05f
private const val SINE_SAMPLE_POINTS = 120

@Composable
fun UnitCircleCard(
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val isPausedState = remember { mutableStateOf(false) }
    val angleState = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(lifecycleOwner, isPausedState.value) {
        if (isPausedState.value) return@LaunchedEffect
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            var lastFrameTimeNanos = 0L
            while (isActive) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameTimeNanos != 0L) {
                        val rawDt = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                        val dt = UnitCircleMath.clampDt(rawDt, MAX_DT_SECONDS)
                        angleState.floatValue = UnitCircleMath.advance(
                            angleState.floatValue,
                            dt,
                            ANGULAR_SPEED_RAD_PER_SEC
                        )
                    }
                    lastFrameTimeNanos = frameTimeNanos
                }
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { isPausedState.value = !isPausedState.value }
            .semantics {
                contentDescription = "انیمیشن دایره\u200cی مثلثاتی؛ برای توقف یا ادامه لمس کنید"
            }
            .testTag("dashboard_unit_circle_card"),
        shape = RoundedCornerShape(16.dp),
        color = ColorBackground,
        border = BorderStroke(1.dp, ColorCyan.copy(alpha = 0.35f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val angle = angleState.floatValue
                val cy = size.height / 2f
                val radius = 45.dp.toPx()
                val cx = 16.dp.toPx() + radius

                // 1. Axes of the unit circle
                val axisMargin = 8.dp.toPx()
                drawLine(
                    color = ColorGrid,
                    start = Offset(cx - radius - axisMargin, cy),
                    end = Offset(cx + radius + axisMargin, cy),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = ColorGrid,
                    start = Offset(cx, cy - radius - axisMargin),
                    end = Offset(cx, cy + radius + axisMargin),
                    strokeWidth = 1.dp.toPx()
                )

                // 2. Unit circle outline
                drawCircle(
                    color = ColorPurple,
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.dp.toPx())
                )

                // 3. Current point on unit circle
                val (cosVal, sinVal) = UnitCircleMath.pointAt(angle)
                val px = cx + radius * cosVal
                val py = cy - radius * sinVal

                // Faint vertical green line from point down/up to x-axis (cos θ)
                drawLine(
                    color = ColorGreen.copy(alpha = 0.45f),
                    start = Offset(px, py),
                    end = Offset(px, cy),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Rotating radius line
                drawLine(
                    color = ColorPurple.copy(alpha = 0.85f),
                    start = Offset(cx, cy),
                    end = Offset(px, py),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Glowing amber dot on circle
                val haloRadiusCircle = 10.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ColorAmber.copy(alpha = 0.55f), Color.Transparent),
                        center = Offset(px, py),
                        radius = haloRadiusCircle
                    ),
                    radius = haloRadiusCircle,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = ColorAmber,
                    radius = 3.5.dp.toPx(),
                    center = Offset(px, py)
                )

                // 4. Sine wave plotting on the right
                val waveStartX = cx + radius + 20.dp.toPx()
                val waveEndX = size.width - 16.dp.toPx()
                val waveWidth = waveEndX - waveStartX

                if (waveWidth > 0f) {
                    // Faint horizontal center axis for sine wave
                    drawLine(
                        color = ColorGrid,
                        start = Offset(waveStartX, cy),
                        end = Offset(waveEndX, cy),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Dashed amber projection line from circle point to wave start
                    drawLine(
                        color = ColorAmber.copy(alpha = 0.65f),
                        start = Offset(px, py),
                        end = Offset(waveStartX, py),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    // Second glowing amber dot at the curve start
                    val haloRadiusWave = 8.dp.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ColorAmber.copy(alpha = 0.55f), Color.Transparent),
                            center = Offset(waveStartX, py),
                            radius = haloRadiusWave
                        ),
                        radius = haloRadiusWave,
                        center = Offset(waveStartX, py)
                    )
                    drawCircle(
                        color = ColorAmber,
                        radius = 3.dp.toPx(),
                        center = Offset(waveStartX, py)
                    )

                    // Sine wave history: 120 points divided into 4 segments with fading alpha toward tail
                    val segments = 4
                    val pointsPerSegment = SINE_SAMPLE_POINTS / segments
                    val segmentAlphas = floatArrayOf(0.95f, 0.68f, 0.42f, 0.18f)

                    for (seg in 0 until segments) {
                        val path = Path()
                        val startIndex = seg * pointsPerSegment
                        val endIndex = (seg + 1) * pointsPerSegment
                        var first = true

                        for (i in startIndex..endIndex.coerceAtMost(SINE_SAMPLE_POINTS - 1)) {
                            val frac = i.toFloat() / (SINE_SAMPLE_POINTS - 1)
                            val x = waveStartX + frac * waveWidth
                            val pastAngle = angle - frac * UnitCircleMath.TWO_PI
                            val y = cy - radius * sin(pastAngle.toDouble()).toFloat()

                            if (first) {
                                path.moveTo(x, y)
                                first = false
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = ColorCyan.copy(alpha = segmentAlphas[seg]),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }
            }

            // Top-end title overlay
            Text(
                text = "دایره\u200cی مثلثاتی زنده 🌀",
                fontSize = 12.sp,
                color = ColorCyan,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 12.dp)
            )
        }
    }
}
