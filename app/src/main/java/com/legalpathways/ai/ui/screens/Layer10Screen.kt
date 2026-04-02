package com.legalpathways.ai.ui.screens

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
import com.legalpathways.ai.model.Layer10Item
import com.legalpathways.ai.ui.components.*
import com.legalpathways.ai.ui.theme.*
import com.legalpathways.ai.viewmodel.MainViewModel
import com.legalpathways.ai.viewmodel.UiState

@Composable
fun Layer10Screen(onBack: () -> Unit, vm: MainViewModel = viewModel()) {
    val state by vm.layer10State.collectAsState()
    LaunchedEffect(Unit) { vm.loadLayer10() }

    Scaffold(topBar = { LegalTopBar("Phase 10 – Post-Divorce Transition", onBack) }) { padding ->
        when (val s = state) {
            is UiState.Loading -> LoadingContent()
            is UiState.Error   -> ErrorContent(s.message) { vm.loadLayer10() }
            is UiState.Success -> {
                val items = s.data.items
                LazyColumn(
                    contentPadding      = padding + PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Ensure legal, financial, identity, and compliance closure after divorce.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    itemsIndexed(items) { index, item ->
                        PostDivorceCard(index + 1, item)
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun PostDivorceCard(index: Int, item: Layer10Item) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Index badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldAccent.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("$index", style = MaterialTheme.typography.titleSmall, color = EmeraldAccent, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "➡ ${item.whatToUpdate}",
                        style    = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color    = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        item.authorityOffice,
                        style  = MaterialTheme.typography.bodySmall,
                        color  = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null,
                        tint = GoldPrimary
                    )
                }
            }

            if (expanded) {
                GoldDivider(Modifier.padding(horizontal = 14.dp))
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    if (item.documentsRequired.isNotEmpty()) {
                        SectionHeader("📎 Required Documents", Icons.Default.AttachFile)
                        BulletList(item.documentsRequired)
                    }

                    SectionHeader("How to Do It", Icons.Default.HowToReg)
                    Text(item.howToDoIt, style = MaterialTheme.typography.bodySmall)

                    // Risk warning
                    Surface(
                        shape  = RoundedCornerShape(8.dp),
                        color  = CrimsonAccent.copy(alpha = 0.06f)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Warning, null, tint = CrimsonAccent, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text("Risk if Not Done", style = MaterialTheme.typography.labelSmall, color = CrimsonAccent, fontWeight = FontWeight.Bold)
                                Text(item.riskIfNotDone, style = MaterialTheme.typography.bodySmall, color = CrimsonAccent)
                            }
                        }
                    }
                }
            }
        }
    }
}

private operator fun PaddingValues.plus(other: PaddingValues): PaddingValues = PaddingValues(
    start  = calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + other.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
    top    = calculateTopPadding() + other.calculateTopPadding(),
    end    = calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + other.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
    bottom = calculateBottomPadding() + other.calculateBottomPadding()
)