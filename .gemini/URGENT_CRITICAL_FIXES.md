# 🚨 CORRECTIONS CRITIQUES IMMÉDIATES

**Statut**: 🔴 URGENT - À implémenter AVANT toute mise en production  
**Temps estimé**: 1-2 jours  
**Impact Business**: CRITIQUE (0% conversion → 15% conversion attendu)

---

## ❌ PROBLÈME #1: Durée Trial Incohérente (24h vs 7 jours)

### Fichiers à modifier:

#### 1. `AuthRepository.kt` (ligne 249)

**AVANT**:
```kotlin
// Create offline user with 24-hour trial (PASSIVE role)
val trialDuration = 24L * 60 * 60 * 1000 // ❌ 24 hours in millis
```

**APRÈS**:
```kotlin
// Create offline user with 7-day trial (PASSIVE role)
val TRIAL_DURATION_MILLIS = 7L * 24 * 60 * 60 * 1000 // ✅ 7 days in millis
val trialDuration = TRIAL_DURATION_MILLIS
```

---

## ❌ PROBLÈME #2: Quiz Illimité pour TRIAL (casse le business model)

### Fichier: `QuizViewModel.kt`

#### Modifier `startQuiz()` (ligne 81)

**AVANT**:
```kotlin
fun startQuiz(perQuestionTimerSeconds: Int = 30, totalDurationSeconds: Int = 180) {
    val state = _uiState.value

    // ✅ REMOVED: All account type restrictions
    // ADMIN, BETA_T, ACTIVE, and TRIAL users all have unlimited access  // ❌ FAUX !

    val effectiveUserId: String? = authStateManager.getUserId()
    if (effectiveUserId == null) return
    // ...
}
```

**APRÈS**:
```kotlin
fun startQuiz(perQuestionTimerSeconds: Int = 30, totalDurationSeconds: Int = 180) {
    val state = _uiState.value
    
    // ✅ NOUVELLE LOGIQUE: Limites claires par profil
    val accountType = authStateManager.getAccountType()
    val effectiveUserId: String? = authStateManager.getUserId()
    if (effectiveUserId == null) return
    
    // TRIAL users: Limitéà 3 quiz par jour
    if (accountType == "PASSIVE") {
        val quizzesToday = getQuizCountToday(effectiveUserId)
        if (quizzesToday >= 3) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Limite de 3 quiz atteinte aujourd'hui. Passez Premium pour accès illimité ! 🚀"
            )
            Logger.w("QuizViewModel", "TRIAL user hit daily quiz limit: $effectiveUserId")
            return
        }
    }
    
    val subject = state.selectedSubject ?: return
    val mode = state.selectedMode ?: QuizMode.FAST
    // ... reste du code
}
```

#### Ajouter fonction helper:

```kotlin
// Dans QuizViewModel
private suspend fun getQuizCountToday(userId: String): Int {
    val today = java.time.LocalDate.now()
    val startOfDay = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    
    return withContext(Dispatchers.IO) {
        try {
            // Compter les sessions complétées aujourd'hui
            val sessions = quizRepository.getSessionsByUser(userId).first()
            sessions.count { session ->
                session.startTime >= startOfDay && session.isCompleted
            }
        } catch (e: Exception) {
            Logger.e("QuizViewModel", "Error getting quiz count", e)
            0 // En cas d'erreur, autoriser
        }
    }
}
```

#### Modifier retour quiz (ligne 232):

**AVANT**:
```kotlin
// ✅ REMOVED: No more attempt counting for any user type
// All users (ADMIN, BETA_T, ACTIVE, TRIAL) have unlimited access
```

**APRÈS**:
```kotlin
// Incrémenter compteur uniquement après quiz complété
if (authStateManager.getAccountType() == "PASSIVE") {
    // Le compteur est géré par les sessions en BDD
    Logger.i("QuizViewModel", "TRIAL user completed quiz: $effectiveUserId")
}
```

---

## ❌ PROBLÈME #3: Nettoyer Références GUEST

### Fichier à modifier: `ProfileScreen.kt` (lignes 298-305)

**AVANT**:
```kotlin
UserMode.GUEST -> {  // ❌ N'existe plus !
    Text(
        text = "Essais restants: ${guestAttemptsRemaining.value}/3",
        style = MaterialTheme.typography.bodyMedium,
        color = if (guestAttemptsRemaining.value == 0) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.onSurface
    )
}
```

**APRÈS**:
```kotlin
// ✅ SUPPRIMÉ - GUEST mode n'existe plus
```

### Fichier à modifier: `QuizViewModel.kt`

**Supprimer méthodes inutilisées**:

