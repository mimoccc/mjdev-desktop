package eu.mjdev.desktop.managers.connectivity

import eu.mjdev.desktop.data.WifiInfo
import eu.mjdev.desktop.helpers.system.Shell
import eu.mjdev.desktop.log.Log

// todo improve
@Suppress("MemberVisibilityCanBePrivate", "unused")
class ConnectivityManager {
    val netDevices: List<String> = Shell.executeAndReadLines(
        "ls",
        "-1",
        "/sys/class/net/"
    ).filter { nd -> !nd.contentEquals("lo") }

    val isWifiAdapterAvailable: Boolean
        get() = netDevices.map { wa ->
            Shell.executeAndReadLines(
                "ls",
                "-1",
                "/sys/class/net/$wa/"
            ).any { w -> w.contentEquals("wireless") }
        }.any { hasWifi -> hasWifi }

    // todo
    val isEthAdapterAvailable
        get() = netDevices.isNotEmpty()

    // todo
    val isBthAdapterAvailable
        get() = netDevices.isNotEmpty()

    // todo dbus
    val wifiConnections
        get() = try {
//            rescanWifi()
//            getWifiNetworks()
            emptyList<WifiInfo>()
        } catch (e: Throwable) {
            Log.e(e)
            emptyList()
        }

//    fun rescanWifi() = Shell.executeAndReadLines(
//        CMD_NMCLI, *CMD_NMCLI_RESCAN
//    )

//    fun getWifiNetworks() = Shell.executeAndReadLines(
//        CMD_NMCLI, *CMD_NMCLI_GET_NETWORKS
//    ).map { ws ->
//        WifiInfo(ws.split(":"))
//    }.distinctBy { it.ssid }.sortedByDescending { it.isActive }

}
