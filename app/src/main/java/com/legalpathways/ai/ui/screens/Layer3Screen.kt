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
import com.legalpathways.ai.model.ScenarioItem
import com.legalpathways.ai.ui.components.*
import com.legalpathways.ai.ui.theme.*
import com.legalpathways.ai.viewmodel.MainViewModel
import com.legalpathways.ai.viewmodel.UiState

fun typeColor(type: String): Pair<Color, Color> = when {
    type.lowercase().contains("financial") && type.lowercase().contains("legal") -> Pair(Color(0xFF6B21A8), Color(0xFFF3E8FF))
    type.lowercase().contains("both")       -> Pair(Color(0xFF92400E), Color(0xFFFFF3E0))
    type.lowercase().contains("financial")  -> Pair(Color(0xFF92400E), Color(0xFFFEF3C7))
    type.lowercase().contains("non-financial") -> Pair(Color(0xFF0C4A6E), Color(0xFFE0F2FE))
    else                                    -> Pair(EmeraldAccent, Color(0xFFF0FDF4))
}

@Composable
fun Layer3Screen(onBack: () -> Unit, vm: MainViewModel = viewModel()) {
    val state      by vm.layer3State.collectAsState()
    var typeFilter by remember { mutableStateOf("") }
    var search     by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadLayer3Events() }

    Scaffold(topBar = { LegalTopBar("Phase 3 – Maintenance & Custody", onBack) }) { padding ->
        when (val s = state) {
            is UiState.Loading -> LoadingContent()
            is UiState.Error   -> ErrorContent(s.message) { vm.loadLayer3Events() }
            is UiState.Success -> {
                val items   = s.data
                val types   = items.map { it.type.trim() }.distinct()
                val filtered = items.filter { item ->
                    (typeFilter.isEmpty() || item.type.trim() == typeFilter) &&
                            (search.isEmpty()    || item.scenario.contains(search, ignoreCase = true) ||
                                    item.courtRelevance.contains(search, ignoreCase = true))
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    // Search bar
                    OutlinedTextField(
                        value         = search,
                        onValueChange = { search = it },
                        placeholder   = { Text("Search scenarios…") },
                        leadingIcon   = { Icon(Icons.Default.Search, null, tint = GoldPrimary) },
                        modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape         = RoundedCornerShape(24.dp),
                        singleLine    = true,
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = GoldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Type filter chips
                    LazyRow(
                        contentPadding      = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = typeFilter.isEmpty(),
                                onClick  = { typeFilter = "" },
                                label    = { Text("All (${items.size})") },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyMid,
                                    selectedLabelColor     = Color.White
                                )
                            )
                        }
                        items(types) { type ->
                            val (col, _) = typeColor(type)
                            FilterChip(
                                selected = typeFilter == type,
                                onClick  = { typeFilter = if (typeFilter == type) "" else type },
                                label    = { Text("$type (${items.count { it.type.trim() == type }})") },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = col.copy(alpha = 0.15f),
                                    selectedLabelColor     = col
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "Showing ${filtered.size} scenario${if (filtered.size != 1) "s" else ""} — tap to expand",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    LazyColumn(
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered) { scenario ->
                            ScenarioCard(scenario)
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
fun ScenarioCard(item: ScenarioItem) {
    var expanded by remember { mutableStateOf(false) }
    val (col, bg) = typeColor(item.type)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, col.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = bg,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        item.type,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = col,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    item.scenario,
                    style    = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color    = MaterialTheme.colorScheme.onSurface,
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
                GoldDivider()

                // Court relevance
                Surface(color = bg, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Gavel, null, tint = col, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Court Relevance: ${item.courtRelevance}",
                            style = MaterialTheme.typography.bodySmall,
                            color = col
                        )
                    }
                }

                // Maintenance vs Custody grid
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                        Text("Maintenance", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE67E22), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(item.impactOnMaintenance, style = MaterialTheme.typography.bodySmall)
                    }
                    VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                        Text("Custody", style = MaterialTheme.typography.labelSmall, color = NavyMid, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(item.impactOnCustody, style = MaterialTheme.typography.bodySmall)
                    }
                }

                // How to claim
                if (!item.howToClaimMaintenance.isNullOrEmpty()) {
                    GoldDivider()
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("How to Claim Maintenance", style = MaterialTheme.typography.labelSmall, color = EmeraldAccent, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        item.howToClaimMaintenance.forEachIndexed { i, step ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("${i+1}.", style = MaterialTheme.typography.bodySmall, color = EmeraldAccent, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                                Text(step, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}