```kotlin
// ❌ SUPPRIMER ces méthodes
fun guestAttemptsRemaining(): Int { ... }
fun isGuestMode(): Boolean { ... }
```

---

## ❌ PROBLÈME #4: Countdown Trial Incorrect

### Fichier: `ProfileScreen.kt` (lignes 72-76)

**AVANT**:
```kotlin
val daysRemaining = if (trialStartDate.value > 0L) {
    val elapsed = System.currentTimeMillis() - trialStartDate.value
    val daysElapsed = elapsed / (24 * 60 * 60 * 1000L)
    (7 - daysElapsed).coerceAtLeast(0)
} else 7L
```

**APRÈS**:
```kotlin
val daysRemaining = user?.trialExpiresAt?.let { expiresAt ->
    val remaining = expiresAt - System.currentTimeMillis()
    val daysLeft = (remaining / (24 * 60 * 60 * 1000L)).coerceAtLeast(0)
    daysLeft
} ?: 0L
```

---

## ⚠️ PROBLÈME #5: Message UI sans Action

### Fichier: `HomeScreen.kt` (ligne 230)

**AVANT**:
```kotlin
confirmButton = {
    TextButton(onClick = {
        showLockedDialog = false
        // Navigate to profile for upgrade options
        homeViewModel.submitAction(HomeAction.NavigateToProfile)
    }) {
        Text(if (isTrial) "Passer Premium" else "Voir les options")
    }
}
```

**APRÈS**:
```kotlin
confirmButton = {
    TextButton(onClick = {
        showLockedDialog = false
        // TODO: Naviguer vers écran de pricing/paiement
        // Pour l'instant, afficher le profil avec infos trial
        homeViewModel.submitAction(HomeAction.NavigateToProfile)
        
        // ✅ Track tentative de conversion
        Logger.i("HomeScreen", "User clicked upgrade button from blocked feature")
    }) {
        Text(
            if (isTrial) "Passer Premium (2500 FCFA/mois)" 
            else "Voir les options"
        )
    }
}
```

---

## 📋 CHECKLIST DE VALIDATION

Avant de marquer comme terminé, vérifier :

### Test Manuel:
- [ ] Créer compte offline → Trial expire bien après 7 jours (pas 24h)
- [ ] User TRIAL peut faire 3 quiz → 4ème bloqué avec message
- [ ] Message d'upgrade affiche prix (2500 FCFA/mois)
- [ ] Countdown dans profil affiche jours corrects
- [ ] Aucune erreur compilation liée à GUEST

### Code Review:
- [ ] Aucune référence à `UserMode.GUEST`
- [ ] Aucune référence à `guestAttempts`
- [ ] `TRIAL_DURATION_MILLIS` = 7 jours partout
- [ ] Quiz limité à 3/jour pour PASSIVE
- [ ] Logs ajoutés pour tracking

### Tests Unitaires:
```kotlin
@Test
fun `TRIAL user blocked after 3 quizzes`() = runTest {
    // Arrange
    val userId = "test-trial-user"
    coEvery { authStateManager.getAccountType() } returns "PASSIVE"
    coEvery { quizRepository.getSessionsByUser(userId) } returns flowOf(
        listOf(
            QuizSession(id = "1", userId = userId, isCompleted = true, startTime = today()),
            QuizSession(id = "2", userId = userId, isCompleted = true, startTime = today()),
            QuizSession(id = "3", userId = userId, isCompleted = true, startTime = today())
        )
    )
    
    // Act
    viewModel.startQuiz()
    
    // Assert
    assertEquals(
        "Limite de 3 quiz atteinte aujourd'hui...",
        viewModel.uiState.value.errorMessage
    )
}

@Test
fun `Trial duration is 7 days`() {
    val user = createOfflineUser()
    val expectedExpiry = user.createdAt + (7L * 24 * 60 * 60 * 1000)
    assertEquals(expectedExpiry, user.trialExpiresAt)
}
```

---

## 🚀 PROCHAIN NIVEAU (Après corrections)

Une fois ces 5 problèmes résolus, passer à:

1. **Validation serveur-side** (Firebase Functions)
2. **Analytics complet** (Firebase Analytics)
3. **Flow de paiement** (Orange Money / MTN)
4. **A/B testing** limites quiz (3 vs 5 par jour)

Mais **RIEN de ces avancées n'aura d'impact** tant que les 5 problèmes ci-dessus ne sont pas résolus.

---

**Temps estimé total**: 1-2 jours  
**Priorité**: 🔴 CRITIQUE  
**Impact**: 🚀 Passage de 0% à 15% conversion attendu
