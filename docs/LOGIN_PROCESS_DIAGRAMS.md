# 📊 Diagrammes du Processus de Login - EduCam

**Date**: 2025-12-03  
**Version**: 1.0  

> 💡 **Note**: Ces diagrammes sont rendus automatiquement dans GitHub, VS Code (avec extension), et la plupart des visualiseurs Markdown modernes.

---

## 🔄 Diagramme 1: Flux Complet - Du Démarrage au Menu Principal

Ce diagramme montre le flux complet depuis le lancement de l'application jusqu'à l'affichage du menu principal, incluant tous les chemins possibles (succès, erreurs, retry).

```mermaid
flowchart TD
    Start([🚀 Démarrage Application]) --> MainActivity[MainActivity.onCreate]
    MainActivity --> EnableEdge[enableEdgeToEdge]
    EnableEdge --> SetContent[setContent - Compose UI]
    
    SetContent --> InjectMain[Inject MainViewModel<br/>Gestion Thème]
    InjectMain --> ApplyTheme[Appliquer BacXTheme]
    ApplyTheme --> AppContent[appContent Composable]
    
    AppContent --> InjectVMs[Injection ViewModels Hilt]
    InjectVMs --> InjectAuth[AuthViewModel]
    InjectVMs --> InjectNav[NavigationViewModel]
    InjectVMs --> InjectMain2[MainViewModel]
    
    InjectAuth --> AuthInit[AuthViewModel.initialize]
    
    AuthInit --> CleanExpired[🧹 Nettoyage Comptes Expirés]
    CleanExpired --> CleanQuery{Comptes > 24h<br/>non synchronisés?}
    CleanQuery -->|Oui| DeleteExpired[UserDao.deleteExpiredUnsyncedUsers]
    CleanQuery -->|Non| CheckSession
    DeleteExpired --> CheckSession[🔍 Vérification Session]
    
    CheckSession --> GetUserId[SecurePrefs.getUserId]
    GetUserId --> HasSession{Session<br/>trouvée?}
    
    HasSession -->|Oui| LoadUser[AuthRepository.getUser]
    LoadUser --> QueryDB[UserDao.getUserById]
    QueryDB --> UserFound{User<br/>existe?}
    UserFound -->|Oui| SetAuthenticated[AuthState.Authenticated]
    UserFound -->|Non| SetUnauthenticated[AuthState.Unauthenticated]
    
    HasSession -->|Non| SetUnauthenticated
    
    SetAuthenticated --> DetermineStart{Déterminer<br/>Destination}
    SetUnauthenticated --> DetermineStart
    
    DetermineStart -->|Authenticated| StartHome[startDestination = Home]
    DetermineStart -->|Unauthenticated| StartLogin[startDestination = Login]
    
    StartHome --> ShowSplash[Afficher Splash Screen]
    StartLogin --> ShowSplash
    
    ShowSplash --> SplashDelay[Délai 2 secondes]
    SplashDelay --> NavDecision{AuthState?}
    
    NavDecision -->|Authenticated| NavToHome[Navigation → HomeScreen]
    NavDecision -->|Unauthenticated| NavToLogin[Navigation → LoginScreen]
    
    NavToHome --> HomeMenu[📱 Menu Principal Affiché]
    
    NavToLogin --> DisplayLogin[Afficher LoginScreen]
    DisplayLogin --> UserInput[👤 User Input<br/>Pseudo + Code]
    UserInput --> ValidInput{Input<br/>valide?}
    
    ValidInput -->|Non| DisplayLogin
    ValidInput -->|Oui| ClickLogin[onClick Login Button]
    
    ClickLogin --> VmLogin[AuthViewModel.login]
    VmLogin --> SetLoading[AuthState.Loading<br/>🔄 Spinner UI]
    SetLoading --> RepoLogin[AuthRepository.login]
    
    RepoLogin --> QueryUser[UserDao.getUserByPseudo]
    QueryUser --> UserExists{User<br/>trouvé?}
    
    UserExists -->|Non| LoginError1[Error: Compte non trouvé]
    UserExists -->|Oui| CheckHash{passwordHash<br/>vide?}
    
    CheckHash -->|Oui| LoginError2[Error: Sécurité]
    CheckHash -->|Non| ValidatePassword[Validation PBKDF2]
    
    ValidatePassword --> ExtractSalt[Extraire salt du User]
    ExtractSalt --> HashInput[Hash password input<br/>PBKDF2-HMAC-SHA256<br/>10k iterations]
    HashInput --> CompareHash{Hash<br/>correspond?}
    
    CompareHash -->|Non| LoginError3[Error: Code incorrect]
    CompareHash -->|Oui| CreateSession[✅ Créer Session]
    
    CreateSession --> SaveUserId[SecurePrefs.saveUserId]
    SaveUserId --> SaveCreds[SecurePrefs.saveOfflineCredentials]
    SaveCreds --> SaveMode[SecurePrefs.saveAuthMode]
    SaveMode --> ReturnSuccess[Result.success User]
    
    ReturnSuccess --> UpdateAuthState[AuthState.Authenticated<br/>sur Main Thread]
    UpdateAuthState --> NavGraphObserve[NavGraph LaunchedEffect<br/>observe authState]
    NavGraphObserve --> NavCheck{isLoggedIn &&<br/>currentRoute = Login?}
    
    NavCheck -->|Oui| NavCommand[NavigationViewModel.navigate]
    NavCommand --> PopBackstack[Pop jusqu'à Login<br/>inclusive=true]
    PopBackstack --> NavHomeScreen[Navigation → HomeScreen]
    NavHomeScreen --> HomeMenu
    
    LoginError1 --> ShowError[Afficher Error UI]
    LoginError2 --> ShowError
    LoginError3 --> ShowError
    ShowError --> RetryBtn{Click<br/>Réessayer?}
    RetryBtn -->|Oui| DisplayLogin
    RetryBtn -->|Non| ShowError
    
    style Start fill:#4CAF50,color:#fff
    style HomeMenu fill:#2196F3,color:#fff
    style CreateSession fill:#FF9800,color:#fff
    style LoginError1 fill:#F44336,color:#fff
    style LoginError2 fill:#F44336,color:#fff
    style LoginError3 fill:#F44336,color:#fff
    style SetLoading fill:#FFC107,color:#000
    style ValidatePassword fill:#9C27B0,color:#fff
```

