package com.excell44.educam.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Moniteur de connexion réseau avec Flow réactif.
 * Permet de réagir aux changements de connectivité en temps réel.
 */
class NetworkMonitor(private val context: Context) {
    
    private val connectivityManager = 
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Vérifie si le réseau est disponible actuellement.
     */
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Flow qui émet true/false selon l'état de la connexion.
     * Écoute les changements en temps réel.
     */
    fun observeNetworkState(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) && networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )
                trySend(hasInternet)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Émettre l'état initial
        trySend(isNetworkAvailable())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    /**
     * Obtient le type de connexion actuel.
     */
    fun getConnectionType(): ConnectionType {
        val network = connectivityManager.activeNetwork ?: return ConnectionType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network) 
            ?: return ConnectionType.NONE

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.MOBILE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            else -> ConnectionType.OTHER
        }
    }

    enum class ConnectionType {
        NONE, WIFI, MOBILE, ETHERNET, OTHER
    }
}

/**
 * Extension Composable pour observer l'état du réseau.
 */
@androidx.compose.runtime.Composable
fun rememberNetworkState(context: Context): androidx.compose.runtime.State<Boolean> {
    val networkMonitor = androidx.compose.runtime.remember { NetworkMonitor(context) }
    return networkMonitor.observeNetworkState()
        .collectAsState(initial = networkMonitor.isNetworkAvailable())
}
