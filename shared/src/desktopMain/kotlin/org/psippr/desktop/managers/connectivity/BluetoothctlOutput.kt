package org.psippr.desktop.managers.connectivity

import org.psippr.desktop.data.BthDevice

/**
 * Reads `bluetoothctl` output. Besides its answer bluetoothctl prints colored event lines
 * (`[NEW] Device …`, `[NEW] Media …`), so lines are cleaned before matching.
 */
object BluetoothctlOutput {
    // `Device AA:BB:CC:DD:EE:FF Some name`, the address in group 1, the name in group 2
    private val DEVICE_LINE = Regex("^Device\\s+((?:[0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2})\\s*(.*)$")

    // terminal color sequences such as ESC[0;92m
    private val COLOR_CODE = Regex("\u001B\\[[0-9;]*m")

    // event prefix of lines bluetoothctl prints on its own: [NEW], [CHG], [DEL]
    private val EVENT_PREFIX = Regex("^\\[(NEW|CHG|DEL)]\\s*")

    /** Separator bluez puts between the bytes of an address it uses as name of a nameless device. */
    private const val NAMELESS_SEPARATOR = "-"

    /** Address separator in bluetoothctl output. */
    private const val ADDRESS_SEPARATOR = ":"

    /** Line of `bluetoothctl show` telling whether the adapter is on. */
    private const val POWERED_PREFIX = "Powered:"

    /** Value of [POWERED_PREFIX] for an adapter that is on. */
    private const val YES = "yes"

    /** Addresses of the `Device` answer lines of [lines] (event lines are skipped). */
    fun addresses(lines: List<String>): Set<String> = answerDevices(lines).map { (address, _) -> address }.toSet()

    /**
     * Devices of a `bluetoothctl devices` answer, flagged with [connected] / [paired]. A device
     * bluez knows no name of (its name is its address) is left out unless it is paired: a scan
     * finds many of those beacons and they cannot be told apart.
     */
    fun devices(
        lines: List<String>,
        connected: Set<String>,
        paired: Set<String>,
    ): List<BthDevice> = answerDevices(lines)
        .map { (address, name) ->
            BthDevice(
                name = name.ifBlank { address },
                address = address,
                connected = address in connected,
                paired = address in paired,
            )
        }.filter { device -> device.paired || !isNameless(device) }
        .distinctBy { device -> device.address }

    /** True when `bluetoothctl show` [lines] report the adapter as on. */
    fun isPowered(lines: List<String>): Boolean = lines
        .map(::clean)
        .any { line -> line.startsWith(POWERED_PREFIX) && line.contains(YES) }

    // device lines of the answer itself; event lines ([NEW] Device …) describe changes, not the list
    private fun answerDevices(lines: List<String>): List<Pair<String, String>> = lines
        .map { line -> COLOR_CODE.replace(line, "").trim() }
        .filterNot { line -> EVENT_PREFIX.containsMatchIn(line) }
        .mapNotNull { line -> DEVICE_LINE.matchEntire(line) }
        .map { match -> match.groupValues[1] to match.groupValues[2].trim() }

    private fun clean(line: String): String = EVENT_PREFIX.replace(COLOR_CODE.replace(line, "").trim(), "")

    private fun isNameless(device: BthDevice): Boolean = device.name.isBlank() ||
        device.name.equals(device.address, ignoreCase = true) ||
        device.name.equals(device.address.replace(ADDRESS_SEPARATOR, NAMELESS_SEPARATOR), ignoreCase = true)
}
