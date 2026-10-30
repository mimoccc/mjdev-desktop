/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.psippr.desktop.managers.connectivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okio.Path.Companion.toPath
import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.data.BthDevice
import org.psippr.desktop.data.EthNetwork
import org.psippr.desktop.data.Ipv4Settings
import org.psippr.desktop.data.NetDetail
import org.psippr.desktop.data.NetDevice
import org.psippr.desktop.data.WifiNetwork
import org.psippr.desktop.extensions.PathExt.all
import org.psippr.desktop.helpers.adb.AdbDiscover.Companion.adbDevicesHandler
import org.psippr.desktop.helpers.system.shell.Shell

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

    // old: NetDevice.isEth looks for a sysfs "ethernet" folder the kernel never creates, so no
    // wired adapter was ever found and the Ethernet tab never showed
//    override val ethDevices: MutableMap<String, NetDevice>
//        get() =
//            allDevices
//                .filter { e ->
//                    e.value.isEth
//                }.toMutableMap()

    /** Wired adapters as NetworkManager reports them (type ethernet, managed by it). */
    override val ethDevices: MutableMap<String, NetDevice>
        get() {
            val names = NmcliOutput.ethernetDevices(Shell.executeAndReadLines(CMD_NMCLI, *CMD_NMCLI_DEVICE_TYPES))
            return allDevices.filterKeys { name -> name in names }.toMutableMap()
        }

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

    // old: bluetoothctl without a timeout (hung the page while bluetoothd was unreachable) and
    // every nameless beacon of a scan listed under its address
//    override val bthNetworks: MutableMap<String, BthDevice>
//        get() {
//            val connected = bluetoothAddresses(*CMD_BTH_CONNECTED)
//            val paired = bluetoothAddresses(*CMD_BTH_PAIRED)
//            return Shell
//                .executeAndReadLines(CMD_BLUETOOTHCTL, *CMD_BTH_DEVICES)
//                .mapNotNull { line -> BTH_LINE_REGEX.matchEntire(line.trim()) }
//                .associate { match ->
//                    val address = match.groupValues[1]
//                    address to
//                        BthDevice(
//                            name = match.groupValues[2].ifBlank { address },
//                            address = address,
//                            connected = address in connected,
//                            paired = address in paired,
//                        )
//                }.toMutableMap()
//        }
//
//    override val isBluetoothPowered: Boolean
//        get() =
//            Shell
//                .executeAndReadLines(CMD_BLUETOOTHCTL, CMD_BTH_SHOW)
//                .any { it.trim().startsWith(BTH_POWERED_PREFIX) && it.contains(BTH_YES) }

    override val bthNetworks: MutableMap<String, BthDevice>
        get() {
            val connected = BluetoothctlOutput.addresses(bluetoothctl(*CMD_BTH_CONNECTED))
            val paired = BluetoothctlOutput.addresses(bluetoothctl(*CMD_BTH_PAIRED))
            return BluetoothctlOutput
                .devices(bluetoothctl(*CMD_BTH_DEVICES), connected, paired)
                .associateBy { device -> device.address }
                .toMutableMap()
        }

    override val isBluetoothPowered: Boolean
        get() = BluetoothctlOutput.isPowered(bluetoothctl(CMD_BTH_SHOW))

    override fun setDeviceConnected(
        device: String,
        connected: Boolean,
    ): Result<Boolean> = succeeded(
        CMD_NMCLI,
        "device",
        if (connected) "connect" else "disconnect",
        device,
    )

    override fun ipv4Settings(device: String): Ipv4Settings? {
        val connection = deviceDetails(device)[NetDetail.Connection.key] ?: return null
        val lines =
            Shell.executeAndReadLines(
                CMD_NMCLI,
                "-t",
                "-f",
                NmcliOutput.IPV4_FIELDS,
                "connection",
                "show",
                connection,
            )
        return if (lines.isEmpty()) null else NmcliOutput.ipv4(connection, lines)
    }

    // modify the profile, then bring it up again so the new settings take effect at once
    override fun applyIpv4Settings(settings: Ipv4Settings): Result<Boolean> {
        val modified = succeeded(CMD_NMCLI, *NmcliOutput.modifyArgs(settings).toTypedArray())
        if (!modified.getOrDefault(false)) return modified
        return succeeded(CMD_NMCLI, "connection", "up", settings.connection)
    }

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

    // old: powering on failed with org.bluez.Error.Blocked while the adapter was rfkill soft-blocked
