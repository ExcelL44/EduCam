# 📊 Diagrammes de Flux Corrigés - Basés sur le Code Réel

**Date**: 2025-12-03  
**Version**: 1.0 (Corrigé)  

> ⚠️ **Note** : Ces diagrammes ont été créés en analysant directement le code source et corrigent les erreurs des diagrammes générés automatiquement.

---

## ❌ Erreurs Corrigées

1. **Splash Screen** : Il n'y a qu'**UN SEUL** Splash Screen, pas deux
2. **HomeScreen → Register** : Cette navigation **N'EXISTE PAS** dans le code
3. **Flux simplifié** : Basé uniquement sur le code réel vérifié

---

## 1️⃣ Flux Complet de Login (CORRIGÉ)

```mermaid
flowchart TD
    Start([🚀 MainActivity.onCreate]) --> AuthInit[AuthViewModel.initialize<br/>Dispatchers.IO]
    
    AuthInit --> CleanExpired[🧹 Nettoyage comptes expirés<br/>deleteExpiredUnsyncedUsers]
    CleanExpired --> CheckSession[🔍 SecurePrefs.getUserId]
    
    CheckSession --> HasSession{Session<br/>trouvée?}
    
    HasSession -->|Non| SetUnauth[AuthState.Unauthenticated]
    HasSession -->|Oui| LoadUser[UserDao.getUserById]
    LoadUser --> SetAuth[AuthState.Authenticated]
    
    SetUnauth --> DetermineDest{Déterminer<br/>postSplashDest}
    SetAuth --> DetermineDest
    
    DetermineDest -->|Authenticated| DestHome[postSplash = Home]
    DetermineDest -->|Unauthenticated| DestLogin[postSplash = Login]
    
    DestHome --> NavGraph[NavGraph démarre sur<br/>Screen.Splash TOUJOURS]
    DestLogin --> NavGraph
    
    NavGraph --> ShowSplash[📱 UN SEUL Splash Screen<br/>Animation 2 secondes]
    
    ShowSplash --> NavDecision{postSplashDest?}
    
    NavDecision -->|Home| NavToHome[Navigation → HomeScreen]
    NavDecision -->|Login| NavToLogin[Navigation → LoginScreen]
    
    NavToHome --> HomeMenu[🏠 Menu Principal]
    
    NavToLogin --> UserInput[👤 User entre pseudo + code]
    UserInput --> ValidInput{Input<br/>valide?}
    
    ValidInput -->|Non| UserInput
    ValidInput -->|Oui| ClickLogin[Click Se connecter]
    
    ClickLogin --> VmLogin[AuthViewModel.login<br/>Dispatchers.IO]
    VmLogin --> SetLoading[AuthState.Loading]
    SetLoading --> RepoLogin[AuthRepository.login]
    
    RepoLogin --> QueryUser[UserDao.getUserByPseudo]
    QueryUser --> UserExists{User<br/>trouvé?}
    
    UserExists -->|Non| Error1[❌ Error: Compte non trouvé]
    UserExists -->|Oui| ValidatePass[🔐 Validation PBKDF2<br/>10k iterations]
    
    ValidatePass --> PassValid{Hash<br/>match?}
    
    PassValid -->|Non| Error2[❌ Error: Code incorrect]
    PassValid -->|Oui| CreateSession[✅ Créer Session]
    
    CreateSession --> SaveId[SecurePrefs.saveUserId]
    SaveId --> SaveCreds[SecurePrefs.saveOfflineCredentials]
    SaveCreds --> SaveMode[SecurePrefs.saveAuthMode]
    SaveMode --> UpdateAuth[AuthState.Authenticated<br/>Dispatchers.Main]
    
    UpdateAuth --> NavObserve[NavGraph observe authState]
    NavObserve --> NavHome[Navigate → HomeScreen]
    NavHome --> HomeMenu
    
    Error1 --> ShowError[Afficher Error UI]
    Error2 --> ShowError
    ShowError --> Retry{Réessayer?}
    Retry -->|Oui| UserInput
    Retry -->|Non| ShowError
    
    style Start fill:#9C27B0,color:#fff
    style HomeMenu fill:#4CAF50,color:#fff
    style ShowSplash fill:#673AB7,color:#fff
    style CreateSession fill:#FF9800,color:#fff
    style Error1 fill:#F44336,color:#fff
    style Error2 fill:#F44336,color:#fff
    style ValidatePass fill:#9C27B0,color:#fff
```

