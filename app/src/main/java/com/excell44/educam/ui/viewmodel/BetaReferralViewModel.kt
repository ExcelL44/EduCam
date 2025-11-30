package com.excell44.educam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.excell44.educam.data.local.SecurePrefs
import com.excell44.educam.domain.referral.model.ReferralStatus
import com.excell44.educam.domain.referral.usecase.GetBetaReferralStatusUseCase
import com.excell44.educam.domain.referral.usecase.RequestBetaPaymentUseCase
import com.excell44.educam.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * États UI pour le BetaReferral.
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

/**
 * ViewModel pour gérer l'état du système de parrainage Beta-User.
 * Gère la logique UI et les interactions utilisateur.
 */
@HiltViewModel
class BetaReferralViewModel @Inject constructor(
    private val getStatus: GetBetaReferralStatusUseCase,
    private val requestPayment: RequestBetaPaymentUseCase,
    private val securePrefs: SecurePrefs
) : ViewModel() {

    private val _referralState = MutableStateFlow<UiState<ReferralStatus>>(UiState.Loading)
    val referralState: StateFlow<UiState<ReferralStatus>> = _referralState.asStateFlow()

    init {
        loadReferralStatus()
    }

    /**
     * Charge le statut de parrainage de l'utilisateur actuel.
     */
    private fun loadReferralStatus() {
        viewModelScope.launch {
            securePrefs.getUserId()?.let { userId ->
                getStatus(userId)
                    .onStart { _referralState.value = UiState.Loading }
                    .catch { exception ->
                        Logger.e("BetaReferralViewModel", "Error loading referral status", exception)
                        _referralState.value = UiState.Error(
                            exception.message ?: "Erreur lors du chargement du statut"
                        )
                    }
                    .collect { status ->
                        _referralState.value = UiState.Success(status)
                        Logger.d("BetaReferralViewModel", "Referral status loaded: ${status.displayText}")
                    }
            } ?: run {
                // Aucun utilisateur connecté
                _referralState.value = UiState.Error("Aucun utilisateur connecté")
            }
        }
    }

    /**
     * Gère le clic sur le bouton cadeau.
     * Envoie une demande de paiement WhatsApp.
     */
    fun onGiftButtonClicked() {
        if (_referralState.value is UiState.Success) {
            viewModelScope.launch {
                _referralState.value = UiState.Loading
                Logger.d("BetaReferralViewModel", "Processing gift button click")

                securePrefs.getUserId()?.let { userId ->
                    val result = requestPayment(userId)
                    if (result.isSuccess) {
                        Logger.i("BetaReferralViewModel", "Payment request sent successfully for user $userId")
                        // Recharger le statut après envoi réussi
                        loadReferralStatus()
                    } else {
                        Logger.w("BetaReferralViewModel", "Payment request failed: ${result.exceptionOrNull()?.message}")
                        _referralState.value = UiState.Error(
                            result.exceptionOrNull()?.message ?: "Erreur lors de l'envoi de la demande"
                        )
                    }
                } ?: run {
                    _referralState.value = UiState.Error("Utilisateur non identifié")
                }
            }
        } else {
            Logger.w("BetaReferralViewModel", "Gift button clicked but state is not Success")
        }
    }

    /**
     * Force le rechargement du statut (utile après des changements externes).
     */
    fun refreshStatus() {
        Logger.d("BetaReferralViewModel", "Manual refresh requested")
        loadReferralStatus()
    }
}
