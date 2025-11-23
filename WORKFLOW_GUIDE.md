# Guide de Workflow - EduCam

Ce guide vous aidera à travailler efficacement avec votre application EduCam en suivant votre workflow : Cursor/VS Code → GitHub → Build → Import → Visualisation Android Studio.

## 📋 Prérequis

- Android Studio (version récente recommandée)
- Cursor ou VS Code avec extensions Android/Kotlin
- Git configuré
- JDK 11 ou supérieur
- Gradle 8.x
- Compte GitHub (pour GitHub Actions)

## 🔄 Workflow Recommandé

### 1. Développement dans Cursor/VS Code

#### Avec Cursor (Recommandé)
Cursor offre une meilleure intégration AI pour le développement Android/Kotlin.

```bash
# Ouvrir le projet dans Cursor
cursor .

# Utiliser les scripts Windows pour faciliter le développement
scripts\check-before-commit.bat  # Vérifier avant commit
scripts\build-local.bat          # Build complet
```

#### Avec VS Code
```bash
# Ouvrir le projet dans VS Code
code .

# Vérifier les erreurs de compilation
./gradlew build --no-daemon

# Nettoyer le projet si nécessaire
./gradlew clean --no-daemon
```

#### Extensions recommandées pour Cursor/VS Code
- Kotlin Language
- Android (par Google)
- Gradle for Java
- Error Lens (pour voir les erreurs inline)

### 2. Vérification Avant Commit

**Windows :**
```batch
# Utiliser le script de vérification
scripts\check-before-commit.bat
```

**Linux/Mac :**
```bash
# Vérifier la compilation
./gradlew compileDebugKotlin compileReleaseKotlin --no-daemon

# Exécuter les tests
./gradlew test --no-daemon

# Vérifier le lint
./gradlew lintDebug --no-daemon
```

### 3. Commit et Push vers GitHub

```bash
# Vérifier les changements
git status

# Ajouter les fichiers modifiés
git add .

# Commit avec un message descriptif
git commit -m "Description des changements"

# Push vers GitHub
git push origin main
```

**Note :** Le push déclenchera automatiquement le workflow GitHub Actions pour build et tests.

### 4. Build du Projet

#### Build Local (Windows)
```batch
# Utiliser le script de build
scripts\build-local.bat
```

#### Build Manuel
```bash
# Build complet
./gradlew build --no-daemon

# Build de l'APK de debug
./gradlew assembleDebug --no-daemon

# Build de l'APK de release
./gradlew assembleRelease --no-daemon
```

### 5. GitHub Actions (CI/CD Automatique)

Le projet inclut un workflow GitHub Actions (`.github/workflows/android-build.yml`) qui :
- ✅ Se déclenche automatiquement sur push/PR vers `main` ou `develop`
- ✅ Build le projet
- ✅ Exécute les tests
- ✅ Génère les APK debug et release
- ✅ Upload les artefacts (APK + résultats de tests)

**Accéder aux artefacts :**
1. Aller sur GitHub → Actions
2. Sélectionner le workflow "Android Build"
3. Cliquer sur le run récent
4. Télécharger les APK dans la section "Artifacts"

**Déclencher manuellement :**
- GitHub → Actions → Android Build → Run workflow

### 6. Import dans Android Studio

1. Ouvrir Android Studio
2. File → Open → Sélectionner le dossier du projet
3. Attendre la synchronisation Gradle
4. Si des erreurs apparaissent :
   - File → Invalidate Caches / Restart
   - Build → Clean Project
   - Build → Rebuild Project

### 7. Visualisation et Test

- Exécuter l'application sur un émulateur ou un appareil physique
- Utiliser le Layout Inspector pour déboguer l'UI
- Utiliser Logcat pour voir les logs

## 🛠️ Résolution de Problèmes Courants

### Problème : Erreur de daemon Kotlin

**Solution :**
```bash
# Arrêter tous les daemons Gradle
./gradlew --stop

# Nettoyer le projet
./gradlew clean --no-daemon

# Rebuild
./gradlew build --no-daemon
```

### Problème : Erreurs de synchronisation Gradle

**Solution :**
1. Supprimer le dossier `.gradle` dans le projet
2. Supprimer le dossier `build` dans `app/`
3. Dans Android Studio : File → Invalidate Caches / Restart
4. Rebuild le projet

### Problème : Erreurs de compilation Room

**Solution :**
1. Vérifier que KSP est correctement configuré
2. Nettoyer et rebuild :
```bash
./gradlew clean --no-daemon
./gradlew build --no-daemon
```

### Problème : Erreurs Hilt

**Solution :**
1. Vérifier que `@HiltAndroidApp` est présent sur `EduCamApplication`
2. Vérifier que tous les modules sont correctement annotés avec `@Module` et `@InstallIn`
3. Rebuild le projet

## 📱 Configuration pour Build Android

### Optimisations Mémoire (Déjà Configurées)

Le projet est optimisé pour réduire l'utilisation mémoire sur machines lentes :

**Dans `gradle.properties` :**
```properties
# Gradle daemon - Réduit de 4g à 2g
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8 -Dkotlin.daemon.jvm.options="-Xmx1g"

# Kotlin daemon - Réduit de 2g à 1g
kotlin.daemon.jvmargs=-Xmx1g -XX:MaxMetaspaceSize=512m
```

