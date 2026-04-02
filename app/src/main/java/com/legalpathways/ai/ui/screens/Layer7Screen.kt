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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.legalpathways.ai.ui.components.*
import com.legalpathways.ai.ui.theme.*
import com.legalpathways.ai.viewmodel.MainViewModel
import com.legalpathways.ai.viewmodel.UiState

@Composable
fun Layer7Screen(onBack: () -> Unit, vm: MainViewModel = viewModel()) {
    val state by vm.layer7State.collectAsState()
    LaunchedEffect(Unit) { vm.loadLayer7() }

    Scaffold(topBar = { LegalTopBar("Phase 7 – Interim Maintenance & Custody", onBack) }) { padding ->
        when (val s = state) {
            is UiState.Loading -> LoadingContent()
            is UiState.Error   -> ErrorContent(s.message) { vm.loadLayer7() }
            is UiState.Success -> {
                val data = s.data
                LazyColumn(
                    contentPadding      = padding + PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            data.description,
                            style  = MaterialTheme.typography.bodyMedium,
                            color  = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(data.modules) { module ->
                        ExpandableCard(
                            title = module.issue,
                            leadingIcon = Icons.Default.HourglassTop,
                            badge = {
                                StatusBadge(module.discretionLevel,
                                    when (module.discretionLevel.lowercase()) {
                                        "high"   -> CrimsonAccent
                                        "medium" -> GoldPrimary
                                        else     -> EmeraldAccent
                                    })
                            }
                        ) {
                            if (module.legalProvision.isNotEmpty()) {
                                SectionHeader("Legal Provision", Icons.Default.Gavel)
                                BulletList(module.legalProvision)
                                Spacer(Modifier.height(8.dp))
                            }

                            SectionHeader("What Law Says", Icons.Default.MenuBook)
                            Text(module.whatLawSays, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))

                            SectionHeader("Practical Reality", Icons.Default.Lightbulb)
                            Text(module.practicalReality, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))

                            if (module.keyLegalLimits.isNotEmpty()) {
                                SectionHeader("Key Legal Limits", Icons.Default.Block)
                                BulletList(module.keyLegalLimits, CrimsonAccent)
                                Spacer(Modifier.height(8.dp))
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                InfoChip("⏳ ${module.timeLimitStatutary}", GoldPrimary)
                                if (module.enforcementRequired) InfoChip("Separate Proceeding", CrimsonAccent)
                            }
                        }
                    }

                    // Engine flags
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors   = CardDefaults.cardColors(containerColor = NavyMid.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SectionHeader("Engine Strategy Flags", Icons.Default.Memory)
                                Spacer(Modifier.height(8.dp))
                                data.engineFlags.forEach { (key, value) ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (value) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            null,
                                            tint     = if (value) EmeraldAccent else SlateGray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            key.replace("_", " ").replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.bodySmall
                                        )
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

@Composable
fun InfoChip(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        shape  = RoundedCornerShape(16.dp),
        color  = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text,
            style    = MaterialTheme.typography.labelSmall,
            color    = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

private operator fun PaddingValues.plus(other: PaddingValues): PaddingValues = PaddingValues(
    start  = calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + other.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
    top    = calculateTopPadding() + other.calculateTopPadding(),
    end    = calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + other.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
    bottom = calculateBottomPadding() + other.calculateBottomPadding()
)