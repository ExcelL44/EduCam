# ✅ CHECKLIST DÉVELOPPEUR - FAIL-SAFE

> Guide rapide pour garantir que votre code est **incassable**

---

## 🎯 Avant de Coder

- [ ] J'ai lu `FAIL_SAFE_SYSTEM.md`
- [ ] J'ai compris le système de navigation transactionnelle
- [ ] J'ai vu l'exemple dans `ExampleFailSafeViewModel.kt`

---

## 🧭 Navigation

### ✅ À FAIRE

```kotlin
// Dans NavGraph ou Composable
val navigationViewModel: NavigationViewModel = hiltViewModel()

// Navigation simple
navigationViewModel.navigate(NavCommand.NavigateTo("profile"))

// Navigation avec popUpTo
navigationViewModel.navigate(
    NavCommand.NavigateTo(
        route = "home",
        popUpTo = "login",
        inclusive = true
    )
)

// Retour en arrière
navigationViewModel.navigate(NavCommand.PopBack)

// Clear stack
navigationViewModel.navigate(NavCommand.NavigateAndClear("login"))
```

### ❌ À ÉVITER

```kotlin
// ❌ NE JAMAIS faire ça
navController.navigate("profile")
navController.popBackStack()

// ❌ NE JAMAIS appeler directement sans NavigationViewModel
onNavigateToProfile = { navController.navigate("profile") }
```

---

## 🧩 ViewModel

### ✅ À FAIRE

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val failSafe: FailSafeRepositoryHelper
) : FailSafeViewModel<MyState, MyAction>(MyState()) {

    // 1. Implémenter handleActionSafely
    override suspend fun handleActionSafely(action: MyAction) {
        when (action) {
            is MyAction.Load -> loadData()
        }
    }

    // 2. Implémenter createErrorState
    override fun MyState.createErrorState(message: String): MyState {
        return copy(error = message, loading = false)
    }

    // 3. Fonctions privées sans try-catch (déjà protégées!)
    private suspend fun loadData() {
        updateState { copy(loading = true) }
        
        val result = failSafe.executeSafely("loadData") {
            repository.getData()
        }
        
        result.onSuccess { data ->
            updateState { copy(loading = false, data = data) }
        }
    }
}
```

### ❌ À ÉVITER

```kotlin
// ❌ Pas de try-catch manuel (déjà géré)
private suspend fun loadData() {
    try {
        val data = repository.getData() // Risque de timeout
        updateState { copy(data = data) }
    } catch (e: Exception) {
        // Pas besoin, FailSafeViewModel gère automatiquement
    }
}
```

---

## 📦 Repository

### ✅ À FAIRE

```kotlin
@Singleton
class MyRepository @Inject constructor(
    private val api: ApiService,
    private val failSafe: FailSafeRepositoryHelper
) {
    suspend fun getData(): Result<Data> {
        return failSafe.executeSafely(
            operationName = "getData",
            requiresMutex = false, // true si critique
            retries = 2
        ) {
            api.getData()
        }
    }
}
```

### ❌ À ÉVITER

```kotlin
// ❌ Pas de suspend fun qui throw
suspend fun getData(): Data {
    return api.getData() // Peut timeout ou crash
}

// ❌ Pas de try-catch basique
suspend fun getData(): Data? {
    return try {
        api.getData()
    } catch (e: Exception) {
        null // Perte d'information d'erreur
    }
}
```

---

## 🎨 Composable

### ✅ À FAIRE

```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel(),
    navigationViewModel: NavigationViewModel = hiltViewModel()
) {
    // 1. Collecter l'état
    val state by viewModel.uiState.collectAsState()
    
    // 2. Écouter les commandes de navigation du ViewModel
    NavigationCommandHandler(viewModel, navigationViewModel)
    
    // 3. UI en fonction de l'état
    when {
        state.loading -> LoadingIndicator()
        state.error != null -> ErrorMessage(state.error!!)
        else -> Content(state.data)
    }
    
    // 4. Soumettre des actions
    Button(onClick = { viewModel.submitAction(MyAction.Load) }) {
        Text("Charger")
    }
}
```

### ❌ À ÉVITER

```kotlin
// ❌ Pas de LaunchedEffect avec navigation directe
LaunchedEffect(key1 = Unit) {
    navController.navigate("profile") // Race condition possible
}