---

## 2️⃣ Navigation Complète de l'Application (CORRIGÉ)

```mermaid
flowchart TD
    Splash[Splash Screen<br/>⏱️ 2 secondes]
    Login[Login Screen<br/>🔐]
    Register[Register Screen<br/>📝]
    Home[Home Screen<br/>🏠 HUB CENTRAL]
    Quiz[Quiz Flow<br/>📚]
    Subjects[Subjects Screen<br/>📖]
    Smarty[Smarty IA<br/>💬 Problem Solver]
    Profile[Profile Screen<br/>👤]
    Bilan[Bilan Screen<br/>📊]
    AdminMenu[Admin Menu<br/>⚙️]
    RemoteDash[Remote Dashboard<br/>☁️]
    LocalDB[Local Database<br/>💾]
    
    Splash -->|Unauthenticated| Login
    Splash -->|Authenticated| Home
    
    Login -->|S'inscrire| Register
    Login -->|Login success| Home
    
    Register -->|Retour| Login
    Register -->|Register success| Home
    
    Home -->|Quiz button| Quiz
    Home -->|Subjects button<br/>🔒 TRIAL locked| Subjects
    Home -->|Smarty button<br/>🔒 TRIAL locked| Smarty
    Home -->|Profile icon| Profile
    Home -->|Admin button<br/>⚠️ if role=ADMIN| AdminMenu
    Home -->|Logout icon| Login
    
    Quiz -->|Back| Home
    Subjects -->|Back| Home
    Smarty -->|Back| Home
    
    Profile -->|Bilan| Bilan
    Profile -->|Back| Home
    Profile -->|Logout| Login
    
    Bilan -->|Back| Profile
    
    AdminMenu -->|Remote Dashboard| RemoteDash
    AdminMenu -->|Local Database| LocalDB
    AdminMenu -->|Back| Home
    
    RemoteDash -->|Back| AdminMenu
    LocalDB -->|Back| AdminMenu
    
    style Splash fill:#9C27B0,color:#fff
    style Login fill:#F44336,color:#fff
    style Register fill:#FF9800,color:#fff
    style Home fill:#4CAF50,color:#fff,stroke:#000,stroke-width:4px
    style AdminMenu fill:#1976D2,color:#fff
    
    linkStyle 5 stroke:#F44336,stroke-width:3px
```

---

## 3️⃣ HomeScreen - Actions de Navigation Détaillées

```mermaid
flowchart LR
    subgraph HomeAction["HomeAction (Sealed Class)"]
        A1[NavigateToQuiz]
        A2[NavigateToSubjects]
        A3[NavigateToProblemSolver]
        A4[NavigateToProfile]
        A5[NavigateToAdmin]
        A6[Logout]
    end
    
    Home[🏠 HomeScreen] --> A1
    Home --> A2
    Home --> A3
    Home --> A4
    Home --> A5
    Home --> A6
    
    A1 --> Quiz[📚 Quiz Flow]
    A2 --> Subjects[📖 Subjects Screen<br/>🔒 if TRIAL]
    A3 --> Smarty[💬 Smarty IA<br/>🔒 if TRIAL]
    A4 --> Profile[👤 Profile Screen]
    A5 --> Admin[⚙️ Admin Menu<br/>⚠️ if role=ADMIN only]
    A6 --> Login[🔐 Login Screen<br/>❌ Session cleared]
    
    Note[❌ PAS de navigation<br/>vers Register Screen]
    
    style Home fill:#4CAF50,color:#fff,stroke:#000,stroke-width:3px
    style Note fill:#F44336,color:#fff
    style Admin stroke-dasharray: 5 5
    style Subjects stroke:#FFC107,stroke-width:2px
    style Smarty stroke:#FFC107,stroke-width:2px
```

