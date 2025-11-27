# 🧪 Plan de Tests de Robustesse - EduCam

## 🎯 Objectif

Valider que l'architecture robuste fonctionne parfaitement dans **TOUS** les scénarios, y compris les cas extrêmes.

---

## 📋 GROUPE 1 : Tests de Stress (Scénarios Extrêmes)

### ✅ TEST 1.1: Clics Rapides Massifs
**Objectif:** Vérifier le debounce sous stress intense

**Procédure:**
1. Installer un auto-clicker (ou script ADB)
2. Configurer pour cliquer 100x/seconde
3. Cible: Bouton "Démarrer Quiz"
4. Durée: 30 secondes

**Résultat attendu:**
- ✅ 1 seule navigation vers le quiz
- ✅ Aucun crash
- ✅ Logs "Click ignored (debounce)"

**Commande ADB:**
```bash
# Cliquer 100 fois très rapidement
for i in {1..100}; do adb shell input tap 500 1000; done
```

---

### ✅ TEST 1.2: Navigation Back Rapide
**Objectif:** Tester navigateSafe() sous pression

**Procédure:**
1. Aller sur Quiz
2. Appuyer sur Back 50 fois rapidement
3. Ré-entrer et répéter

**Résultat attendu:**
- ✅ Retour propre à Home
- ✅ Pas de crash "IllegalStateException"
- ✅ Navigation ignorée si debounce actif

---

### ✅ TEST 1.3: Rotation Continue
**Objectif:** Tester la persistance d'état

**Procédure:**
1. Démarrer un quiz
2. Répondre à 2 questions
3. Faire pivoter l'appareil 20 fois
4. Vérifier les réponses

**Résultat attendu:**
- ✅ État du quiz préservé
- ✅ Réponses sauvegardées
- ✅ Pas de perte de données

**Script adb:**
```bash
# Rotation automatique
for i in {1..20}; do
  adb shell content insert --uri content://settings/system \
    --bind name:s:user_rotation --bind value:i:1
  sleep 1
done
```

---

### ✅ TEST 1.4: Mémoire Faible
**Objectif:** Tester le comportement en mémoire limitée

**Procédure:**
1. Activer "Ne pas conserver les activités" (Dev Options)
2. Utiliser l'app normalement
3. Switcher vers 10 autres apps
4. Revenir à EduCam

**Résultat attendu:**
- ✅ Session restaurée
- ✅ Données préservées
- ✅ Pas de crash au retour

---

### ✅ TEST 1.5: Network Toggle Rapide
**Objectif:** Tester l'adaptation réseau

**Procédure:**
1. Activer/désactiver WiFi 20x rapidement
2. Pendant qu'une sync est en cours

**Résultat attendu:**
- ✅ OfflineModeBanner réactif
- ✅ Sync annulée/reprise proprement
- ✅ Pas de crash NetworkException

---

## 📋 GROUPE 2 : Tests de Durabilité (Longue Durée)

### ✅ TEST 2.1: Session Marathon (2h)
**Objectif:** Tester stabilité sur longue durée

**Procédure:**
1. Utiliser l'app pendant 2h non-stop
2. Faire 50+ quiz
3. Switcher entre écrans continuellement
4. Créer/supprimer comptes

**Métriques à vérifier:**
- 📊 Pas d'augmentation mémoire (Memory Profiler)
- 📊 Crash rate < 1 par heure
- 📊 Frame drops < 5% du temps

---

### ✅ TEST 2.2: Background 24h
**Objectif:** Tester les fuites mémoire

**Procédure:**
1. Lancer l'app
2. Mettre en background
3. Laisser 24h
4. Vérifier l'état au retour

**Résultat attendu:**
- ✅ Mémoire stable (pas de leak)
- ✅ Session restaurée
- ✅ Données intactes

---

### ✅ TEST 2.3: 100 Comptes (Stress Multi-User)
**Objectif:** Tester les limites du système

**Procédure:**
1. Créer 3 comptes
2. Essayer d'en créer 97 de plus
3. Vérifier le message d'erreur

**Résultat attendu:**
- ✅ Max 3 comptes strictement respecté
- ✅ Message clair "Limite atteinte"
- ✅ Pas de bypass possible

---

## 📋 GROUPE 3 : Tests de Recovery (Situations Critiques)

### ✅ TEST 3.1: Crashs Répétés → Safe Mode
**Objectif:** Tester le système d'urgence

**Procédure:**
1. Forcer 3 crashs en < 5 min
2. Redémarrer l'app
3. Vérifier Safe Mode

**Résultat attendu:**
- ✅ Safe Mode activé automatiquement
- ✅ SafeModeDialog s'affiche
- ✅ SafeModeBanner visible
- ✅ Features lourdes désactivées

---

### ✅ TEST 3.2: Data Corruption → Recovery
**Objectif:** Tester la récupération de données

**Procédure:**
1. Créer des données (quiz, résultats)
2. Corrompre la DB (supprimer fichier .db)
3. Relancer l'app

**Résultat attendu:**
- ✅ App ne crash pas
- ✅ DB recréée
- ✅ Message utilisateur clair

---

### ✅ TEST 3.3: Full Storage → Graceful Fail
**Objectif:** Gérer le stockage plein

