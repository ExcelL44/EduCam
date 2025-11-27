package com.excell44.educam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.excell44.educam.data.local.AIConfigDataStore
import com.excell44.educam.data.local.entity.QuizEntity
import com.excell44.educam.data.local.entity.QuizResultEntity
import com.excell44.educam.domain.usecase.GetAIEnhancedQuizzesUseCase
import com.excell44.educam.domain.usecase.SubmitQuizWithAIFeedbackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val getQuizzesUseCase: GetAIEnhancedQuizzesUseCase,
    private val submitQuizUseCase: SubmitQuizWithAIFeedbackUseCase,
    private val aiConfig: AIConfigDataStore
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()
    
    init {
        loadQuizzes()
    }
    
    private fun loadQuizzes() {
        viewModelScope.launch {
            aiConfig.isAIEnabled.collect { isEnabled ->
                // Ici on pourrait filtrer ou charger différemment selon isEnabled
                // Pour l'exemple on charge les quiz IA
                getQuizzesUseCase().collect { quizzes ->
                    _uiState.value = _uiState.value.copy(quizzes = quizzes)
                }
            }
        }
    }
    
    fun submitQuiz(quizId: Long, answers: Map<Long, String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            submitQuizUseCase(quizId, answers)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        result = result,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
        }
    }
}

data class QuizUiState(
    val quizzes: List<QuizEntity> = emptyList(),
    val currentQuizId: Long = 0,
    val result: QuizResultEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
