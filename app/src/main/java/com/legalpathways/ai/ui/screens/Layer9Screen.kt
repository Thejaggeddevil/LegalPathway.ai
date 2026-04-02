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
import com.legalpathways.ai.model.CaseItem
import com.legalpathways.ai.ui.components.*
import com.legalpathways.ai.ui.theme.*
import com.legalpathways.ai.viewmodel.MainViewModel
import com.legalpathways.ai.viewmodel.UiState

fun precedentWeight(court: String) = when {
    court.contains("Supreme") -> 3
    court.contains("High")    -> 2
    else                      -> 1
}

@Composable
fun Layer9Screen(onBack: () -> Unit, vm: MainViewModel = viewModel()) {
    val state      by vm.layer9State.collectAsState()
    var sideFilter by remember { mutableStateOf("all") }
    var areaFilter by remember { mutableStateOf("all") }

    LaunchedEffect(Unit) { vm.loadLayer9() }

    Scaffold(topBar = { LegalTopBar("Phase 9 – Judicial Precedents", onBack) }) { padding ->
        when (val s = state) {
            is UiState.Loading -> LoadingContent()
            is UiState.Error   -> ErrorContent(s.message) { vm.loadLayer9() }
            is UiState.Success -> {
                val data  = s.data
                val all   = ((data.husbandJudgments ?: emptyList()).map { it to "husband" } +
                        (data.wifeJudgments ?: emptyList()).map { it to "wife" })
                val areas = listOf("all") + all.map { it.first.area }.distinct()

                val filtered = all.filter { (c, side) ->
                    (sideFilter == "all" || side == sideFilter) &&
                            (areaFilter == "all" || c.area == areaFilter)
                }

                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    // Info header
                    Surface(color = NavyMid.copy(alpha = 0.06f)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(data.phase, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = NavyMid)
                            Text(data.jurisdiction, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Side filter
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("all" to "All", "husband" to "Husband Advantage", "wife" to "Wife Advantage").forEach { (v, label) ->
                            FilterChip(
                                selected = sideFilter == v,
                                onClick  = { sideFilter = v },
                                label    = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyMid,
                                    selectedLabelColor     = Color.White
                                )
                            )
                        }
                    }

                    // Area filter
                    LazyRow(
                        contentPadding        = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(areas) { area ->
                            FilterChip(
                                selected = areaFilter == area,
                                onClick  = { areaFilter = area },
                                label    = { Text(if (area == "all") "All Areas" else area, style = MaterialTheme.typography.labelSmall) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary.copy(alpha = 0.15f),
                                    selectedLabelColor     = GoldDark
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    GoldDivider(Modifier.padding(horizontal = 16.dp))

                    LazyColumn(
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filtered) { (caseItem, side) ->
                            CaseCard(caseItem, side)
                        }

                        // Engine logic card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors   = CardDefaults.cardColors(containerColor = NavyMid.copy(alpha = 0.05f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    SectionHeader("Engine Strategy Logic", Icons.Default.Memory)
                                    Spacer(Modifier.height(6.dp))
                                    val rules = listOf(
                                        "Supreme Court precedents carry highest weight (3)",
                                        "High Court precedents carry moderate weight (2)",
                                        "Maintenance overlap automatically checked",
                                        "Adultery disqualification logic enabled",
                                        "Mental cruelty via false complaint logic enabled",
                                        "Shared household override detection active",
                                        "Clean-break settlement preferred where viable"
                                    )
                                    BulletList(rules)
                                }
                            }
                        }

                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun CaseCard(c: CaseItem, side: String) {
    var expanded by remember { mutableStateOf(false) }
    val weight   = precedentWeight(c.court)
    val sideColor = if (side == "husband") NavyMid else CrimsonAccent

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, sideColor.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(c.caseName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("(${c.year})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(c.area, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("⭐".repeat(weight), style = MaterialTheme.typography.bodySmall)
                    }
                }
                StatusBadge(if (side == "husband") "Husband" else "Wife", sideColor)
            }

            if (expanded) {
                GoldDivider(Modifier.padding(horizontal = 14.dp))
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(c.court, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)

                    c.ratioDecidendi?.let {
                        SectionHeader("Ratio Decidendi")
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                    c.courtDecision?.let {
                        SectionHeader("Court Decision")
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                    c.keyPrinciples?.takeIf { it.isNotEmpty() }?.let {
                        SectionHeader("Key Principles")
                        BulletList(it)
                    }
                    c.husbandGain?.takeIf { it.isNotEmpty() }?.let {
                        SectionHeader("Husband Gain")
                        BulletList(it, NavyMid)
                    }
                    c.wifeGain?.takeIf { it.isNotEmpty() }?.let {
                        SectionHeader("Wife Gain")
                        BulletList(it, CrimsonAccent)
                    }
                    c.enforceability?.takeIf { it.isNotEmpty() }?.let {
                        SectionHeader("Enforceability")
                        BulletList(it)
                    }
                }
            }
        }
    }
}