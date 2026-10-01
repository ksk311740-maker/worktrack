package com.example.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Reusable press scale effect for interactive buttons, cards, and icons.
 * Provides a responsive, tactile feel like high-end video editing suites.
 */
@Composable
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    scaleDownTo: Float = 0.94f,
    onClick: () -> Unit
): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDownTo else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press_scale"
    )

    return this
        .scale(scale)
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null) {
                        onClick()
                    }
                }
            }
        }
}

/**
 * Animated interaction scale for standard Buttons, FloatingActionButtons, and Cards.
 */
@Composable
fun rememberPressScaleModifier(
    interactionSource: MutableInteractionSource,
    scaleDownTo: Float = 0.94f
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDownTo else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press_scale_state"
    )
    return Modifier.scale(scale)
}

/**
 * Staggered entrance animation for form sections and lists.
 */
@Composable
fun StaggeredEntrance(
    index: Int,
    delayStepMs: Int = 38,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay((index * delayStepMs).toLong())
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "stagger_alpha"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 16f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "stagger_offset"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.97f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "stagger_scale"
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(0, offsetY.dp.roundToPx()) }
            .scale(scale)
            .alpha(alpha)
    ) {
        content()
    }
}

/**
 * Subtle Video Editing Timeline Track Graphic.
 * Displays a clean editing track with tick marks and an elegant animated playhead.
 */
@Composable
fun VideoTimelineRuler(
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "timeline_sweep")
    val sweepPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "playhead_x"
    )

    val tickColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    val playheadColor = accentColor.copy(alpha = 0.75f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
    ) {
        val width = size.width
        val height = size.height

        // Bottom timeline bar line
        drawLine(
            color = tickColor,
            start = Offset(0f, height - 2f),
            end = Offset(width, height - 2f),
            strokeWidth = 1.5f
        )

        // Ticks every 36px
        val step = 36f
        var currentX = 8f
        var count = 0
        while (currentX < width - 8f) {
            val isMajor = (count % 5 == 0)
            val tickHeight = if (isMajor) height * 0.65f else height * 0.35f

            drawLine(
                color = if (isMajor) accentColor.copy(alpha = 0.45f) else tickColor,
                start = Offset(currentX, height - 2f),
                end = Offset(currentX, height - 2f - tickHeight),
                strokeWidth = if (isMajor) 1.5f else 1f
            )
            currentX += step
            count++
        }

        // Animated playhead needle
        val playheadX = sweepPosition * (width - 16f) + 8f
        drawLine(
            color = playheadColor,
            start = Offset(playheadX, 0f),
            end = Offset(playheadX, height),
            strokeWidth = 2f
        )

        // Tiny playhead diamond tip
        val tipSize = 3.5f
        drawCircle(
            color = accentColor,
            radius = tipSize,
            center = Offset(playheadX, tipSize)
        )
    }
}

/**
 * Viewfinder Corner Ticks.
 * Draws subtle camera/editing viewfinder crop marks around prominent cards like Video Minutes.
 */
@Composable
fun ViewfinderMarks(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val arm = 14f
        val stroke = 2f

        // Top-left
        drawLine(color, Offset(0f, 0f), Offset(arm, 0f), stroke)
        drawLine(color, Offset(0f, 0f), Offset(0f, arm), stroke)

        // Top-right
        drawLine(color, Offset(w, 0f), Offset(w - arm, 0f), stroke)
        drawLine(color, Offset(w, 0f), Offset(w, arm), stroke)

        // Bottom-left
        drawLine(color, Offset(0f, h), Offset(arm, h), stroke)
        drawLine(color, Offset(0f, h), Offset(0f, h - arm), stroke)

        // Bottom-right
        drawLine(color, Offset(w, h), Offset(w - arm, h), stroke)
        drawLine(color, Offset(w, h), Offset(w, h - arm), stroke)
    }
}

/**
 * Animated Checkmark for Save Confirmation.
 * Communicates: "Project successfully recorded."
 * Fast, satisfying animation under 500-600ms total.
 */
@Composable
fun SuccessCheckmarkAnimation(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF10B981),
    onAnimationEnd: () -> Unit = {}
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
        )
        delay(90)
        onAnimationEnd()
    }

    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        val p = progress.value

        val path = Path()
        val startX = size.width * 0.22f
        val startY = size.height * 0.52f
        val midX = size.width * 0.44f
        val midY = size.height * 0.74f
        val endX = size.width * 0.80f
        val endY = size.height * 0.30f

        path.moveTo(startX, startY)
        if (p <= 0.45f) {
            val step = p / 0.45f
            path.lineTo(startX + (midX - startX) * step, startY + (midY - startY) * step)
        } else {
            path.lineTo(midX, midY)
            val step = (p - 0.45f) / 0.55f
            path.lineTo(midX + (endX - midX) * step, midY + (endY - midY) * step)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

/**
 * Animated count-up text for dashboard statistics when numbers change.
 */
@Composable
fun AnimatedNumberText(
    targetValue: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    prefix: String = "",
    suffix: String = "",
    decimals: Int = 0
) {
    val animatedValue by animateFloatAsState(
        targetValue = targetValue.toFloat(),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "animated_number"
    )

    val formatted = if (decimals == 0) {
        "${prefix}${animatedValue.toInt()}${suffix}"
    } else {
        "${prefix}${String.format(Locale.US, "%,.${decimals}f", animatedValue)}${suffix}"
    }

    Text(
        text = formatted,
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight
    )
}

/**
 * Tiny Video Waveform graphic for subtle visual texture inspired by video editing suites.
 */
@Composable
fun MiniWaveformGraphic(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
    barCount: Int = 14
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Canvas(modifier = modifier.height(14.dp)) {
        val w = size.width
        val h = size.height
        val barWidth = 2.5f
        val gap = (w - (barCount * barWidth)) / (barCount - 1).coerceAtLeast(1)

        for (i in 0 until barCount) {
            val sinVal = Math.sin(phase + (i * 0.55)).toFloat()
            val normalizedHeight = 0.3f + 0.6f * ((sinVal + 1f) / 2f)
            val barH = (h * normalizedHeight).coerceAtLeast(2f)
            val x = i * (barWidth + gap)
            val y = (h - barH) / 2f
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f)
            )
        }
    }
}
