/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.connectivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okio.Path.Companion.toPath
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.data.BthDevice
import org.mjdev.desktop.data.EthNetwork
import org.mjdev.desktop.data.NetDevice
import org.mjdev.desktop.data.WifiNetwork
import org.mjdev.desktop.extensions.PathExt.all
import org.mjdev.desktop.helpers.adb.AdbDiscover.Companion.adbDevicesHandler
import org.mjdev.desktop.helpers.system.shell.Shell

class ConnectivityManagerLinux(
    context: IDesktopContext,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : ConnectivityManagerStub(context) {
    override val allDevices: MutableMap<String, NetDevice>
        get() =
            (netDevicesDir.all + bthDevicesDir.all)
                .associate { ndp ->
                    Pair(ndp.name, NetDevice(ndp))
                }.toMutableMap()

    override val bthDevices: MutableMap<String, NetDevice>
        get() =
            allDevices
                .filter { e ->
                    e.value.isBluetooth
                }.toMutableMap()

    override val ethDevices: MutableMap<String, NetDevice>
        get() =
            allDevices
                .filter { e ->
                    e.value.isEth
                }.toMutableMap()

    override val wifiDevices: MutableMap<String, NetDevice>
        get() =
            allDevices
                .filter { e ->
                    e.value.isWifi
                }.toMutableMap()

    // todo
    override val ethNetworks: MutableMap<String, EthNetwork> = mutableMapOf()

    override val wifiNetworks: MutableMap<String, WifiNetwork>
        get() {
            rescanWifi()
            return getWifiNetworks().associateBy { wi -> wi.name }.toMutableMap()
        }

    override val bthNetworks: MutableMap<String, BthDevice>
        get() {
            val connected = bluetoothAddresses(*CMD_BTH_CONNECTED)
            val paired = bluetoothAddresses(*CMD_BTH_PAIRED)
            return Shell
                .executeAndReadLines(CMD_BLUETOOTHCTL, *CMD_BTH_DEVICES)
                .mapNotNull { line -> BTH_LINE_REGEX.matchEntire(line.trim()) }
                .associate { match ->
                    val address = match.groupValues[1]
                    address to
                        BthDevice(
                            name = match.groupValues[2].ifBlank { address },
                            address = address,
                            connected = address in connected,
                            paired = address in paired,
                        )
                }.toMutableMap()
        }

    override val isBluetoothPowered: Boolean
        get() = Shell
            .executeAndReadLines(CMD_BLUETOOTHCTL, CMD_BTH_SHOW)
            .any { it.trim().startsWith(BTH_POWERED_PREFIX) && it.contains(BTH_YES) }

    override fun setDeviceConnected(
        device: String,
        connected: Boolean,
    ): Result<Boolean> = succeeded(
        CMD_NMCLI,
        "device",
        if (connected) "connect" else "disconnect",
        device,
    )

    override fun deviceDetails(device: String): Map<String, String> {
        val result = linkedMapOf<String, String>()
        Shell
            .executeAndReadLines(CMD_NMCLI, "-t", "-f", NMCLI_DETAIL_FIELDS, "device", "show", device)
            .forEach { line ->
                // Terse nmcli output: KEY:value, a colon inside the value is escaped as \:
                val key = line.substringBefore(':').substringBefore('[')
                val value = line.substringAfter(':', "").replace("\\:", ":").trim()
                if (key.isNotBlank() && value.isNotBlank() && value != NMCLI_EMPTY) {
                    result[key] = result[key]?.let { "$it, $value" } ?: value
                }
            }
        return result
    }

    override fun setBluetoothPowered(powered: Boolean): Result<Boolean> = succeeded(
        CMD_BLUETOOTHCTL,
        "power",
        if (powered) BTH_ON else BTH_OFF,
    )

    override fun setBluetoothDeviceConnected(
        address: String,
        connected: Boolean,
    ): Result<Boolean> = succeeded(
        CMD_BLUETOOTHCTL,
        if (connected) "connect" else "disconnect",
        address,
    )

    private fun bluetoothAddresses(vararg args: String): Set<String> = Shell
        .executeAndReadLines(CMD_BLUETOOTHCTL, *args)
        .mapNotNull { line -> BTH_LINE_REGEX.matchEntire(line.trim())?.groupValues?.get(1) }
        .toSet()

    /** Runs a command and reports whether it exited with 0. */
    private fun succeeded(
        cmd: String,
        vararg args: String,
    ): Result<Boolean> = Shell.execute(cmd, *args).map { process -> process.exitValue() == 0 }

    @Suppress("unused")
    val adbHandler =
        adbDevicesHandler(
            coroutineScope = scope,
            onAdded = { device ->
                connectedDevices[device.name] = NetDevice((device.host + ":" + device.port).toPath())
            },
            onRemoved = { device ->
                connectedDevices.remove(device.name)
            },
        )

    private fun rescanWifi() = Shell.executeAndReadLines(
        CMD_NMCLI,
        *CMD_NMCLI_RESCAN,
    )

    private fun getWifiNetworks() = Shell
        .executeAndReadLines(
            CMD_NMCLI,
            *CMD_NMCLI_GET_NETWORKS,
        ).map { ws ->
            WifiNetwork(ws.split(":"))
        }.distinctBy { it.ssid }
        .sortedByDescending { it.isActive }

    override fun connectWifi(ssid: String): Result<Boolean> = Shell
        .execute(
            CMD_NMCLI,
            "c",
            "up",
            "id",
            ssid,
        ).let { p ->
            Result.success(p.isSuccess)
        }

    override fun connectWifi(
        ssid: String,
        password: String,
        deviceName: String, // ommited yet
        store: Boolean, // ommited yet
    ): Result<Boolean> =
        Shell
            .execute(
                CMD_NMCLI,
                "device",
                "wifi",
                "connect",
                ssid,
                "password",
                password,
            ).let { p ->
                Result.success(p.isSuccess)
            }

    companion object {
        val netDevicesDir = "/sys/class/net/".toPath(true)
        val bthDevicesDir = "/sys/class/bluetooth/".toPath(true)

        const val CMD_NMCLI = "nmcli"
        const val CMD_BLUETOOTHCTL = "bluetoothctl"
        const val CMD_BTH_SHOW = "show"
        val CMD_BTH_DEVICES = arrayOf("devices")
        val CMD_BTH_CONNECTED = arrayOf("devices", "Connected")
        val CMD_BTH_PAIRED = arrayOf("devices", "Paired")

        // `bluetoothctl devices` prints: Device AA:BB:CC:DD:EE:FF Some name
        private val BTH_LINE_REGEX = Regex("^Device\\s+(\\S+)\\s*(.*)$")
        private const val BTH_POWERED_PREFIX = "Powered:"
        private const val BTH_YES = "yes"
        private const val BTH_ON = "on"
        private const val BTH_OFF = "off"

        private const val NMCLI_DETAIL_FIELDS =
            "GENERAL.STATE,GENERAL.CONNECTION,IP4.ADDRESS,IP4.GATEWAY,IP4.DNS,GENERAL.HWADDR"
        private const val NMCLI_EMPTY = "--"
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
