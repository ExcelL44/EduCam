# 🎨 Guide d'Utilisation des Polices - Bac-X_237

## ✅ Règle Simple

```
Exo2 (BacXBrandFont)  → UNIQUEMENT "Bac-X_237" 
Inter (automatique)   → TOUT LE RESTE
```

---

## 📝 Examples de Code

### ✅ CORRECT - Branding "Bac-X_237"

```kotlin
// LoginScreen.kt, RegisterScreen.kt, etc.
Text(
    text = "Bac-X_237",
    style = MaterialTheme.typography.displayMedium.copy(
        fontFamily = BacXBrandFont  // ⚠️ Exception unique
    ),
    color = MaterialTheme.colorScheme.primary
)
```

### ✅ CORRECT - Tout le reste du contenu

```kotlin
// Titres
Text(
    text = "Bienvenue",
    style = MaterialTheme.typography.headlineLarge
    // Inter appliquée automatiquement ✅
)

// Corps de texte
Text(
    text = "Voici une longue explication pédagogique...",
    style = MaterialTheme.typography.bodyLarge
    // Inter appliquée automatiquement ✅
)

// Boutons
Button(onClick = { /* ... */ }) {
    Text("Se connecter")
    // Inter appliquée automatiquement ✅
}

// Labels
Text(
    text = "Email",
    style = MaterialTheme.typography.labelMedium
    // Inter appliquée automatiquement ✅
)
```

### ❌ INCORRECT - N'utilisez PAS BacXBrandFont ailleurs

```kotlin
// ❌ NE FAITES PAS ÇA
Text(
    text = "Contenu normal",
    style = MaterialTheme.typography.bodyLarge.copy(
        fontFamily = BacXBrandFont  // ❌ MAUVAIS
    )
)
```

---

## 🎯 Styles Typographiques Disponibles

### Titres Principaux (Display)
```kotlin
MaterialTheme.typography.displayLarge   // 57sp, Bold
MaterialTheme.typography.displayMedium  // 45sp, Bold
MaterialTheme.typography.displaySmall   // 36sp, Bold
```
**Usage**: Titres d'écran, splash screen

### Titres de Section (Headline)
```kotlin
MaterialTheme.typography.headlineLarge  // 32sp, SemiBold
MaterialTheme.typography.headlineMedium // 28sp, SemiBold
MaterialTheme.typography.headlineSmall  // 24sp, SemiBold
```
**Usage**: Sections principales, catégories

### Titres de Composants (Title)
```kotlin
MaterialTheme.typography.titleLarge     // 22sp, SemiBold
MaterialTheme.typography.titleMedium    // 16sp, Medium
MaterialTheme.typography.titleSmall     // 14sp, Medium
```
**Usage**: Cartes, listes, toolbars

### 📖 Corps de Texte (Body) - LECTURE INTENSIVE
```kotlin
MaterialTheme.typography.bodyLarge      // 16sp, Normal, 26sp line-height
MaterialTheme.typography.bodyMedium     // 14sp, Normal, 22sp line-height
MaterialTheme.typography.bodySmall      // 12sp, Normal, 18sp line-height
```
**Usage**: Paragraphes, explications, contenu pédagogique
**Optimisé pour**: Longues sessions de lecture

### Labels et Boutons
```kotlin
MaterialTheme.typography.labelLarge     // 14sp, Medium
MaterialTheme.typography.labelMedium    // 12sp, Medium
MaterialTheme.typography.labelSmall     // 11sp, Medium
```
**Usage**: Boutons, chips, badges

---

## 🎨 Quand Utiliser Quel Style

### Écran de Login
```kotlin
// Logo "Bac-X_237"
Text("Bac-X_237", style = MaterialTheme.typography.displayMedium.copy(
    fontFamily = BacXBrandFont
))

// Slogan
Text("Révision rapide et intelligente", style = MaterialTheme.typography.bodyMedium)

// Labels de formulaire
Text("Pseudo", style = MaterialTheme.typography.labelMedium)

// Boutons
Button { Text("Se connecter", style = MaterialTheme.typography.labelLarge) }
```

