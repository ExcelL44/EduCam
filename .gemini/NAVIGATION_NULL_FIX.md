# 🔧 FIX: Navigation Rejetée (NavController null)

**Date**: 2025-11-30  
**Problème**: `NavigationViewModel` rejette toutes les navigations avec l'erreur `NavController null`  
**Statut**: ✅ **RÉSOLU**  

---

## 🔴 PROBLÈME IDENTIFIÉ

### Symptôme
Logs : `❌ Navigation rejetée (NavController null): NavigateTo(...)`

### Cause Racine : Race Condition

1. `MainActivity` crée le `NavController`.
2. `NavGraph` est composé et planifie un `LaunchedEffect` pour attacher le controller au VM.
3. `SplashScreen` (écran de démarrage) est composé **immédiatement**.
4. `SplashScreen` décide de naviguer (ex: user déjà connecté).
5. **CRASH** : La navigation est demandée **AVANT** que le `LaunchedEffect` de `NavGraph` n'ait eu le temps de s'exécuter.
6. Résultat : `navController` est `null` dans le VM au moment de l'appel.

---

## ✅ SOLUTION APPLIQUÉE

### 1. Injection et Attachement Précoce (`MainActivity.kt`)

Nous avons déplacé l'injection du `NavigationViewModel` et l'attachement du controller directement dans `MainActivity`, en utilisant `SideEffect` (exécuté après chaque recomposition réussie, mais avant les effets lancés).

```kotlin
val navigationViewModel: NavigationViewModel = hiltViewModel()
val navController = rememberNavController()

// ✅ Attachement IMMÉDIAT (synchrone avec la composition)
SideEffect {
    navigationViewModel.setNavController(navController)
}
```

### 2. Passage de l'Instance (`MainActivity.kt` -> `NavGraph.kt`)

Nous passons maintenant l'instance **déjà initialisée** de `NavigationViewModel` au `NavGraph`.

```kotlin
NavGraph(
    navController = navController,
    navigationViewModel = navigationViewModel // ✅ Instance avec controller attaché
)
```

### 3. Nettoyage (`NavGraph.kt`)

Suppression du `LaunchedEffect` redondant et tardif dans `NavGraph`.

---

## 🧪 VÉRIFICATION

1. **Lancer l'app**
2. Si connecté : Splash -> Home (Succès ✅)
3. Si non connecté : Splash -> Login (Succès ✅)
4. Depuis Home : Navigation vers Quiz/Sujets (Succès ✅)

Plus aucune erreur `NavController null` ne devrait apparaître dans les logs.
