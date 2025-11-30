package com.excell44.educam.domain.model

/**
 * Connection state for graceful UX degradation.
 * Instead of blocking UI when offline, show appropriate feedback.
 */
sealed class ConnectionState {
    /**
     * Device is online, all features available.
     */
    object Online : ConnectionState()
    
    /**
     * Actively syncing data to/from server.
     */
    object Syncing : ConnectionState()
    
    /**
     * Device is offline, local-only mode.
     * Save to Room, sync later automatically.
     */
    object Offline : ConnectionState()
}