---

## 🏗️ Diagramme 2: Architecture en Couches

Ce diagramme illustre l'organisation architecturale de l'application en couches distinctes avec leurs dépendances.

```mermaid
flowchart LR
    subgraph UI["🎨 UI Layer (Presentation)"]
        MainActivity[MainActivity]
        LoginScreen[LoginScreen]
        NavGraph[NavGraph]
        AuthVM[AuthViewModel]
    end
    
    subgraph Domain["🧠 Domain Layer"]
        AuthState[AuthState<br/>Sealed Class]
        AuthRepo[AuthRepository]
    end
    
    subgraph Data["💾 Data Layer"]
        SecurePrefs[SecurePrefs<br/>EncryptedSharedPrefs]
        UserDao[UserDao<br/>Room Interface]
        RoomDB[(Room Database<br/>SQLite)]
    end
    
    subgraph Infrastructure["⚙️ Infrastructure"]
        NetworkObs[NetworkObserver]
        Logger[Logger]
        Hilt[Hilt DI]
    end
    
    MainActivity --> LoginScreen
    LoginScreen --> AuthVM
    AuthVM --> AuthState
    AuthVM --> AuthRepo
    NavGraph --> AuthVM
    
    AuthRepo --> SecurePrefs
    AuthRepo --> UserDao
    AuthRepo --> NetworkObs
    UserDao --> RoomDB
    
    AuthVM -.Inject.-> Hilt
    AuthRepo -.Inject.-> Hilt
    SecurePrefs -.Inject.-> Hilt
    
    AuthRepo --> Logger
    
    style UI fill:#E3F2FD
    style Domain fill:#F3E5F5
    style Data fill:#E8F5E9
    style Infrastructure fill:#FFF3E0
```

---

## 🔄 Diagramme 3: Séquence de Login Réussi

Ce diagramme de séquence montre les interactions entre composants lors d'un login réussi.

