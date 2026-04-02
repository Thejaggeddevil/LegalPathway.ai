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
import com.legalpathways.ai.ui.components.*
import com.legalpathways.ai.ui.theme.*
import com.legalpathways.ai.viewmodel.MainViewModel
import com.legalpathways.ai.viewmodel.UiState

val severityOptions = listOf(
    "Emotional Distress"           to ("🧠" to SlateGray),
    "Financial Neglect"            to ("💸" to GoldDark),
    "Abuse Risk (Physical / Sexual)" to ("🚨" to Color(0xFFE67E22)),
    "Child Safety Risk"            to ("👶" to CrimsonAccent),
    "Severe Threat / Life Danger"  to ("⚠️" to CrimsonAccent)
)

fun urgencyColor(level: String?) = when (level?.lowercase()) {
    "critical" -> CrimsonAccent
    "high"     -> Color(0xFFE67E22)
    "moderate" -> GoldPrimary
    else       -> EmeraldAccent
}

@Composable
fun Layer4Screen(onBack: () -> Unit, vm: MainViewModel = viewModel()) {
    val state   by vm.layer4State.collectAsState()
    var selected by remember { mutableStateOf("") }

    Scaffold(topBar = { LegalTopBar("Phase 4 – Severity Direction", onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Select the severity level that best matches your situation:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            severityOptions.forEach { (label, meta) ->
                val (emoji, color) = meta
                val isSelected = selected == label
                Card(
                    onClick   = { selected = label },
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = if (isSelected) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                    ),
                    border    = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) color else MaterialTheme.colorScheme.outline
                    ),
                    elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(emoji, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            label,
                            style    = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) Icon(Icons.Default.RadioButtonChecked, null, tint = color, modifier = Modifier.size(20.dp))
                        else Icon(Icons.Default.RadioButtonUnchecked, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }
            }

            GoldButton(
                "🔍 Generate Legal Direction",
                onClick  = { if (selected.isNotEmpty()) vm.classifySeverity(selected) },
                enabled  = selected.isNotEmpty() && state !is UiState.Loading
            )

            when (val s = state) {
                is UiState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = GoldPrimary)
                is UiState.Error   -> Surface(
                    color  = CrimsonAccent.copy(alpha = 0.08f),
                    shape  = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CrimsonAccent.copy(alpha = 0.3f))
                ) { Text(s.message, color = CrimsonAccent, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall) }

                is UiState.Success -> {
                    val d = s.data
                    val uc = urgencyColor(d.urgencyLevel)

                    // Header card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors   = CardDefaults.cardColors(containerColor = uc.copy(alpha = 0.06f)),
                        border   = BorderStroke(1.dp, uc.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Legal Direction", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.weight(1f))
                                StatusBadge(d.urgencyLevel, uc)
                            }
                            Text(d.systemLegalDirection, style = MaterialTheme.typography.bodyMedium)
                            if (d.policeIntervention != null && d.policeIntervention != false) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalPolice, null, tint = CrimsonAccent, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Police Intervention: ${d.policeIntervention}", style = MaterialTheme.typography.bodySmall, color = CrimsonAccent, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            d.overrideFlag?.let {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = GoldDark, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Immediate actions
                    if (!d.immediateActions.isNullOrEmpty()) {
                        ExpandableCard(title = "Immediate Actions", leadingIcon = Icons.Default.FlashOn) {
                            BulletList(d.immediateActions, CrimsonAccent)
                        }
                    }

                    // Digital vault
                    if (!d.digitalVaultEvidence.isNullOrEmpty()) {
                        ExpandableCard(title = "Digital Vault – Evidence", leadingIcon = Icons.Default.FolderOpen) {
                            BulletList(d.digitalVaultEvidence)
                        }
                    }

                    // Legal basis
                    if (!d.primaryLegalBasis.isNullOrEmpty()) {
                        ExpandableCard(title = "Primary Legal Basis", leadingIcon = Icons.Default.MenuBook) {
                            BulletList(d.primaryLegalBasis)
                        }
                    }

                    // Rationale
                    d.routeRationale?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors   = CardDefaults.cardColors(containerColor = NavyMid.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                SectionHeader("Why This Route", Icons.Default.Info)
                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
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