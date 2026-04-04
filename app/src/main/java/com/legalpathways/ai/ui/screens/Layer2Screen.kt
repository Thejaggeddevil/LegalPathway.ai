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
import com.legalpathways.ai.ui.components.DropdownSelector

class Layer2ViewModel : ViewModel() {
    private val api = RetrofitClient.apiService

    // Changed: acts now returns List<String> (just the act names/routes)
    private val _acts = MutableStateFlow<UiState<List<String>>>(UiState.Idle)
    val acts: StateFlow<UiState<List<String>>> = _acts

    private val _detail = MutableStateFlow<UiState<Map<String, Any>>>(UiState.Idle)
    val detail: StateFlow<UiState<Map<String, Any>>> = _detail

    fun loadActs() {
        _acts.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = api.getLayer2Checklist()
                if (resp.isSuccessful && resp.body()?.success == true) {
                    val rawData = resp.body()!!.data ?: emptyList()

                    // Extract marriage_route from each map
                    val routes = rawData.mapNotNull { item ->
                        (item["marriage_route"] as? String)?.takeIf { it.isNotEmpty() }
                    }

                    _acts.value = if (routes.isNotEmpty()) {
                        UiState.Success(routes)
                    } else {
                        UiState.Error("No marriage acts found in response")
                    }
                } else {
                    _acts.value = UiState.Error(resp.body()?.message ?: "Failed to load acts")
                }
            } catch (e: Exception) {
                _acts.value = UiState.Error("Parse error: ${e.message ?: "Unknown error"}")
            }
        }
    }

    fun loadDetail(act: String) {
        _detail.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = api.getLayer2ChecklistDetail(act)
                if (resp.isSuccessful && resp.body()?.success == true) {
                    val data = resp.body()!!.data
                    if (data != null && data.isNotEmpty()) {
                        _detail.value = UiState.Success(data)
                    } else {
                        _detail.value = UiState.Error("No details available for this act")
                    }
                } else {
                    _detail.value = UiState.Error(resp.body()?.message ?: "Failed to load details")
                }
            } catch (e: Exception) {
                _detail.value = UiState.Error("Parse error: ${e.message ?: "Unknown error"}")
            }
        }
    }

    // Safe getter for nested data
    private fun getStringValue(map: Map<String, Any>?, key: String): String {
        return when (val value = map?.get(key)) {
            is String -> value
            else -> ""
        }
    }

    private fun getListValue(map: Map<String, Any>?, key: String): List<String> {
        return when (val value = map?.get(key)) {
            is List<*> -> value.filterIsInstance<String>()
            else -> emptyList()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Act selection pills
            when (val s = actsState) {
                is UiState.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = GoldPrimary)
                }
                is UiState.Success -> {
                    if (s.data.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    "No marriage acts available.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(s.data) { actName ->
                                FilterChip(
                                    selected = selectedAct == actName,
                                    onClick  = {
                                        selectedAct = actName
                                        vm.loadDetail(actName)
                                        activeTab = 0
                                    },
                                    label    = {
                                        Text(
                                            actName,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 2
                                        )
                                    },
                                    colors   = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NavyMid,
                                        selectedLabelColor     = Color.White,
                                        labelColor             = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, CrimsonAccent.copy(alpha = 0.5f))
                    ) {
                        Text(
                            "⚠️ ${s.message}",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CrimsonAccent
                        )
                    }
                }
                else -> {}
            }

            Spacer(Modifier.height(12.dp))

            // Detail content
            when (val s = detailState) {
                is UiState.Loading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = GoldPrimary)
                            Spacer(Modifier.height(12.dp))
                            Text("Loading details...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                is UiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, CrimsonAccent.copy(alpha = 0.5f))
                    ) {
                        Text(
                            "⚠️ ${s.message}",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CrimsonAccent
                        )
                    }
                }

                is UiState.Success -> {
                    val d = s.data
                    val riskLevel = safeGetString(d, "risk_level_if_weak_documentation")
                    val riskColor = when {
                        riskLevel.lowercase().contains("high") -> CrimsonAccent
                        riskLevel.lowercase().contains("medium") -> GoldPrimary
                        else -> EmeraldAccent
                    }

                    // Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors   = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.08f)),
                        border   = BorderStroke(1.dp, riskColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    safeGetString(d, "marriage_route"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    safeGetString(d, "governing_law_for_divorce"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge("Risk: $riskLevel", riskColor)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Tab Row
                    val tabs = listOf("Details", "Documents", "Risks")
                    TabRow(
                        selectedTabIndex = activeTab,
                        contentColor = GoldPrimary,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        tabs.forEachIndexed { i, title ->
                            Tab(
                                selected = activeTab == i,
                                onClick = { activeTab = i },
                                text = {
                                    Text(
                                        title,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Tab Content
                    when (activeTab) {
                        0 -> {
                            // Court examination focus
                            val courtFocus = safeGetList(d, "court_examination_focus")
                            if (courtFocus.isNotEmpty()) {
                                SectionHeader("Court Examination Focus", Icons.Default.Gavel)
                                BulletList(courtFocus)
                                Spacer(Modifier.height(12.dp))
                            }

                            val complianceActions = safeGetList(d, "compliance_engine_actions")
                            if (complianceActions.isNotEmpty()) {
                                SectionHeader("Compliance Actions", Icons.Default.Settings)
                                BulletList(complianceActions, EmeraldAccent)
                            }
                        }

                        1 -> {
                            val docs = safeGetList(d, "required_core_documents")
                            if (docs.isNotEmpty()) {
                                SectionHeader("Required Documents", Icons.Default.AttachFile)
                                docs.forEach { doc ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            null,
                                            tint = EmeraldAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(doc, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            } else {
                                Text(
                                    "No documents listed.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        2 -> {
                            val risks = safeGetList(d, "common_litigation_risks")
                            if (risks.isNotEmpty()) {
                                SectionHeader("Common Litigation Risks", Icons.Default.Warning)
                                risks.forEach { risk ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Cancel,
                                            null,
                                            tint = CrimsonAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(risk, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            } else {
                                Text(
                                    "No risks listed.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                else -> {
                    if (selectedAct.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    "👆 Select a marriage act above to view compliance details.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Safe data extraction helpers ──────────────────────────────────────────────
private fun safeGetString(map: Map<String, Any>, key: String): String {
    return try {
        when (val value = map[key]) {
            is String -> value
            else -> ""
        }
    } catch (e: Exception) {
        ""
    }
}

private fun safeGetList(map: Map<String, Any>, key: String): List<String> {
    return try {
        when (val value = map[key]) {
            is List<*> -> value.filterIsInstance<String>().filter { it.isNotEmpty() }
            is String -> listOf(value)
            else -> emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }
}