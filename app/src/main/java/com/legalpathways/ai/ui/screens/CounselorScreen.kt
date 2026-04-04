package com.legalpathways.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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

val counselorStarters = listOf(
    "I feel overwhelmed with my divorce",
    "How do I cope with separation anxiety?",
    "I'm stressed about child custody",
    "How to manage financially after divorce?"
)

@Composable
fun CounselorScreen(onBack: () -> Unit, vm: MainViewModel = viewModel()) {
    val messages by vm.counselorMessages.collectAsState()
    val loading  by vm.counselorLoading.collectAsState()
    var input    by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            LegalTopBar(
                title  = "Emotional Support Counselor",
                onBack = onBack
            )
        },
        bottomBar = {
            Column {
                GoldDivider()
                ChatInputBar(
                    value         = input,
                    onValueChange = { input = it },
                    onSend = {
                        if (input.isNotBlank()) {
                            vm.sendCounselorChat(input)
                            input = ""
                        }
                    },
                    placeholder   = "Ask about stress, healing, separation…",
                    enabled       = !loading
                )
            }
        }
    ) { padding ->
        LazyColumn(
            state          = listState,
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier       = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // Header card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(containerColor = EmeraldAccent.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = EmeraldAccent.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Favorite, null, tint = EmeraldAccent, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Your Safe Space", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                            Text(
                                "Talk about stress, healing, or anything on your mind.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (messages.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            StarterChips(questions = counselorStarters, onSelect = { vm.sendCounselorChat(it) })
                        }
                    }
                }
            } else {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                }
                if (loading) {
                    item { TypingIndicator() }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}