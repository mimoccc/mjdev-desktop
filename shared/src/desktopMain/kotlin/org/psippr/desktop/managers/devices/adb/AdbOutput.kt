package org.psippr.desktop.managers.devices.adb

import org.psippr.desktop.managers.devices.DeviceState

/** Reads the text adb prints: the device list and the device info answer. */
object AdbOutput {
    /** Header line of `adb devices`. */
    private const val LIST_HEADER = "List of devices"

    /** adb's state of a usable device. */
    private const val STATE_DEVICE = "device"
    private const val STATE_UNAUTHORIZED = "unauthorized"

    /** `model:` property of `adb devices -l`; underscores stand for spaces. */
    private const val MODEL_KEY = "model:"
    private const val MODEL_SPACE = "_"

    /** Marks of a network device id: `host:port` or an mDNS name of wireless debugging. */
    private const val PORT_SEPARATOR = ":"
    private const val MDNS_MARK = "._adb-tls-connect."

    /** Battery lines of `dumpsys battery`. */
    private const val BATTERY_LEVEL = "level:"
    private val CHARGING_LINES = listOf("AC powered: true", "USB powered: true", "Wireless powered: true")

    /** `inet 192.168.1.5/24` of `ip addr`. */
    private val INET = Regex("inet (\\d{1,3}(?:\\.\\d{1,3}){3})/")

    /** Devices of an `adb devices -l` answer. */
    fun devices(lines: List<String>): List<AdbDeviceEntry> = lines
        .map { line -> line.trim() }
        .filter { line -> line.isNotEmpty() && !line.startsWith(LIST_HEADER) && !line.startsWith("*") }
        .mapNotNull { line ->
            val parts = line.split(Regex("\\s+"))
            val serial = parts.getOrNull(0) ?: return@mapNotNull null
            val state = parts.getOrNull(1) ?: return@mapNotNull null
            AdbDeviceEntry(
                serial = serial,
                state = state(state),
                model =
                    parts
                        .firstOrNull { it.startsWith(MODEL_KEY) }
                        ?.removePrefix(MODEL_KEY)
                        ?.replace(MODEL_SPACE, " ")
                        .orEmpty(),
                wireless = serial.contains(PORT_SEPARATOR) || serial.contains(MDNS_MARK),
            )
        }

    /**
     * Facts of the info answer: the first three lines are manufacturer, model and Android
     * version (getprop), then `dumpsys battery`.
     */
    fun info(lines: List<String>): AdbDeviceInfo {
        val props = lines.take(PROP_LINES).map { it.trim() }
        val level =
            lines
                .map { it.trim() }
                .firstOrNull { it.startsWith(BATTERY_LEVEL) }
                ?.removePrefix(BATTERY_LEVEL)
                ?.trim()
                ?.toIntOrNull()
        return AdbDeviceInfo(
            manufacturer = props.getOrNull(0).orEmpty(),
            model = props.getOrNull(1).orEmpty(),
            androidVersion = props.getOrNull(2).orEmpty(),
            batteryPercent = level,
            charging = lines.any { line -> CHARGING_LINES.any { line.trim() == it } },
        )
    }

    /** First IPv4 address in an `ip addr` answer, null when the device has none. */
    fun address(lines: List<String>): String? =
        lines.firstNotNullOfOrNull { line -> INET.find(line)?.groupValues?.get(1) }

    private fun state(adbState: String): DeviceState = when (adbState) {
        STATE_DEVICE -> DeviceState.READY
        STATE_UNAUTHORIZED -> DeviceState.UNAUTHORIZED
        else -> DeviceState.OFFLINE
    }

    /** Lines of getprop answers at the start of the info answer. */
    private const val PROP_LINES = 3
}
