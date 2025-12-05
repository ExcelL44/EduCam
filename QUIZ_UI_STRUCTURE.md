# 📐 STRUCTURE UI COMPLÈTE DU QUIZ

**Date:** 2025-12-05T17:25:43+01:00  
**Version:** 1.0  
**Système:** EduCam Quiz Module

---

## 🎨 VUE D'ENSEMBLE DE L'UI

### Architecture UI (State Machine)
```
┌─────────────┐
│    START    │
└──────┬──────┘
       ↓
┌──────────────────────────────────────┐
│  QuizFlowCoordinator (State Manager) │
│  ┌────────────────────────────────┐  │
│  │  QuizStep Enum State Machine   │  │
│  │  • MENU                         │  │
│  │  • CONFIGURATION                │  │
│  │  • EXECUTION                    │  │
│  │  • EVALUATION                   │  │
│  │  • RESULTS                      │  │
│  └────────────────────────────────┘  │
└──────────────────────────────────────┘
```

---

## 🏗️ COMPOSANTS UI PAR ÉTAPE

### **ÉTAPE 1: MENU (QuizMenuScreen)**

**Fichier:** `QuizMenuScreen.kt`  
**Rôle:** Point d'entrée - Sélection du mode de quiz

#### 📱 Structure Visuelle
```
┌─────────────────────────────────────┐
│ ← Quiz                        [Top] │
├─────────────────────────────────────┤
│                                     │
│    Choisissez votre mode de quiz    │
│                                     │
│  ┌───────────────────────────────┐  │
│  │ ⚡ 48dp                        │  │
│  │ Quiz Rapide ⚡                 │  │
│  │ 10 questions · Temps limité   │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │ ⏱️ 48dp                        │  │
│  │ Quiz Approfondi 🎯            │  │
│  │ 20 questions · Plus de temps  │  │
│  └───────────────────────────────┘  │
│                                     │
└─────────────────────────────────────┘
```

#### 🧩 Composants
```kotlin
QuizMenuScreen(
    onModeSelected: (QuizModeType) -> Unit,
    onNavigateBack: () -> Unit
)
├─ Scaffold
│  ├─ TopAppBar
│  │  ├─ Title: "Quiz"
│  │  └─ NavigationIcon: ArrowBack
│  └─ Content: Column (centered)
│     ├─ Text: "Choisissez votre mode de quiz"
│     │  └─ Typography: headlineMedium
│     ├─ Spacer(48.dp)
│     ├─ QuizModeCard (FAST)
│     │  ├─ Icon: Speed (48dp)
│     │  ├─ Title: "Quiz Rapide ⚡"
│     │  └─ Description: "10 questions · Temps limité"
│     ├─ Spacer(24.dp)
│     └─ QuizModeCard (SLOW)
│        ├─ Icon: AccessTime (48dp)
│        ├─ Title: "Quiz Approfondi 🎯"
│        └─ Description: "20 questions · Plus de temps"
```

#### 🎨 QuizModeCard (Composant Interne)
```kotlin
Card (cliquable, elevation 4dp)
├─ Row (padding 24dp)
│  ├─ Icon (48dp, tint primary)
│  ├─ Spacer(16dp)
│  └─ Column
│     ├─ Text: title (titleLarge)
│     ├─ Spacer(4dp)
│     └─ Text: description (bodyMedium, onSurfaceVariant)
```

#### 📊 États & Données
- **QuizModeType Enum:** `FAST`, `SLOW`
- **Layout:** Center vertical/horizontal
- **Interactions:** Click sur Card → Navigation

---

### **ÉTAPE 2: CONFIGURATION (QuizConfigScreen)**

**Fichier:** `QuizConfigScreen.kt`  
**Rôle:** Paramétrage du quiz (matière + temps)

