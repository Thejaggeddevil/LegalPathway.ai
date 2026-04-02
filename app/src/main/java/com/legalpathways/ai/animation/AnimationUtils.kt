package com.legalpathways.ai.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.legalpathways.ai.ui.theme.EmeraldAccent
import com.legalpathways.ai.ui.theme.GoldPrimary
import kotlinx.coroutines.delay

/**
 * MCQ Swipe-to-Answer Animation Module
 * Handles smooth fill animations and state transitions
 */

// ── State Tracking ────────────────────────────────────────────────────────────
data class MCQAnimationState(
    val fillProgress: Float = 0f,
    val isSelected: Boolean = false,
    val isDragging: Boolean = false,
    val showHint: Boolean = true
)

// ── Fill Progress Indicator ───────────────────────────────────────────────────
@Composable
fun FillProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = GoldPrimary
) {
    Box(
        modifier = modifier
            .fillMaxWidth(progress)
            .background(
                color = color.copy(alpha = 0.3f + (progress * 0.6f)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
            )
    )
}

// ── Confirmation Animation ────────────────────────────────────────────────────
// ✅ FIXED: Changed DampingRatioHighBouncy to spring(dampingRatio = 0.5f)
@Composable
fun ConfirmationCheckmark(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            scale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = 0.5f,  // ✅ CORRECTED
                    stiffness = Spring.StiffnessHigh
                )
            )
        } else {
            scale.snapTo(0f)
        }
    }

    if (isVisible) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = "Selected",
            tint = Color.White,
            modifier = modifier
                .size(24.dp)
                .scale(scale.value)
        )
    }
}

// ── Selection Pulse Animation ─────────────────────────────────────────────────
@Composable
fun SelectionPulse(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(isActive) {
        if (isActive) {
            while (true) {
                alpha.animateTo(0.3f, tween(600, easing = EaseInOutCubic))
                alpha.animateTo(1f, tween(600, easing = EaseInOutCubic))
            }
        }
    }

    Box(
        modifier = modifier
            .size(4.dp)
            .background(color = GoldPrimary.copy(alpha = alpha.value), shape = CircleShape)
    )
}

// ── Swipe Hint Animation ──────────────────────────────────────────────────────
@Composable
fun SwipeHintArrow(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            while (true) {
                offsetX.animateTo(16f, tween(1000, easing = EaseInOutCubic))
                offsetX.animateTo(0f, tween(1000, easing = EaseInOutCubic))
            }
        }
    }

    if (isVisible) {
        Text(
            "→",
            modifier = modifier.offset(x = offsetX.value.dp),
            color = GoldPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize
        )
    }
}

// ── Drag Progress Feedback ────────────────────────────────────────────────────
@Composable
fun DragProgressFeedback(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left track
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .background(
                    color = GoldPrimary.copy(alpha = 0.15f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                )
        )

        // Progress label
        Text(
            "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = GoldPrimary.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 8.dp),
            fontWeight = FontWeight.Bold
        )

        // Right track
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .background(
                    color = GoldPrimary.copy(alpha = 0.15f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                )
        )
    }
}

// ── Threshold Warning ─────────────────────────────────────────────────────────
@Composable
fun ThresholdWarning(
    isNearThreshold: Boolean,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isNearThreshold) {
        if (isNearThreshold) {
            while (true) {
                scale.animateTo(1.05f, tween(300, easing = EaseInOutCubic))
                scale.animateTo(1f, tween(300, easing = EaseInOutCubic))
            }
        } else {
            scale.snapTo(1f)
        }
    }

    Box(
        modifier = modifier.scale(scale.value)
    ) {
        Row(
            modifier = Modifier
                .background(
                    color = GoldPrimary.copy(alpha = 0.1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                if (isNearThreshold) "Release to select!" else "",
                style = MaterialTheme.typography.labelSmall,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ── Batch Answer Animation ────────────────────────────────────────────────────
@Composable
fun BatchAnswerAnimation(
    answeredCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val fillWidth = remember { Animatable(0f) }

    LaunchedEffect(answeredCount) {
        fillWidth.animateTo(
            (answeredCount.toFloat() / totalCount.toFloat()),
            animationSpec = tween(800, easing = EaseOutCubic)
        )
    }

    Box(modifier = modifier.fillMaxWidth()) {
        // Background track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
        )

        // Progress fill
        Box(
            modifier = Modifier
                .fillMaxWidth(fillWidth.value)
                .height(8.dp)
                .background(
                    color = EmeraldAccent,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
        )

        // Progress text
        Text(
            "$answeredCount/$totalCount",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
        )
    }
}

// ── Completion Celebration Animation ──────────────────────────────────────────
@Composable
fun CompletionCelebration(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val scaleX = remember { Animatable(1f) }
    val scaleY = remember { Animatable(1f) }

    LaunchedEffect(isActive) {
        if (isActive) {
            scaleX.animateTo(1.1f, tween(300, easing = EaseOutCubic))
            scaleY.animateTo(1.1f, tween(300, easing = EaseOutCubic))

            delay(200)

            scaleX.animateTo(0.95f, tween(200, easing = EaseInCubic))
            scaleY.animateTo(0.95f, tween(200, easing = EaseInCubic))

            delay(200)

            scaleX.animateTo(1f, tween(300, easing = EaseOutCubic))
            scaleY.animateTo(1f, tween(300, easing = EaseOutCubic))
        }
    }

    if (isActive) {
        Box(
            modifier = modifier
                .size(80.dp)
                .scale(scaleY = scaleY.value, scaleX = scaleX.value)
                .background(
                    color = EmeraldAccent.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Complete",
                tint = EmeraldAccent,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

// ── State Management Extension ────────────────────────────────────────────────
fun MCQAnimationState.copy(
    fillProgress: Float = this.fillProgress,
    isSelected: Boolean = this.isSelected,
    isDragging: Boolean = this.isDragging,
    showHint: Boolean = this.showHint
) = MCQAnimationState(fillProgress, isSelected, isDragging, showHint)

// ── Helper: Check if at critical threshold ───────────────────────────────────
fun isAtCriticalThreshold(progress: Float, threshold: Float = 0.75f): Boolean {
    return progress >= threshold && progress < 1f
}

// ── Helper: Should show hint ──────────────────────────────────────────────────
fun shouldShowHint(progress: Float, isSelected: Boolean, isDragging: Boolean): Boolean {
    return progress == 0f && !isSelected && !isDragging
}

// ── Helper: Map progress to alpha ─────────────────────────────────────────────
fun progressToAlpha(progress: Float): Float {
    return 0.3f + (progress * 0.7f)
}

// ── Helper: Map progress to color ────────────────────────────────────────────
fun progressToColor(progress: Float, selectedColor: Color, inactiveColor: Color): Color {
    val ratio = progress.coerceIn(0f, 1f)
    return Color(
        red = inactiveColor.red * (1 - ratio) + selectedColor.red * ratio,
        green = inactiveColor.green * (1 - ratio) + selectedColor.green * ratio,
        blue = inactiveColor.blue * (1 - ratio) + selectedColor.blue * ratio,
        alpha = inactiveColor.alpha * (1 - ratio) + selectedColor.alpha * ratio
    )
}