//    override fun setBluetoothPowered(powered: Boolean): Result<Boolean> = succeeded(
//        CMD_BLUETOOTHCTL,
//        "power",
//        if (powered) BTH_ON else BTH_OFF,
//    )

    // old: right after the rfkill unblock bluez is still enabling the adapter and answers
    // org.bluez.Error.Busy, so the single power on failed and the switch did nothing
//    override fun setBluetoothPowered(powered: Boolean): Result<Boolean> {
//        if (powered) succeeded(CMD_RFKILL, RFKILL_UNBLOCK, RFKILL_BLUETOOTH)
//        return succeeded(
//            CMD_BLUETOOTHCTL,
//            "power",
//            if (powered) BTH_ON else BTH_OFF,
//        )
//    }

    // old: bluetoothctl without a timeout, a hanging call froze the switch
//    override fun setBluetoothPowered(powered: Boolean): Result<Boolean> {
//        if (powered) succeeded(CMD_RFKILL, RFKILL_UNBLOCK, RFKILL_BLUETOOTH)
//        return retrying(BTH_POWER_ATTEMPTS, BTH_POWER_RETRY_MS) {
//            succeeded(CMD_BLUETOOTHCTL, "power", if (powered) BTH_ON else BTH_OFF)
//        }
//    }

    /**
     * Powers the adapter, lifting an rfkill soft block first (bluez refuses to power a blocked
     * adapter) and retrying while bluez is still enabling the freshly unblocked adapter.
     */
    override fun setBluetoothPowered(powered: Boolean): Result<Boolean> {
        if (powered) succeeded(CMD_RFKILL, RFKILL_UNBLOCK, RFKILL_BLUETOOTH)
        return retrying(BTH_POWER_ATTEMPTS, BTH_POWER_RETRY_MS) {
            bluetoothctlSucceeded(BTH_TIMEOUT_S, BTH_POWER, if (powered) BTH_ON else BTH_OFF)
        }
    }

    // discovery runs for BTH_SCAN_S seconds: bluetoothctl's own timeout ends the scan, the outer
    // limit only guards against a hanging bluetoothd
    override fun scanBluetooth(): Result<Boolean> =
        bluetoothctlSucceeded(BTH_SCAN_LIMIT_S, BTH_TIMEOUT_OPTION, BTH_SCAN_S, BTH_SCAN, BTH_ON)

    // old: connect only, a device found by a scan (not paired yet) could never be connected
