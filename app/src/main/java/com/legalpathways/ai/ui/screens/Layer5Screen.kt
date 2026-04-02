package com.legalpathways.ai.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.legalpathways.ai.model.Layer5Request
import com.legalpathways.ai.ui.components.*
import com.legalpathways.ai.ui.theme.*
import com.legalpathways.ai.viewmodel.MainViewModel
import com.legalpathways.ai.viewmodel.UiState

@Composable
fun Layer5Screen(onBack: () -> Unit, vm: MainViewModel = viewModel()) {
    val state          by vm.layer5State.collectAsState()
    var caseType       by remember { mutableStateOf("") }
    var childrenFlag   by remember { mutableStateOf(false) }
    var incomeRatio    by remember { mutableStateOf("") }
    var settlementType by remember { mutableStateOf("") }

    val caseTypes      = listOf("maintenance","custody","divorce","domestic_violence","child_support")
    val incomeRatios   = listOf("husband_higher" to "Husband Higher","wife_higher" to "Wife Higher","equal_income" to "Equal Income")
    val settlementTypes = listOf(
        "mutual_settlement"       to "Mutual Settlement",
        "mediated_settlement"     to "Mediated Settlement",
        "one_time_settlement"     to "One-Time Settlement",
        "child_focused_settlement" to "Child-Focused Settlement",
        "global_settlement"       to "Global Settlement"
    )

    Scaffold(topBar = { LegalTopBar("Phase 5 – Pre-Litigation Resolution", onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Indicative settlement guidance before litigation.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Case Type
            LabelledDropdown("Case Type", caseTypes.map { it to it.replace("_", " ").replaceFirstChar { c -> c.uppercase() } }, caseType) { caseType = it }

            // Children
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Children Involved", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(false to "No", true to "Yes").forEach { (v, label) ->
                            FilterChip(
                                selected = childrenFlag == v,
                                onClick  = { childrenFlag = v },
                                label    = { Text(label) },
                                modifier = Modifier.weight(1f),
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyMid,
                                    selectedLabelColor     = androidx.compose.ui.graphics.Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Income Ratio
            LabelledDropdown("Income Ratio", incomeRatios, incomeRatio) { incomeRatio = it }

            // Settlement Type
            LabelledDropdown("Settlement Type", settlementTypes, settlementType) { settlementType = it }

            val ready = caseType.isNotEmpty() && incomeRatio.isNotEmpty() && settlementType.isNotEmpty()
            GoldButton(
                "🔍 Generate Settlement Guidance",
                onClick  = { vm.getSettlement(Layer5Request(caseType, childrenFlag, incomeRatio, settlementType)) },
                enabled  = ready && state !is UiState.Loading
            )

            when (val s = state) {
                is UiState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = GoldPrimary)
                is UiState.Error   -> Surface(color = CrimsonAccent.copy(alpha = 0.08f), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, CrimsonAccent.copy(alpha = 0.3f))) {
                    Text(s.message, color = CrimsonAccent, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                }
                is UiState.Success -> {
                    val d = s.data

                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = EmeraldAccent.copy(alpha = 0.06f)), border = BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.3f))) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            d.settlementId?.let { Text("Settlement ID: $it", style = MaterialTheme.typography.labelMedium, color = EmeraldAccent, fontWeight = FontWeight.Bold) }
                            d.rationale?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                    }

                    if (!d.legalBasis.isNullOrEmpty()) {
                        ExpandableCard("Legal Basis", Icons.Default.MenuBook) { BulletList(d.legalBasis) }
                    }

                    if (!d.suggestedTerms.isNullOrEmpty()) {
                        ExpandableCard("Suggested Terms", Icons.Default.Article) {
                            d.suggestedTerms.forEach { (k, v) ->
                                InfoRow("${k.replace("_"," ").replaceFirstChar{it.uppercase()}}:", v.toString())
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }

                    if (!d.mediationNotes.isNullOrEmpty()) {
                        ExpandableCard(" Mediation Notes", Icons.Default.Chat) { BulletList(d.mediationNotes) }
                    }

                    d.disclaimer?.let {
                        Surface(color = GoldPrimary.copy(alpha = 0.06f), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(it, style = MaterialTheme.typography.bodySmall, color = GoldDark)
                            }
                        }
                    }
                }
                else -> {}
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun LabelledDropdown(label: String, options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            DropdownSelector(options = options, selected = selected, onSelect = onSelect)
        }
    }
}