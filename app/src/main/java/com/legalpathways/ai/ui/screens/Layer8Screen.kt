package com.legalpathways.ai.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.legalpathways.ai.ui.components.*
import com.legalpathways.ai.ui.theme.*
import com.legalpathways.ai.viewmodel.MainViewModel
import com.legalpathways.ai.viewmodel.UiState

private val financialKeywords = listOf("financial","income","assets","liabilities","maintenance","alimony","expenses")

fun isFinancial(text: String) = financialKeywords.any { text.lowercase().contains(it) }

@Composable
fun Layer8Screen(onBack: () -> Unit, vm: MainViewModel = viewModel()) {
    val state by vm.layer8State.collectAsState()
    LaunchedEffect(Unit) { vm.loadLayer8() }

    Scaffold(topBar = { LegalTopBar("Phase 8 – Trial Timeline", onBack) }) { padding ->
        when (val s = state) {
            is UiState.Loading -> LoadingContent()
            is UiState.Error   -> ErrorContent(s.message) { vm.loadLayer8() }
            is UiState.Success -> {
                val data = s.data
                LazyColumn(
                    contentPadding      = padding + PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors   = CardDefaults.cardColors(containerColor = NavyMid.copy(alpha = 0.06f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(data.phase, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                InfoRow("Dataset:", data.datasetName)
                                InfoRow("Jurisdiction:", data.jurisdiction)
                                SectionHeader("Applicable Laws", Icons.Default.Gavel)
                                BulletList(data.applicableLaws)
                            }
                        }
                    }

                    // Stage cards
                    itemsIndexed(data.stages) { index, stage ->
                        Card(
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(12.dp),
                            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            var expanded by remember { mutableStateOf(false) }
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expanded = !expanded }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Step number circle
                                    Surface(
                                        shape = CircleShape,
                                        color = GoldPrimary.copy(alpha = 0.15f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("${index + 1}", style = MaterialTheme.typography.labelMedium, color = GoldDark, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        stage.caseStage,
                                        style    = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (expanded) {
                                    GoldDivider(Modifier.padding(horizontal = 14.dp))
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        // Duration
                                        val dur = stage.expectedDuration
                                        if (dur != null) {
                                            SectionHeader("⏳ Expected Duration")
                                            when (dur) {
                                                is Map<*, *> -> dur.forEach { (k, v) -> InfoRow("${k.toString().replace("_"," ").replaceFirstChar{it.uppercase()}}:", v.toString()) }
                                                else         -> Text(dur.toString(), style = MaterialTheme.typography.bodySmall)
                                            }
                                        }

                                        // Delays
                                        if (stage.commonDelays.isNotEmpty()) {
                                            SectionHeader("Common Delays")
                                            BulletList(stage.commonDelays, CrimsonAccent)
                                        }

                                        // Required actions
                                        if (stage.requiredActions.isNotEmpty()) {
                                            SectionHeader("Required Actions")
                                            stage.requiredActions.forEach { action ->
                                                val financial = isFinancial(action)
                                                Surface(
                                                    shape  = RoundedCornerShape(6.dp),
                                                    color  = if (financial) GoldPrimary.copy(alpha = 0.08f) else Color.Transparent,
                                                    border = if (financial) BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)) else null,
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                                ) {
                                                    Row(modifier = Modifier.padding(if (financial) 8.dp else 0.dp), verticalAlignment = Alignment.CenterVertically) {
                                                        if (financial) {
                                                            Text("💰", modifier = Modifier.padding(end = 6.dp))
                                                        } else {
                                                            Text("•", color = GoldPrimary, modifier = Modifier.padding(end = 8.dp))
                                                        }
                                                        Text(action, style = MaterialTheme.typography.bodySmall)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
            else -> {}
        }
    }
}

private operator fun PaddingValues.plus(other: PaddingValues): PaddingValues = PaddingValues(
    start  = calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + other.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
    top    = calculateTopPadding() + other.calculateTopPadding(),
    end    = calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + other.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
    bottom = calculateBottomPadding() + other.calculateBottomPadding()
)