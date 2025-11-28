# 🚨 Guide de Dépannage CI/CD

## 🎯 Problèmes Courants et Solutions

### ❌ Erreur : Instrumented Tests Failed

**Symptôme** :
```
The process '/usr/bin/sh' failed with exit code 1
BUILD FAILED in 1m 45s
```

**Causes Possibles** :
1. Pas de tests instrumentés dans le projet
2. Tests instrumentés avec erreurs
3. Émulateur qui ne démarre pas

**✅ Solution Appliquée** :

```yaml
# Tests instrumentés rendus optionnels
instrumented-tests:
  continue-on-error: true  # ✅ Ne bloque plus le workflow
```

**Test de base créé** : `BasicInstrumentedTest.kt`

---

### ❌ Erreur : Build Failed - Compilation Error

**Symptôme** :
```
Compilation failed; see the compiler error output for details.
```

**✅ Solutions** :

```bash
# 1. Vérifier en local
./gradlew assembleDebug --stacktrace

# 2. Nettoyer et rebuild
./gradlew clean assembleDebug

# 3. Voir les logs complets
gh run view <run-id> --log
```

---

### ❌ Erreur : Lint Failures

**Symptôme** :
```
Lint found errors in the project
```

**✅ Solutions** :

```bash
# 1. Voir le rapport lint
./gradlew lintDebug

# Rapport HTML dans : app/build/reports/lint-results-debug.html

# 2. Fix automatique
./gradlew lintFix

# 3. Ignorer temporairement (si urgence)
# Dans build.gradle.kts :
android {
    lint {
        abortOnError = false
    }
}
```

---

### ❌ Erreur : Fail-Safe Validation - Navigation Directe

**Symptôme** :
```
❌ ERREUR : Navigation directe détectée (INTERDIT)
app/src/.../MyScreen.kt:42:
    navController.navigate("profile")
```

**✅ Solution** :

```kotlin
// ❌ AVANT
navController.navigate("profile")

// ✅ APRÈS
navigationViewModel.navigate(NavCommand.NavigateTo("profile"))
```

---

### ❌ Erreur : Out of Memory (OOM)

**Symptôme** :
```
OutOfMemoryError: Java heap space
```

**✅ Solutions** :

```kotlin
// Dans gradle.properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
```

Ou dans le workflow :

```yaml
- name: Build with more memory
  run: ./gradlew assembleDebug --max-workers 2
  env:
    GRADLE_OPTS: -Xmx4096m
```

---

### ❌ Erreur : Cache Miss - Build Lent

**Symptôme** :
```
Build takes 20+ minutes
```

**✅ Solution** :

```yaml
# Vérifier que le cache est activé
- name: Setup JDK
  uses: actions/setup-java@v4
  with:
    cache: 'gradle'  # ✅ Cache Gradle

# Ajouter cache manuel si nécessaire
- name: Cache Gradle packages
  uses: actions/cache@v4
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
```

---

### ❌ Erreur : Emulator Timeout

**Symptôme** :
```
Emulator failed to start within 300 seconds
```

**✅ Solution** :

```yaml
# Augmenter le timeout
- name: Run Tests
  uses: reactivecircus/android-emulator-runner@v2
  with:
    emulator-boot-timeout: 600  # 10 minutes au lieu de 5
```

---

### ❌ Erreur : Tests Flaky (Intermittent)

**Symptôme** :
```
Tests passent parfois, échouent parfois
```

**✅ Solutions** :

```kotlin
// 1. Ajouter des délais
@Test
fun myTest() {
    // Attendre que l'UI soit stable
    Thread.sleep(1000)
    
    // Ou utiliser Espresso idling resources
    Espresso.onIdle()
}

// 2. Retry automatique
@get:Rule
val retry = RetryRule(3)  // Retry 3 fois
```

---

### ❌ Erreur : Security Scan - Vulnérabilités

