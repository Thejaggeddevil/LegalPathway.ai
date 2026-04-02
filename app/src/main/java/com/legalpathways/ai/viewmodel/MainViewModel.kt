package com.legalpathways.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legalpathways.ai.model.*
import com.legalpathways.ai.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Idle    : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class MainViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    // ── Chat (Legal AI) ───────────────────────────────────────────────────────
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())

    // ── Counselor Chat ────────────────────────────────────────────────────────
    private val _counselorMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val counselorMessages: StateFlow<List<ChatMessage>> = _counselorMessages.asStateFlow()

    private val _counselorLoading = MutableStateFlow(false)
    val counselorLoading: StateFlow<Boolean> = _counselorLoading.asStateFlow()

    // ── Roadmap ───────────────────────────────────────────────────────────────
    private val _roadmapState = MutableStateFlow<UiState<RoadmapData>>(UiState.Idle)
    val roadmapState: StateFlow<UiState<RoadmapData>> = _roadmapState.asStateFlow()

    // ── Layer States ──────────────────────────────────────────────────────────
    private val _layer0State = MutableStateFlow<UiState<Layer0Data>>(UiState.Idle)
    val layer0State: StateFlow<UiState<Layer0Data>> = _layer0State.asStateFlow()

    private val _phase1State = MutableStateFlow<UiState<Phase1Data>>(UiState.Idle)
    val phase1State: StateFlow<UiState<Phase1Data>> = _phase1State.asStateFlow()

    private val _layer3State = MutableStateFlow<UiState<List<ScenarioItem>>>(UiState.Idle)
    val layer3State: StateFlow<UiState<List<ScenarioItem>>> = _layer3State.asStateFlow()

    private val _layer4State = MutableStateFlow<UiState<Layer4Data>>(UiState.Idle)
    val layer4State: StateFlow<UiState<Layer4Data>> = _layer4State.asStateFlow()

    private val _layer5State = MutableStateFlow<UiState<Layer5Data>>(UiState.Idle)
    val layer5State: StateFlow<UiState<Layer5Data>> = _layer5State.asStateFlow()

    private val _layer7State = MutableStateFlow<UiState<Layer7Data>>(UiState.Idle)
    val layer7State: StateFlow<UiState<Layer7Data>> = _layer7State.asStateFlow()

    private val _layer8State = MutableStateFlow<UiState<Layer8Data>>(UiState.Idle)
    val layer8State: StateFlow<UiState<Layer8Data>> = _layer8State.asStateFlow()

    private val _layer9State = MutableStateFlow<UiState<Layer9Data>>(UiState.Idle)
    val layer9State: StateFlow<UiState<Layer9Data>> = _layer9State.asStateFlow()

    private val _layer10State = MutableStateFlow<UiState<Layer10Data>>(UiState.Idle)
    val layer10State: StateFlow<UiState<Layer10Data>> = _layer10State.asStateFlow()

    var selectedMarriageType = MutableStateFlow("hindu")
    var selectedRole         = MutableStateFlow("husband")

    // ─────────────────────────────────────────────────────────────────────────
    // Chat (Legal AI) — FIXED: handles multiple response shapes from backend
    // ─────────────────────────────────────────────────────────────────────────
    fun sendLegalChat(question: String, religion: String) {
        val userMsg = ChatMessage(question, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg
        _chatLoading.value  = true

        viewModelScope.launch {
            try {
                val contextual = buildContext(question, _chatHistory.value)
                val resp = api.ask(AskRequest(contextual, mapReligion(religion)))

                val text: String = when {
                    // Shape 1: { success: true, data: { answer: "...", explanation: "..." } }
                    resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null -> {
                        val d = resp.body()!!.data!!
                        buildFullAnswer(d.answer, d.explanation)
                    }
                    // Shape 2: success=false with a message string
                    resp.isSuccessful && resp.body()?.message != null -> {
                        resp.body()!!.message!!
                    }
                    // Shape 3: HTTP error
                    !resp.isSuccessful -> {
                        "❌ Server returned error ${resp.code()}. Check backend logs."
                    }
                    else -> "❌ Could not get a response. Please try again."
                }

                val suggestions = generateSuggestions(text, religion, question)
                val botMsg = ChatMessage(text, isUser = false, suggestions = suggestions)
                _chatMessages.value = _chatMessages.value + botMsg
                _chatHistory.value  = (_chatHistory.value + userMsg + botMsg).takeLast(6)

            } catch (e: Exception) {
                val errMsg = when {
                    e.message?.contains("Unable to resolve host") == true ->
                        "❌ Cannot reach backend.\n\nCheck:\n• Backend running on port 8000?\n• Phone & PC on same Wi-Fi?\n• BASE_URL set to your PC IP (192.168.x.x)?"
                    e.message?.contains("timeout") == true ->
                        "❌ Request timed out. Backend may be slow."
                    else ->
                        "❌ Error: ${e.message}"
                }
                _chatMessages.value = _chatMessages.value + ChatMessage(errMsg, false)
            } finally {
                _chatLoading.value = false
            }
        }
    }

    // Combine answer + explanation cleanly, skip empty parts
    private fun buildFullAnswer(answer: String?, explanation: String?): String {
        val parts = mutableListOf<String>()
        answer?.trim()?.takeIf { it.isNotEmpty() }?.let { parts.add(it) }
        explanation?.trim()?.takeIf { it.isNotEmpty() && it != answer?.trim() }?.let { parts.add(it) }
        return parts.joinToString("\n\n").ifEmpty { "No answer found for this question." }
    }

    fun clearLegalChat() {
        _chatMessages.value = emptyList()
        _chatHistory.value  = emptyList()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Counselor Chat — FIXED: handles multiple response shapes
    // ─────────────────────────────────────────────────────────────────────────
    fun sendCounselorChat(question: String) {
        val userMsg = ChatMessage(question, isUser = true)
        _counselorMessages.value = _counselorMessages.value + userMsg
        _counselorLoading.value  = true

        viewModelScope.launch {
            try {
                val resp = api.counselorChat(CounselorRequest(question))

                val text: String = when {
                    resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null -> {
                        val d = resp.body()!!.data!!
                        buildCounselorText(d)
                    }
                    resp.isSuccessful && resp.body()?.message != null -> {
                        resp.body()!!.message!!
                    }
                    else -> "❌ Could not get a response. Please try again."
                }

                _counselorMessages.value = _counselorMessages.value + ChatMessage(text, false)

            } catch (e: Exception) {
                _counselorMessages.value = _counselorMessages.value + ChatMessage(
                    "❌ Error: ${e.message}", false
                )
            } finally {
                _counselorLoading.value = false
            }
        }
    }

    private fun buildCounselorText(d: CounselorData): String = buildString {
        if (d.introduction.isNotBlank()) {
            append(d.introduction.trim())
            append("\n\n")
        }
        if (d.understanding.isNotBlank()) {
            append(d.understanding.trim())
            append("\n\n")
        }
        if (d.keyPoints.isNotEmpty()) {
            append("Key Points:\n")
            d.keyPoints.forEach { append("• $it\n") }
            append("\n")
        }
        if (d.conclusion.isNotBlank()) {
            append(d.conclusion.trim())
            append("\n\n")
        }
        if (d.motivation.isNotBlank()) {
            append("💪 ")
            append(d.motivation.trim())
        }
    }.trim()

    // ─────────────────────────────────────────────────────────────────────────
    // Roadmap
    // ─────────────────────────────────────────────────────────────────────────
    fun loadRoadmap(marriage: String, role: String) {
        _roadmapState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = api.getRoadmap(marriage, role)
                if (resp.isSuccessful && resp.body()?.success == true) {
                    _roadmapState.value = UiState.Success(resp.body()!!.data!!)
                } else {
                    _roadmapState.value = UiState.Error("No roadmap available for this selection.")
                }
            } catch (e: Exception) {
                _roadmapState.value = UiState.Error("Failed to load roadmap: ${e.message}")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer 0
    // ─────────────────────────────────────────────────────────────────────────
    fun submitLayer0(request: Layer0Request) {
        _layer0State.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = api.getLayer0Position(request)
                if (resp.isSuccessful && resp.body()?.success == true) {
                    _layer0State.value = UiState.Success(resp.body()!!.data!!)
                } else {
                    _layer0State.value = UiState.Error(resp.body()?.message ?: "Error getting position")
                }
            } catch (e: Exception) {
                _layer0State.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetLayer0() { _layer0State.value = UiState.Idle }

    // ─────────────────────────────────────────────────────────────────────────
    // Phase 1
    // ─────────────────────────────────────────────────────────────────────────
    fun loadPhase1(religion: String) {
        _phase1State.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = api.getPhase1(religion)
                if (resp.isSuccessful && resp.body()?.success == true) {
                    _phase1State.value = UiState.Success(resp.body()!!.data!!)
                } else {
                    _phase1State.value = UiState.Error("Failed to load phase 1")
                }
            } catch (e: Exception) {
                _phase1State.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer 3
    // ─────────────────────────────────────────────────────────────────────────
    fun loadLayer3Events() {
        _layer3State.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = api.getLayer3Events()
                if (resp.isSuccessful && resp.body()?.success == true) {
                    _layer3State.value = UiState.Success(resp.body()!!.data!!)
                } else {
                    _layer3State.value = UiState.Error("Failed to load scenarios")
                }
            } catch (e: Exception) {
                _layer3State.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer 4
    // ─────────────────────────────────────────────────────────────────────────
    fun classifySeverity(level: String) {
        _layer4State.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = api.classifyLayer4(Layer4Request(level))
                if (resp.isSuccessful && resp.body()?.success == true) {
                    _layer4State.value = UiState.Success(resp.body()!!.data!!)
                } else {
                    _layer4State.value = UiState.Error(resp.body()?.message ?: "No matching route found")
                }
            } catch (e: Exception) {
                _layer4State.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer 5
    // ─────────────────────────────────────────────────────────────────────────
    fun getSettlement(request: Layer5Request) {
        _layer5State.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = api.getLayer5Settlement(request)
                if (resp.isSuccessful && resp.body()?.success == true) {
                    _layer5State.value = UiState.Success(resp.body()!!.data!!)
                } else {
                    _layer5State.value = UiState.Error(resp.body()?.message ?: "Error")
                }
            } catch (e: Exception) {
                _layer5State.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer 7
    // ─────────────────────────────────────────────────────────────────────────
    fun loadLayer7() {
        _layer7State.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = api.getLayer7()
                if (resp.isSuccessful && resp.body()?.success == true) {
                    _layer7State.value = UiState.Success(resp.body()!!.data!!)
                } else {
                    _layer7State.value = UiState.Error("Failed to load")
                }
            } catch (e: Exception) {
                _layer7State.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer 8
    // ─────────────────────────────────────────────────────────────────────────
    fun loadLayer8() {
        _layer8State.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = api.getLayer8()
                if (resp.isSuccessful && resp.body()?.success == true) {
                    _layer8State.value = UiState.Success(resp.body()!!.data!!)
                } else {
                    _layer8State.value = UiState.Error("Failed to load")
                }
            } catch (e: Exception) {
                _layer8State.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer 9
    // ─────────────────────────────────────────────────────────────────────────
    fun loadLayer9() {
        _layer9State.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = api.getLayer9()
                if (resp.isSuccessful && resp.body()?.success == true) {
                    _layer9State.value = UiState.Success(resp.body()!!.data!!)
                } else {
                    _layer9State.value = UiState.Error("Failed to load")
                }
            } catch (e: Exception) {
                _layer9State.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer 10
    // ─────────────────────────────────────────────────────────────────────────
    fun loadLayer10() {
        _layer10State.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = api.getLayer10()
                if (resp.isSuccessful && resp.body()?.success == true) {
                    _layer10State.value = UiState.Success(resp.body()!!.data!!)
                } else {
                    _layer10State.value = UiState.Error("Failed to load")
                }
            } catch (e: Exception) {
                _layer10State.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────
    private fun buildContext(question: String, history: List<ChatMessage>): String {
        if (history.isEmpty()) return question
        val recent = history.takeLast(4).joinToString(" | ") { it.text.take(120) }
        return "Context: $recent --- Question: $question"
    }

    private fun mapReligion(r: String) = when (r.lowercase()) {
        "sma" -> "special_marriage"
        else  -> r.lowercase()
    }

    private fun generateSuggestions(answer: String, religion: String, question: String): List<String> {
        val text = (answer + " " + question).lowercase()
        val pool = mapOf(
            "maintenance" to listOf("How much maintenance can I claim?", "For how long is maintenance paid?", "Can maintenance be modified?"),
            "custody"     to listOf("Who gets custody of children?", "What is visitation rights?", "How is custody decided?"),
            "property"    to listOf("How is property divided?", "Can I claim spouse's property?", "What about joint assets?"),
            "hindu"       to listOf("What is mutual consent divorce?", "What is the cooling period?", "Can Hindu marriage be annulled?"),
            "muslim"      to listOf("What is the iddat period?", "What is mahr/mehr?", "What is khula divorce?"),
            "christian"   to listOf("What does the Divorce Act say?", "Is cruelty a valid ground?", "What about judicial separation?"),
            "sma"         to listOf("What is the SMA notice period?", "Can family object?", "How is alimony calculated under SMA?")
        )
        val matched = mutableListOf<String>()
        if (text.contains("mainten")) matched.addAll(pool["maintenance"] ?: emptyList())
        if (text.contains("custod") || text.contains("child")) matched.addAll(pool["custody"] ?: emptyList())
        if (text.contains("propert") || text.contains("asset")) matched.addAll(pool["property"] ?: emptyList())
        if (matched.isEmpty()) matched.addAll(pool[religion.lowercase()] ?: pool["hindu"] ?: emptyList())
        return matched.distinct().take(3)
    }
}