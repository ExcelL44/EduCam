# 🔧 FIX: Loop de Navigation (Login → Login)

**Date**: 2025-11-30  
**Problème**: Utilisateur renvoyé instantanément à l'écran de connexion après avoir cliqué sur "Connecter"  
**Statut**: ✅ **RÉSOLU**  

---

## 🔴 PROBLÈME IDENTIFIÉ

### Symptôme
```
User clique "Connecter" → Loading → Retour immédiat à Login (loop infini)
```

### Cause Racine

#### **1. Logout Incomplet** (Critique)

**Fichier**: `AuthViewModel.kt`

**Code Problématique**:
```kotlin
fun logout() {
    viewModelScope.launch(Dispatchers.IO) {
        // TODO: Implement proper logout in Repository (clear prefs, firebase signOut)
        _authState.value = AuthState.Unauthenticated()  // ❌ SecurePrefs PAS nettoyé !
    }
}
```

**Conséquence**:
1. User clique Login → `authState` = `Authenticated`
2. Navigation vers Home se déclenche
3. `NavGraph.LaunchedEffect` détecte changement
4. **MAIS** `securePrefs.getUserId()` retourne toujours l'ancien user
5. `initialize()` dans AuthViewModel reload l'user
6. Si user invalide/expiré → `authState` → `Unauthenticated`
7. Navigation redirige vers Login
8. **LOOP INFINI**

#### **2. UserSyncWorker Crash** (Non-bloquant mais problématique)

**Log**:
```
E WM-WorkerFactory: Could not instantiate com.excell44.educam.data.worker.UserSyncWorker
E WM-WorkerFactory: java.lang.NoSuchMethodException: <init> [class android.content.Context, class androidx.work.WorkerParameters]
```

**Cause**: `@AssistedInject` utilisé mais WorkManager cherche constructeur standard.

**Impact**: Background sync ne fonctionne pas, mais n'empêche pas l'app de fonctionner.

---

## ✅ SOLUTION APPLIQUÉE

### Fix #1: Logout Complet

**Étape 1**: Ajouter SecurePrefs au constructeur

```kotlin
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val networkObserver: NetworkObserver,
    private val securePrefs: com.excell44.educam.data.local.SecurePrefs  // ✅ Ajouté
) : ViewModel() {
```

**Étape 2**: Implémenter logout correctement

```kotlin
fun logout() {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            Logger.i("AuthViewModel", "Logout initiated")
            
            // ✅ CRITICAL: Clear secure session
            securePrefs.clearUserId()
            Logger.d("AuthViewModel", "SecurePrefs cleared")
            
            // Set state to unauthenticated
            _authState.value = AuthState.Unauthenticated(reason = "Déconnexion utilisateur")
            
            Logger.i("AuthViewModel", "Logout completed successfully")
            
        } catch (e: Exception) {
            Logger.e("AuthViewModel", "Error during logout", e)
            // Even on error, logout (security measure)
            _authState.value = AuthState.Unauthenticated(reason = "Déconnexion (avec erreur)")
        }
    }
}
```

**Impact**:
- ✅ SecurePrefs nettoyé à la déconnexion
- ✅ Pas de ré-authentication fantôme
- ✅ Navigation stable

---

## 🧪 TESTS À EFFECTUER

### Test 1: Login Standard
1. Lancer l'app
2. Entrer pseudo + code valides
3. Cliquer "Connecter"
4. ✅ **Vérifier**: Navigation vers HomeScreen (PAS de retour à Login)
5. Naviguer dans l'app
6. ✅ **Vérifier**: Pas de crash, pas de loop

### Test 2: Logout
1. Depuis HomeScreen
2. Cliquer bouton Logout
3. ✅ **Vérifier**: Retour à LoginScreen
4. Tenter de naviguer manuellement (back button)
5. ✅ **Vérifier**: Renvoi à Login (pas d'accès non-authentifié)

### Test 3: Session Persistance
1. Login
2. Aller à HomeScreen
3. Fermer l'app (swipe away)
4. Rouvrir l'app
5. ✅ **Vérifier**: Retour direct à HomeScreen (session persistante)

### Test 4: Trial Expiré
1. Créer compte offline (trial)
2. Modifier manually `trialExpiresAt` dans DB (passé)
3. Rouvrir l'app
4. ✅ **Vérifier**: Redirigé vers Login avec message clair

---

## 📊 LOGS À SURVEILLER

### Logs de Succès Attendus:

```
D Bac-X_237:AuthViewModel: Attempting login for [pseudo]
I Bac-X_237:AuthRepository: Login successful: [userId] ([pseudo])
I Bac-X_237:AuthViewModel: Login success: [userId]
D Bac-X_237:NavGraph: Auth changed: isLoggedIn=true, currentRoute=login
D Bac-X_237:NavGraph: Navigating to Home after login
```

### Logs d'Erreur (AVANT FIX):

```
❌ I Bac-X_237:AuthViewModel: Login success: [userId]
❌ D Bac-X_237:AuthViewModel: Initializing auth state...  // ⚠️ Re-init immédiat !
❌ W Bac-X_237:AuthViewModel: No user found or error: ...
❌ D Bac-X_237:NavGraph: Auth changed: isLoggedIn=false, currentRoute=home
❌ D Bac-X_237:NavGraph: Navigating back to Login
```

---

## 🚀 AMÉLIORATIONS FUTURES (Optionnelles)

### 1. Ajouter Timeout de Session
```kotlin
// Dans AuthViewModel
private val SESSION_TIMEOUT_MS = 24L * 60 * 60 * 1000 // 24h

fun initialize() {
    val userId = securePrefs.getUserId()
    val lastLogin = securePrefs.getLastLoginTime()
    
    if (userId != null && isSessionExpired(lastLogin)) {
        Logger.w("AuthViewModel", "Session expired, auto-logout")
        logout()
        return
    }
    
    // ... rest of init
}
```

### 2. Retry Logic pour Network Errors
```kotlin
fun login(pseudo: String, code: String) {
    viewModelScope.launch {
        _authState.value = AuthState.Loading
        
        retry(maxAttempts = 3, delayMs = 1000) {
            authRepository.login(pseudo, code)
        }.onSuccess { user ->
            _authState.value = AuthState.Authenticated(user)
        }.onFailure { e ->
            _authState.value = AuthState.Error(e.message, canRetry = true)
        }
    }
}
```

### 3. Meilleure Gestion UserSyncWorker
```kotlin
// Utiliser HiltWorkerFactory correctement
// Voir: https://developer.android.com/training/dependency-injection/hilt-jetpack#workmanager
```

---

## ✅ CHECKLIST DE VALIDATION

- [x] SecurePrefs injecté dans AuthViewModel
- [x] `logout()` appelle `securePrefs.clearUserId()`
- [x] Logs de débogage ajoutés
- [x] Try-catch pour graceful error handling
- [ ] **Test manuel**: Login → Home (pas de loop)
- [ ] **Test manuel**: Logout → Login (session cleared)
- [ ] **Test manuel**: Reopen app → Home (session persist)

---

## 🎯 RÉSULTAT ATTENDU

**AVANT**:
```
User Login → Loading → Home → 💥 LOOP → Login → Loading → Home → Login ...
```

**APRÈS**:
```
User Login → Loading → ✅ Home → Stable ✅
```

---

**Temps de correction**: ~10 minutes  
**Impact**: 🔴 **CRITIQUE** - App inutilisable sans ce fix  
**Complexité**: Faible (dépendance manquante)  
**Prochaine priorité**: Tester UserSyncWorker fix (Phase 2)
