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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.legalpathways.ai.network.RetrofitClient
import com.legalpathways.ai.ui.components.*
import com.legalpathways.ai.ui.theme.*
import com.legalpathways.ai.viewmodel.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class Layer2ViewModel : ViewModel() {
    private val api = RetrofitClient.apiService

    private val _acts = MutableStateFlow<UiState<List<Map<String, String>>>>(UiState.Idle)
    val acts: StateFlow<UiState<List<Map<String, String>>>> = _acts

    private val _detail = MutableStateFlow<UiState<Map<String, Any>>>(UiState.Idle)
    val detail: StateFlow<UiState<Map<String, Any>>> = _detail

    fun loadActs() {
        _acts.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = api.getLayer2Checklist()
                if (resp.isSuccessful && resp.body()?.success == true) {
                    _acts.value = UiState.Success(resp.body()!!.data!!)
                } else {
                    _acts.value = UiState.Error("Failed to load acts")
                }
            } catch (e: Exception) {
                _acts.value = UiState.Error(e.message ?: "Error")
            }
        }
    }

    fun loadDetail(act: String) {
        _detail.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = api.getLayer2ChecklistDetail(act)
                if (resp.isSuccessful && resp.body()?.success == true) {
                    _detail.value = UiState.Success(resp.body()!!.data!!)
                } else {
                    _detail.value = UiState.Error("Failed to load details")
                }
            } catch (e: Exception) {
                _detail.value = UiState.Error(e.message ?: "Error")
            }
        }
    }
}

@Composable
fun Layer2Screen(onBack: () -> Unit, vm: Layer2ViewModel = viewModel()) {
    val actsState   by vm.acts.collectAsState()
    val detailState by vm.detail.collectAsState()
    var selectedAct by remember { mutableStateOf("") }
    var activeTab   by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { vm.loadActs() }

    Scaffold(topBar = { LegalTopBar("Phase 2 – Compliance Checklist", onBack) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Act pills
            when (val s = actsState) {
                is UiState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = GoldPrimary)
                is UiState.Success -> {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(s.data) { item ->
                            val route = item["marriage_route"] ?: return@items
                            FilterChip(
                                selected = selectedAct == route,
                                onClick  = { selectedAct = route; vm.loadDetail(route); activeTab = 0 },
                                label    = { Text(route, style = MaterialTheme.typography.labelSmall) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyMid,
                                    selectedLabelColor     = Color.White
                                )
                            )
                        }
                    }
                }
                is UiState.Error -> Text(s.message, color = CrimsonAccent)
                else -> {}
            }

            Spacer(Modifier.height(12.dp))

            when (val s = detailState) {
                is UiState.Loading -> LoadingContent()
                is UiState.Error   -> ErrorContent(s.message)
                is UiState.Success -> {
                    val d = s.data
                    val riskLevel = d["risk_level_if_weak_documentation"]?.toString() ?: ""
                    val riskColor = when { riskLevel.lowercase().contains("high") -> CrimsonAccent; riskLevel.lowercase().contains("medium") -> GoldPrimary; else -> EmeraldAccent }

                    // Header
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors   = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.06f)),
                        border   = BorderStroke(1.dp, riskColor.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(d["marriage_route"]?.toString() ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(d["governing_law_for_divorce"]?.toString() ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusBadge("Risk: $riskLevel", riskColor)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    val tabs = listOf("Details", "Documents", "Risks")
                    TabRow(selectedTabIndex = activeTab, contentColor = GoldPrimary, containerColor = MaterialTheme.colorScheme.surface) {
                        tabs.forEachIndexed { i, title ->
                            Tab(selected = activeTab == i, onClick = { activeTab = i },
                                text = { Text(title, style = MaterialTheme.typography.labelMedium) })
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        when (activeTab) {
                            0 -> {
                                // Court examination focus
                                val courtFocus = d["court_examination_focus"]
                                if (courtFocus is List<*>) {
                                    SectionHeader("Court Examination Focus", Icons.Default.Gavel)
                                    BulletList(courtFocus.filterIsInstance<String>())
                                }
                                val complianceActions = d["compliance_engine_actions"]
                                if (complianceActions is List<*>) {
                                    Spacer(Modifier.height(12.dp))
                                    SectionHeader("Compliance Actions", Icons.Default.Settings)
                                    BulletList(complianceActions.filterIsInstance<String>(), EmeraldAccent)
                                }
                            }
                            1 -> {
                                val docs = d["required_core_documents"]
                                if (docs is List<*>) {
                                    SectionHeader("Required Documents", Icons.Default.AttachFile)
                                    docs.filterIsInstance<String>().forEach { doc ->
                                        Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, null, tint = EmeraldAccent, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(doc, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                            2 -> {
                                val risks = d["common_litigation_risks"]
                                if (risks is List<*>) {
                                    SectionHeader("Common Litigation Risks", Icons.Default.Warning)
                                    risks.filterIsInstance<String>().forEach { risk ->
                                        Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Cancel, null, tint = CrimsonAccent, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(risk, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    if (selectedAct.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select a marriage act above to view compliance details.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}