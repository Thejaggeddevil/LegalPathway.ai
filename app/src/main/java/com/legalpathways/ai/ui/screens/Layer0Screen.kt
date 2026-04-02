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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.legalpathways.ai.model.Layer0Request
import com.legalpathways.ai.ui.components.*
import com.legalpathways.ai.ui.theme.*
import com.legalpathways.ai.viewmodel.MainViewModel
import com.legalpathways.ai.viewmodel.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Layer0Screen(onBack: () -> Unit, vm: MainViewModel = viewModel()) {
    val state      by vm.layer0State.collectAsState()
    var step       by remember { mutableStateOf(1) }
    var relStatus  by remember { mutableStateOf("") }
    var religion   by remember { mutableStateOf("") }
    var marriageAct by remember { mutableStateOf("") }
    var children   by remember { mutableStateOf(false) }
    var childrenSelected by remember { mutableStateOf(false) }
    var income     by remember { mutableStateOf("") }
    var risk       by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    // MCQ Data
    val mcqSteps = listOf(
        MCQQuestion(
            id = "relStatus",
            question = "What is your current relationship status?",
            options = listOf(
                "dating" to "Dating",
                "engaged" to "Engaged",
                "married" to "Married",
                "separated" to "Separated"
            )
        ),
        MCQQuestion(
            id = "religion",
            question = "Which religion / marriage act applies?",
            options = listOf(
                "Hindu" to "Hindu (HMA)",
                "Muslim" to "Muslim (MLA)",
                "Christian" to "Christian (IDA)",
                "Special Marriage" to "Special Marriage Act (SMA)",
                "Parsi" to "Parsi (PMDA)"
            )
        ),
        MCQQuestion(
            id = "children",
            question = "Do you have children together?",
            options = listOf(
                "yes" to "Yes",
                "no" to "No"
            )
        ),
        MCQQuestion(
            id = "income",
            question = "What is your income bracket?",
            options = listOf(
                "low" to "Low Income",
                "medium" to "Medium Income",
                "high" to "High Income"
            )
        ),
        MCQQuestion(
            id = "risk",
            question = "Any risk indicators in your situation?",
            options = listOf(
                "none" to "No Risk",
                "financial_dependency" to "Financial Dependency",
                "abuse" to "Abuse or Harm",
                "violence" to "Violence",
                "abandonment" to "Abandonment"
            )
        )
    )

    LaunchedEffect(state) {
        if (state is UiState.Idle) step = 1
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            NavyDeep.copy(alpha = 0.15f),
            ParchmentLight
        ),
        startY = 0f,
        endY = 1200f
    )

    Scaffold(topBar = { LegalTopBar("Phase 0 – Legal Positioning", onBack) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(padding)
        ) {
            when (val s = state) {
                is UiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Layer0Result(data = s.data) { vm.resetLayer0(); step = 1 }
                    }
                }
                is UiState.Loading -> LoadingContent()
                is UiState.Error   -> ErrorContent(s.message) { vm.resetLayer0() }
                else -> {
                    // MCQ Flow - FIXED LAYOUT
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Main content - scrollable
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp),
                            verticalArrangement = Arrangement.Top
                        ) {
                            when (step) {
                                1 -> MCQStepDisplayFixed(
                                    mcqSteps[0],
                                    relStatus,
                                    onSelect = {
                                        relStatus = it
                                        coroutineScope.launch {
                                            delay(400)
                                            step++
                                        }
                                    }
                                )
                                2 -> MCQStepDisplayFixed(
                                    mcqSteps[1],
                                    religion,
                                    onSelect = {
                                        religion = it
                                        marriageAct = mapReligionToAct(it)
                                        coroutineScope.launch {
                                            delay(400)
                                            step++
                                        }
                                    }
                                )
                                3 -> {
                                    val childMcq = MCQQuestion(
                                        id = "children",
                                        question = mcqSteps[2].question,
                                        options = mcqSteps[2].options
                                    )
                                    MCQStepDisplayFixed(
                                        childMcq,
                                        if (childrenSelected) (if (children) "yes" else "no") else "",
                                        onSelect = {
                                            children = it == "yes"
                                            childrenSelected = true
                                            coroutineScope.launch {
                                                delay(400)
                                                step++
                                            }
                                        }
                                    )
                                }
                                4 -> MCQStepDisplayFixed(
                                    mcqSteps[3],
                                    income,
                                    onSelect = {
                                        income = it
                                        coroutineScope.launch {
                                            delay(400)
                                            step++
                                        }
                                    }
                                )
                                5 -> MCQStepDisplayFixed(
                                    mcqSteps[4],
                                    risk,
                                    onSelect = {
                                        risk = it
                                        coroutineScope.launch {
                                            delay(400)
                                            step++
                                        }
                                    }
                                )
                                6 -> ReviewStepModern(relStatus, religion, children, income, risk)
                            }
                        }

                        // Bottom action bar
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // PREVIOUS button - ALWAYS visible in all modes
                                IconButton(
                                    onClick = { if (step > 1) step-- },
                                    enabled = step > 1,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = if (step > 1)
                                                NavyMid
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant,
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.ChevronLeft,
                                        contentDescription = "Previous Step",
                                        tint = if (step > 1)
                                            Color.White
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // PROGRESS dots
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    repeat(6) { i ->
                                        Box(
                                            modifier = Modifier
                                                .size(if (i == step - 1) 10.dp else 8.dp)
                                                .background(
                                                    color = if (i < step)
                                                        NavyMid
                                                    else
                                                        MaterialTheme.colorScheme.surfaceVariant,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }

                                // NEXT button (sirf step 1-5 pe)
                                if (step < 6) {
                                    IconButton(
                                        onClick = { step++ },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                color = NavyMid,
                                                shape = CircleShape
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = "Next Step",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                // SUBMIT button (sirf step 6 pe)
                                if (step == 6) {
                                    Button(
                                        onClick = {
                                            vm.submitLayer0(
                                                request = Layer0Request(
                                                    relationshipStatus = relStatus,
                                                    religion = religion,
                                                    marriageAct = marriageAct,
                                                    childrenFlag = children,        // hasChildren ❌ → childrenFlag ✅
                                                    incomeRange = income,           // incomeBracket ❌ → incomeRange ✅
                                                    riskIndicator = risk            // listOf(risk) ❌ → simply risk ✅
                                                )
                                            )
                                        },
                                        modifier = Modifier
                                            .wrapContentWidth()
                                            .height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NavyMid),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Send,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("Submit", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// FIXED MCQ STEP DISPLAY
// - Question FIXED at top
// - Options don't push question
// - Clear visual selection with COLOR FILL + BORDER
// ==========================================
@Composable
fun MCQStepDisplayFixed(
    mcq: MCQQuestion,
    selectedValue: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ─── FIXED QUESTION (TOP) ───
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = NavyMid.copy(alpha = 0.08f),
            border = BorderStroke(1.5.dp, NavyMid.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    mcq.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 17.sp,
                    lineHeight = 24.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // ─── OPTIONS (BELOW QUESTION) ───
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            mcq.options.forEachIndexed { index, (value, label) ->
                MCQOptionButtonFixed(
                    label = label,
                    letter = ('A' + index).toString(),
                    isSelected = selectedValue == value,
                    onClick = { onSelect(value) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ==========================================
// FIXED MCQ OPTION BUTTON
// - BLUE FILL COLOR (NavyMid)
// - VISIBLE BORDER when selected
// - CHECKMARK icon
// - 600ms animation
// ==========================================
@Composable
fun MCQOptionButtonFixed(
    label: String,
    letter: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val fillAnimation = remember { Animatable(0f) }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            fillAnimation.animateTo(1f, animationSpec = tween(600, easing = EaseOutCubic))
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
        // ─── ANIMATED FILL (BLUE COLOR) ───
        if (fillAnimation.value > 0f) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(fillAnimation.value)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart),
                color = NavyMid.copy(alpha = 0.75f),
                shape = RoundedCornerShape(28.dp)
            ) {}
        }

        // ─── WHITE BACKGROUND + BORDER + CONTENT ───
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
            border = BorderStroke(
                width = if (isSelected) 3.dp else 2.dp,
                color = if (isSelected) NavyMid else NavyMid.copy(alpha = 0.25f)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ─── LETTER CIRCLE ───
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) NavyMid else NavyMid.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            letter,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) Color.White else NavyDeep,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // ─── OPTION LABEL ───
                Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = NavyDeep,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )

                // ─── CHECKMARK (when selected) ───
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = NavyMid,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Review step with modern design
 */
@Composable
fun ReviewStepModern(rel: String, religion: String, children: Boolean, income: String, risk: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp)
    ) {
        Text(
            "Review Your Answers",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReviewRow("Relationship Status:", rel.replaceFirstChar { it.uppercase() })
                ReviewRow("Religion / Act:", religion)
                ReviewRow("Children:", if (children) "Yes" else "No")
                ReviewRow("Income Bracket:", income.replaceFirstChar { it.uppercase() })
                ReviewRow("Risk Indicators:", risk.replace("_", " ").replaceFirstChar { it.uppercase() })
            }
        }

        Text(
            " Click Submit to get your legal position analysis",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
    }
}

/**
 * Map religion selection to marriage act code
 */
fun mapReligionToAct(religion: String): String = when (religion) {
    "Hindu" -> "HMA"
    "Muslim" -> "MLA"
    "Christian" -> "IDA"
    "Special Marriage" -> "SMA"
    "Parsi" -> "PMDA"
    else -> "HMA"
}

/**
 * Existing result component (kept from original)
 */
@Composable
fun Layer0Result(data: com.legalpathways.ai.model.Layer0Data, onReset: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("📍 Your Legal Position", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)

        data.applicableLaw?.let {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = NavyMid.copy(alpha = 0.06f))) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SectionHeader("Applicable Law", Icons.Default.MenuBook)
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        if (!data.allowedActions.isNullOrEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = EmeraldAccent.copy(alpha = 0.06f))) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SectionHeader("Allowed Actions", Icons.Default.CheckCircle)
                    BulletList(data.allowedActions, EmeraldAccent)
                }
            }
        }

        if (!data.blockedActions.isNullOrEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CrimsonAccent.copy(alpha = 0.06f))) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SectionHeader("Blocked Actions", Icons.Default.Block)
                    BulletList(data.blockedActions, CrimsonAccent)
                }
            }
        }

        data.recommendedNextStep?.let {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = GoldPrimary.copy(alpha = 0.06f))) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SectionHeader("Recommended Next Step", Icons.Default.ArrowForward)
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, NavyMid),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyMid)
        ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("New Assessment")
        }
    }
}


// Data class - Keep from original
data class MCQQuestion(
    val id: String,
    val question: String,
    val options: List<Pair<String, String>>,
    val imageUrl: String? = null
)