#### 📱 Structure Visuelle
```
┌─────────────────────────────────────┐
│ ← Configuration du Quiz       [Top] │
├─────────────────────────────────────┤
│ ┌─────────────────────────────────┐ │
│ │ Mode sélectionné                │ │
│ │ ⚡ Quiz Rapide / 🎯 Approfondi  │ │
│ │ 10 questions / 20 questions     │ │
│ └─────────────────────────────────┘ │
│                                     │
│ Choisissez une matière              │
│ ┌─────────────────────────────────┐ │
│ │ ✓ 📐 Mathématiques         [►]  │ │ ← Selected
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │   ⚛️ Physique                   │ │
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │   🧪 Chimie                     │ │
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │   🧬 Biologie            [🔒]   │ │ ← Locked
│ │   Bientôt disponible            │ │
│ └─────────────────────────────────┘ │
│                                     │
│ Temps par question                  │
│ 30 secondes         [-10s] [+10s]   │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │   ► Démarrer le Quiz            │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

#### 🧩 Composants
```kotlin
QuizConfigScreen(
    mode: QuizModeType,
    availableSubjects: List<SubjectInfo>,
    onStartQuiz: (subject, time) -> Unit,
    onNavigateBack: () -> Unit
)
├─ Scaffold
│  ├─ TopAppBar
│  │  ├─ Title: "Configuration du Quiz"
│  │  └─ NavigationIcon: ArrowBack
│  └─ Content: Column
│     ├─ Card (Mode Selected)
│     │  ├─ backgroundColor: primaryContainer
│     │  └─ Content
│     │     ├─ "Mode sélectionné" (labelMedium)
│     │     ├─ "⚡ Quiz Rapide" / "🎯 Approfondi" (titleLarge)
│     │     └─ "10 questions" / "20 questions" (bodyMedium)
│     ├─ Spacer(24.dp)
│     ├─ Text: "Choisissez une matière" (titleMedium)
│     ├─ Spacer(12.dp)
│     ├─ LazyColumn (weight 1f)
│     │  └─ items(availableSubjects)
│     │     └─ SubjectSelectionCard
│     ├─ Spacer(16.dp)
│     ├─ Text: "Temps par question" (titleMedium)
│     ├─ Spacer(8.dp)
│     ├─ Row (Time Controls)
│     │  ├─ Text: "$timePerQuestion secondes"
│     │  └─ Row
│     │     ├─ FilledIconButton("-10s")
│     │     ├─ Spacer(8.dp)
│     │     └─ FilledIconButton("+10s")
│     ├─ Spacer(24.dp)
│     └─ Button: "Démarrer le Quiz"
│        ├─ Icon: PlayArrow
│        └─ enabled: selectedSubject != null
```

#### 🎨 SubjectSelectionCard (Composant Interne)
```kotlin
Card (cliquable)
├─ backgroundColor:
│  • surfaceVariant (si !isAvailable)
│  • primaryContainer (si isSelected)
│  • surface (sinon)
├─ border: 2dp primary (si isSelected)
└─ Row (padding 16dp)
   ├─ Column (weight 1f)
   │  ├─ Text: displayName (titleMedium)
   │  │  • Color varie selon état
   │  └─ Text: "Bientôt disponible" (si !isAvailable)
   └─ Icon
      • PlayArrow (si isSelected, tint primary)
      • Lock (si !isAvailable, tint onSurfaceVariant)
```

#### 📊 États & Données
```kotlin
// State variables
var selectedSubject: String? = null
var timePerQuestion: Int = 30 (FAST) / 60 (SLOW)

// SubjectInfo Data Class
data class SubjectInfo(
    val name: String,           // "Mathématiques"
    val displayName: String,    // "📐 Mathématiques"
    val isAvailable: Boolean    // true/false
)

// Available Subjects (hardcoded)
- Mathématiques ✅
- Physique ✅
- Chimie ✅
- Biologie ❌
- Histoire ❌
- Géographie ❌
- Français ❌
- Anglais ❌
- Philosophie ❌
```

#### 🎯 Logique d'Interaction
1. **Sélection Matière:** Click sur Card → `selectedSubject = name` (si available)
2. **Ajustement Temps:** `-10s` / `+10s` (limites: 10-180s)
3. **Validation:** Button enabled seulement si matière sélectionnée
4. **Démarrage:** `onStartQuiz(subject, time)`

---

### **ÉTAPE 3: EXÉCUTION (QuizScreen)**

**Fichier:** `QuizScreen.kt`  
**Rôle:** Affichage et interaction avec les questions

#### 📱 Structure Visuelle
```
┌─────────────────────────────────────┐
│ [══════════  ] 45s   [Annuler]      │ ← Timer Bar
├─────────────────────────────────────┤
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ WebView Content Area            │ │
│ │                                 │ │
│ │ <h3>Théorème de Pythagore</h3>  │ │
│ │ <p>Dans un triangle rectangle   │ │
│ │ si les cathètes mesurent 3 cm   │ │
│ │ et 4 cm, l'hypoténuse mesure:   │ │
│ │                                 │ │
│ │ [Image SVG si présente]         │ │
│ │                                 │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌──────────────┬──────────────┐    │
│ │ ○ 5 cm       │ ○ 7 cm       │    │ ← Answer Grid 2x2
│ └──────────────┴──────────────┘    │
│ ┌──────────────┬──────────────┐    │
│ │ ○ 1 cm       │ ○ 12 cm      │    │
│ └──────────────┴──────────────┘    │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ ➡️ Question suivante            │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

