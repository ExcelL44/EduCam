# 🧪 GUIDE DE TESTS STRESS - FAIL-SAFE

## 🎯 Objectif

Valider que l'application **ne peut pas** crasher, même sous conditions extrêmes.

---

## ✅ Tests Obligatoires Avant Production

### **Test 1 : Triple-Tap Stress**

**But** : Vérifier qu'on ne peut pas corrompre la navigation

**Procédure** :
1. Lancer l'app en DEBUG
2. Sur chaque bouton de navigation, **cliquer 10 fois rapidement**
3. Observer les logs

**Résultat Attendu** :
```
📨 Commande acceptée: NavigateTo(profile)
⏭️ Commande remplacée (spam): NavigateTo(profile)
⏭️ Commande remplacée (spam): NavigateTo(profile)
🧭 Navigation START: NavigateTo(profile)
✅ Navigation SUCCESS: NavigateTo(profile)
```

**Critère de Succès** : ✅ Aucun écran blanc, aucun crash

**Durée** : 5 minutes

---

### **Test 2 : Navigation Rapide A→B→A→B**

**But** : Vérifier qu'on ne peut pas corrompre le backstack

**Procédure** :
1. Créer un script automatisé ou manuel :
   ```kotlin
   repeat(100) {
       navViewModel.navigate(NavCommand.NavigateTo("profile"))
       delay(100)
       navViewModel.navigate(NavCommand.PopBack)
       delay(100)
   }
   ```
2. Observer les logs et l'UI

**Résultat Attendu** :
- Navigation fluide sans saccades
- Logs montrent alternance IDLE/NAVIGATING
- Backstack reste cohérent

**Critère de Succès** : ✅ Pas d'écran blanc, pas de retour auto

**Durée** : 3 minutes

---

### **Test 3 : Rotation Stress**

**But** : Vérifier qu'on ne fuite pas de mémoire sur rotation

**Procédure** :
1. Activer **Auto-rotate** sur le téléphone
2. **Tourner le téléphone 20 fois** rapidement
3. Ouvrir **LeakCanary** et vérifier les leaks

**Résultat Attendu** :
```
LeakCanary: 0 retained objects
```

**Critère de Succès** : ✅ 0 leaks détectés

**Durée** : 2 minutes

---

### **Test 4 : StrictMode Violations**

**But** : Vérifier qu'on ne fait pas d'I/O sur Main Thread

**Procédure** :
1. Build DEBUG (StrictMode activé)
2. Utiliser toutes les fonctionnalités de l'app
3. Observer l'écran et Logcat

**Résultat Attendu** :
- ❌ **Aucun flash rouge** à l'écran
- ❌ **Aucune ligne "StrictMode policy violation"** dans Logcat

**Critère de Succès** : ✅ 0 violations

**Durée** : 10 minutes

---

### **Test 5 : Network Offline**

**But** : Vérifier que l'app reste stable sans réseau

**Procédure** :
1. **Activer le mode Avion**
2. Utiliser toutes les fonctionnalités
3. Vérifier que l'app ne crash pas

**Résultat Attendu** :
- UI affiche des messages d'erreur clairs
- L'app ne crash jamais
- Les données en cache restent accessibles

**Critère de Succès** : ✅ 0 crash, UI responsive

**Durée** : 5 minutes

---

### **Test 6 : Low Memory**

**But** : Vérifier que l'app gère la mémoire limitée

**Procédure** :
1. Via **Developer Options** → **Don't keep activities**
2. Naviguer entre plusieurs écrans
3. Observer le comportement

**Résultat Attendu** :
- États restaurés correctement
- Pas de perte de données
- Pas de crash

**Critère de Succès** : ✅ État restauré, 0 crash

**Durée** : 5 minutes

---

### **Test 7 : Action Spam**

**But** : Vérifier qu'on ne peut pas spammer les actions