### Écran de Quiz
```kotlin
// Question
Text("Quelle est la dérivée de x² ?", style = MaterialTheme.typography.headlineSmall)

// Réponses
Text("2x", style = MaterialTheme.typography.bodyLarge)

// Explication
Text("La dérivée de x^n est nx^(n-1)...", style = MaterialTheme.typography.bodyMedium)

// Timer/Score
Text("0:45", style = MaterialTheme.typography.titleMedium)
```

### Écran de Chat IA (Smarty)
```kotlin
// Messages utilisateur et IA
Text(message.content, style = MaterialTheme.typography.bodyLarge)

// Indicateur de confiance
Text("Confiance: 85%", style = MaterialTheme.typography.bodySmall)

// Indicateur "Smarty écrit..."
Text("Smarty écrit...", style = MaterialTheme.typography.bodySmall)
```

### Écran de Profil
```kotlin
// Nom d'utilisateur
Text(user.name, style = MaterialTheme.typography.headlineMedium)

// Statistiques (titres)
Text("Quiz réussis", style = MaterialTheme.typography.labelMedium)

// Statistiques (valeurs)
Text("42", style = MaterialTheme.typography.titleLarge)

// Sections
Text("Paramètres", style = MaterialTheme.typography.titleMedium)
```

---

## 🔍 Vérification Rapide

### ✅ Checklist

Avant de merger votre code, vérifiez :

- [ ] Le label "Bac-X_237" utilise `BacXBrandFont`
- [ ] TOUS les autres textes utilisent `MaterialTheme.typography.xxx`
- [ ] Aucun `fontFamily = BacXBrandFont` ailleurs
- [ ] Les formules mathématiques sont lisibles (I, l, 1, O, 0 distincts)
- [ ] Le texte long est confortable à lire (bodyLarge recommandé)

---

## 🎯 Résumé Visuel

```
┌─────────────────────────────────────┐
│         Bac-X_237               ← Exo2 (BacXBrandFont)
│   Révision intelligente         ← Inter (auto)
├─────────────────────────────────────┤
│                                     │
│  ┌───────────────────────────┐     │
│  │ Titre de Section      ← Inter   │
│  │                             │    │
│  │ Lorem ipsum dolor sit  ← Inter  │
│  │ amet, consectetur...       │    │
│  │                             │    │
│  │ [Bouton Action] ← Inter    │    │
│  └───────────────────────────┘     │
│                                     │
└─────────────────────────────────────┘
   Tout en Inter sauf le logo
```

---

## 🚀 Build et Test

```bash
# Rebuild complet
.\gradlew clean :app:assembleDebug

# Installer
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Vérifier visuellement
# 1. Logo "Bac-X_237" = Exo2 (futuriste)
# 2. Tout le reste = Inter (lisible, confortable)
```

---

## 💡 Tips

### Pour texte très long (cours, explications)
```kotlin
Text(
    text = longText,
    style = MaterialTheme.typography.bodyLarge.copy(
        lineHeight = 28.sp  // Encore plus d'espace si besoin
    )
)
```

### Pour formules mathématiques
```kotlin
Text(
    text = "f(x) = x² + 2x + 1",
    style = MaterialTheme.typography.bodyLarge,
    fontFeatureSettings = "tnum"  // Chiffres tabulaires
)
```

### Pour code/monospace (si nécessaire)
```kotlin
// Si vous affichez du code, utilisez une police monospace
Text(
    text = "function solve() { ... }",
    style = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = FontFamily.Monospace
    )
)
```

---

## ✅ Conclusion

**Une seule règle à retenir** :
- Si c'est "Bac-X_237" → `BacXBrandFont`
- Sinon → `MaterialTheme.typography.xxx` (Inter automatique)

**C'est tout !** 🎉