#### 🧩 Composants
```kotlin
QuizScreen(
    question: QuizQuestion,
    onAnswerSelected: (Int) -> Unit,
    onNextClicked: () -> Unit,
    onCancelQuiz: () -> Unit
)
├─ Column (fillMaxSize)
│  ├─ Row (Top Bar)
│  │  ├─ TimerBar (weight 1f)
│  │  │  └─ LinearProgressIndicator
│  │  │     • progress: currentSeconds / totalSeconds
│  │  │     • color: error si > 70%, sinon primary
│  │  │     • height: 4dp
│  │  ├─ Spacer(16.dp)
│  │  └─ OutlinedButton: "Annuler"
│  │     ├─ Icon: Close (18dp)
│  │     └─ Text: "Annuler"
│  ├─ Spacer(16.dp)
│  ├─ WebViewContent (weight 1f)
│  │  └─ AndroidView<WebView>
│  │     • JavaScript enabled
│  │     • KaTeX rendering
│  │     • Zoom controls
│  ├─ Spacer(24.dp)
│  ├─ AnswerGrid
│  │  └─ Column
│  │     └─ For each row of 2 answers
│  │        └─ Row
│  │           ├─ AnswerButton (weight 1f)
│  │           ├─ Spacer(16.dp) si 2 items
│  │           └─ AnswerButton (weight 1f)
│  ├─ Spacer(24.dp)
│  └─ PrimaryButton: "➡️ Question suivante"
│     • enabled: selectedAnswer != -1
```

#### 🎨 WebViewContent (Composant)
```kotlin
AndroidView<WebView>
├─ JavaScript: true
├─ Libraries:
│  └─ KaTeX 0.16.9
│     ├─ katex.min.css
│     ├─ katex.min.js
│     ├─ auto-render.min.js
│     └─ mhchem.min.js (chimie)
├─ Delimiters:
│  ├─ $$ ... $$ (display mode)
│  └─ $ ... $ (inline mode)
└─ CSS:
   ├─ Font: Inter, sans-serif
   ├─ Padding: 16px
   ├─ Line-height: 1.6
   └─ Color: #1A1A1A
```

#### 🎨 AnswerButton (Composant)
```kotlin
Card (onClick, height 64dp)
├─ backgroundColor (animated):
│  • Green #4CAF50 (si showFeedback && isCorrect)
│  • Red #F44336 (si showFeedback && isSelected && !isCorrect)
│  • secondaryContainer (si isSelected)
│  • surface (sinon)
├─ elevation: 4dp (si isSelected) / 1dp (sinon)
└─ Row
   ├─ RadioButton (selected, disabled click)
   ├─ Spacer(12.dp)
   └─ Text: answer text
      • Color: White (si feedback correct/incorrect)
      • Color: onSurface (sinon)
```

#### 📊 États & Données
```kotlin
// QuizQuestion (UI Model)
data class QuizQuestion(
    val id: String,
    val content: String,        // HTML avec formules
    val answers: List<Answer>,  // 2-4 réponses
    val correctAnswerIndex: Int,
    val timeSpent: Int = 0,
    val subject: String,
    val difficulty: String
)

// Answer
data class Answer(
    val text: String,
    val id: String = ""
)

// Local State
var selectedAnswer: Int = -1
var showFeedback: Boolean = false
var showCancelDialog: Boolean = false
```