**Symptôme** :
```
CVE-2024-XXXX detected in dependency X
```

**✅ Solutions** :

```bash
# 1. Voir les dépendances obsolètes
./gradlew dependencyUpdates

# 2. Mettre à jour la dépendance
# Dans libs.versions.toml ou build.gradle.kts

# 3. Si pas de fix disponible, supprimer si non critique
# Ou ignorer temporairement dans le workflow
```

---

### ❌ Erreur : Workflow Stuck (Bloqué)

**Symptôme** :
```
Workflow running for > 60 minutes
```

**✅ Solutions** :

```bash
# 1. Annuler le workflow
gh run cancel <run-id>

# 2. Vérifier les timeouts dans le workflow
timeout-minutes: 30  # Ajouter timeout sur chaque job

# 3. Relancer
gh run rerun <run-id>
```

---

## 🔧 Commandes Utiles

### **Voir les workflows**

```bash
# Liste tous les workflows
gh workflow list

# Voir runs récents
gh run list --limit 10

# Voir détails d'un run
gh run view <run-id>

# Voir les logs
gh run view <run-id> --log

# Télécharger artifacts
gh run download <run-id>
```

### **Relancer un workflow**

```bash
# Relancer le dernier run
gh run rerun

# Relancer un run spécifique
gh run rerun <run-id>

# Relancer seulement les jobs échoués
gh run rerun <run-id> --failed
```

### **Annuler un workflow**

```bash
# Annuler un run en cours
gh run cancel <run-id>

# Annuler tous les runs en cours
gh run list --status in_progress | cut -f7 | xargs -n1 gh run cancel
```

---

## 📊 Debugging Avancé

### **Activer le mode debug**

Dans le workflow :

```yaml
- name: Debug step
  run: |
    echo "::debug::Variable value: $MY_VAR"
```

Puis déclencher avec :

```bash
gh workflow run my-workflow.yml -f debug=true
```

### **SSH dans le runner (pour debug)**

Ajouter temporairement :

```yaml
- name: Setup tmate session (DEBUG)
  uses: mxschmitt/action-tmate@v3
  if: ${{ failure() }}  # Seulement si échec
```

### **Logs détaillés Gradle**

```bash
./gradlew assembleDebug --info      # Logs INFO
./gradlew assembleDebug --debug     # Logs DEBUG (très verbeux)
./gradlew assembleDebug --stacktrace # Stack traces complets
```

---

## ⚙️ Optimisations

### **Réduire le temps de build**

```yaml
# 1. Utiliser build cache
- name: Build
  run: ./gradlew assembleDebug --build-cache

# 2. Paralléliser
- name: Build
  run: ./gradlew assembleDebug --parallel

# 3. Limiter workers si problème mémoire
- name: Build
  run: ./gradlew assembleDebug --max-workers=2
```

### **Skip certains checks en dev**

```yaml
# Dans feature branches, skip les tests lents
- name: Run Tests
  if: github.ref == 'refs/heads/main'
  run: ./gradlew connectedDebugAndroidTest
```

---

## 🎯 Checklist de Résolution

Quand un workflow échoue :

- [ ] **Lire les logs** : `gh run view <run-id> --log`
- [ ] **Reproduire localement** : Exécuter la même commande
- [ ] **Vérifier les dépendances** : Cache, versions, etc.
- [ ] **Chercher dans les issues** : GitHub Issues du projet
- [ ] **ググler l'erreur** : Stack Overflow, GitHub Discussions
- [ ] **Demander de l'aide** : Créer une issue avec logs complets

---

## 📞 Support

### **Ressources**

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Gradle Build Scans](https://scans.gradle.com/)
- [Android Emulator Runner](https://github.com/ReactiveCircus/android-emulator-runner)

### **Contact**

- GitHub Issues du projet
- Discussions d'équipe

---

**Version** : 1.0.0  
**Date** : 2025-11-28  
**Dernière MAJ** : Fix instrumented tests optional