```mermaid
sequenceDiagram
    actor User
    participant UI as LoginScreen
    participant VM as AuthViewModel
    participant Repo as AuthRepository
    participant DAO as UserDao
    participant DB as Room Database
    participant SP as SecurePrefs
    participant Nav as NavGraph
    
    User->>UI: Entre Pseudo + Code
    User->>UI: Click "Se connecter"
    
    UI->>VM: login(pseudo, code)
    activate VM
    VM->>VM: _authState = Loading
    VM-->>UI: AuthState.Loading
    UI->>UI: Affiche Spinner
    
    VM->>Repo: login(pseudo, password)
    activate Repo
    
    Repo->>DAO: getUserByPseudo(pseudo)
    activate DAO
    DAO->>DB: SELECT * FROM users WHERE pseudo = ?
    DB-->>DAO: User | null
    deactivate DAO
    
    alt User non trouvé
        Repo-->>VM: Result.failure("Compte non trouvé")
        VM->>VM: _authState = Error
        VM-->>UI: AuthState.Error
        UI->>UI: Affiche message erreur
    else User trouvé
        Repo->>Repo: Valider password (PBKDF2)
        
        alt Password invalide
            Repo-->>VM: Result.failure("Code incorrect")
            VM->>VM: _authState = Error
            VM-->>UI: AuthState.Error
        else Password valide
            Repo->>SP: saveUserId(user.id)
            activate SP
            SP->>SP: Encrypt & Store
            deactivate SP
            
            Repo->>SP: saveOfflineCredentials(pseudo, hash)
            activate SP
            SP->>SP: Encrypt & Store
            deactivate SP
            
            Repo->>SP: saveAuthMode(OFFLINE/ONLINE)
            activate SP
            SP->>SP: Encrypt & Store
            deactivate SP
            
            Repo-->>VM: Result.success(user)
            deactivate Repo
            
            VM->>VM: _authState = Authenticated(user)
            deactivate VM
            VM-->>UI: AuthState.Authenticated
            UI-->>User: Affiche "Connexion réussie"
            
            VM-->>Nav: authState changed
            activate Nav
            Nav->>Nav: Observe isLoggedIn = true
            Nav->>Nav: navigate(Home, popUpTo=Login)
            deactivate Nav
            
            Nav-->>UI: Navigation vers HomeScreen
            UI-->>User: 📱 Menu Principal affiché
        end
    end
```

---

## 🔀 Diagramme 4: Machine à États d'Authentification

Ce diagramme montre tous les états possibles et leurs transitions.

```mermaid
stateDiagram-v2
    [*] --> Loading: App Start
    
    Loading --> Authenticated: Session trouvée
    Loading --> Unauthenticated: Aucune session
    Loading --> Error: Erreur DB
    
    Unauthenticated --> Loading: Tentative Login
    
    Loading --> Authenticated: Login réussi
    Loading --> Error: Login échoué
    
    Error --> Loading: Retry
    Error --> Unauthenticated: Abandon
    
    Authenticated --> NeedsSync: Données offline
    Authenticated --> Unauthenticated: Logout
    Authenticated --> OfflineTrial: Compte Trial 24h
    
    OfflineTrial --> Passive: Trial expiré
    OfflineTrial --> Authenticated: Synchronisation OK
    
    NeedsSync --> Authenticated: Sync terminé
    
    Passive --> Authenticated: Activation compte
    
    Authenticated --> [*]: App fermée
    
    note right of Authenticated
        User dans HomeScreen
        Accès complet
    end note
    
    note right of OfflineTrial
        24h restantes
        Accès limité
    end note
    
    note right of Passive
        Lecture seule
        Paiement requis
    end note
```

---

## 🔐 Diagramme 5: Processus de Validation PBKDF2

Ce diagramme détaille le processus de hashing et validation des mots de passe.

