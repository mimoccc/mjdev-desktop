/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.connectivity

import org.mjdev.desktop.helpers.adb.AdbDiscover.Companion.adbDevicesHandler
import org.mjdev.desktop.helpers.system.shell.Shell
import org.mjdev.desktop.data.EthNetwork
import org.mjdev.desktop.data.WifiNetwork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okio.Path.Companion.toPath
import org.mjdev.desktop.data.BthDevice
import org.mjdev.desktop.data.NetDevice
import org.mjdev.desktop.extensions.PathExt.all
import org.mjdev.desktop.context.IDesktopContext

class ConnectivityManagerLinux(
    context: IDesktopContext,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : ConnectivityManagerStub(context) {
    override val allDevices: MutableMap<String, NetDevice>
        get() = (netDevicesDir.all + bthDevicesDir.all).associate { ndp ->
            Pair(ndp.name, NetDevice(ndp))
        }.toMutableMap()

    override val bthDevices: MutableMap<String, NetDevice>
        get() = allDevices.filter { e ->
            e.value.isBluetooth
        }.toMutableMap()

    override val ethDevices: MutableMap<String, NetDevice>
        get() = allDevices.filter { e ->
            e.value.isEth
        }.toMutableMap()

    override val wifiDevices: MutableMap<String, NetDevice>
        get() = allDevices.filter { e ->
            e.value.isWifi
        }.toMutableMap()

    // todo
    override val ethNetworks: MutableMap<String, EthNetwork> = mutableMapOf()

    override val wifiNetworks: MutableMap<String, WifiNetwork>
        get() {
            rescanWifi()
            return getWifiNetworks().associateBy { wi -> wi.name }.toMutableMap()
        }

    // todo
    override val bthNetworks: MutableMap<String, BthDevice> = mutableMapOf()

    @Suppress("unused")
    val adbHandler = adbDevicesHandler(
        coroutineScope = scope,
        onAdded = { device ->
            connectedDevices[device.name] = NetDevice((device.host + ":" + device.port).toPath())
        },
        onRemoved = { device ->
            connectedDevices.remove(device.name)
        }
    )

    private fun rescanWifi() = Shell.executeAndReadLines(
        CMD_NMCLI, *CMD_NMCLI_RESCAN
    )

    private fun getWifiNetworks() = Shell.executeAndReadLines(
        CMD_NMCLI, *CMD_NMCLI_GET_NETWORKS
    ).map { ws ->
        WifiNetwork(ws.split(":"))
    }.distinctBy { it.ssid }.sortedByDescending { it.isActive }

    override fun connectWifi(
        ssid: String
    ): Result<Boolean> = Shell.execute(
        CMD_NMCLI, "c", "up", "id", ssid
    ).let { p ->
        Result.success(p.isSuccess)
    }

    override fun connectWifi(
        ssid: String,
        password: String,
        deviceName: String, // ommited yet
        store: Boolean // ommited yet
    ): Result<Boolean> = Shell.execute(
        CMD_NMCLI, "device", "wifi", "connect", ssid, "password", password
    ).let { p ->
        Result.success(p.isSuccess)
    }

    companion object {
        val netDevicesDir = "/sys/class/net/".toPath(true)
        val bthDevicesDir = "/sys/class/bluetooth/".toPath(true)

        const val CMD_NMCLI = "nmcli"
        val CMD_NMCLI_RESCAN = arrayOf("dev", "wifi", "rescan")
        val CMD_NMCLI_GET_NETWORKS = arrayOf("-t", "-f", "ALL", "dev", "wifi")
    }

    // net devices  : ls /sys/class/net
    // connect gui  : nmtui
    // eth settings : iwconfig
    // nmcli -t -f ALL dev wifi
    // nmcli device show
    // nmcli c up id ssid
}
