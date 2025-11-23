# Structure du Projet Android - Explication

## 📁 Pourquoi le dossier s'appelle "java" alors qu'on utilise Kotlin ?

C'est **normal et standard** dans les projets Android ! Même si vous utilisez uniquement Kotlin, le dossier s'appelle toujours `java` par convention Android.

### Structure Standard Android

```
app/src/main/
├── java/          ← Nommé "java" même pour Kotlin (convention Android)
│   └── com/excell44/educam/
│       ├── MainActivity.kt
│       ├── data/
│       ├── ui/
│       └── ...
├── res/           ← Ressources (images, layouts, strings, etc.)
└── AndroidManifest.xml
```

## 🔍 Où trouver vos fichiers Kotlin ?

Vos fichiers Kotlin sont dans :
```
app/src/main/java/com/excell44/educam/
```

### Structure complète :

```
app/src/main/java/com/excell44/educam/
├── MainActivity.kt                    ← Point d'entrée
├── EduCamApplication.kt               ← Application principale
├── data/
│   ├── dao/                          ← Accès aux données (Room)
│   ├── database/                     ← Configuration Room
│   ├── model/                        ← Modèles de données
│   └── repository/                   ← Logique métier
├── di/                               ← Injection de dépendances (Hilt)
├── ui/
│   ├── navigation/                   ← Navigation Compose
│   ├── screen/                       ← Écrans de l'application
│   ├── theme/                        ← Thème et couleurs
│   └── viewmodel/                    ← ViewModels
└── util/                             ← Utilitaires
```

## 📱 Dans Android Studio

Dans Android Studio, vous verrez la structure comme suit :

1. **Vue Android** (recommandée) :
   - `app` → `java` → `com.excell44.educam`
   - Tous vos fichiers `.kt` seront visibles ici

2. **Vue Project** :
   - `app/src/main/java/com/excell44/educam/`
   - Structure complète du système de fichiers

## ✅ Vérification

Pour vérifier que tout est en place :

1. **Dans Android Studio** :
   - Ouvrez le projet
   - Dans le panneau de gauche, sélectionnez la vue "Android"
   - Développez `app` → `java` → `com.excell44.educam`
   - Vous devriez voir tous vos fichiers Kotlin

2. **Dans l'explorateur de fichiers** :
   - Naviguez vers : `app/src/main/java/com/excell44/educam/`
   - Tous vos fichiers `.kt` sont là

## 🎯 Points Importants

- ✅ Le nom "java" est une **convention Android**, pas une erreur
- ✅ Vous pouvez mettre du **Kotlin** dans ce dossier
- ✅ Vous pouvez même mélanger **Java et Kotlin** dans le même projet
- ✅ Android Studio reconnaît automatiquement les fichiers `.kt`

## 🔧 Si vous ne voyez pas le dossier dans Android Studio

1. **Synchroniser Gradle** :
   - File → Sync Project with Gradle Files

2. **Changer la vue** :
   - En haut du panneau de fichiers, changez de "Project" à "Android"

3. **Actualiser** :
   - Clic droit sur le projet → Synchronize

4. **Invalidate Caches** :
   - File → Invalidate Caches / Restart

## 📝 Résumé

**C'est normal !** Le dossier `java` contient vos fichiers Kotlin. C'est la structure standard Android, même pour les projets 100% Kotlin.