#### 🎯 Logique d'Interaction
1. **Sélection:** Click answer → `selectedAnswer = index`, `showFeedback = true`
2. **Validation:** Click "Suivant" → `onNextClicked()`, reset state
3. **Annulation:** Click "Annuler" → Dialog confirmation
4. **Timer:** Progress bar décrémente visuellement
5. **Feedback Visuel:** Animation couleur (vert/rouge) selon correction

---

### **ÉTAPE 4: ÉVALUATION (QuizEvaluationScreen)**

**Fichier:** `QuizEvaluationScreen.kt`  
**Rôle:** Analyse détaillée question par question

#### 📱 Structure Visuelle
```
┌─────────────────────────────────────┐
│ ┌─────────────────────────────────┐ │
│ │         🏆                       │ │
│ │    Score : 80%                   │ │
│ │    8 / 10 réponses correctes     │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ 📊 Statistiques                  │ │
│ │ Mode            ⚡ Rapide        │ │
│ │ Temps total     5min 23s         │ │
│ │ Temps moyen/Q   32s              │ │
│ │ Bonnes          8                │ │
│ │ Mauvaises       2                │ │
│ └─────────────────────────────────┘ │
│                                     │
│ 📝 Analyse détaillée                │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Question 1           ✓ 45s      │ │ ← Correct (green tint)
│ │ Mathématiques                   │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Question 2           ✗ 12s      │ │ ← Incorrect (red tint)
│ │ Physique                         │ │
│ │ ┌─────────────────────────────┐ │ │
│ │ │ ℹ️ Bonne réponse            │ │ │
│ │ │ Option A                    │ │ │
│ │ └─────────────────────────────┘ │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │      Continuer                  │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

#### 🧩 Composants
```kotlin
QuizEvaluationScreen(
    questions: List<QuizQuestion>,
    userAnswers: List<Int>,
    timeSpentPerQuestion: List<Int>,
    mode: QuizModeType,
    onContinue: () -> Unit
)
└─ LazyColumn (screenPadding)
   ├─ Item: Score Card
   │  ├─ backgroundColor: varie selon score
   │  │  • primaryContainer (≥80%)
   │  │  • tertiaryContainer (≥50%)
   │  │  • errorContainer (<50%)
   │  └─ Content
   │     ├─ Text: emoji (displayLarge)
   │     ├─ Text: "Score : X%" (headlineLarge)
   │     └─ Text: "X / Y réponses correctes" (titleMedium)
   ├─ Spacer(16.dp)
   ├─ Item: Statistics Card
   │  └─ Column
   │     ├─ Text: "📊 Statistiques" (titleLarge)
   │     ├─ StatRow("Mode", "⚡ Rapide")
   │     ├─ StatRow("Temps total", "5min 23s")
   │     ├─ StatRow("Temps moyen/question", "32s")
   │     ├─ StatRow("Bonnes réponses", "8")
   │     └─ StatRow("Mauvaises réponses", "2")
   ├─ Item: Section Header
   │  └─ Text: "📝 Analyse détaillée" (titleLarge)
   ├─ itemsIndexed(questions)
   │  └─ QuestionResultCard
   └─ Item: Button
      └─ Button: "Continuer" (fillMaxWidth)
```

#### 🎨 QuestionResultCard (Composant)
```kotlin
Card
├─ backgroundColor (alpha 0.3):
│  • primaryContainer (si correct)
│  • errorContainer (si incorrect)
└─ Column (padding 16dp)
   ├─ Row (SpaceBetween)
   │  ├─ Text: "Question X" (titleMedium)
   │  └─ Row
   │     ├─ Icon: CheckCircle / Cancel
   │     │  • tint: primary / error
   │     ├─ Spacer(4.dp)
   │     └─ Text: "Xs" (bodySmall)
   ├─ Spacer(8.dp)
   ├─ Text: subject (labelMedium, primary)
   └─ Si incorrect:
      └─ Surface (shape small, surface color)
         └─ Column (padding 12dp)
            ├─ Row
            │  ├─ Icon: Info (16dp, primary)
            │  ├─ Spacer(4.dp)
            │  └─ Text: "Bonne réponse" (labelSmall)
            ├─ Spacer(4.dp)
            └─ Text: correct answer (bodyMedium, primary)
