# 🧹 RAPPORT DE NETTOYAGE DU CODE (GUEST MODE)

**Date**: 2025-11-30  
**Statut**: ✅ **NETTOYÉ**

---

## 🗑️ ÉLÉMENTS SUPPRIMÉS

Le mode "Invité" (Guest) a été entièrement retiré de la base de code pour simplifier l'architecture et éliminer le code mort.

### 1. Logique Métier (ViewModel & Repository)
- ❌ `AuthViewModel.loginAsGuest()` : Supprimé.
- ❌ `AuthRepository.loginAnonymous()` : Supprimé.
- ❌ `AuthAction.GuestMode` : Supprimé.
- ❌ `AuthUiState.guestAttemptsRemaining` : Supprimé.

### 2. Interface Utilisateur (UI)
- ❌ `HomeState.isGuest` : Supprimé.
- ✅ `LoginScreen` : Vérifié (aucun bouton invité résiduel).

### 3. Fonctionnalités Quiz
- ❌ `QuizViewModel.guestHintLimit` : Supprimé (plus de limite d'indices spécifique aux invités).
- ❌ `QuizViewModel.hintsUsed` : Supprimé.
- ❌ `SampleQuestionsProvider.getGuestQuestions()` : Supprimé.

---

## 🛡️ CONSISTANCE DU SYSTÈME

### Authentification
Le système ne supporte plus que deux modes d'entrée :
1. **Connexion** (Compte existant)
2. **Inscription** (Nouveau compte)
   - Online (Paiement) -> `ACTIVE`
   - Offline (Essai 7 jours) -> `PASSIVE` (Trial)

### Modèle de Données
- `UserMode.GUEST` a été retiré de l'enum (vérifié : n'existe plus).
- Le fallback par défaut est maintenant `UserMode.TRIAL` (Mode Passif).

---

## ✅ IMPACT
- Réduction de la dette technique.
- Moins de confusion dans la logique de navigation.
- Code plus maintenable et focus sur le modèle Freemium.

Le système est maintenant propre et prêt pour la suite.
