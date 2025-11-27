package com.excell44.educam.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache mémoire thread-safe avec TTL (Time To Live).
 * 
 * @param defaultTtlMs Durée de vie par défaut des entrées en millisecondes (5 minutes par défaut)
 */
class MemoryCache<K, V>(
    private val defaultTtlMs: Long = 5 * 60 * 1000L // 5 minutes
) {
    private data class CacheEntry<V>(
        val value: V,
        val expiresAt: Long
    )

    private val cache = ConcurrentHashMap<K, CacheEntry<V>>()
    private val mutex = Mutex()

    /**
     * Récupère une valeur du cache.
     * @return La valeur si elle existe et n'est pas expirée, null sinon
     */
    suspend fun get(key: K): V? = mutex.withLock {
        val entry = cache[key] ?: return null
        
        // Vérifier l'expiration
        if (System.currentTimeMillis() > entry.expiresAt) {
            cache.remove(key)
            return null
        }
        
        return entry.value
    }

    /**
     * Ajoute ou met à jour une valeur dans le cache.
     * @param key La clé
     * @param value La valeur
     * @param ttlMs Durée de vie personnalisée (optionnel)
     */
    suspend fun put(key: K, value: V, ttlMs: Long = defaultTtlMs): Unit = mutex.withLock {
        val expiresAt = System.currentTimeMillis() + ttlMs
        cache[key] = CacheEntry(value, expiresAt)
    }

    /**
     * Supprime une entrée du cache.
     */
    suspend fun remove(key: K): Unit = mutex.withLock {
        cache.remove(key)
        Unit
    }

    /**
     * Vide complètement le cache.
     */
    suspend fun clear(): Unit = mutex.withLock {
        cache.clear()
    }

    /**
     * Invalide les entrées expirées.
     */
    suspend fun evictExpired() = mutex.withLock {
        val now = System.currentTimeMillis()
        cache.entries.removeIf { it.value.expiresAt < now }
    }

    /**
     * Retourne la taille actuelle du cache.
     */
    fun size(): Int = cache.size
}
