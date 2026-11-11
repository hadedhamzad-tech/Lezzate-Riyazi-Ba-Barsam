package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
import com.example.util.TransformMath
import com.example.util.UnitCircleMath
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.hypot

private val ColorBackground = Color(0xFF0B132B)
private val ColorCyan = Color(0xFF00E5FF)
private val ColorPurple = Color(0xFF7C4DFF)
private val ColorAmber = Color(0xFFFFB300)
private val ColorGreen = Color(0xFF00E676)

private const val PHASE_SPEED_RAD_PER_SEC = 0.6f
private const val MAX_DT_SECONDS = 0.05f

private val UNIT_L_VERTICES = listOf(
    0f to 0f,
    1f to 0f,
    1f to 0.35f,
    0.35f to 0.35f,
    0.35f to 1.0f,
    0f to 1.0f
)

@Composable
fun TransformationsCard(
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isPaused by remember { mutableStateOf(false) }
    var selectedMode by rememberSaveable { mutableIntStateOf(0) }

    // Start with phase = -PI/2 so initial t = progressAt(-PI/2) = 0f
    var phase by remember { mutableFloatStateOf(-PI.toFloat() / 2f) }
    val progressState = remember { mutableFloatStateOf(0f) }

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
                        progressState.floatValue = TransformMath.progressAt(phase)
                    }
                    lastFrameTimeNanos = frameTimeNanos
                }
            }
        }
    }

    val pathOriginal = remember { Path() }
    val pathMoving = remember { Path() }
    val pathGhost = remember { Path() }

    val textMeasurer = rememberTextMeasurer()
    val readTextStyle = remember {
        TextStyle(
            color = ColorCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textDirection = TextDirection.Ltr
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // a) Mode chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedMode == 0,
                onClick = { selectedMode = 0 },
                label = { Text("انتقال") },
                modifier = Modifier.testTag("lab_transform_mode_translate")
            )
            FilterChip(
                selected = selectedMode == 1,
                onClick = { selectedMode = 1 },
                label = { Text("بازتاب") },
                modifier = Modifier.testTag("lab_transform_mode_reflect")
            )
            FilterChip(
                selected = selectedMode == 2,
                onClick = { selectedMode = 2 },
                label = { Text("دوران") },
                modifier = Modifier.testTag("lab_transform_mode_rotate")
            )
        }

        // b) Stage
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clickable { isPaused = !isPaused }
                .semantics {
                    contentDescription = "انیمیشن تبدیل\u200cهای هندسی؛ برای توقف یا ادامه لمس کنید"
                }
                .testTag("lab_transform_card"),
            shape = RoundedCornerShape(16.dp),
            color = ColorBackground,
            border = BorderStroke(1.dp, ColorCyan.copy(alpha = 0.35f))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                clipRect {
                    val w = size.width
                    val h = size.height
                    val t = progressState.floatValue
                    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

                    fun buildPath(path: Path, points: List<Pair<Float, Float>>) {
                        path.reset()
                        if (points.isNotEmpty()) {
                            path.moveTo(points[0].first, points[0].second)
                            for (i in 1 until points.size) {
                                path.lineTo(points[i].first, points[i].second)
                            }
                            path.close()
                        }
                    }

                    when (selectedMode) {
                        0 -> {
                            // Translation mode
                            val s = 0.22f * h
                            val baseX = 0.12f * w
                            val baseY = 0.55f * h
                            val vx = 0.44f * w
                            val vy = 0.14f * h

                            val origPoints = UNIT_L_VERTICES.map { (u, v) ->
                                Pair(baseX + u * s, baseY - v * s)
                            }
                            val movingPoints = origPoints.map { (ox, oy) ->
                                TransformMath.translate(ox, oy, vx, vy, t)
                            }
                            val finalPoints = origPoints.map { (ox, oy) ->
                                TransformMath.translate(ox, oy, vx, vy, 1f)
                            }

                            // 1. Guides: dashed vectors from original to final, plus faint ghost
                            for (i in origPoints.indices) {
                                drawLine(
                                    color = ColorPurple.copy(alpha = 0.40f),
                                    start = Offset(origPoints[i].first, origPoints[i].second),
                                    end = Offset(finalPoints[i].first, finalPoints[i].second),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = dashedEffect
                                )
                            }
                            buildPath(pathGhost, finalPoints)
                            drawPath(
                                path = pathGhost,
                                color = ColorPurple.copy(alpha = 0.35f),
                                style = Stroke(width = 1.2.dp.toPx(), pathEffect = dashedEffect)
                            )

                            // 2. Original L shape: cyan outline 2.dp + faint fill
                            buildPath(pathOriginal, origPoints)
                            drawPath(path = pathOriginal, color = ColorCyan.copy(alpha = 0.10f), style = Fill)
                            drawPath(path = pathOriginal, color = ColorCyan, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                            drawCircle(color = ColorGreen, center = Offset(origPoints[0].first, origPoints[0].second), radius = 3.dp.toPx())

                            // 3. Moving L shape: amber fill + outline 2.dp
                            buildPath(pathMoving, movingPoints)
                            drawPath(path = pathMoving, color = ColorAmber.copy(alpha = 0.35f), style = Fill)
                            drawPath(path = pathMoving, color = ColorAmber, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                            drawCircle(color = ColorGreen, center = Offset(movingPoints[0].first, movingPoints[0].second), radius = 3.5.dp.toPx())
                        }

                        1 -> {
                            // Reflection mode
                            val s = 0.22f * h
                            val mirrorX = 0.50f * w
                            val baseX = 0.42f * w - s
                            val baseY = 0.61f * h

                            val origPoints = UNIT_L_VERTICES.map { (u, v) ->
                                Pair(baseX + u * s, baseY - v * s)
                            }
                            val movingPoints = origPoints.map { (ox, oy) ->
                                TransformMath.reflectVertical(ox, oy, mirrorX, t)
                            }
                            val finalPoints = origPoints.map { (ox, oy) ->
                                TransformMath.reflectVertical(ox, oy, mirrorX, 1f)
                            }

                            // 1. Mirror line: vertical dashed green line across full stage
                            drawLine(
                                color = ColorGreen.copy(alpha = 0.85f),
                                start = Offset(mirrorX, 0f),
                                end = Offset(mirrorX, h),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = dashedEffect
                            )

                            // Ghost of reflected shape
                            buildPath(pathGhost, finalPoints)
                            drawPath(
                                path = pathGhost,
                                color = ColorPurple.copy(alpha = 0.35f),
                                style = Stroke(width = 1.2.dp.toPx(), pathEffect = dashedEffect)
                            )

                            // 2. Original L shape
                            buildPath(pathOriginal, origPoints)
                            drawPath(path = pathOriginal, color = ColorCyan.copy(alpha = 0.10f), style = Fill)
                            drawPath(path = pathOriginal, color = ColorCyan, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                            drawCircle(color = ColorGreen, center = Offset(origPoints[0].first, origPoints[0].second), radius = 3.dp.toPx())

                            // 3. Moving L shape (flattens at t = 0.5 and mirrors at t = 1)
                            buildPath(pathMoving, movingPoints)
                            drawPath(path = pathMoving, color = ColorAmber.copy(alpha = 0.35f), style = Fill)
                            drawPath(path = pathMoving, color = ColorAmber, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                            drawCircle(color = ColorGreen, center = Offset(movingPoints[0].first, movingPoints[0].second), radius = 3.5.dp.toPx())
                        }

                        2 -> {
                            // Rotation mode
                            val s = 0.20f * h
                            val cx = 0.50f * w
                            val cy = 0.50f * h
                            val baseX = 0.24f * w
                            val baseY = 0.46f * h

                            val origPoints = UNIT_L_VERTICES.map { (u, v) ->
                                Pair(baseX + u * s, baseY - v * s)
                            }
                            val angle = TransformMath.rotationAngle(t)
                            val movingPoints = origPoints.map { (ox, oy) ->
                                TransformMath.rotate(ox, oy, cx, cy, angle)
                            }

                            // 1. Guides: dashed circles through vertices around center C
                            for (idx in listOf(0, 1, 4)) {
                                val r = hypot(origPoints[idx].first - cx, origPoints[idx].second - cy)
                                drawCircle(
                                    color = ColorPurple.copy(alpha = 0.35f),
                                    center = Offset(cx, cy),
                                    radius = r,
                                    style = Stroke(width = 1.dp.toPx(), pathEffect = dashedEffect)
                                )
                            }

                            // Center C dot: glowing amber
                            val haloRadius = 10.dp.toPx()
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(ColorAmber.copy(alpha = 0.6f), ColorAmber.copy(alpha = 0f)),
                                    center = Offset(cx, cy),
                                    radius = haloRadius
                                ),
                                center = Offset(cx, cy),
                                radius = haloRadius
                            )
                            drawCircle(
                                color = ColorAmber,
                                center = Offset(cx, cy),
                                radius = 3.5.dp.toPx()
                            )

                            // 2. Original L shape
                            buildPath(pathOriginal, origPoints)
                            drawPath(path = pathOriginal, color = ColorCyan.copy(alpha = 0.10f), style = Fill)
                            drawPath(path = pathOriginal, color = ColorCyan, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                            drawCircle(color = ColorGreen, center = Offset(origPoints[0].first, origPoints[0].second), radius = 3.dp.toPx())

                            // 3. Moving L shape
                            buildPath(pathMoving, movingPoints)
                            drawPath(path = pathMoving, color = ColorAmber.copy(alpha = 0.35f), style = Fill)
                            drawPath(path = pathMoving, color = ColorAmber, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                            drawCircle(color = ColorGreen, center = Offset(movingPoints[0].first, movingPoints[0].second), radius = 3.5.dp.toPx())

                            // 4. Live readout at top-end area
                            val degStr = TransformMath.formatDegrees(angle)
                            val readout = "θ = $degStr"
                            val layout = textMeasurer.measure(
                                text = readout,
                                style = readTextStyle
                            )
                            val readoutTopLeft = Offset(w - layout.size.width - 16.dp.toPx(), 14.dp.toPx())
                            val padH = 6.dp.toPx()
                            val padV = 3.dp.toPx()
                            drawRoundRect(
                                color = ColorBackground.copy(alpha = 0.75f),
                                topLeft = Offset(readoutTopLeft.x - padH, readoutTopLeft.y - padV),
                                size = Size(layout.size.width + 2f * padH, layout.size.height + 2f * padV),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                            drawText(
                                textLayoutResult = layout,
                                topLeft = readoutTopLeft
                            )
                        }
                    }
                }
            }
        }

        // c) Caption (one sentence per mode, plain Text, bodyMedium, onBackground)
        val caption = when (selectedMode) {
            0 -> "در انتقال، همه\u200cی نقطه\u200cها به یک اندازه و در یک جهت جابه\u200cجا می\u200cشوند و شکل تغییر نمی\u200cکند."
            1 -> "در بازتاب، هر نقطه به اندازه\u200cی فاصله\u200cاش از خط تقارن، آن طرف خط می\u200cرود و شکل برعکس می\u200cشود."
            else -> "در دوران، هر نقطه روی دایره\u200cای به مرکز دوران می\u200cچرخد و فاصله\u200cاش تا مرکز ثابت می\u200cماند."
        }
        Text(
            text = caption,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // d) Slider with label
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "پیشرفت تبدیل",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Slider(
                value = progressState.floatValue,
                onValueChange = { newT ->
                    isPaused = true
                    progressState.floatValue = newT
                    phase = TransformMath.phaseForProgress(newT)
                },
                valueRange = 0f..1f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lab_transform_slider")
            )
        }
    }
}