---

## 4️⃣ Cycle de Vie AuthState (CORRIGÉ)

```mermaid
stateDiagram-v2
    [*] --> Loading: App Start<br/>AuthViewModel.init
    
    Loading --> Authenticated: Session found<br/>+ User in DB
    Loading --> Unauthenticated: No session
    Loading --> Error: DB error
    
    Unauthenticated --> Loading: User clicks Login
    
    Loading --> Authenticated: Login success<br/>+ Session saved
    Loading --> Error: Login failed<br/>(User not found / Invalid password)
    
    Error --> Loading: User clicks Retry
    Error --> Unauthenticated: User clicks Abandon
    
    Authenticated --> Unauthenticated: User logout<br/>SecurePrefs cleared
    
    Authenticated --> [*]: App closed
    
    note right of Authenticated
        User in HomeScreen
        Full access (or limited if TRIAL)
        Session persisted in SecurePrefs
    end note
    
    note right of Loading
        Spinner UI shown
        viewModelScope.launch(IO)
    end note
    
    note right of Error
        Error message shown
        canRetry = true
        User can dismiss
    end note
```

---

## 5️⃣ Threads & Dispatchers

```mermaid
flowchart TB
    subgraph Main["🎨 Main Thread"]
        UI[LoginScreen UI]
        Collect[collectAsState]
        Recompose[Recomposition]
    end
    
    subgraph VM["⚙️ ViewModel Scope"]
        AuthVM[AuthViewModel]
        StateFlow[MutableStateFlow<br/>authState]
    end
    
    subgraph IO["💾 IO Dispatcher"]
        Login[login function]
        DBQuery[UserDao queries]
        SecureSave[SecurePrefs save]
        PBKDF2[PBKDF2 validation<br/>10k iterations]
    end
    
    UI -->|onClick| AuthVM
    AuthVM -->|launch IO| Login
    
    Login --> DBQuery
    Login --> PBKDF2
    DBQuery --> SecureSave
    
    SecureSave -->|withContext Main| UpdateState[Update StateFlow]
    UpdateState --> StateFlow
    StateFlow --> Collect
    Collect --> Recompose
    Recompose -->|Trigger| NavGraph[NavGraph navigation]
    
    style Main fill:#E3F2FD
    style VM fill:#F3E5F5
    style IO fill:#E8F5E9
```

---

## 📝 Résumé des Corrections

### ✅ Ce qui est correct maintenant :

1. **UN SEUL Splash Screen** - Affiché 2 secondes au démarrage
2. **Navigation HomeScreen** :
   - ✅ Quiz, Subjects, Smarty IA, Profile, Admin Menu (si ADMIN), Logout
   - ❌ **PAS de navigation vers Register**
3. **Flux Login** :
   - Vérification session → Splash → Login (si nécessaire) → Home
   - Validation PBKDF2 avec 10k iterations
   - Session sauvegardée dans SecurePrefs (chiffré AES-256)

### 🔍 Sources vérifiées :

- ✅ `MainActivity.kt` (lignes 74-95)
- ✅ `NavGraph.kt` (lignes 88-115)
- ✅ `HomeScreen.kt` (lignes 36-43, 94-119)
- ✅ `AuthViewModel.kt` (initialize, login)
- ✅ `AuthRepository.kt` (login, PBKDF2)

### 🎯 Navigation réelle depuis Home :

```kotlin
sealed class HomeAction : UiAction {
    object NavigateToQuiz : HomeAction()
    object NavigateToSubjects : HomeAction()
    object NavigateToProblemSolver : HomeAction()  // Smarty IA
    object NavigateToProfile : HomeAction()
    object NavigateToAdmin : HomeAction()
    object Logout : HomeAction()
    // ❌ PAS de NavigateToRegister
}
```

---

**📅 Dernière vérification** : 2025-12-03  
**✅ Basé sur le code source réel** : Analysé et vérifié  
**👨‍💻 EduCam - Bac-X_237**
