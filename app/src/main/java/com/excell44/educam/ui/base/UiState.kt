package com.excell44.educam.ui.base

/**
 * Interface marqueur pour tous les états de l'interface utilisateur.
 * Chaque écran doit définir son propre UiState (data class ou sealed interface).
 */
interface UiState

/**
 * État de chargement générique.
 */
object Loading : UiState

/**
 * État vide générique.
 */
object Empty : UiState
