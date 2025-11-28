# 🤖 GitHub Actions Workflows - Documentation

## 📋 Vue d'ensemble

Ce projet utilise **3 workflows GitHub Actions** pour garantir la qualité et la fiabilité du code :

```
┌─────────────────────────────────────────────────────────┐
│  WORKFLOW 1 : Fail-Safe Validation (Principal)         │
│  ├─ Déclenché sur : Push & Pull Request                │
│  ├─ Jobs : 6 (Build, Lint, Tests, Validation, etc.)    │
│  └─ Durée : ~30-45 minutes                             │
├─────────────────────────────────────────────────────────┤
│  WORKFLOW 2 : Stress Tests (Automatisés)               │
│  ├─ Déclenché sur : Cron (2h du matin) + Manuel        │
│  ├─ Jobs : 4 (Navigation, Bouton, Rotation, Mémoire)   │
│  └─ Durée : ~60-90 minutes                             │
├─────────────────────────────────────────────────────────┤
│  WORKFLOW 3 : PR Checks (Obligatoires)                 │
│  ├─ Déclenché sur : Pull Request uniquement            │
│  ├─ Jobs : 5 (Style, Compliance, Coverage, Sécurité)   │
│  └─ Durée : ~20-30 minutes                             │
└─────────────────────────────────────────────────────────┘
```

---

## 🛡️ Workflow 1 : Fail-Safe Validation

**Fichier** : `.github/workflows/fail-safe-validation.yml`

### **Déclencheurs**

- ✅ Push sur `main` ou `develop`
- ✅ Pull Request vers `main` ou `develop`
- ✅ Manuel (workflow_dispatch)

### **Jobs (6)**

#### **1. 🔨 Build & Compile**
- Compile l'APK Debug
- Upload l'APK en artifact (7 jours)
- Vérifie qu'il n'y a pas d'erreurs de compilation

#### **2. 🔍 Lint & Code Quality**
- Exécute Kotlin Lint
- Upload le rapport HTML
- Continue même si warnings (ne bloque pas)

#### **3. 🧪 Unit Tests**
- Exécute tous les tests unitaires
- Publie le rapport JUnit
- Upload les résultats HTML

#### **4. 🛡️ Fail-Safe Code Validation**
Vérifie automatiquement :
- ❌ **Navigation directe** (`navController.navigate()` interdit)
- ⚠️ **Try-catch manuels** dans ViewModels (suspect)
- ⚠️ **Suspend fun sans Result<T>** dans repositories

**Résultat** :
- ✅ Passe = Code respecte les patterns Fail-Safe
- ❌ Échoue = Violations détectées → Fix requis

#### **5. 🤖 Instrumented Tests**
- Lance un émulateur Android (API 30)
- Exécute les tests instrumentés
- Upload les résultats

#### **6. 🔒 Security Check**
- Vérifie les dépendances obsolètes
- Upload le rapport de dépendances

### **Job Final : ✅ Success Report**
Affiche un récapitulatif visuel si tous les checks passent.

### **Usage**

```bash
# Déclencher manuellement
gh workflow run fail-safe-validation.yml

# Voir les runs
gh run list --workflow=fail-safe-validation.yml
```

---

## 🔥 Workflow 2 : Stress Tests

**Fichier** : `.github/workflows/stress-tests.yml`

### **Déclencheurs**

- ⏰ **Cron** : Tous les jours à 2h du matin
- ✅ **Manuel** : Avec durée configurable

### **Jobs (4)**

#### **1. 🧭 Navigation Stress Test**
- Simule 100 navigations rapides A→B→A→B
- Vérifie qu'aucun écran blanc n'apparaît
- Analyse les crashs dans les logs

#### **2. 🖱️ Button Spam Test**
- Clique 100 fois rapidement sur chaque bouton
- Vérifie que le debounce fonctionne
- Confirme qu'une seule action est exécutée

#### **3. 🔄 Rotation Stress Test**
- Effectue 20 rotations rapides
- Analyse les leaks avec LeakCanary
- Vérifie que l'état est restauré

#### **4. 💾 Memory Pressure Test**
- Limite la RAM à 2GB (low-end device)
- Teste la stabilité sous mémoire limitée
- Vérifie pas de crash

### **Rapport Final**

Génère un rapport consolidé de tous les tests :
```
✅ Navigation Stress : SUCCÈS
✅ Button Spam : SUCCÈS
✅ Rotation Stress : SUCCÈS
✅ Memory Pressure : SUCCÈS
```

### **Usage**

```bash
# Déclencher manuellement avec durée custom
gh workflow run stress-tests.yml -f test_duration=60

# Voir les résultats
gh run watch
```

### **Artifacts**

Tous les résultats sont sauvegardés pendant **30 jours** :
- `navigation-stress-results`
- `button-spam-results`
- `rotation-stress-results`
- `memory-stress-results`

---

## 🔎 Workflow 3 : PR Checks

**Fichier** : `.github/workflows/pr-checks.yml`

### **Déclencheurs**

- ✅ Pull Request opened/synchronize/reopened
- ✅ Uniquement vers `main` ou `develop`

### **Jobs (5)**

#### **1. 📝 Code Style**
- Cherche les TODOs/FIXMEs non résolus
- Alerte si fichiers > 500 lignes
- Avertissements uniquement (ne bloque pas)

