package com.legalpathways.ai.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.legalpathways.ai.model.EvidenceItem
import com.legalpathways.ai.ui.components.*
import com.legalpathways.ai.ui.theme.*
import com.legalpathways.ai.viewmodel.MainViewModel
import com.legalpathways.ai.viewmodel.UiState

@Composable
fun Layer1Screen(onBack: () -> Unit, vm: MainViewModel = viewModel()) {
    val state      by vm.phase1State.collectAsState()
    var religion   by remember { mutableStateOf("") }
    var role       by remember { mutableStateOf("") }
    var activeTab  by remember { mutableStateOf(0) }

    Scaffold(topBar = { LegalTopBar("Phase 1 – Readiness & Evidence", onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            // Selectors
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Religion", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    DropdownSelector(
                        options  = listOf("Hindu" to "Hindu", "Muslim" to "Muslim", "Christian" to "Christian", "Special Marriage" to "Special Marriage"),
                        selected = religion,
                        onSelect = { religion = it }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Your Role", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    DropdownSelector(
                        options  = listOf("husband" to "Husband", "wife" to "Wife"),
                        selected = role,
                        onSelect = { role = it }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            GoldButton(
                "🚀 Generate Strategy",
                onClick  = { if (religion.isNotEmpty() && role.isNotEmpty()) vm.loadPhase1(religion) },
                enabled  = religion.isNotEmpty() && role.isNotEmpty()
            )
            Spacer(Modifier.height(16.dp))

            when (val s = state) {
                is UiState.Loading -> LoadingContent()
                is UiState.Error   -> ErrorContent(s.message)
                is UiState.Success -> {
                    val data = s.data
                    // Tabs
                    val tabs = listOf("Eligibility", "High Evidence", "Medium Evidence", "Low Evidence")
                    ScrollableTabRow(
                        selectedTabIndex = activeTab,
                        containerColor   = MaterialTheme.colorScheme.surface,
                        contentColor     = GoldPrimary,
                        edgePadding      = 0.dp
                    ) {
                        tabs.forEachIndexed { i, title ->
                            Tab(selected = activeTab == i, onClick = { activeTab = i },
                                text = { Text(title, style = MaterialTheme.typography.labelMedium) })
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        when (activeTab) {
                            0 -> data.eligibility?.let { elig ->
                                EligibilitySection(elig)
                            }
                            1 -> EvidenceSection(data.evidenceCategories?.highRelevance ?: emptyList(), role, "High")
                            2 -> EvidenceSection(data.evidenceCategories?.mediumRelevance ?: emptyList(), role, "Medium")
                            3 -> EvidenceSection(data.evidenceCategories?.lowRelevance ?: emptyList(), role, "Low")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun EligibilitySection(elig: com.legalpathways.ai.model.EligibilityData) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("${elig.religion} – Eligibility", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        elig.applicableActs?.let { InfoRow("Acts:", it.joinToString(", ")) }
        elig.minAge?.let { InfoRow("Min Age:", "Male ${it.male} / Female ${it.female}") }
        elig.consentDetails?.let { InfoRow("Consent:", it) }
        elig.witnessRequirement?.details?.let { InfoRow("Witness:", it) }
        elig.residencyRequirement?.details?.let { InfoRow("Residency:", it) }
    }
}

@Composable
fun EvidenceSection(items: List<EvidenceItem>, role: String, label: String) {
    val weightColor = when (label) {
        "High"   -> Pair(GoldPrimary, GoldPrimary.copy(alpha = 0.08f))
        "Medium" -> Pair(NavyMid, NavyMid.copy(alpha = 0.08f))
        else     -> Pair(SlateGray, SlateGray.copy(alpha = 0.08f))
    }
    if (items.isEmpty()) {
        Text("No $label evidence items.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            ExpandableCard(
                title = item.type,
                badge = { StatusBadge(item.legalWeight, weightColor.first) }
            ) {
                item.description?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                val strategy = if (role == "husband") item.husbandStrategy else item.wifeStrategy
                if (!strategy.isNullOrEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    SectionHeader("Strategy")
                    BulletList(strategy)
                }
                if (!item.requirements.isNullOrEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    SectionHeader("Requirements")
                    BulletList(item.requirements)
                }
            }
        }
    }
}