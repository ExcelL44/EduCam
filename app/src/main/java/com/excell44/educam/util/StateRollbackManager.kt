package com.excell44.educam.util

import java.util.ArrayDeque

/**
 * Gestionnaire d'historique d'états pour permettre le rollback en cas d'erreur.
 * Garde une pile des N derniers états.
 */
class StateRollbackManager<T>(private val maxHistory: Int = 5) {
    private val history = ArrayDeque<T>(maxHistory)

    /**
     * Sauvegarde l'état actuel avant une modification risquée.
     */
    fun saveState(state: T) {
        if (history.size >= maxHistory) {
            history.removeFirst()
        }
        history.addLast(state)
    }

    /**
     * Récupère le dernier état sauvegardé et le retire de la pile.
     * @return L'état précédent ou null si l'historique est vide.
     */
    fun rollback(): T? {
        return if (history.isNotEmpty()) history.removeLast() else null
    }
    
    /**
     * Vide l'historique.
     */
    fun clear() {
        history.clear()
    }
    
    fun canRollback(): Boolean = history.isNotEmpty()
}
