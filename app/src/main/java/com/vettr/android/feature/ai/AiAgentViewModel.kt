package com.vettr.android.feature.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.remote.AiAgentQuestionResponse
import com.vettr.android.core.data.remote.AiAgentResponse
import com.vettr.android.core.data.remote.AiAgentUsageResponse
import com.vettr.android.core.data.repository.AiAgentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class AiConversationMessage(
    val isUser: Boolean,
    val content: String,
    val response: AiAgentResponse? = null
)

data class AiAgentUiState(
    val ticker: String = "",
    val inputText: String = "",
    val messages: List<AiConversationMessage> = emptyList(),
    val suggestedQuestions: List<AiAgentQuestionResponse> = emptyList(),
    val questionCategories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val filteredQuestions: List<AiAgentQuestionResponse> = emptyList(),
    val usage: AiAgentUsageResponse? = null,
    val isLoading: Boolean = false,
    val isLoadingQuestions: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AiAgentViewModel @Inject constructor(
    private val aiAgentRepository: AiAgentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAgentUiState())
    val uiState: StateFlow<AiAgentUiState> = _uiState.asStateFlow()

    init {
        loadQuestionsAndUsage()
    }

    private fun loadQuestionsAndUsage() {
        viewModelScope.launch {
            val questionsResult = aiAgentRepository.getQuestions()
            questionsResult.onSuccess { questions ->
                val categories = questions.map { it.category }.distinct().sorted()
                _uiState.value = _uiState.value.copy(
                    suggestedQuestions = questions,
                    filteredQuestions = questions,
                    questionCategories = categories,
                    isLoadingQuestions = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoadingQuestions = false)
            }
        }

        viewModelScope.launch {
            val usageResult = aiAgentRepository.getUsage()
            usageResult.onSuccess { usage ->
                _uiState.value = _uiState.value.copy(usage = usage)
            }
        }
    }

    fun selectCategory(category: String?) {
        val state = _uiState.value
        val filtered = if (category == null) {
            state.suggestedQuestions
        } else {
            state.suggestedQuestions.filter { it.category == category }
        }
        _uiState.value = state.copy(
            selectedCategory = category,
            filteredQuestions = filtered
        )
    }

    fun updateTicker(ticker: String) {
        _uiState.value = _uiState.value.copy(ticker = ticker.uppercase(), error = null)
    }

    fun updateInput(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text, error = null)
    }

    fun askQuestion(questionId: String? = null, customQuestion: String? = null) {
        val state = _uiState.value
        if (state.ticker.isBlank()) {
            _uiState.value = state.copy(error = "Please enter a stock ticker")
            return
        }

        val displayQuestion = customQuestion ?: state.inputText
        if (displayQuestion.isBlank() && questionId == null) {
            _uiState.value = state.copy(error = "Please enter a question or select one")
            return
        }

        viewModelScope.launch {
            // Add user message
            val userMessage = AiConversationMessage(
                isUser = true,
                content = displayQuestion.ifBlank {
                    state.suggestedQuestions.find { it.id == questionId }?.question ?: "..."
                }
            )
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + userMessage,
                inputText = "",
                isLoading = true,
                error = null
            )

            val result = aiAgentRepository.askQuestion(
                ticker = state.ticker,
                questionId = questionId,
                customQuestion = if (questionId == null) displayQuestion else null
            )

            result.onSuccess { response ->
                val aiMessage = AiConversationMessage(
                    isUser = false,
                    content = response.answer,
                    response = response
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMessage,
                    isLoading = false
                )
                // Refresh usage
                aiAgentRepository.getUsage().onSuccess { usage ->
                    _uiState.value = _uiState.value.copy(usage = usage)
                }
            }.onFailure { e ->
                Timber.e(e, "AI agent error")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to get AI response"
                )
            }
        }
    }

    fun askFollowUp(question: String) {
        updateInput(question)
        askQuestion(customQuestion = question)
    }

    fun clearConversation() {
        _uiState.value = _uiState.value.copy(messages = emptyList(), error = null)
    }
}
