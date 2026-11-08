package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.util.ConicMath
import com.example.util.ThalesMath
import com.example.util.UnitCircleMath
import kotlinx.coroutines.isActive

private val ColorBackground = Color(0xFF0B132B)
private val ColorCyan = Color(0xFF00E5FF)
private val ColorPurple = Color(0xFF7C4DFF)
private val ColorAmber = Color(0xFFFFB300)
private val ColorGreen = Color(0xFF00E676)

private const val PHASE_SPEED_RAD_PER_SEC = 0.5f
private const val MAX_DT_SECONDS = 0.05f

@Composable
fun SimilarTrianglesCard(
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isPaused by remember { mutableStateOf(false) }
    val ratioState = remember { mutableFloatStateOf(ThalesMath.ratioAt(0f)) }
    var phase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(lifecycleOwner, isPaused) {
        if (isPaused) return@LaunchedEffect
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            var lastFrameTimeNanos = 0L
            while (isActive) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameTimeNanos != 0L) {
                        val rawDt = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                        val dt = UnitCircleMath.clampDt(rawDt, MAX_DT_SECONDS)
                        phase = ConicMath.advancePhase(phase, dt, PHASE_SPEED_RAD_PER_SEC)
                        ratioState.floatValue = ThalesMath.ratioAt(phase)
                    }
                    lastFrameTimeNanos = frameTimeNanos
                }
            }
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val letterTextStyle = remember {
        TextStyle(
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textDirection = TextDirection.Ltr
        )
    }
    val readTextStyle = remember {
        TextStyle(
            color = ColorCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textDirection = TextDirection.Ltr
        )
    }

    val pathABC = remember { Path() }
    val pathADE = remember { Path() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clickable { isPaused = !isPaused }
                .semantics {
                    contentDescription = "انیمیشن تشابه و قضیه\u200cی تالس؛ برای توقف یا ادامه لمس کنید"
                }
                .testTag("lab_similar_card"),
            shape = RoundedCornerShape(16.dp),
            color = ColorBackground,
            border = BorderStroke(1.dp, ColorCyan.copy(alpha = 0.35f))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                clipRect {
                    val w = size.width
                    val h = size.height

                    val ax = 0.38f * w
                    val ay = 0.14f * h
                    val bx = 0.12f * w
                    val by = 0.84f * h
                    val cx = 0.88f * w
                    val cy = 0.84f * h

                    val k = ratioState.floatValue
                    val (dx, dy) = ThalesMath.pointOnSegment(ax, ay, bx, by, k)
                    val (ex, ey) = ThalesMath.pointOnSegment(ax, ay, cx, cy, k)

                    // 1. Triangle ABC: fill cyan alpha 0.10 + outline cyan 2.dp
                    pathABC.reset()
                    pathABC.moveTo(ax, ay)
                    pathABC.lineTo(bx, by)
                    pathABC.lineTo(cx, cy)
                    pathABC.close()

                    drawPath(
                        path = pathABC,
                        color = ColorCyan.copy(alpha = 0.10f),
                        style = Fill
                    )
                    drawPath(
                        path = pathABC,
                        color = ColorCyan,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // 2. Faint dashed line showing BC is parallel
                    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    drawLine(
                        color = ColorPurple.copy(alpha = 0.9f),
                        start = Offset(bx, by),
                        end = Offset(cx, cy),
                        strokeWidth = 2.5.dp.toPx(),
                        pathEffect = dashedEffect
                    )

                    // 3. Triangle ADE: fill amber alpha 0.28 + outline amber 2.dp
                    pathADE.reset()
                    pathADE.moveTo(ax, ay)
                    pathADE.lineTo(dx, dy)
                    pathADE.lineTo(ex, ey)
                    pathADE.close()

                    drawPath(
                        path = pathADE,
                        color = ColorAmber.copy(alpha = 0.28f),
                        style = Fill
                    )
                    drawPath(
                        path = pathADE,
                        color = ColorAmber,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // 4. Segment DE: bright green 3.dp with soft wider glow underneath
                    drawLine(
                        color = ColorGreen.copy(alpha = 0.25f),
                        start = Offset(dx, dy),
                        end = Offset(ex, ey),
                        strokeWidth = 7.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = ColorGreen,
                        start = Offset(dx, dy),
                        end = Offset(ex, ey),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // 5. Glowing dots for points A, B, C, D, E
                    fun drawPointDot(x: Float, y: Float, color: Color) {
                        val haloRadius = 10.dp.toPx()
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(color.copy(alpha = 0.6f), color.copy(alpha = 0f)),
                                center = Offset(x, y),
                                radius = haloRadius
                            ),
                            center = Offset(x, y),
                            radius = haloRadius
                        )
                        drawCircle(
                            color = color,
                            center = Offset(x, y),
                            radius = 3.5.dp.toPx()
                        )
                    }

                    drawPointDot(ax, ay, ColorCyan)
                    drawPointDot(bx, by, ColorCyan)
                    drawPointDot(cx, cy, ColorCyan)
                    drawPointDot(dx, dy, ColorGreen)
                    drawPointDot(ex, ey, ColorGreen)

                    // 6. Point letters outside triangle
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "A",
                        topLeft = Offset(ax - 5.dp.toPx(), ay - 24.dp.toPx()),
                        style = letterTextStyle
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "B",
                        topLeft = Offset(bx - 20.dp.toPx(), by + 4.dp.toPx()),
                        style = letterTextStyle
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "C",
                        topLeft = Offset(cx + 8.dp.toPx(), cy + 4.dp.toPx()),
                        style = letterTextStyle
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "D",
                        topLeft = Offset(dx - 18.dp.toPx(), dy - 8.dp.toPx()),
                        style = letterTextStyle
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "E",
                        topLeft = Offset(ex + 8.dp.toPx(), ey - 8.dp.toPx()),
                        style = letterTextStyle
                    )

                    // 7. Live readout centered below side BC
                    val kStr = ThalesMath.formatRatio(k)
                    val k2Str = ThalesMath.formatRatio(ThalesMath.areaRatio(k))
                    val readout = "k = $kStr    k² = $k2Str"
                    val layout = textMeasurer.measure(
                        text = readout,
                        style = readTextStyle
                    )
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset((size.width - layout.size.width) / 2f, by + 14.dp.toPx())
                    )
                }
            }
        }

        Text(
            text = "خط DE همیشه با BC موازی است و مثلث ADE با ABC متشابه می\u200cماند ✨",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "نسبت تشابه",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Slider(
                value = ratioState.floatValue,
                onValueChange = { newK ->
                    isPaused = true
                    ratioState.floatValue = newK
                    phase = ThalesMath.phaseForRatio(newK)
                },
                valueRange = 0.15f..0.95f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lab_similar_slider")
            )
        }
    }
}
