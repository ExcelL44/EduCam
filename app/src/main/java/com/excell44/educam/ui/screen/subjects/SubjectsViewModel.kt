package com.excell44.educam.ui.screen.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.excell44.educam.data.model.Subject
import com.excell44.educam.data.repository.SubjectRepository
import com.excell44.educam.util.AuthStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectsUiState(
    val subjects: List<Subject> = emptyList(),
    val selectedSubject: String? = null,
    val selectedSubjectContent: Subject? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SubjectsViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectsUiState())
    val uiState: StateFlow<SubjectsUiState> = _uiState.asStateFlow()

    private val gradeLevel = "Terminale" // À récupérer depuis le profil utilisateur

    fun loadSubjects() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Pour l'instant, on charge tous les sujets
                // Dans une vraie app, on filtrerait par gradeLevel
                subjectRepository.getSubjectsBySubjectAndGrade("Math", gradeLevel)
                    .collect { subjects ->
                        _uiState.value = _uiState.value.copy(
                            subjects = subjects,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun filterBySubject(subject: String?) {
        _uiState.value = _uiState.value.copy(selectedSubject = subject)
        // Recharger les sujets filtrés
        viewModelScope.launch {
            val gradeLevel = "Terminale"
            val filterSubject = subject ?: "Math"
            subjectRepository.getSubjectsBySubjectAndGrade(filterSubject, gradeLevel)
                .collect { subjects ->
                    _uiState.value = _uiState.value.copy(subjects = subjects)
                }
        }
    }

    fun selectSubject(subjectId: String) {
        viewModelScope.launch {
            val subject = subjectRepository.getSubjectById(subjectId)
            _uiState.value = _uiState.value.copy(selectedSubjectContent = subject)
        }
    }

    fun clearSelectedSubject() {
        _uiState.value = _uiState.value.copy(selectedSubjectContent = null)
    }
}