#### **2. 🛡️ Fail-Safe Compliance**
**Checks BLOQUANTS** :
- ❌ Navigation directe → **ERREUR** (bloque la PR)
- ⚠️ ViewModels sans héritage → **WARNING**
- ⚠️ Repositories sans Result<T> → **WARNING**

#### **3. 📊 Test Coverage**
- Exécute tests avec Jacoco
- Upload vers Codecov
- Ne bloque pas si échec

#### **4. 🔒 Security Scan**
- Scan Trivy pour vulnérabilités
- Upload vers GitHub Security
- Alerte si CVE détectés

#### **5. 🔨 Build APK**
- Build l'APK de la PR
- Upload avec numéro de PR
- Permet de tester manuellement

### **Commentaire Automatique**

Le workflow poste un commentaire sur la PR avec les résultats :

```markdown
## 🎉 Tous les checks sont passés!

✅ **Code Style**: success
✅ **Fail-Safe Compliance**: success
✅ **Test Coverage**: success
✅ **Security Scan**: success
✅ **Build APK**: success

---
🛡️ **Cette PR respecte tous les standards Fail-Safe**
🚀 Prêt pour review et merge

📊 [Voir les détails complets](...)
```

### **Usage**

Automatique sur toute PR. Pour checks manuels :

```bash
# Voir le statut des checks
gh pr checks

# Réexécuter les checks
gh run rerun <run-id>
```

---

## 🎯 Configuration Recommandée

### **Branch Protection Rules**

Dans GitHub Settings → Branches → Add Rule :

```yaml
Branch name pattern: main

Require status checks to pass before merging: ✅
  Required checks:
    - 🔨 Build & Compile
    - 🛡️ Fail-Safe Code Validation
    - 🧪 Unit Tests
    - 🛡️ Fail-Safe Compliance

Require branches to be up to date before merging: ✅

Require approvals: 1

Block force pushes: ✅
```

### **Secrets à Configurer**

Si vous utilisez des services externes :

```bash
# GitHub Settings → Secrets → Actions

CODECOV_TOKEN=<token>  # Pour coverage
SLACK_WEBHOOK=<url>    # Pour notifications (optionnel)
```

---

## 📊 Tableaux de Bord

### **Voir tous les workflows**

```bash
gh workflow list
```

### **Voir les runs récents**

```bash
gh run list --limit 20
```

### **Télécharger les artifacts**

```bash
gh run download <run-id>
```

---

## 🚨 Que Faire Si un Workflow Échoue

### **Build Failure**

```bash
# Vérifier les logs
gh run view <run-id> --log

# Reproduire localement
./gradlew assembleDebug --stacktrace
```

### **Fail-Safe Violations**

```bash
# Exemple : Navigation directe détectée
❌ ERREUR : Navigation directe détectée (INTERDIT)
app/src/.../MyScreen.kt:42:
    navController.navigate("profile")

# Fix :
navigationViewModel.navigate(NavCommand.NavigateTo("profile"))
```

### **Stress Test Failure**

```bash
# Télécharger les rapports
gh run download <run-id> -n navigation-stress-results

# Analyser les crashs
cat */logcat.txt | grep -i "crash\|fatal"
```

---

## 🎓 Bonnes Pratiques

### **Avant de Commiter**

```bash
# 1. Build local
./gradlew assembleDebug

# 2. Tests local
./gradlew testDebugUnitTest

# 3. Lint local
./gradlew lintDebug

# 4. Vérifier les patterns
grep -r "navController\.navigate(" app/src/main/java
```

### **Avant de Merger**

- [ ] Tous les checks PR sont ✅
- [ ] Au moins 1 approbation
- [ ] Aucun commentaire non résolu
- [ ] Branch à jour avec base

---

## 📈 Métriques de Qualité

### **Objectifs**

| Métrique | Objectif | Actuel |
|----------|----------|--------|
| Build Success Rate | > 99% | - |
| Test Coverage | > 80% | - |
| Stress Tests Pass | 100% | - |
| Security Vulns | 0 Critical | - |
| PR Check Failures | < 5% | - |

---

## 🔧 Maintenance

### **Mise à Jour des Workflows**

```bash
# Tester en local avec act
act -W .github/workflows/fail-safe-validation.yml

# Push et surveiller
git push
gh run watch
```

### **Optimisation des Temps**

- Cache Gradle : Activé ✅
- Parallel jobs : Activé ✅
- Timeout limits : Configurés ✅

---

## 📞 Support

### **Problèmes Courants**

**Q : Le workflow est bloqué ?**  
R : Vérifier les timeouts (max 60 min). Annuler et relancer.

**Q : Trop de faux positifs ?**  
R : Ajuster les patterns dans les scripts grep.

**Q : Environnement CI différent de local ?**  
R : Vérifier versions JDK, Gradle, SDK dans workflows.

---

## 🏆 Checklist de Déploiement CI/CD

- [x] Workflows créés (3/3)
- [ ] Branch protection configurée
- [ ] Secrets configurés (si nécessaire)
- [ ] Premier run réussi
- [ ] Équipe formée sur les workflows
- [ ] Documentation lue

---

**Version** : 1.0.0  
**Date** : 2025-11-28  
**Auteur** : CI/CD EduCam Fail-Safe