**Procédure:**
1. Remplir le stockage (Dev Options)
2. Essayer de sauvegarder un quiz

**Résultat attendu:**
- ✅ Message d'erreur clair
- ✅ Pas de crash
- ✅ Fonctionnalités en lecture OK

---

## 📋 GROUPE 4 : Tests de Performance (Benchmarks)

### ✅ TEST 4.1: Quiz Load Time
**Objectif:** Vérifier les performances de chargement

**Métriques:**
- 🎯 Cold start: < 1000ms
- 🎯 Cache hit: < 100ms
- 🎯 DB query: < 500ms

**Outil:** AppHealthMonitor + Logcat

---

### ✅ TEST 4.2: Jank Detection
**Objectif:** Détecter les frame drops

**Procédure:**
1. Activer MonitorJank sur tous les écrans
2. Utiliser l'app pendant 30 min
3. Vérifier les logs

**Résultat attendu:**
- ✅ Janks < 5 par minute
- ✅ Aucun jank > 50ms
- ✅ Smooth 60 FPS global

---

### ✅ TEST 4.3: Cache Performance
**Objectif:** Valider le cache TTL

**Procédure:**
1. Charger quiz (DB hit)
2. Recharger immédiatement (Cache hit)
3. Attendre 6 min (TTL expiré)
4. Recharger (DB hit)

**Vérifier via logs:**
- ✅ "Cache hit" pour étape 2
- ✅ "Cache miss" pour étape 4

---

## 📋 GROUPE 5 : Tests d'Intégration (Bout en Bout)

### ✅ TEST 5.1: User Journey Complet
**Scénario:**
1. Premier lancement
2. Créer compte
3. Faire 5 quiz
4. Voir résultats
5. Se déconnecter
6. Créer 2e compte
7. Switcher entre comptes

**Résultat attendu:**
- ✅ Aucun crash
- ✅ Données isolées par compte
- ✅ Switch instantané

---

### ✅ TEST 5.2: Offline → Online → Offline
**Scénario:**
1. Démarrer offline
2. Faire quiz offline
3. Activer réseau
4. Vérifier sync
5. Désactiver réseau

**Résultat attendu:**
- ✅ Quiz sauvegardé offline
- ✅ Sync auto au retour réseau
- ✅ Banner offline visible

---

### ✅ TEST 5.3: Admin Flow
**Scénario:**
1. Login admin
2. Voir LocalDatabase
3. Voir RemoteDashboard
4. Voir HealthMonitor
5. Factory Reset

**Résultat attendu:**
- ✅ Tous les écrans fonctionnels
- ✅ Métriques affichées
- ✅ Reset propre

---

## 📊 Checklist de Validation Finale

### Tests Critiques (BLOQUANTS)
- [ ] Clics rapides → Aucun crash
- [ ] Navigation rapide → Aucun crash
- [ ] 3 comptes max → Strictement respecté
- [ ] Safe Mode → Active après 3 crashs
- [ ] Offline-first → Fonctionne sans réseau

### Tests Importants
- [ ] Session 2h → Stable
- [ ] Rotation → État préservé
- [ ] Memory leak → Aucun
- [ ] Cache TTL → Fonctionne
- [ ] Monitoring → Métriques exactes

### Tests Performance
- [ ] Quiz load < 1s
- [ ] Jank < 5/min
- [ ] 60 FPS constant
- [ ] Mémoire stable

---

## 🎯 Critères de Succès

### ✅ VALIDATION COMPLÈTE SI :
1. **100%** des tests CRITIQUES passent
2. **95%** des tests IMPORTANTS passent
3. **80%** des tests PERFORMANCE passent
4. **Zéro** regression vs version précédente

---

## 🚀 Plan d'Exécution

### Jour 1 (4h)
- Tests de Stress (Groupe 1)
- Tests de Recovery (Groupe 3)

### Jour 2 (4h)
- Tests de Durabilité (Groupe 2)
- Tests de Performance (Groupe 4)

### Jour 3 (4h)
- Tests d'Intégration (Groupe 5)
- Corrections de bugs
- Validation finale

---

## 📝 Template de Rapport de Bug

```markdown
### BUG-XXX: [Titre court]

**Sévérité:** CRITIQUE / HAUTE / MOYENNE / BASSE

**Test:** [Nom du test qui a échoué]

**Reproduction:**
1. [Étape 1]
2. [Étape 2]
3. [Résultat obtenu]

**Attendu:**
[Ce qui devrait se passer]

**Obtenu:**
[Ce qui se passe réellement]

**Logs:**
```
[Logs pertinents]
```

**Screenshot/Video:**
[Si applicable]

**Priorité Fix:** IMMÉDIATE / HAUTE / NORMALE
```

---

## 📈 Métriques de Succès

**OBJECTIFS:**
- 🎯 Crash-free rate: > 99.9%
- 🎯 ANR rate: 0%
- 🎯 Cold start: < 1s
- 🎯 Frame drop rate: < 1%
- 🎯 User satisfaction: "App très stable"

---

**SI TOUS LES TESTS PASSENT → L'APP EST PRODUCTION-READY** ✅

*Document créé le: 27 novembre 2024*