//    override fun setBluetoothDeviceConnected(
//        address: String,
//        connected: Boolean,
//    ): Result<Boolean> = succeeded(
//        CMD_BLUETOOTHCTL,
//        if (connected) "connect" else "disconnect",
//        address,
//    )

    /**
     * Connects or disconnects [address]. A device not paired yet is paired and trusted first;
     * trusted devices also reconnect by themselves when they come back in range.
     */
    override fun setBluetoothDeviceConnected(
        address: String,
        connected: Boolean,
    ): Result<Boolean> {
        if (!connected) return bluetoothctlSucceeded(BTH_TIMEOUT_S, BTH_DISCONNECT, address)
        if (address !in BluetoothctlOutput.addresses(bluetoothctl(*CMD_BTH_PAIRED))) {
            val paired = bluetoothctlSucceeded(BTH_CONNECT_TIMEOUT_S, BTH_AGENT_OPTION, BTH_AGENT, BTH_PAIR, address)
            if (!paired.getOrDefault(false)) return paired
            bluetoothctlSucceeded(BTH_TIMEOUT_S, BTH_TRUST, address)
        }
        return bluetoothctlSucceeded(BTH_CONNECT_TIMEOUT_S, BTH_CONNECT, address)
    }

    /**
     * bluetoothctl answer lines. The coreutils limit kills a call an unreachable bluetoothd would
     * block forever; bluetoothctl's own --timeout is not used, it always waits its full length.
     */
    private fun bluetoothctl(vararg args: String): List<String> =
        Shell.executeAndReadLines(CMD_TIMEOUT, BTH_TIMEOUT_S, CMD_BLUETOOTHCTL, *args)

    /** Runs bluetoothctl for at most [timeoutSeconds] and reports whether it exited with 0. */
    private fun bluetoothctlSucceeded(
        timeoutSeconds: String,
        vararg args: String,
    ): Result<Boolean> = succeeded(CMD_TIMEOUT, timeoutSeconds, CMD_BLUETOOTHCTL, *args)

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
        ).let(::parseWifiNetworks)

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
        /** Runs [action] until it reports true, at most [attempts] times, [delayMs] apart. */
        internal fun retrying(
            attempts: Int,
            delayMs: Long,
            action: () -> Result<Boolean>,
        ): Result<Boolean> {
            var result = action()
            repeat(attempts - 1) {
                if (result.getOrDefault(false)) return result
                Thread.sleep(delayMs)
                result = action()
            }
            return result
        }

        /**
         * Parses terse `nmcli dev wifi` lines into one network per SSID. An SSID broadcast by
         * several access points keeps the active one, so the connected network is never dropped.
         */
        fun parseWifiNetworks(lines: List<String>): List<WifiNetwork> = lines
            .map { ws -> WifiNetwork(ws.split(":")) }
            .sortedByDescending { it.isActive }
            .distinctBy { it.ssid }

        val netDevicesDir = "/sys/class/net/".toPath(true)
        val bthDevicesDir = "/sys/class/bluetooth/".toPath(true)

        const val CMD_NMCLI = "nmcli"
        const val CMD_BLUETOOTHCTL = "bluetoothctl"
        const val CMD_BTH_SHOW = "show"

        /** rfkill tool, the user owns /dev/rfkill in a logind seat so no root is needed. */
        const val CMD_RFKILL = "rfkill"
        private const val RFKILL_UNBLOCK = "unblock"
        private const val RFKILL_BLUETOOTH = "bluetooth"
        val CMD_BTH_DEVICES = arrayOf("devices")
        val CMD_BTH_CONNECTED = arrayOf("devices", "Connected")
        val CMD_BTH_PAIRED = arrayOf("devices", "Paired")

        // `bluetoothctl devices` prints: Device AA:BB:CC:DD:EE:FF Some name
        private val BTH_LINE_REGEX = Regex("^Device\\s+(\\S+)\\s*(.*)$")
        private const val BTH_POWERED_PREFIX = "Powered:"
        private const val BTH_YES = "yes"
        private const val BTH_ON = "on"
        private const val BTH_OFF = "off"

        /** coreutils command killing a call that runs longer than its limit (exit code 124). */
        private const val CMD_TIMEOUT = "timeout"

        /** bluetoothctl option making a scan last a given number of seconds. */
        private const val BTH_TIMEOUT_OPTION = "--timeout"

        /** Seconds a plain bluetoothctl query or switch may take. */
        private const val BTH_TIMEOUT_S = "5"

        /** Seconds a pair or connect may take (the device answers slowly). */
        private const val BTH_CONNECT_TIMEOUT_S = "20"

        /** Seconds one device search runs. */
        private const val BTH_SCAN_S = "8"

        /** Seconds after which a device search is killed (search time plus start up). */
        private const val BTH_SCAN_LIMIT_S = "15"

        /** bluetoothctl option registering a pairing agent for the call. */
        private const val BTH_AGENT_OPTION = "--agent"

        /** Agent that pairs without asking for a PIN (headphones, speakers, mice). */
        private const val BTH_AGENT = "NoInputNoOutput"
        private const val BTH_POWER = "power"
        private const val BTH_SCAN = "scan"
        private const val BTH_PAIR = "pair"
        private const val BTH_TRUST = "trust"
        private const val BTH_CONNECT = "connect"
        private const val BTH_DISCONNECT = "disconnect"

        /** Power on tries while bluez enables a just unblocked adapter (answers Busy meanwhile). */
        private const val BTH_POWER_ATTEMPTS = 10

        /** Pause between two power tries in milliseconds, 10 x 300 ms covers the ~1 s enabling. */
        private const val BTH_POWER_RETRY_MS = 300L

        private const val NMCLI_DETAIL_FIELDS =
            "GENERAL.STATE,GENERAL.CONNECTION,IP4.ADDRESS,IP4.GATEWAY,IP4.DNS,GENERAL.HWADDR"
        private const val NMCLI_EMPTY = "--"
        val CMD_NMCLI_RESCAN = arrayOf("dev", "wifi", "rescan")

        /** Device list with type and state, read by [NmcliOutput.ethernetDevices]. */
        val CMD_NMCLI_DEVICE_TYPES = arrayOf("-t", "-f", "DEVICE,TYPE,STATE", "device")
        val CMD_NMCLI_GET_NETWORKS = arrayOf("-t", "-f", "ALL", "dev", "wifi")
    }

    // net devices  : ls /sys/class/net
    // connect gui  : nmtui
    // eth settings : iwconfig
    // nmcli -t -f ALL dev wifi
    // nmcli device show
    // nmcli c up id ssid
}
