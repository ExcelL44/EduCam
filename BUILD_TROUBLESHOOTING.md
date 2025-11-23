# Guide de Résolution des Problèmes de Build

Ce guide vous aidera à résoudre les problèmes courants lors du build de l'application EduCam.

## 🔧 Problèmes de Build Gradle

### Erreur : "Failed to apply plugin"

**Solution :**
```bash
# Nettoyer le cache Gradle
rm -rf .gradle
rm -rf build
rm -rf app/build

# Rebuild
./gradlew clean --no-daemon
./gradlew build --no-daemon
```

### Erreur : "Kotlin daemon connection failed"

**Solution :**
```bash
# Arrêter tous les daemons
./gradlew --stop

# Nettoyer
./gradlew clean --no-daemon

# Augmenter la mémoire dans gradle.properties
# org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g
```

### Erreur : "Unresolved reference"

**Solution :**
1. Vérifier que toutes les dépendances sont dans `libs.versions.toml`
2. Synchroniser Gradle : File → Sync Project with Gradle Files
3. Invalider les caches : File → Invalidate Caches / Restart

## 🗄️ Problèmes de Base de Données Room

### Erreur : "Cannot find symbol: TypeConverter"

**Solution :**
1. Vérifier que `Converters` est annoté avec `@TypeConverter`
2. Vérifier que `@TypeConverters(Converters::class)` est présent sur `EduCamDatabase`
3. Rebuild le projet

### Erreur : "Schema export directory is not provided"

**Solution :**
Dans `EduCamDatabase.kt`, ajouter :
```kotlin
@Database(
    ...
    exportSchema = false  // Ou true avec un chemin spécifié
)
```

## 🔌 Problèmes Hilt

### Erreur : "Hilt annotation processor not found"

**Solution :**
1. Vérifier que le plugin Hilt est dans `build.gradle.kts` :
```kotlin
alias(libs.plugins.hilt.android)
```

2. Vérifier que KSP est configuré :
```kotlin
ksp(libs.hilt.compiler)
```

3. Rebuild

### Erreur : "Missing @HiltAndroidApp"

**Solution :**
Vérifier que `EduCamApplication` a l'annotation :
```kotlin
@HiltAndroidApp
class EduCamApplication : Application()
```

Et que dans `AndroidManifest.xml` :
```xml
<application
    android:name=".EduCamApplication"
    ...>
```

## 📱 Problèmes Android Studio

### L'application ne se lance pas

**Solution :**
1. Vérifier que l'émulateur/appareil est connecté : `adb devices`
2. Vérifier les logs dans Logcat
3. Nettoyer et rebuild :
```bash
./gradlew clean --no-daemon
./gradlew installDebug --no-daemon
```

### Erreurs de synchronisation

**Solution :**
1. File → Invalidate Caches / Restart
2. Supprimer `.idea` et `.gradle` (sauf wrapper)
3. Re-ouvrir le projet

## 🐛 Problèmes Spécifiques

### Erreur : "Unresolved reference: androidx"

**Solution :**
Vérifier que `android.useAndroidX=true` est dans `gradle.properties`

### Erreur : "Package androidx.compose not found"

**Solution :**
Vérifier que le BOM Compose est inclus :
```kotlin
implementation(platform(libs.androidx.compose.bom))
```

### Erreur lors du build Release

**Solution :**
1. Vérifier `proguard-rules.pro`
2. Désactiver temporairement ProGuard :
```kotlin
release {
    isMinifyEnabled = false
}
```

## 📊 Commandes de Diagnostic

```bash
# Vérifier les dépendances
./gradlew dependencies --no-daemon

# Vérifier les problèmes
./gradlew check --no-daemon

# Voir les tâches disponibles
./gradlew tasks --no-daemon

# Build avec plus de détails
./gradlew build --no-daemon --stacktrace --info
```

## 🔍 Vérifications Système

1. **JDK Version** : `java -version` (doit être 11+)
2. **Android SDK** : Vérifier dans Android Studio → SDK Manager
3. **Gradle Version** : Vérifier dans `gradle/wrapper/gradle-wrapper.properties`
4. **Espace disque** : Au moins 5GB libres

## 💡 Conseils Généraux

1. Toujours utiliser `--no-daemon` pour éviter les problèmes de daemon
2. Nettoyer régulièrement : `./gradlew clean`
3. Synchroniser Gradle après chaque changement de dépendance
4. Vérifier les logs dans Logcat pour les erreurs runtime
5. Utiliser Android Studio pour les builds complexes

## 🆘 Support

Si le problème persiste :
1. Vérifier les logs complets : `./gradlew build --no-daemon --stacktrace`
2. Vérifier les issues GitHub similaires
3. Consulter la documentation officielle Android

