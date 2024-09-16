package eu.mjdev.desktop.helpers

import dev.tmapps.konnection.Konnection
import dev.tmapps.konnection.NetworkConnection

@Suppress("MemberVisibilityCanBePrivate", "unused")
class ConnectivityManager {
    val connection = Konnection.createInstance(enableDebugLog = false)

    // todo
    val isWifiAdapterAvailable
        get() = true

    // todo
    val isEthAdapterAvailable
        get() = true

    val isConnected
        get() = connection.isConnected()

    val isWifiConnection
        get() = isConnected && connection.getCurrentNetworkConnection() == NetworkConnection.WIFI

    val isEthConnection
        get() = isConnected && connection.getCurrentNetworkConnection() == NetworkConnection.ETHERNET

    val isMobileConnection
        get() = isConnected && connection.getCurrentNetworkConnection() == NetworkConnection.MOBILE

}