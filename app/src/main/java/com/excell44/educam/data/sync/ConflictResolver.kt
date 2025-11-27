package com.excell44.educam.data.sync

/**
 * Stratégies de résolution de conflits pour la synchronisation.
 */
sealed class ConflictResolutionStrategy {
    /**
     * Last-Write-Wins : La dernière modification écrase toujours.
     * Simple et efficace pour la plupart des cas.
     */
    object LastWriteWins : ConflictResolutionStrategy()

    /**
     * Server Truth : Le serveur a toujours raison.
     * Utilisé pour les données critiques gérées centralement.
     */
    object ServerTruth : ConflictResolutionStrategy()

    /**
     * Manual : Demande à l'utilisateur de choisir.
     * Utilisé pour les conflits complexes.
     */
    data class Manual(
        val localData: Any,
        val serverData: Any,
        val onResolved: (resolvedData: Any) -> Unit
    ) : ConflictResolutionStrategy()
}

/**
 * Gestionnaire de conflits de synchronisation.
 */
class ConflictResolver {

    /**
     * Résout un conflit entre données locales et serveur.
     * 
     * @param localTimestamp Timestamp de la dernière modification locale
     * @param serverTimestamp Timestamp de la dernière modification serveur
     * @param strategy Stratégie de résolution
     * @return True si on garde les données locales, False si on garde le serveur
     */
    fun <T> resolveConflict(
        localData: T,
        serverData: T,
        localTimestamp: Long,
        serverTimestamp: Long,
        strategy: ConflictResolutionStrategy = ConflictResolutionStrategy.LastWriteWins
    ): T {
        return when (strategy) {
            is ConflictResolutionStrategy.LastWriteWins -> {
                if (localTimestamp > serverTimestamp) localData else serverData
            }
            is ConflictResolutionStrategy.ServerTruth -> {
                serverData
            }
            is ConflictResolutionStrategy.Manual -> {
                // Pour les conflits manuels, on retourne les données locales par défaut
                // et on notifie l'UI pour qu'elle affiche un dialogue
                localData
            }
        }
    }

    /**
     * Fusionne deux objets de données si possible.
     * Utilisé pour les structures complexes où on peut merger certains champs.
     * 
     * Example usage:
     * ```
     * val mergedUser = conflictResolver.mergeData(localUser, serverUser) { local, server ->
     *     MergeStrategies.mergeUserProfiles(local, server)
     * }
     * ```
     */
    fun <T> mergeData(
        localData: T,
        serverData: T,
        mergeLogic: (local: T, server: T) -> T
    ): T {
        return mergeLogic(localData, serverData)
    }
    
    /**
     * Exemple de merge automatique selon le type de données.
     * Utilise les stratégies prédéfinies de MergeStrategies.
     */
    inline fun <reified T> autoMerge(localData: T, serverData: T): T {
        return when (T::class) {
            com.excell44.educam.data.model.User::class -> {
                MergeStrategies.mergeUserProfiles(
                    localData as com.excell44.educam.data.model.User,
                    serverData as com.excell44.educam.data.model.User
                ) as T
            }
            com.excell44.educam.data.local.entity.QuizResultEntity::class -> {
                MergeStrategies.mergeQuizResults(
                    localData as com.excell44.educam.data.local.entity.QuizResultEntity,
                    serverData as com.excell44.educam.data.local.entity.QuizResultEntity
                ) as T
            }
            else -> {
                // Par défaut, utilise Last-Write-Wins basé sur timestamp
                localData // Retourne local par défaut
            }
        }
    }
}
