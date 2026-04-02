package com.legalpathways.ai.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.legalpathways.ai.ui.theme.*

/**
 * Modern MCQ Screen with Auto-Advance
 * - Beautiful purple gradient background matching reference design
 * - Tap to select (no swipe)
 * - Auto-advances to next question on selection
 * - Fill animation on tap
 * - Optional prev/next navigation at bottom
 */

//data class MCQQuestion(
//    val id: String,
//    val question: String,
//    val options: List<Pair<String, String>>, // value to label
//    val imageUrl: String? = null
//)

@Composable
fun MCQScreen(
    questions: List<MCQQuestion>,
    onComplete: (Map<String, String>) -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    val answers = remember { mutableMapOf<String, String>() }
    val isLastQuestion = currentIndex == questions.size - 1
    var showingFillAnimation by remember { mutableStateOf(false) }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE6D7F0), // Light lavender top
            Color(0xFFF0E6FF)  // Very light purple bottom
        ),
        startY = 0f,
        endY = 2000f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 60.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ─── Question and Options (Main Content) ─────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(Modifier.height(20.dp))

                // Question container
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFB092D6),
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            questions[currentIndex].question,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 18.sp,
                            lineHeight = 24.sp
                        )
                    }
                }

                Spacer(Modifier.height(60.dp))

                // Options
                val currentQuestion = questions[currentIndex]
                currentQuestion.options.forEachIndexed { index, (value, label) ->
                    MCQOptionButtonNew(
                        label = label,
                        letter = ('A' + index).toString(),
                        isSelected = answers[currentQuestion.id] == value,
                        onAnimationComplete = {
                            if (!isLastQuestion) {
                                currentIndex++
                            }
                        },
                        onClick = {
                            answers[currentQuestion.id] = value
                            showingFillAnimation = true
                        }
                    )
                    Spacer(Modifier.height(14.dp))
                }
            }

            // ─── Bottom Navigation ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button (always visible)
                IconButton(
                    onClick = {
                        if (currentIndex > 0) {
                            currentIndex--
                            showingFillAnimation = false
                        }
                    },
                    enabled = currentIndex > 0,
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            color = if (currentIndex > 0) Color(0xFFB092D6) else Color(0xFFB092D6).copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Previous Question",
                        tint = if (currentIndex > 0) Color.White else Color(0xFFB092D6).copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Progress indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                ) {
                    repeat(questions.size) { i ->
                        Box(
                            modifier = Modifier
                                .size(if (i == currentIndex) 10.dp else 8.dp)
                                .background(
                                    color = if (i <= currentIndex) Color(0xFFB092D6) else Color(0xFFB092D6).copy(alpha = 0.25f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Next button or Submit
                if (currentIndex < questions.size - 1) {
                    IconButton(
                        onClick = { currentIndex++ },
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                Color(0xFFB092D6),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Next Question",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    // Submit button on last question
                    Button(
                        onClick = { onComplete(answers.toMap()) },
                        modifier = Modifier
                            .size(width = 120.dp, height = 52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB092D6)),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Text(
                            "Submit",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual MCQ Option Button with Tap Animation
 * - No swipe required
 * - Fill animation on tap (600ms)
 * - Calls onAnimationComplete when fill animation finishes
 * - Auto-advances when tapped
 */
@Composable
fun MCQOptionButtonNew(
    label: String,
    letter: String,
    isSelected: Boolean,
    onAnimationComplete: () -> Unit = {},
    onClick: () -> Unit
) {
    val fillAnimation = remember { Animatable(0f) }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            fillAnimation.animateTo(1f, animationSpec = tween(600, easing = EaseOutCubic))
            onAnimationComplete() // Call after animation completes
        } else {
            fillAnimation.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(enabled = !isSelected) { onClick() }
    ) {
        // Border + Content (behind the fill)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
            border = BorderStroke(
                width = if (isSelected) 2.5.dp else 2.dp,
                color = Color(0xFFB092D6).copy(alpha = if (isSelected) 1f else 0.4f)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Letter circle
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFB092D6).copy(alpha = if (isSelected) 1f else 0.3f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            letter,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) Color.White else Color(0xFF6B4A8A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Label text
                Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) Color.White else Color(0xFF6B4A8A),
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )

                // Checkmark or placeholder
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Background fill animation (overlay)
        if (fillAnimation.value > 0f) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(fillAnimation.value)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart),
                color = Color(0xFFB092D6).copy(alpha = 0.7f),
                shape = RoundedCornerShape(28.dp)
            ) {}
        }
    }
}

// ─── Preview ────────────────────────────────────────────────────────────────
@Composable
fun MCQScreenPreview() {
    val sampleQuestions = listOf(
        MCQQuestion(
            id = "q1",
            question = "Which planet in the Solar System is the smallest?",
            options = listOf(
                "pluto" to "Pluto",
                "earth" to "Earth",
                "mercury" to "Mercury",
                "mars" to "Mars"
            )
        ),
        MCQQuestion(
            id = "q2",
            question = "What is the capital of France?",
            options = listOf(
                "paris" to "Paris",
                "lyon" to "Lyon",
                "marseille" to "Marseille",
                "nice" to "Nice"
            )
        ),
        MCQQuestion(
            id = "q3",
            question = "Which element has the chemical symbol Au?",
            options = listOf(
                "silver" to "Silver",
                "gold" to "Gold",
                "aluminum" to "Aluminum",
                "argon" to "Argon"
            )
        )
    )

    MCQScreen(
        questions = sampleQuestions,
        onComplete = { answers -> println("Completed: $answers") }
    )
}