```mermaid
flowchart TD
    subgraph Registration["📝 Inscription (Création Hash)"]
        RegStart[Utilisateur entre password]
        RegGen[Générer Salt aléatoire<br/>16 bytes SecureRandom]
        RegAlgo{API Level?}
        RegAlgo -->|< 26| RegSHA1[PBKDF2-HMAC-SHA1]
        RegAlgo -->|≥ 26| RegSHA256[PBKDF2-HMAC-SHA256]
        RegSHA1 --> RegHash[Hash password<br/>10,000 iterations<br/>256 bits output]
        RegSHA256 --> RegHash
        RegHash --> RegStore[Stocker en DB:<br/>passwordHash + salt]
        
        RegStart --> RegGen
        RegGen --> RegAlgo
    end
    
    subgraph Login["🔑 Login (Validation Hash)"]
        LogStart[Utilisateur entre password]
        LogQuery[Récupérer User de DB<br/>passwordHash + salt]
        LogExtract[Extraire salt stocké]
        LogAlgo{API Level?}
        LogAlgo -->|< 26| LogSHA1[PBKDF2-HMAC-SHA1]
        LogAlgo -->|≥ 26| LogSHA256[PBKDF2-HMAC-SHA256]
        LogSHA1 --> LogHash[Hash password input<br/>avec même salt<br/>10,000 iterations]
        LogSHA256 --> LogHash
        LogHash --> LogCompare{Computed Hash<br/>== Stored Hash?}
        LogCompare -->|Oui| LogSuccess[✅ Login Success]
        LogCompare -->|Non| LogFail[❌ Login Failed]
        
        LogStart --> LogQuery
        LogQuery --> LogExtract
        LogExtract --> LogAlgo
    end
    
    RegStore -.Stockage DB.-> LogQuery
    
    style RegHash fill:#4CAF50,color:#fff
    style LogSuccess fill:#4CAF50,color:#fff
    style LogFail fill:#F44336,color:#fff
    style RegStore fill:#2196F3,color:#fff
```

---

## 🗄️ Diagramme 6: Architecture de Persistance des Données

Ce diagramme montre comment les données sont stockées et sécurisées.

```mermaid
flowchart TD
    subgraph App["Application Layer"]
        VM[AuthViewModel]
        Repo[AuthRepository]
    end
    
    subgraph Secure["🔐 Stockage Sécurisé"]
        SP[SecurePrefs<br/>EncryptedSharedPreferences]
        MK[MasterKey<br/>Android Keystore]
        
        SP -->|Protected by| MK
    end
    
    subgraph Clear["💾 Stockage Local"]
        Room[Room Database<br/>SQLite]
        ASM[AuthStateManager<br/>SharedPreferences]
    end
    
    subgraph Data["Données Stockées"]
        subgraph SecureData["Dans SecurePrefs (Chiffré)"]
            SD1[user_id: String]
            SD2[offline_pseudo: String]
            SD3[offline_hash: String]
            SD4[auth_mode: OFFLINE/ONLINE]
        end
        
        subgraph RoomData["Dans Room DB (Clair)"]
            RD1[User Entity:<br/>- id, pseudo<br/>- passwordHash, salt<br/>- role, gradeLevel<br/>- syncStatus, etc.]
        end
        
        subgraph PrefsData["Dans SharedPreferences (Clair)"]
            PD1[account_type: String]
            PD2[trial_start_date: Long]
            PD3[phone_count_XXX: Int]
        end
    end
    
    VM --> Repo
    Repo --> SP
    Repo --> Room
    Repo --> ASM
    
    SP --> SecureData
    Room --> RoomData
    ASM --> PrefsData
    
    style Secure fill:#FFEBEE
    style Clear fill:#E8F5E9
    style SecureData fill:#FFCDD2
    style RoomData fill:#C8E6C9
    style PrefsData fill:#C8E6C9
```

---

## 🌐 Diagramme 7: Navigation Flow Complete

Ce diagramme montre tous les écrans et leurs connexions de navigation.