```

#### 🎨 StatRow (Composant Utilitaire)
```kotlin
Row (fillMaxWidth, padding vertical 4dp)
├─ Text: label (bodyMedium, onSurfaceVariant)
└─ Text: value (bodyMedium)
```

#### 📊 Calculs
```kotlin
// Score percentage
val scorePercentage = (correctCount * 100) / totalQuestions

// Total time
val totalTimeSpent = timeSpentPerQuestion.sum()

// Average time
val averageTime = totalTimeSpent / totalQuestions

// Correct count
val correctCount = questions.indices.count { i ->
    userAnswers[i] == questions[i].correctAnswerIndex
}
```

#### 🎨 Emoji Mapping (Score)
```kotlin
≥90% → "🏆" (Trophée)
≥80% → "🎉" (Fête)
≥70% → "👍" (Pouce)
≥50% → "😊" (Sourire)
≥30% → "😐" (Neutre)
<30% → "💪" (Courage)
```

---

### **ÉTAPE 5: RÉSULTATS (QuizResultsScreen)**

**Fichier:** `QuizResultsScreen.kt`  
**Rôle:** Résumé final avec options de navigation

#### 📱 Structure Visuelle (Dialog + Background)

##### QuizResultsDialog (Popup)
```
┌─────────────────────────────────────┐
│            🏆                        │
│                                     │
│        Quiz Terminé !               │
│                                     │
│         Score                       │
│          85%                        │
│    17 / 20 réponses correctes       │
│                                     │
│  Très bon travail !                 │
│  Continuez comme ça !               │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ 📊 Voir le bilan détaillé       │ │ ← Button
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │ 🔄 Refaire ce quiz              │ │ ← OutlinedButton
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │ 🏠 Nouveau quiz                 │ │ ← OutlinedButton
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │      Fermer                     │ │ ← TextButton
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

##### Background Screen
```
┌─────────────────────────────────────┐
│                                     │
│          (Centered)                 │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │         🏆                       │ │
│ │    Quiz Terminé !               │ │
│ │         85%                     │ │
│ │  17 / 20 réponses correctes     │ │
│ │                                 │ │
│ │ [███████████░░░░░░░░] 85%       │ │ ← ProgressBar
│ │                                 │ │
│ │ Session ID: a3f9b2c1...         │ │
│ └─────────────────────────────────┘ │
│                                     │
│ Les résultats ont été enregistrés   │
│                                     │
└─────────────────────────────────────┘
```

#### 🧩 Composants

##### QuizResultsDialog
```kotlin
AlertDialog
├─ icon: Text(emoji, displayLarge)
├─ title: Text("Quiz Terminé !", centered)
├─ text: Column
│  ├─ Text: "Score" (labelMedium, onSurfaceVariant)
│  ├─ Text: "X%" (displayMedium)
│  │  └─ color selon score (primary/tertiary/error)
│  ├─ Text: "X / Y réponses correctes" (bodyMedium)
│  ├─ Spacer(16.dp)
│  └─ Text: encouragement message (bodyMedium, centered)
└─ confirmButton: Column (fillMaxWidth, spacing 8dp)
   ├─ Button: "📊 Voir le bilan détaillé"
   │  └─ Icon: Assessment
   ├─ OutlinedButton: "🔄 Refaire ce quiz"
   │  └─ Icon: Refresh
   ├─ OutlinedButton: "🏠 Nouveau quiz"
   │  └─ Icon: Home
   └─ TextButton: "Fermer"
```

##### QuizResultsScreen (Background)
```kotlin
Column (centered)
└─ Card (fillMaxWidth)
   ├─ backgroundColor selon score
   └─ Column (padding 32dp, centered)
      ├─ Text: emoji (displayLarge)
      ├─ Spacer(16.dp)
      ├─ Text: "Quiz Terminé !" (headlineLarge)
      ├─ Spacer(8.dp)
      ├─ Text: "X%" (displayLarge)
      │  └─ color selon score
      ├─ Text: "X / Y réponses correctes" (titleMedium)
      ├─ Spacer(16.dp)
      ├─ LinearProgressIndicator (fillMaxWidth, height 8dp)
      ├─ Spacer(16.dp)
      └─ Text: "Session ID: ..." (bodySmall, onSurfaceVariant)
```

