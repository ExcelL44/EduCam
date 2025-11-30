# 🔥 Stress Tests Workflow - Fixed & Enhanced

## ✅ **PROBLÈMES CORRIGÉS**

### 1. **Erreur YAML Critique**
**Problème**: 
```
Required property is missing: jobs
A mapping was not expected (Line 41, 50, 176)
```

**Cause**: 
- Manquait `jobs:` au début de la section
- Indentation incorrecte (commentaires au mauvais niveau)

**Solution**:
```yaml
env:
  GRADLE_OPTS: -Dorg.gradle.daemon=false -Xmx6g

jobs:  # ← AJOUTÉ
  prepare-apk:
    name: 📦 Phase 1
    steps:  # ← AJOUTÉ (manquait dans phase 1)
      - name: Download APK
```

---

### 2. **Workflow Ne Démarre Pas Après Fastbuild**

**Problème**: 
- `stress-tests.yml` est censé se déclencher automatiquement après `Fastbuild.yml`
- Ne fonctionnait pas à cause de l'erreur YAML

**Cause**:
```yaml
workflow_run:
  workflows: ["🚀 Bac-X_237 Build & Test"]  # ← Nom doit correspondre EXACTEMENT
```

**Vérification Requise**:
Confirme que le nom dans `Fastbuild.yml` est bien :
```yaml
name: 🚀 Bac-X_237 Build & Test
```

Si différent, mettre à jour `stress-tests.yml` ligne 6.

---

## 🧪 **NOUVELLE PHASE 4: TORTURE TESTS**

### Tests Automatisés Ajoutés

| Test | Description | Validation |
|------|-------------|------------|
| **Test 1** | Offline Registration → Sync | ✅ Logs de sync |
| **Test 2** | Multi-Device Conflict | ✅ Champ `localId` existe |
| **Test 3** | Cleanup Crash Resilience | ✅ App survit DB corrompue |
| **Test 4** | Clock Manipulation | ⚠️ Cleanup toléré (server-side requis) |
| **Test 5** | Graceful Offline UX | ✅ ConnectionState logs |

### Architecture de Phase 4

```yaml
torture-tests:
  needs: [prepare-apk, performance-tests]
  
  steps:
    1. Setup Emulator
    2. Install APK
    3. Run 5 Torture Tests
    4. Generate Report
```

### Tests Détaillés

#### 🧪 **Test 1: Offline Registration → Sync**
```bash
1. Enable airplane mode
2. Launch app
3. (Manual) Register offline
4. Disable airplane mode
5. Check sync logs
```

#### 🧪 **Test 2: Multi-Device Conflict**
```bash
1. Extract database
2. Verify `localId` field exists
3. FAIL if missing (BOMBE #2 non désamorcée)
```

#### 🧪 **Test 3: Cleanup Resilience**
```bash
1. Delete database file
2. Restart app
3. FAIL if app crashes (BOMBE #1 non désamorcée)
```

#### 🧪 **Test 4: Clock Manipulation**
```bash
1. Change time +2 days
2. Restart app
3. Check cleanup behavior
4. Warning if triggered (expected, server-side requis)
```

#### 🧪 **Test 5: Graceful Offline UX**
```bash
1. Enable airplane mode
2. Check ConnectionState.Offline logs
3. Disable airplane mode
4. Check ConnectionState.Online logs
```

---

## 🔄 **WORKFLOW COMPLET**

```
Fastbuild (success)
    ↓
Stress Tests Triggered
    ↓
Phase 1: Download APK
    ↓
Phase 2: Performance Tests
    ↓
Phase 3: Capacity Analysis
    ↓
Phase 4: Torture Tests ← NOUVEAU
```

---

## 📊 **VALIDATION PRODUCTION**

### Avant Merge:
- [x] YAML syntax valide
- [x] Phase 4 ajoutée
- [ ] Test manuel du workflow sur GitHub Actions
- [ ] Vérifier nom exact de Fastbuild

### Critères de Succès Phase 4:
```
✅ Test 2: localId field exists
✅ Test 3: App survives corrupted DB
⚠️ Test 1,4,5: Manual validation + logs check
```

### Si Phase 4 Fail:
```
Test 2 FAIL → BOMBE #2 non désamorcée → NE PAS MERGER
Test 3 FAIL → BOMBE #1 non désamorcée → NE PAS MERGER
Test 5 FAIL → BOMBE #3 non désamorcée → UX Review
```

---

## 🛠️ **TROUBLESHOOTING**

### Workflow Ne Démarre Toujours Pas?

**Check 1: Nom du Workflow**
```bash
# Dans Fastbuild.yml
name: 🚀 Bac-X_237 Build & Test  # ← Noter EXACTEMENT

# Dans stress-tests.yml (ligne 6)
workflows: ["🚀 Bac-X_237 Build & Test"]  # ← Doit matcher
```

**Check 2: Branches**
```yaml
branches: [ main, develop, release-security-overhaul ]
```
Assure-toi que ton push est sur une de ces branches.

**Check 3: Fastbuild Succès**
```yaml
if: |
  github.event_name != 'workflow_run' ||
  github.event.workflow_run.conclusion == 'success'
```
Stress-tests ne démarre QUE si Fastbuild = SUCCESS.

**Check 4: Artifact Name**
```yaml
# Fastbuild upload:
name: Bac-X_237-Debug

# Stress-tests download:
name: Bac-X_237-Debug  # ← Doit matcher
```

---

## 🚀 **NEXT STEPS**

1. **Commit & Push** ces changements
2. **Déclencher Fastbuild** (push ou manual)
3. **Vérifier** que stress-tests démarre automatiquement
4. **Analyser** les résultats Phase 4

### Si Tests Passent:
```
✅ App prête pour prod (côté robustesse)
→ Continuer avec UI ConnectionState
```

### Si Tests Échouent:
```
❌ Identifier quelle bombe n'est pas désamorcée
→ Revoir SECURITY_FIXES.md
→ Re-run après correction
```

---

## 📝 **COMMANDE RAPIDE DEBUG**

```bash
# Valider YAML localement
yamllint .github/workflows/stress-tests.yml

# Check workflow syntax (GitHub CLI)
gh workflow view "🔥 Bac-X_237 Stress & Performance Tests"

# Trigger manual
gh workflow run stress-tests.yml

# View runs
gh run list --workflow=stress-tests.yml
```

---

**Status**: ✅ **YAML FIXED** + 🧪 **TORTURE TESTS ADDED**  
**Prêt pour**: Test sur GitHub Actions