```mermaid
flowchart TD
    Splash[Splash Screen<br/>📱]
    Login[Login Screen<br/>🔐]
    Register[Register Screen<br/>📝]
    Home[Home Screen<br/>🏠]
    Quiz[Quiz Flow<br/>📚]
    Subjects[Subjects Screen<br/>📖]
    Chat[Chat Screen<br/>💬<br/>Smarty IA]
    Solver[Problem Solver<br/>🧮]
    Profile[Profile Screen<br/>👤]
    Bilan[Bilan Screen<br/>📊]
    AdminMenu[Admin Menu<br/>⚙️]
    RemoteDash[Remote Dashboard<br/>☁️]
    LocalDB[Local Database<br/>💾]
    
    Splash -->|Authenticated| Home
    Splash -->|Unauthenticated| Login
    
    Login -->|Login Success| Home
    Login -->|S'inscrire| Register
    Login -->|Support| WhatsApp[WhatsApp Support]
    
    Register -->|Register Success| Home
    Register -->|Retour| Login
    
    Home -->|Quiz| Quiz
    Home -->|Matières| Subjects
    Home -->|Smarty IA| Chat
    Home -->|Problem Solver| Solver
    Home -->|Profil| Profile
    Home -->|Admin Button<br/>si role=ADMIN| AdminMenu
    
    Quiz -->|Back| Home
    Subjects -->|Back| Home
    Chat -->|Back| Home
    Solver -->|Back| Home
    
    Profile -->|Bilan| Bilan
    Profile -->|Logout| Login
    Profile -->|Back| Home
    
    Bilan -->|Back| Profile
    
    AdminMenu -->|Remote Dashboard| RemoteDash
    AdminMenu -->|Local Database| LocalDB
    AdminMenu -->|Back| Home
    
    RemoteDash -->|Back| AdminMenu
    LocalDB -->|Back| AdminMenu
    
    style Splash fill:#9C27B0,color:#fff
    style Login fill:#F44336,color:#fff
    style Register fill:#FF9800,color:#fff
    style Home fill:#4CAF50,color:#fff
    style AdminMenu fill:#2196F3,color:#fff
```

---

## 🔄 Diagramme 8: Cycle de Vie AuthState

Ce diagramme montre comment `AuthState` évolue durant le cycle de vie de l'application.

```mermaid
flowchart TD
    Start([App Launch])
    
    Start --> Init[AuthViewModel.init<br/>viewModelScope.launch]
    Init --> Clean[Nettoyage comptes expirés]
    Clean --> CheckPref{SecurePrefs<br/>a userId?}
    
    CheckPref -->|Non| StateUnauth[AuthState =<br/>Unauthenticated]
    CheckPref -->|Oui| QueryUser[Room: getUserById]
    
    QueryUser --> UserExists{User<br/>existe?}
    UserExists -->|Non| StateUnauth
    UserExists -->|Oui| CheckNetwork{Network<br/>disponible?}
    
    CheckNetwork -->|Online| StateAuthOnline[AuthState =<br/>Authenticated<br/>isOffline=false]
    CheckNetwork -->|Offline| StateAuthOffline[AuthState =<br/>Authenticated<br/>isOffline=true]
    
    StateUnauth --> DisplayLogin[UI: LoginScreen]
    StateAuthOnline --> DisplayHome[UI: HomeScreen]
    StateAuthOffline --> DisplayHome
    
    DisplayLogin --> UserLogin[User clique Login]
    UserLogin --> StateLoading[AuthState = Loading]
    StateLoading --> RepoCall[AuthRepository.login]
    
    RepoCall --> LoginResult{Result?}
    LoginResult -->|Success| StateAuthOnline
    LoginResult -->|Failure| StateError[AuthState = Error]
    
    StateError --> UserRetry{User clique<br/>Retry?}
    UserRetry -->|Oui| StateLoading
    UserRetry -->|Non| StateError
    
    DisplayHome --> UserLogout[User clique Logout]
    UserLogout --> ClearSession[SecurePrefs.clearAll<br/>Room: delete?]
    ClearSession --> StateUnauth
    
    style Start fill:#9C27B0,color:#fff
    style StateUnauth fill:#F44336,color:#fff
    style StateLoading fill:#FFC107,color:#000
    style StateError fill:#FF5722,color:#fff
    style StateAuthOnline fill:#4CAF50,color:#fff
    style StateAuthOffline fill:#8BC34A,color:#fff
    style DisplayHome fill:#2196F3,color:#fff
```

---

## 📱 Diagramme 9: Thread Management & Dispatchers

Ce diagramme illustre la gestion des threads et des dispatchers dans le processus d'authentification.

