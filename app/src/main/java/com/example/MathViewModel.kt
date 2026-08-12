package com.example

import android.app.Application
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GeminiMathService
import com.example.data.MathQuestionEntity
import com.example.model.MathSolution
import com.example.model.SolutionStep
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

import androidx.compose.runtime.mutableStateListOf
import com.example.model.ChatMessage
import com.example.model.ChatSender

class MathViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val dao = database.mathQuestionDao()
    private val geminiService = GeminiMathService()

    var isLoading = androidx.compose.runtime.mutableStateOf(false)
        private set

    var errorMessage = androidx.compose.runtime.mutableStateOf<String?>(null)
        private set

    var currentSolution = androidx.compose.runtime.mutableStateOf<MathSolution?>(null)
        private set

    // AI Assistant State
    val chatMessages = mutableStateListOf<ChatMessage>()
    var isAssistantThinking = androidx.compose.runtime.mutableStateOf(false)
        private set

    val historyState: StateFlow<List<MathQuestionEntity>> = dao.getAllQuestions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    init {
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isTtsInitialized = true
            }
        }

        // Add welcome message from MathLens AI Tutor
        chatMessages.add(
            ChatMessage(
                sender = ChatSender.ASSISTANT,
                text = "Hello! 👋 I'm MathLens AI Tutor. Ask me any math question, ask for step-by-step guidance on homework, or ask me to explain formulas and theorems!"
            )
        )
    }

    fun sendAssistantMessage(userPrompt: String) {
        if (userPrompt.isBlank()) return
        val userMsg = ChatMessage(sender = ChatSender.USER, text = userPrompt)
        chatMessages.add(userMsg)

        viewModelScope.launch {
            isAssistantThinking.value = true
            val replyText = geminiService.chatWithAssistant(
                userPrompt = userPrompt,
                recentHistory = chatMessages.toList()
            )
            isAssistantThinking.value = false
            chatMessages.add(
                ChatMessage(sender = ChatSender.ASSISTANT, text = replyText)
            )
        }
    }

    fun clearAssistantChat() {
        chatMessages.clear()
        chatMessages.add(
            ChatMessage(
                sender = ChatSender.ASSISTANT,
                text = "Chat cleared! How can I help you with your math studies today?"
            )
        )
    }

    fun solveTextQuestion(text: String, imageBitmap: Bitmap? = null) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            val result = geminiService.solveMathQuestion(
                promptText = text,
                imageBitmap = imageBitmap,
                followUpContext = currentSolution.value?.let { sol ->
                    "Question: ${sol.questionText}\nCategory: ${sol.category}\nFinal Answer: ${sol.finalAnswer}"
                }
            )

            isLoading.value = false

            result.onSuccess { solution ->
                currentSolution.value = solution
                // Save to Room DB automatically
                saveSolutionToDb(solution, hasImage = imageBitmap != null)
            }.onFailure { error ->
                errorMessage.value = error.message ?: "Failed to solve question."
            }
        }
    }

    fun solveCanvasDrawing(bitmap: Bitmap) {
        solveTextQuestion("Solve the handwritten math problem or geometric drawing in this image", bitmap)
    }

    private fun saveSolutionToDb(solution: MathSolution, hasImage: Boolean) {
        viewModelScope.launch {
            try {
                val stepsJson = JSONArray().apply {
                    solution.steps.forEach { step ->
                        put(JSONObject().apply {
                            put("stepNumber", step.stepNumber)
                            put("title", step.title)
                            put("explanation", step.explanation)
                            put("mathExpression", step.mathExpression)
                            put("keyFormula", step.keyFormula)
                        })
                    }
                }.toString()

                val entity = MathQuestionEntity(
                    questionText = solution.questionText,
                    hasImage = hasImage,
                    category = solution.category,
                    summary = solution.summary,
                    stepsJson = stepsJson,
                    finalAnswer = solution.finalAnswer,
                    keyConceptsJson = JSONArray(solution.keyConcepts).toString(),
                    isBookmarked = false
                )

                val generatedId = dao.insertQuestion(entity)
                currentSolution.value = solution.copy(id = generatedId.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleBookmark(id: Int, isBookmarked: Boolean) {
        viewModelScope.launch {
            dao.updateBookmark(id, isBookmarked)
            if (currentSolution.value?.id == id) {
                currentSolution.value = currentSolution.value?.copy(isBookmarked = isBookmarked)
            }
        }
    }

    fun toggleCurrentBookmark(isBookmarked: Boolean) {
        currentSolution.value?.let { sol ->
            if (sol.id > 0) {
                toggleBookmark(sol.id, isBookmarked)
            } else {
                currentSolution.value = sol.copy(isBookmarked = isBookmarked)
            }
        }
    }

    fun deleteQuestion(id: Int) {
        viewModelScope.launch {
            dao.deleteQuestion(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            dao.clearAll()
        }
    }

    fun speakText(text: String) {
        if (isTtsInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "MathSolverTTS")
        }
    }

    fun clearCurrentSolution() {
        currentSolution.value = null
        errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