**Procédure** :
1. Sur un bouton qui déclenche une action lourde (ex: LoadData)
2. **Cliquer 20 fois rapidement**
3. Observer les logs

**Résultat Attendu** :
```
Action soumise: LoadData
🔄 Exécution: LoadData
Action soumise: LoadData (ignorée par debounce)
Action soumise: LoadData (ignorée par debounce)
✅ Succès: LoadData
```

**Critère de Succès** : ✅ Une seule exécution

**Durée** : 2 minutes

---

## 🤖 Tests Automatisés (Optionnel)

### **Script Espresso : Stress Navigation**

```kotlin
@Test
fun stressTestNavigation_shouldNotCrash() {
    // Stress test : 100 navigations rapides
    repeat(100) {
        onView(withId(R.id.button_navigate_profile))
            .perform(click())
        Thread.sleep(50)
        
        onView(withContentDescription("Navigate up"))
            .perform(click())
        Thread.sleep(50)
    }
    
    // Vérifier que l'app est toujours sur l'écran Home
    onView(withId(R.id.home_screen))
        .check(matches(isDisplayed()))
}
```

### **Script Espresso : Spam Bouton**

```kotlin
@Test
fun stressTestButtonSpam_shouldDebounce() {
    // Cliquer 50 fois sur le bouton de chargement
    repeat(50) {
        onView(withId(R.id.button_load_data))
            .perform(click())
    }
    
    // Attendre que l'action soit complétée
    Thread.sleep(2000)
    
    // Vérifier qu'une seule requête a été faite
    verify(mockRepository, times(1)).getData()
}
```

---

## 📊 Rapport de Test

### **Template à Remplir**

```
# Rapport de Test Stress - EduCam
Date : ________
Testeur : ________
Version : ________

## Résultats

| Test | Durée | Résultat | Notes |
|------|-------|----------|-------|
| Triple-Tap Stress | 5min | ✅ / ❌ | |
| Navigation A→B→A→B | 3min | ✅ / ❌ | |
| Rotation Stress | 2min | ✅ / ❌ | |
| StrictMode Violations | 10min | ✅ / ❌ | |
| Network Offline | 5min | ✅ / ❌ | |
| Low Memory | 5min | ✅ / ❌ | |
| Action Spam | 2min | ✅ / ❌ | |

## LeakCanary Report
Leaks détectés : ___
Detections : ___

## Logcat
Crashes : ___
Erreurs : ___

## Verdict Final
✅ PRODUCTION READY
❌ NEEDS FIX

## Notes
_______________________
```

---

## 🚨 Que Faire Si Un Test Échoue

### **Si Écran Blanc**
1. Vérifier les logs de `NavigationViewModel`
2. Chercher "TIMEOUT" ou "CRASH"
3. Vérifier que `navigationViewModel.navigate()` est utilisé partout

### **Si Crash**
1. Lire la stack trace complète
2. Vérifier qu'on utilise `FailSafeViewModel`
3. Vérifier qu'on utilise `FailSafeRepositoryHelper`

### **Si Leak Détecté**
1. Lire le rapport LeakCanary
2. Chercher les références circulaires
3. Vérifier que les `collect` sont dans `LaunchedEffect`

### **Si StrictMode Violation**
1. Lire le log Logcat
2. Déplacer le code vers `withContext(Dispatchers.IO)`
3. Utiliser `FailSafeRepositoryHelper` pour les I/O

---

## ✅ Checklist Finale

Avant de dire "PRODUCTION READY" :

- [ ] Tous les tests manuels passés (7/7)
- [ ] LeakCanary : 0 leaks après 5 min d'utilisation
- [ ] StrictMode : 0 violations rouges
- [ ] Tests automatisés passés (si implémentés)
- [ ] Build APK release sans erreurs
- [ ] APK testé sur 3 devices différents
- [ ] Documentation à jour

---

**Auteur** : Guide de Tests Stress EduCam  
**Version** : 1.0.0  
**Date** : 2025-11-28
