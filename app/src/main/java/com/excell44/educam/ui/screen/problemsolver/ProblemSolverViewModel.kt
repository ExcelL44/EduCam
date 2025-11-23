package com.excell44.educam.ui.screen.problemsolver

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.excell44.educam.data.model.ProblemSolution
import com.excell44.educam.data.repository.ProblemSolverRepository
import com.excell44.educam.util.AuthStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProblemSolverUiState(
    val imageUri: Uri? = null,
    val pdfUri: Uri? = null,
    val recognizedText: String = "",
    val solution: ProblemSolution? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProblemSolverViewModel @Inject constructor(
    private val problemSolverRepository: ProblemSolverRepository,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProblemSolverUiState())
    val uiState: StateFlow<ProblemSolverUiState> = _uiState.asStateFlow()

    fun captureImage() {
        // Cette fonction sera appelée depuis l'Activity/Fragment
        // Pour l'instant, on simule
        _uiState.value = _uiState.value.copy(isLoading = true)
    }

    fun selectPdf() {
        // Cette fonction sera appelée depuis l'Activity/Fragment
        _uiState.value = _uiState.value.copy(isLoading = true)
    }

    fun processImage(uri: Uri, recognizedText: String) {
        val userId = authStateManager.getUserId() ?: return
        val gradeLevel = "Terminale" // À récupérer depuis le profil utilisateur

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                imageUri = uri,
                recognizedText = recognizedText,
                isLoading = true
            )

            try {
                val solution = problemSolverRepository.solveProblem(
                    userId = userId,
                    recognizedText = recognizedText,
                    imagePath = uri.toString(),
                    gradeLevel = gradeLevel
                )
                _uiState.value = _uiState.value.copy(
                    solution = solution,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun processPdf(uri: Uri, recognizedText: String) {
        val userId = authStateManager.getUserId() ?: return
        val gradeLevel = "Terminale"

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                pdfUri = uri,
                recognizedText = recognizedText,
                isLoading = true
            )

            try {
                val solution = problemSolverRepository.solveProblem(
                    userId = userId,
                    recognizedText = recognizedText,
                    pdfPath = uri.toString(),
                    gradeLevel = gradeLevel
                )
                _uiState.value = _uiState.value.copy(
                    solution = solution,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
}