```mermaid
flowchart LR
    subgraph MainThread["🎨 Main Thread (UI)"]
        LoginUI[LoginScreen<br/>Composable]
        StateCollect[collectAsState]
        Recompose[Recomposition UI]
    end
    
    subgraph ViewModelScope["⚙️ ViewModel Scope"]
        VM[AuthViewModel]
        StateFlow[_authState<br/>MutableStateFlow]
    end
    
    subgraph IOThread["💾 IO Dispatcher"]
        RepoLogin[AuthRepository.login]
        DBQuery[UserDao.getUserByPseudo]
        SecureSave[SecurePrefs.save*]
        Crypto[PBKDF2 Hashing]
    end
    
    subgraph DefaultThread["🧮 Default Dispatcher"]
        HeavyCompute[Calculs CPU intensifs<br/>si nécessaire]
    end
    
    LoginUI -->|onClick| VM
    VM -->|launch IO| RepoLogin
    
    RepoLogin --> DBQuery
    RepoLogin --> Crypto
    DBQuery --> SecureSave
    
    Crypto -.si lourd.-> HeavyCompute
    
    SecureSave -->|success| UpdateState[withContext Main]
    UpdateState --> StateFlow
    StateFlow --> StateCollect
    StateCollect --> Recompose
    Recompose -.trigger.-> Navigation[Navigation]
    
    style MainThread fill:#E3F2FD
    style ViewModelScope fill:#F3E5F5
    style IOThread fill:#E8F5E9
    style DefaultThread fill:#FFF3E0
```

---

## 🔒 Diagramme 10: Sécurité Multi-Couches

Ce diagramme montre les différentes couches de sécurité implémentées.

```mermaid
flowchart TB
    subgraph Layer1["🔐 Couche 1: Input Validation"]
        InputVal[Validation côté client<br/>- Pseudo: max 15 chars<br/>- Code: exactly 4 digits]
    end
    
    subgraph Layer2["🔐 Couche 2: Cryptographie"]
        PBKDF2[PBKDF2-HMAC-SHA256<br/>10,000 iterations<br/>Salt unique]
        AES[EncryptedSharedPrefs<br/>AES-256-GCM]
    end
    
    subgraph Layer3["🔐 Couche 3: Storage Protection"]
        Keystore[Android Keystore<br/>Hardware-backed]
        RoomEncrypt[Room DB<br/>Password hashes only]
    end
    
    subgraph Layer4["🔐 Couche 4: Access Control"]
        Limits[Limites:<br/>- Max 3 comptes offline<br/>- Trial 24h<br/>- Thread-safe Mutex]
    end
    
    subgraph Layer5["🔐 Couche 5: Monitoring"]
        Logging[Logging sécurisé<br/>Pas de PII<br/>Crashlytics]
    end
    
    User[👤 Utilisateur] --> InputVal
    InputVal --> PBKDF2
    PBKDF2 --> AES
    AES --> Keystore
    PBKDF2 --> RoomEncrypt
    RoomEncrypt --> Limits
    Limits --> Logging
    
    style Layer1 fill:#FFEBEE
    style Layer2 fill:#FCE4EC
    style Layer3 fill:#F3E5F5
    style Layer4 fill:#EDE7F6
    style Layer5 fill:#E8EAF6
```

---

## 📝 Légende des Symboles

| Symbole | Signification |
|---------|---------------|
| 🚀 | Point de démarrage |
| 📱 | Interface utilisateur |
| 🔐 | Sécurité / Authentification |
| 💾 | Stockage de données |
| 🔄 | Processus en cours |
| ✅ | Succès |
| ❌ | Échec |
| 🧹 | Nettoyage / Maintenance |
| 🔍 | Vérification |
| 👤 | Action utilisateur |
| ⚙️ | Configuration / Settings |
| 🌐 | Navigation |
| 📊 | Analytics / Rapports |
| 💬 | Communication |
| 🧮 | Calculs |

---

## 🎨 Codes Couleurs

- **Vert** (#4CAF50): Succès, états positifs
- **Rouge** (#F44336): Erreurs, états négatifs
- **Orange** (#FF9800): États intermédiaires importants
- **Jaune** (#FFC107): Avertissements, chargement
- **Bleu** (#2196F3): Navigation, destination finale
- **Violet** (#9C27B0): Processus critiques (validation, crypto)

---

**Note**: Pour une meilleure visualisation, ouvrez ce fichier dans :
- GitHub / GitLab (rendu automatique)
- VS Code (avec extension "Markdown Preview Mermaid Support")
- Obsidian
- Typora
- MarkText

---

**Dernière mise à jour**: 2025-12-03  
**Version**: 1.0.0  
**Projet**: EduCam - Bac-X_237
