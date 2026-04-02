package com.legalpathways.ai.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

class GenericLayerViewModel : ViewModel() {
    private val api = RetrofitClient.apiService

    private val _layer6 = MutableStateFlow<UiState<Map<String, Any>>>(UiState.Idle)
    val layer6: StateFlow<UiState<Map<String, Any>>> = _layer6

    fun loadLayer6() {
        _layer6.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = api.getLayer6()
                if (resp.isSuccessful && resp.body()?.success == true) {
                    _layer6.value = UiState.Success(resp.body()!!.data!!)
                } else {
                    _layer6.value = UiState.Error("Failed to load litigation data")
                }
            } catch (e: Exception) {
                _layer6.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

@Composable
fun Layer6Screen(onBack: () -> Unit, vm: GenericLayerViewModel = viewModel()) {
    val state by vm.layer6.collectAsState()
    LaunchedEffect(Unit) { vm.loadLayer6() }

    Scaffold(topBar = { LegalTopBar("Phase 6 – Litigation Entry", onBack) }) { scaffoldPadding ->
        when (val s = state) {
            is UiState.Loading -> LoadingContent()
            is UiState.Error   -> ErrorContent(s.message) { vm.loadLayer6() }
            is UiState.Success -> {
                val data = s.data
                LazyColumn(
                    contentPadding = PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        top    = scaffoldPadding.calculateTopPadding() + 16.dp,
                        bottom = scaffoldPadding.calculateBottomPadding() + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Structured litigation doctrine before filing in Family Court.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val sections = listOf(
                        "jurisdiction_framework" to ("Jurisdiction Framework" to NavyMid),
                        "wife_side"              to ("Wife Side – Grounds & Reliefs" to CrimsonAccent),
                        "husband_side"           to ("Husband Side – Defence & Strategy" to NavyLight),
                        "mutual_consent_entry"   to ("Mutual Consent Entry" to EmeraldAccent)
                    )

                    items(sections) { (key, meta) ->
                        val (title, color) = meta
                        val sectionData = data[key]
                        if (sectionData != null) {
                            ExpandableCard(
                                title = title,
                                badge = {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(color, CircleShape)
                                    )
                                }
                            ) {
                                GenericJsonRenderer(sectionData)
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
fun GenericJsonRenderer(data: Any?, depth: Int = 0) {
    val indentInt = depth * 8
    val indentDp = indentInt.dp

    when (data) {
        is Map<*, *> -> {
            data.forEach { (k, v) ->
                val keyLabel = k.toString()
                    .replace("_", " ")
                    .replaceFirstChar { it.uppercase() }

                when {
                    v is List<*> && v.all { it is String } -> {
                        Text(
                            text       = keyLabel,
                            style      = MaterialTheme.typography.labelSmall,
                            color      = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        BulletList(v.filterIsInstance<String>())
                    }
                    v is String -> {
                        InfoRow("$keyLabel:", v)
                    }
                    v is Map<*, *> || v is List<*> -> {
                        Text(
                            text       = keyLabel,
                            style      = MaterialTheme.typography.labelSmall,
                            color      = NavyMid,
                            fontWeight = FontWeight.SemiBold,
                            modifier   = Modifier.padding(top = 8.dp, start = indentDp)
                        )
                        GenericJsonRenderer(v, depth + 1)
                    }
                }
            }
        }

        is List<*> -> {
            data.forEach { item ->
                if (item is String) {
                    Row(
                        modifier = Modifier
                            .padding(top = 2.dp, bottom = 2.dp)
                            .padding(start = indentDp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text     = "•",
                            color    = GoldPrimary,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text  = item,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    GenericJsonRenderer(item, depth + 1)
                }
            }
        }

        else -> {
            data?.let {
                Text(
                    text     = it.toString(),
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = indentDp)
                )
            }
        }
    }
}