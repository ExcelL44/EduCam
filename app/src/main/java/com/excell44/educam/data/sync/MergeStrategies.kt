package com.excell44.educam.data.sync

import com.excell44.educam.data.local.entity.QuizResultEntity
import com.excell44.educam.data.model.User

/**
 * Stratégies de fusion (merge) pour différents types de données.
 * Chaque type de donnée a sa propre logique de merge intelligente.
 */
object MergeStrategies {

    /**
     * Fusionne deux profils utilisateur.
     * Priorité : Garde les valeurs les plus complètes.
     * 
     * Règles :
     * - Garde le nom le plus long (plus complet)
     * - Garde la dernière mise à jour du grade
     * - Fusionne les timestamps (garde le plus récent)
     */
    fun mergeUserProfiles(local: User, server: User): User {
        return local.copy(
            // Garde le nom le plus complet (non vide et plus long)
            name = when {
                local.name.isBlank() -> server.name
                server.name.isBlank() -> local.name
                local.name.length > server.name.length -> local.name
                else -> server.name
            },
            
            // Pour le grade, prend le serveur si disponible (source de vérité)
            gradeLevel = server.gradeLevel.takeIf { it.isNotBlank() } ?: local.gradeLevel,
            
            // Garde le compte offline local
            isOfflineAccount = local.isOfflineAccount,
            
            // Garde le trial le plus long si les deux existent
            trialExpiresAt = when {
                local.trialExpiresAt == null -> server.trialExpiresAt
                server.trialExpiresAt == null -> local.trialExpiresAt
                else -> maxOf(local.trialExpiresAt, server.trialExpiresAt)
            },
            
            // Garde les données serveur pour les champs critiques
            pseudo = server.pseudo,
            passwordHash = server.passwordHash.takeIf { it.isNotBlank() } ?: local.passwordHash,
            role = server.role,
            
            // Garde le createdAt le plus ancien
            createdAt = minOf(local.createdAt, server.createdAt)
        )
    }

    /**
     * Fusionne deux résultats de quiz.
     * Priorité : Garde le meilleur score.
     * 
     * Règles :
     * - Si même quiz : garde le meilleur score
     * - Garde le temps le plus rapide en cas d'égalité
     * 
     * Note: QuizResultEntity n'a pas de userId, donc on ne peut pas vérifier
     */
    fun mergeQuizResults(local: QuizResultEntity, server: QuizResultEntity): QuizResultEntity {
        // Vérifier qu'on parle bien du même quiz
        require(local.quizId == server.quizId) { "Cannot merge results from different quizzes" }
        
        return when {
            // Si le score local est meilleur (en pourcentage)
            local.score * 100 / local.maxScore > server.score * 100 / server.maxScore -> local
            
            // Si le score serveur est meilleur
            server.score * 100 / server.maxScore > local.score * 100 / local.maxScore -> server
            
            // Si scores égaux, garde le temps le plus rapide
            local.score * 100 / local.maxScore == server.score * 100 / server.maxScore -> {
                if (local.completionTime < server.completionTime) {
                    local
                } else {
                    server
                }
            }
            
            else -> server // Par défaut, serveur gagne
        }
    }

    /**
     * Fusionne des listes de données.
     * Utilisé pour les listes de quiz, questions, etc.
     * 
     * Règles :
     * - Fusionne les deux listes
     * - Élimine les doublons par ID
     * - Garde la version la plus récente de chaque élément
     */
    inline fun <T> mergeLists(
        localList: List<T>,
        serverList: List<T>,
        crossinline getId: (T) -> String,
        crossinline getTimestamp: (T) -> Long
    ): List<T> {
        val merged = mutableMapOf<String, T>()
        
        // Ajouter tous les éléments locaux
        localList.forEach { item ->
            merged[getId(item)] = item
        }
        
        // Fusionner avec les éléments serveur
        serverList.forEach { serverItem ->
            val id = getId(serverItem)
            val existing = merged[id]
            
            // Si l'élément existe déjà, garde le plus récent
            if (existing != null) {
                if (getTimestamp(serverItem) > getTimestamp(existing)) {
                    merged[id] = serverItem
                }
            } else {
                // Sinon, ajoute l'élément serveur
                merged[id] = serverItem
            }
        }
        
        return merged.values.toList()
    }

    /**
     * Fusionne des maps de paramètres/préférences.
     * Utilisé pour les settings, configurations, etc.
     * 
     * Règles :
     * - Serveur a priorité pour les paramètres critiques
     * - Local a priorité pour les préférences UI
     */
    fun mergePreferences(
        localPrefs: Map<String, Any>,
        serverPrefs: Map<String, Any>,
        serverPriorityKeys: Set<String> = setOf("subscription", "accountType", "expiresAt")
    ): Map<String, Any> {
        val merged = localPrefs.toMutableMap()
        
        serverPrefs.forEach { (key, value) ->
            // Si la clé est dans les priorités serveur, toujours prendre serveur
            if (key in serverPriorityKeys) {
                merged[key] = value
            } else {
                // Sinon, garde local si existe, sinon prend serveur
                if (!merged.containsKey(key)) {
                    merged[key] = value
                }
            }
        }
        
        return merged
    }

    /**
     * Fusionne des données numériques cumulatives.
     * Utilisé pour les stats, points, badges, etc.
     * 
     * Règles :
     * - Pour les stats cumulatives : somme ou max selon le contexte
     * - Pour les achievements : union des deux ensembles
     */
    fun mergeStats(
        localStats: Map<String, Int>,
        serverStats: Map<String, Int>,
        cumulativeKeys: Set<String> = setOf("totalPoints", "quizzesCompleted", "timeSpent")
    ): Map<String, Int> {
        val merged = mutableMapOf<String, Int>()
        val allKeys = localStats.keys + serverStats.keys
        
        allKeys.forEach { key ->
            val localValue = localStats[key] ?: 0
            val serverValue = serverStats[key] ?: 0
            
            merged[key] = if (key in cumulativeKeys) {
                // Pour les stats cumulatives, prendre le max (évite les doublons)
                maxOf(localValue, serverValue)
            } else {
                // Pour les autres, prendre la valeur serveur
                serverValue
            }
        }
        
        return merged
    }
}
