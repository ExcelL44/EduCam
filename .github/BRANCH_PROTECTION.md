# 🔒 Configuration Branch Protection - EduCam

## 📋 Instructions

Cette configuration doit être appliquée dans **GitHub Settings → Branches → Branch protection rules**.

---

## 🛡️ Règle pour `main`

### **Général**

```yaml
Branch name pattern: main
```

### **Require a pull request before merging**

- ✅ Activé
- Required approvals: **1**
- ✅ Dismiss stale pull request approvals when new commits are pushed
- ✅ Require review from Code Owners (si CODEOWNERS configuré)

### **Require status checks to pass before merging**

- ✅ Activé
- ✅ Require branches to be up to date before merging

**Required status checks** :

```
🔨 Build & Compile
🛡️ Fail-Safe Code Validation
🧪 Unit Tests
🛡️ Fail-Safe Compliance
🔨 Build APK
```

### **Require conversation resolution before merging**

- ✅ Activé (tous les commentaires doivent être résolus)

### **Require signed commits**

- ⚠️ Optionnel (recommandé pour équipe)

### **Require linear history**

- ✅ Activé (évite les merge commits complexes)

### **Block force pushes**

- ✅ Activé

### **Allow deletions**

- ❌ Désactivé

---

## 🔧 Règle pour `develop`

### **Général**

```yaml
Branch name pattern: develop
```

### **Require a pull request before merging**

- ✅ Activé
- Required approvals: **1**
- ⚠️ Dismiss stale approvals: Optionnel (moins strict que main)

### **Require status checks to pass before merging**

- ✅ Activé
- ✅ Require branches to be up to date before merging

**Required status checks** :

```
🔨 Build & Compile
🛡️ Fail-Safe Code Validation
🧪 Unit Tests
```

(Moins de checks que `main` pour développement plus rapide)

### **Block force pushes**

- ✅ Activé

### **Allow deletions**

- ❌ Désactivé

---

## 🌿 Règle pour `feature/*` (Optionnel)

### **Général**

```yaml
Branch name pattern: feature/*
```

### **Require status checks to pass before merging**

- ✅ Activé (sans require up-to-date)

**Required status checks** :

```
🔨 Build & Compile
```

(Check minimal pour feature branches)

---

## 📋 Configuration via GitHub CLI

Pour automatiser la configuration :

```bash
# Installer GitHub CLI
# https://cli.github.com/

# Se connecter
gh auth login

# Configurer branch protection pour main
gh api repos/:owner/:repo/branches/main/protection \
  --method PUT \
  -H "Accept: application/vnd.github+json" \
  -f required_status_checks='{"strict":true,"contexts":["🔨 Build & Compile","🛡️ Fail-Safe Code Validation","🧪 Unit Tests","🛡️ Fail-Safe Compliance","🔨 Build APK"]}' \
  -f enforce_admins=false \
  -f required_pull_request_reviews='{"required_approving_review_count":1,"dismiss_stale_reviews":true}' \
  -f restrictions=null \
  -f required_linear_history=true \
  -f allow_force_pushes=false \
  -f allow_deletions=false \
  -f required_conversation_resolution=true

# Configurer branch protection pour develop
gh api repos/:owner/:repo/branches/develop/protection \
  --method PUT \
  -H "Accept: application/vnd.github+json" \
  -f required_status_checks='{"strict":true,"contexts":["🔨 Build & Compile","🛡️ Fail-Safe Code Validation","🧪 Unit Tests"]}' \
  -f enforce_admins=false \
  -f required_pull_request_reviews='{"required_approving_review_count":1}' \
  -f restrictions=null \
  -f allow_force_pushes=false \
  -f allow_deletions=false
```

---

## 🎯 Exceptions (Admins)

### **Bypass settings**

En cas d'urgence, les admins peuvent bypass les protections, MAIS :

- ⚠️ Doit être documenté (pourquoi ?)
- ⚠️ Doit être temporaire
- ⚠️ PR de correction doit suivre immédiatement

**Process** :

1. Bypass → Fix urgent
2. Créer PR immédiate pour conformité
3. Merger après review normale

---

## 📊 Vérification

Pour vérifier que les règles sont bien appliquées :

```bash
# Via GitHub CLI
gh api repos/:owner/:repo/branches/main/protection

# Ou via interface web
# GitHub → Settings → Branches → main → Edit
```

---

## 🔄 Workflow Exempt des Checks

Certains workflows (ex: documentation) peuvent être exempts :

```yaml
# Dans le workflow .yml
on:
  push:
    branches:
      - main
    paths-ignore:
      - 'docs/**'
      - '*.md'
```

---

## 🚨 Que Faire Si Bloqué

### **Cas 1 : Check qui ne passe jamais**

```bash
# Désactiver temporairement le check
gh api repos/:owner/:repo/branches/main/protection \
  --method PUT \
  -f required_status_checks='{"strict":true,"contexts":[]}'

# Fix le workflow

# Réactiver
# (utiliser commande complète ci-dessus)
```

### **Cas 2 : PR urgente**

1. Demander bypass à un admin
2. Documenter dans la PR pourquoi
3. Créer ticket de suivi

---

## ✅ Checklist de Configuration

- [ ] Branch protection sur `main` activée
- [ ] Branch protection sur `develop` activée
- [ ] Required checks configurés
- [ ] Required approvals configurés
- [ ] Force push bloqué
- [ ] Deletions bloquées
- [ ] Linear history activé
- [ ] Conversation resolution activée
- [ ] Testé avec PR test

---

## 🎓 Ressources

- [GitHub Branch Protection Docs](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches)
- [Required Status Checks](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/collaborating-on-repositories-with-code-quality-features/about-status-checks)
- [GitHub CLI Reference](https://cli.github.com/manual/gh_api)

---

**Version** : 1.0.0  
**Date** : 2025-11-28  
**Impact** : 🔴 Critique (sécurité du dépôt)
