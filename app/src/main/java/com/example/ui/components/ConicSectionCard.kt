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
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.util.ConicMath
import com.example.util.ConicType
import com.example.util.UnitCircleMath
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin

private val ColorBackground = Color(0xFF0B132B)
private val ColorCyan = Color(0xFF00E5FF)
private val ColorPurple = Color(0xFF7C4DFF)
private val ColorAmber = Color(0xFFFFB300)
private val ColorGreen = Color(0xFF00E676)
private val ColorGrid = Color(0xFF334155)

private const val PHASE_SPEED_RAD_PER_SEC = 0.35f
private const val MAX_DT_SECONDS = 0.05f

@Composable
fun ConicSectionCard(
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isPaused by remember { mutableStateOf(false) }
    val eccentricityState = remember { mutableFloatStateOf(0f) }
    var phase by remember { mutableFloatStateOf(ConicMath.phaseForEccentricity(0f)) }

    val conicType by remember {
        derivedStateOf { ConicMath.classify(eccentricityState.floatValue) }
    }

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
                        eccentricityState.floatValue = ConicMath.eccentricityAt(phase)
                    }
                    lastFrameTimeNanos = frameTimeNanos
                }
            }
        }
    }

    val path = remember { Path() }

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
                    contentDescription = "انیمیشن مقاطع مخروطی؛ برای توقف یا ادامه لمس کنید"
                }
                .testTag("lab_conic_card"),
            shape = RoundedCornerShape(16.dp),
            color = ColorBackground,
            border = BorderStroke(1.dp, ColorCyan.copy(alpha = 0.35f))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val e = eccentricityState.floatValue
                val fx = size.width * 0.30f
                val fy = size.height * 0.50f
                val pxPerUnit = size.height / 4f
                val currentType = ConicMath.classify(e)

                val curveColor = when (currentType) {
                    ConicType.CIRCLE -> ColorGreen
                    ConicType.ELLIPSE -> ColorCyan
                    ConicType.PARABOLA -> ColorAmber
                    ConicType.HYPERBOLA -> ColorPurple
                }

                clipRect {
                    // 1. Faint axes through focus
                    drawLine(
                        color = ColorGrid,
                        start = Offset(0f, fy),
                        end = Offset(size.width, fy),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = ColorGrid,
                        start = Offset(fx, 0f),
                        end = Offset(fx, size.height),
                        strokeWidth = 1.dp.toPx()
                    )

                    // 2. Directrix as a dashed amber line
                    if (e >= 0.05f) {
                        val directrixX = fx + (1f / e) * pxPerUnit
                        if (directrixX in 0f..size.width) {
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            drawLine(
                                color = ColorAmber.copy(alpha = 0.75f),
                                start = Offset(directrixX, 0f),
                                end = Offset(directrixX, size.height),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = dashEffect
                            )
                        }
                    }

                    // 3. Curve sampling and path construction
                    path.reset()
                    val sampleCount = 360
                    val dTheta = UnitCircleMath.TWO_PI / sampleCount
                    var subPathActive = false
                    val margin = size.height

                    for (i in 0..sampleCount) {
                        val theta = i * dTheta
                        val r = ConicMath.radius(theta, e, 1f)
                        if (r == null) {
                            subPathActive = false
                            continue
                        }
                        val xUnits = r * cos(theta.toDouble()).toFloat()
                        val yUnits = r * sin(theta.toDouble()).toFloat()
                        val cx = fx + xUnits * pxPerUnit
                        val cy = fy - yUnits * pxPerUnit

                        val isInside = cx in -margin..(size.width + margin) && cy in -margin..(size.height + margin)
                        if (!isInside) {
                            subPathActive = false
                            continue
                        }

                        if (!subPathActive) {
                            path.moveTo(cx, cy)
                            subPathActive = true
                        } else {
                            path.lineTo(cx, cy)
                        }
                    }

                    // Soft wider glow underneath
                    drawPath(
                        path = path,
                        color = curveColor.copy(alpha = 0.25f),
                        style = Stroke(
                            width = 6.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                    // Crisp main stroke
                    drawPath(
                        path = path,
                        color = curveColor,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // 4. Glowing amber focus
                    val haloRadius = 12.dp.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ColorAmber.copy(alpha = 0.6f), ColorAmber.copy(alpha = 0f)),
                            center = Offset(fx, fy),
                            radius = haloRadius
                        ),
                        center = Offset(fx, fy),
                        radius = haloRadius
                    )
                    drawCircle(
                        color = ColorAmber,
                        center = Offset(fx, fy),
                        radius = 4.dp.toPx()
                    )
                }
            }
        }

        val conicTypeName = when (conicType) {
            ConicType.CIRCLE -> "دایره"
            ConicType.ELLIPSE -> "بیضی"
            ConicType.PARABOLA -> "سهمی"
            ConicType.HYPERBOLA -> "هذلولی"
        }
        Text(
            text = "نوع منحنی: $conicTypeName",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("lab_conic_type_label")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "خروج از مرکز",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Slider(
                value = eccentricityState.floatValue,
                onValueChange = { newE ->
                    isPaused = true
                    eccentricityState.floatValue = newE
                    phase = ConicMath.phaseForEccentricity(newE)
                },
                valueRange = 0f..2f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lab_conic_slider")
            )
        }
    }
}