**Bénéfices :**
- ✅ Réduction de ~50% de l'utilisation mémoire (de ~7g à ~3.5g)
- ✅ Meilleure performance sur machines avec 8GB RAM ou moins
- ✅ Moins de risque d'OutOfMemoryError

### Optimisations pour Machines Lentes

#### 1. Nettoyer le Cache Gradle Régulièrement
```batch
# Windows - Nettoyer le cache global
Remove-Item -Path "$env:USERPROFILE\.gradle\caches" -Recurse -Force

# Linux/Mac
rm -rf ~/.gradle/caches
```

#### 2. Désactiver les Daemons (si problèmes de mémoire)
Toujours utiliser `--no-daemon` dans les scripts pour éviter les problèmes :
```bash
./gradlew build --no-daemon
```

#### 3. Optimisations Android Studio

1. **Réduire la mémoire** : File → Settings → Build, Execution, Deployment → Compiler → Build process heap size : 1024 MB (au lieu de 2048)

2. **Désactiver les inspections inutiles** : File → Settings → Editor → Inspections → Désactiver celles non nécessaires

3. **Utiliser le build cache** : Déjà activé dans `gradle.properties`

4. **Désactiver l'indexation automatique** : File → Settings → Appearance & Behavior → System Settings → Désactiver "Synchronize files on frame activation"

#### 4. Scripts Utiles (Windows)

Deux scripts batch sont disponibles dans `scripts/` :
- `build-local.bat` : Build complet avec nettoyage
- `check-before-commit.bat` : Vérifications avant commit

## 🔍 Vérifications Avant Commit

### Checklist Automatique (Windows)
```batch
scripts\check-before-commit.bat
```

### Checklist Manuelle
- [ ] Le projet compile sans erreurs : `./gradlew build --no-daemon`
- [ ] Aucune erreur de lint : `./gradlew lintDebug --no-daemon`
- [ ] Les tests passent : `./gradlew test --no-daemon`
- [ ] Le code est formaté correctement
- [ ] Les imports inutiles sont supprimés
- [ ] Les fichiers sensibles ne sont pas commités (`.env`, clés API, etc.)

## 📦 Structure du Projet

```
EduCam/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/excell44/educam/
│   │   │   │   ├── data/          # Modèles, DAOs, Repositories
│   │   │   │   ├── di/             # Modules Hilt
│   │   │   │   ├── ui/             # Écrans, ViewModels, Navigation
│   │   │   │   ├── util/           # Utilitaires
│   │   │   │   ├── MainActivity.kt
│   │   │   │   └── EduCamApplication.kt
│   │   │   └── res/                # Ressources Android
│   │   └── test/                   # Tests unitaires
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradle/libs.versions.toml
```

## 🚀 Commandes Utiles

### Scripts Windows
```batch
# Build complet avec nettoyage
scripts\build-local.bat

# Vérification avant commit
scripts\check-before-commit.bat
```

### Commandes Gradle Directes

```bash
# Arrêter les daemons Gradle
./gradlew --stop

# Nettoyer le projet
./gradlew clean --no-daemon

# Build complet
./gradlew build --no-daemon

# Installer sur un appareil connecté
./gradlew installDebug --no-daemon

# Voir les dépendances
./gradlew dependencies --no-daemon

# Vérifier les problèmes
./gradlew check --no-daemon

# Nettoyer le cache Gradle global (Windows PowerShell)
Remove-Item -Path "$env:USERPROFILE\.gradle\caches" -Recurse -Force
```

## 📝 Notes Importantes

1. **Toujours utiliser `--no-daemon`** pour éviter les problèmes de daemon Kotlin et réduire l'utilisation mémoire
2. **Nettoyer régulièrement** le cache Gradle pour libérer de l'espace disque (peut prendre plusieurs GB)
3. **Vérifier les versions** dans `libs.versions.toml` pour la compatibilité
4. **Sauvegarder avant chaque build** important
5. **Utiliser les scripts batch** sur Windows pour simplifier les opérations courantes
6. **GitHub Actions** s'exécute automatiquement - vérifier les résultats dans l'onglet Actions
7. **Optimisations mémoire** : Le projet est configuré pour machines lentes (2GB Gradle, 1GB Kotlin)

## 🔗 Ressources

### Documentation Technique
- [Documentation Android](https://developer.android.com)
- [Documentation Kotlin](https://kotlinlang.org/docs/home.html)
- [Documentation Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Documentation Room](https://developer.android.com/training/data-storage/room)
- [Documentation Hilt](https://dagger.dev/hilt/)

### Outils et Workflows
- [Cursor Editor](https://cursor.sh) - Éditeur AI pour le développement
- [GitHub Actions](https://docs.github.com/en/actions) - CI/CD automatique
- [Gradle Performance](https://docs.gradle.org/current/userguide/performance.html) - Optimisations Gradle

### Fichiers de Configuration du Projet
- `gradle.properties` - Configuration mémoire et optimisations
- `.github/workflows/android-build.yml` - Workflow CI/CD
- `scripts/build-local.bat` - Script de build Windows
- `scripts/check-before-commit.bat` - Script de vérification Windows