#### 📊 États & Logique
```kotlin
var showDialog: Boolean = true

// Dialog Actions
onViewBilan    → showDialog = false, navigate to EVALUATION
onRetakeQuiz   → showDialog = false, restart quiz
onNewQuiz      → showDialog = false, navigate to MENU
onDismiss      → showDialog = false, navigate to HOME
```

#### 🎨 Messages d'Encouragement
```kotlin
≥90% → "Excellent ! Vous maîtrisez parfaitement ce sujet !"
≥80% → "Très bon travail ! Continuez comme ça !"
≥70% → "Bon résultat ! Quelques révisions et ce sera parfait !"
≥50% → "Pas mal ! Il y a de la marge de progression !"
≥30% → "Continuez à vous entraîner, la réussite viendra !"
<30% → "Ne vous découragez pas ! Chaque tentative vous rapproche du succès !"
```

---

## 🎨 DESIGN SYSTEM

### Couleurs Thématiques
```kotlin
Score Colors:
├─ ≥80%: primaryContainer
├─ ≥50%: tertiaryContainer
└─ <50%: errorContainer

Feedback Colors:
├─ Correct: #4CAF50 (Green)
└─ Incorrect: #F44336 (Red)

State Colors:
├─ Selected: secondaryContainer
├─ Locked: surfaceVariant
└─ Default: surface
```

### Typographie
```kotlin
displayLarge     → Emojis, scores principaux
displayMedium    → Scores dialog
headlineLarge    → Titres principaux
headlineMedium   → Titres sections
titleLarge       → Titres cards
titleMedium      → Sous-titres
bodyLarge        → Texte principal
bodyMedium       → Descriptions
bodySmall        → Metadata
labelMedium      → Labels
labelSmall       → Hints
```

### Espacements
```kotlin
Padding Standard:
├─ Screen: screenPadding()
├─ Card: 16dp / 24dp / 32dp
├─ Row/Column internal: 8dp / 12dp / 16dp
└─ Components spacing: 24dp / 48dp

Sizes:
├─ Icon: 16dp / 24dp / 48dp
├─ Button height: 48dp / 64dp
├─ Timer bar: 4dp
└─ Progress bar: 8dp
```

### Élévations
```kotlin
Card Elevations:
├─ Default: 1dp
├─ Selected: 4dp
└─ Modal: 8dp
```

---

## 🔄 NAVIGATION FLOW

```
START
  ↓
MENU (QuizMenuScreen)
  ↓ Select Mode (FAST/SLOW)
CONFIG (QuizConfigScreen)
  ↓ Select Subject + Time
EXECUTION (QuizScreen)
  ↓ Complete All Questions
  │
  ├─ → EVALUATION (QuizEvaluationScreen)
  │    ↓ Click "Continuer"
  │    RESULTS (QuizResultsScreen + Dialog)
  │    ↓
  │    ├─ "Voir bilan" → EVALUATION
  │    ├─ "Refaire" → EXECUTION (restart)
  │    ├─ "Nouveau" → MENU
  │    └─ "Fermer" → HOME
  │
  └─ "Annuler" → Confirmation Dialog → HOME
```

---

## 📱 RESPONSIVE DESIGN

### Grid Layout (Answers)
```kotlin
// Grille 2x2 automatique
answers.chunked(2).forEach { row ->
    Row {
        row.forEach { answer ->
            Box(Modifier.weight(1f)) {
                AnswerButton(answer)
            }
        }
        // Si 1 seul item dans row:
        if (row.size == 1) {
            Spacer(Modifier.weight(1f))
        }
    }
}
```

### Screen Padding
```kotlin
// Gère status bar, nav bar, clavier
.screenPadding()
```

---

## 🎯 ACCESSIBILITÉ

### Sémantique
```kotlin
• Card.onClick → Role.Button
• RadioButton disabled (géré par Card)
• contentDescription sur Icons
• textAlign.Center pour titres
```

### Feedback Visuel
```kotlin
• Animations couleur (spring stiffness 300f)
• Border 2dp sur sélection
• Elevation change sur interaction
• Progress indicators pour loading
```

---

## 🧪 PRÉVISUALISATIONS

```kotlin
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun QuizScreenPreview() {
    BacXTheme {
        QuizFlow()
    }
}
```

---

**Toute la structure UI est maintenant documentée en détail !** 🎨📐