// ❌ Pas d'appels directs au ViewModel sans action
Button(onClick = { viewModel.loadData() }) { // Public exposure inutile
    Text("Charger")
}
```

---

## 🧪 Tests

### ✅ À FAIRE

```kotlin
@Test
fun `test navigation does not crash on spam`() {
    repeat(100) {
        viewModel.navigate(NavCommand.NavigateTo("profile"))
    }
    // Vérifier qu'une seule navigation a eu lieu
}

@Test
fun `test action rollback on error`() {
    // Given: état initial stable
    val initialState = viewModel.uiState.value
    
    // When: action qui échoue
    viewModel.submitAction(MyAction.FailingAction)
    
    // Then: retour à l'état précédent après 2s
    delay(2500)
    assertEquals(initialState, viewModel.uiState.value)
}
```

---

## 📝 Bonnes Pratiques

### ✅ Navigation

- [ ] Toujours utiliser `NavigationViewModel`
- [ ] Toujours utiliser `NavCommand` (type-safe)
- [ ] Vérifier `canNavigate()` avant navigation critique
- [ ] Utiliser `NavigationCommandHandler` dans les composables

### ✅ ViewModel

- [ ] Hériter de `FailSafeViewModel` pour nouveau code
- [ ] Implémenter `handleActionSafely` sans try-catch
- [ ] Implémenter `createErrorState` pour gestion d'erreur
- [ ] Utiliser `submitAction()` pour toutes les actions
- [ ] Pas de logique métier dans les composables

### ✅ Repository

- [ ] Toujours wrapper avec `failSafe.executeSafely()`
- [ ] Retourner `Result<T>` pour gestion d'erreur propre
- [ ] Utiliser `requiresMutex = true` pour opérations critiques
- [ ] Configurer `retries` selon l'importance de l'opération

### ✅ Composable

- [ ] Collecter l'état avec `collectAsState()`
- [ ] Utiliser `NavigationCommandHandler` si ViewModel émet NavCommands
- [ ] UI réactive à l'état (loading/error/success)
- [ ] Pas de logique métier, seulement affichage

---

## 🚨 Red Flags

### ⚠️ Patterns Dangereux

```kotlin
// 🚫 DANGER 1 : Navigation directe
navController.navigate("screen")

// 🚫 DANGER 2 : Try-catch manuel inutile
try { 
    viewModel.submitAction(action) 
} catch (e: Exception) { }

// 🚫 DANGER 3 : Logique dans composable
Button(onClick = { 
    val data = repository.getData() // ❌
    viewModel.updateState(data)
})

// 🚫 DANGER 4 : Suspend fun qui throw
suspend fun getData(): Data {
    return api.getData() // Peut crash
}

// 🚫 DANGER 5 : État mutable direct
var myState = MutableStateFlow(...) // Dans composable
```

---

## ✅ Review Checklist

Avant de commit :

- [ ] Aucun `navController.navigate()` direct
- [ ] Aucun `try-catch` manuel dans ViewModel
- [ ] Tous les repository retournent `Result<T>`
- [ ] Tous les ViewModels héritent de `FailSafeViewModel` ou `BaseViewModel`
- [ ] Aucune logique métier dans les composables
- [ ] Tests passent (si existants)
- [ ] StrictMode : 0 violations en DEBUG
- [ ] Code lint : 0 erreurs

---

## 🎓 Ressources

- **Architecture** : `FAIL_SAFE_SYSTEM.md`
- **Navigation** : `NAVIGATION_SYSTEM.md`
- **Tests** : `STRESS_TEST_GUIDE.md`
- **Exemple** : `ExampleFailSafeViewModel.kt`
- **Résumé** : `README_FAIL_SAFE.md`

---

## 🏆 Gold Standard

Ton code est **GOLD** si :

1. ✅ NavigationViewModel pour toute navigation
2. ✅ FailSafeViewModel pour nouvelle logique métier
3. ✅ FailSafeRepositoryHelper pour I/O
4. ✅ Result<T> partout
5. ✅ Pas de try-catch manuel
6. ✅ Pas de logique dans composables
7. ✅ Tests de stress passent (7/7)
8. ✅ StrictMode clean
9. ✅ LeakCanary 0 leaks
10. ✅ Code review approuvé

---

**Imprime cette checklist et garde-la à côté de ton clavier! 📌**
