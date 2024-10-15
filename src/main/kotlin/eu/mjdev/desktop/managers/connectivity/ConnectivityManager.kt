package eu.mjdev.desktop.managers.connectivity

import eu.mjdev.desktop.data.WifiInfo
import eu.mjdev.desktop.helpers.system.Shell
import eu.mjdev.desktop.helpers.system.Shell.Companion.CMD_NMCLI
import eu.mjdev.desktop.helpers.system.Shell.Companion.CMD_NMCLI_GET_NETWORKS
import eu.mjdev.desktop.helpers.system.Shell.Companion.CMD_NMCLI_RESCAN

@Suppress("MemberVisibilityCanBePrivate", "unused")
class ConnectivityManager {
    // todo
    val isWifiAdapterAvailable
        get() = true

    // todo
    val isEthAdapterAvailable
        get() = true

    // todo
    val isBthAdapterAvailable
        get() = true

    // todo dbus
    val wifiConnections
        get() = try {
            rescanWifi()
            getWifiNetworks()
        } catch (t: Throwable) {
            t.printStackTrace()
            emptyList()
        }

    fun rescanWifi() = Shell.executeAndReadLines(
        CMD_NMCLI, *CMD_NMCLI_RESCAN
    )

    fun getWifiNetworks() = Shell.executeAndReadLines(
        CMD_NMCLI, *CMD_NMCLI_GET_NETWORKS
    ).map { ws ->
        WifiInfo(ws.split(":"))
    }.distinctBy { it.ssid }.sortedByDescending { it.isActive }

}
