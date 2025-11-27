# 🧪 Guide d'Exécution - Tests d'Intégration EduCam

## Vue d'ensemble

Ce test simule **un parcours utilisateur réel** : un élève ouvre l'app, charge 50 questions en mode offline, répond à toutes, et l'app sauvegarde localement les résultats.

### Conditions simulées
- **Device**: Tecno Spark (1GB RAM)
- **Réseau**: Offline (mode avion)
- **Questions**: 50 questions de Physique Term C
- **Durée**: ~5 minutes

---

## 🚀 Exécution Locale

### Prérequis
- Android Studio Hedgehog+
- Émulateur ou device physique (API 26+)
- Java 17

### Commande Simple
```bash
# Exécute tous les tests d'intégration
./gradlew connectedDebugAndroidTest

# Exécute uniquement QuizIntegrationTest
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=\
com.excell44.educam.integration.QuizIntegrationTest
```

### Depuis Android Studio
1. Ouvrir `QuizIntegrationTest.kt`
2. Clic droit sur le fichier
3. Run 'QuizIntegrationTest'

---

## 📊 Résultats Attendus

### ✅ Tests qui DOIVENT passer
```
test_01_parcours_complet_50_questions_offline_sans_crash
├─ ✓ Login accepté (mode invité)
├─ ✓ 50 questions chargées
├─ ✓ Aucun crash détecté
├─ ✓ Mémoire < 100MB constamment
├─ ✓ Session sauvegardée localement
└─ ✓ Score calculé correctement

test_02_verification_memoire_stable
├─ ✓ Augmentation mémoire < 30MB
└─ ✓ Pas de fuite mémoire

test_03_persistance_apres_force_close
├─ ✓ Session survit à recreate()
└─ ✓ Données disponibles après crash simulé
```

### Console Output Exemple
```
📝 Question 1/50
📝 Question 2/50
...
📝 Question 50/50
✅ Score final : 45/50
⏱️ Temps total : 180s
📊 Mémoire avant: 45MB, après: 68MB (+23MB)
```

---

## 🐛 Debugging

### Si un test échoue

#### AssertionError: "Crash détecté à la question X"
**Cause**: L'activité se termine (finish) avant la fin du test
```bash
# Vérifier les logs
adb logcat | grep -A 20 "FATAL EXCEPTION"
```

#### "Mémoire excessive à QX: YYYMB"
**Cause**: Fuite mémoire ou chargement inefficace
```bash
# Capturer heap dump
adb shell am dumpheap com.excell44.educam.debug /data/local/tmp/heap.hprof
adb pull /data/local/tmp/heap.hprof
# Analyser avec Android Studio Profiler
```

#### "Session perdue après recreate"
**Cause**: Room transactions non complétées
- Vérifier que tous les DAOs utilisent `@Transaction`
- Forcer `commit()` au lieu de `apply()` pour SharedPreferences

---

## ⚙️ CI/CD - GitHub Actions

### Automatique sur chaque Push
Le workflow `.github/workflows/integration-tests.yml` s'exécute automatiquement :
- ✅ Sur push `main` ou `develop`
- ✅ Sur Pull Request
- ⏱️ Timeout: 30 minutes max
- 🎯 Émulateurs: API 28 et 30

### Résultats
Les rapports sont téléchargés dans les Artifacts :
- `test-results-api-28/` : Rapports HTML + XML
- `test-results-api-30/` : Rapports HTML + XML

### Forcer l'exécution manuelle
```bash
# Sur GitHub : Actions → Integration Tests → Run workflow
```

---

## 📈 Métriques de Succès

| Métrique | Seuil | Description |
|----------|-------|-------------|
| **Taux de réussite** | 100% | Tous les tests doivent passer |
| **Mémoire max** | < 100MB | Sur 50 questions |
| **Augmentation mémoire** | < 30MB | Entre début et fin |
| **Temps exécution** | < 10min | CI/CD total |
| **Crashes** | 0 | Aucun crash accepté |

---

## 🔧 Maintenance

### Ajouter de nouvelles assertions
```kotlin
// Dans QuizIntegrationTest.kt
@Test
fun test_04_ma_nouvelle_verification() {
    // ... test logic
}
```

### Modifier le nombre de questions
```kotlin
// Ligne 52 de QuizIntegrationTest.kt
private val mockQuestions = (1..100).map { ... } // 100 au lieu de 50
```

### Changer les conditions du device
```kotlin
// Dans setup()
setMemoryLimit(512) // Simule device plus puissant
```

---

## 🎯 Objectif Final

**Ce test garantit que votre application peut fonctionner dans un lycée camerounais avec :**
- ✅ Connexion internet instable/absente
- ✅ Téléphones low-end (Tecno, Infinix)
- ✅ Sessions longues (50+ questions)
- ✅ Pas de perte de données

**Si ces tests passent → Votre app est prête pour production.**
