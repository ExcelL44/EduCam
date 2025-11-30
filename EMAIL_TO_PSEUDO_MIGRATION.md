# 🔄 Migration Email → Pseudo (App pour Jeunes)

## ❌ PROBLÈME IDENTIFIÉ

L'app utilise actuellement un hack `email` :App pour jeunes → **PAS D'EMAIL REQUIS**
Actuellement : `pseudo` → `"pseudo@local.excell"` (simulé)  
**Requis** : Stocker et utiliser `pseudo` directement

---

## ✅ SOLUTION: Renommer `email` en `pseudo`

### Fichiers à Modifier

#### 1. User.kt (FAIT ✅)
```kotlin
// AVANT
val email: String

// APRÈS  
val pseudo: String // Username/Pseudo (no email required)
```

#### 2. UserDao.kt (FAIT ✅)
```kotlin
// AVANT
@Query("SELECT * FROM users WHERE email = :email LIMIT 1")
suspend fun getUserByEmail(email: String): User?

// APRÈS
@Query("SELECT * FROM users WHERE pseudo = :pseudo LIMIT 1")
suspend fun getUserByPseudo(pseudo: String): User?
```

#### 3. AuthRepository.kt (⚠️ CORROMPU - NÉCESSITE RECONSTRUCTION)

**Fichier corrompu** - Voici la structure correcte :

```kotlin
// login()
suspend fun login(pseudo: String, password: String): Result<User> {
    val user = userDao.getUserByPseudo(pseudo)  // ← Changé
    // ... validation password
}

// register()  
suspend fun register(pseudo: String, password: String, name: String, gradeLevel: String): Result<User> {
    val existingUser = userDao.getUserByPseudo(pseudo)  // ← Changé
    val user = User(
        pseudo = pseudo,  // ← Direct, pas d'email
        // ... rest
    )
}

// registerOffline()
suspend fun registerOffline(pseudo: String, password: String, fullName: String, gradeLevel: String): Result<User> {
    val existingUser = userDao.getUserByPseudo(pseudo)  // ← Changé
    val user = User(
        pseudo = pseudo,  // ← Direct, pas d'email  
        // ... rest
    )
}
```

#### 4. Migration Base de Données (REQUIS)

**AppDatabase.kt** - Ajouter migration v3 → v4:

```kotlin
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Renommer colonne email → pseudo
        database.execSQL(
            "ALTER TABLE users RENAME COLUMN email TO pseudo"
        )
    }
}
```

**DatabaseModule.kt** - Ajouter la migration:

```kotlin
.addMigrations(
    AppDatabase.MIGRATION_1_2,
    AppDatabase.MIGRATION_2_3,
    AppDatabase.MIGRATION_3_4  // ← Nouveau
)
```

---

## 🔧 ACTION IMMÉDIATE REQUISE

### Le fichier AuthRepository.kt est corrompu  

Tu dois le reconstruire manuellement en :

1. **Ouvrant** `AuthRepository.kt`
2. **Remplaçant** toutes les références:
   - `getUserByEmail` → `getUserByPseudo`
   - `email: String` (paramètres) → `pseudo: String`
   - `email =` → `pseudo =`
   - `user.email` → `user.pseudo`

3. **Supprimant** la ligne qui fait:
   ```kotlin
   val email = "${pseudo.lowercase()}@local.excell"  // ← SUPPRIMER
   ```

---

## 📊 IMPACT

### Fichiers Affectés
- ✅ `User.kt` - Modèle mis à jour
- ✅ `UserDao.kt` - Queries mises à jour
- ⚠️ `AuthRepository.kt` - **CORROMPU - RECONSTRUIRE**
- ⏳ `AppDatabase.kt` - Migration à ajouter
- ⏳ `DatabaseModule.kt` - Migration à référencer
- ⏳ `UserSyncWorker.kt` - Logs mentionnent `email`
- ⏳ `AuthViewModel.kt` - Paramètres sont corrects (déjà pseudo)

### Compilation
❌ Le build va **FAIL** jusqu'à ce qu'AuthRepository soit reconstruit correctement.

---

## 🚀 PROCHAINES ÉTAPES

1. **Reconstruire `AuthRepository.kt`** (priorité critique)
2. Ajouter migration v3→v4 dans `AppDatabase.kt`
3. Mettre à jour `DatabaseModule.kt`
4. Tester build
5. Tester login/register

---

## 💡 POURQUOI C'EST IMPORTANT

- **UX Jeunes**: Pas d'email = inscription plus rapide
- **Privacy**: Moins de données personnelles collectées  
- **Simplicité**: Les jeunes retiennent mieux un pseudo
- **RGPD**: Moins de données = moins de risques légaux

---

**Status**: ⚠️ **AuthRepository.kt CORROMPU** - Reconstruction manuelle requise
