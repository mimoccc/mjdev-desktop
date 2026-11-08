package org.psippr.desktop.managers.devices.adb

import org.psippr.desktop.managers.devices.ConnectedDevice
import org.psippr.desktop.managers.devices.DeviceAction
import org.psippr.desktop.managers.devices.DeviceActionResult
import org.psippr.desktop.managers.devices.DeviceState
import org.psippr.desktop.managers.devices.IDeviceProvider
import java.io.File

/**
 * Android phones and tablets connected through ADB (USB debugging or wireless debugging), via
 * the local adb server. [picturesDir] receives screenshots.
 */
class AdbDeviceProvider(
    picturesDir: File,
) : IDeviceProvider {
    override val name: String = PROVIDER

    private val actions = AdbActions(picturesDir)

    override fun devices(): List<ConnectedDevice> {
        val answer = AdbCli.run(null, "devices", "-l")
        if (!answer.ok) return emptyList()
        return AdbOutput.devices(answer.lines).map(::device)
    }

    override fun perform(
        device: ConnectedDevice,
        action: DeviceAction,
    ): DeviceActionResult = actions.perform(serialOf(device), device.name, action)

    private fun device(entry: AdbDeviceEntry): ConnectedDevice {
        val info = if (entry.state == DeviceState.READY) info(entry.serial) else null
        return ConnectedDevice(
            id = ID_PREFIX + entry.serial,
            provider = PROVIDER,
            name = displayName(entry, info),
            state = entry.state,
            summary = summary(entry, info),
            info = rows(entry, info),
            actions = AdbActions.available(entry),
        )
    }

    private fun info(serial: String): AdbDeviceInfo? {
        val answer = AdbCli.run(serial, "shell", INFO_COMMAND)
        return if (answer.ok) AdbOutput.info(answer.lines) else null
    }

    // "Samsung Galaxy S23", else adb's model, else the serial
    private fun displayName(
        entry: AdbDeviceEntry,
        info: AdbDeviceInfo?,
    ): String {
        val model = info?.model?.ifBlank { null } ?: entry.model.ifBlank { null } ?: return entry.serial
        val maker = info?.manufacturer?.replaceFirstChar { it.uppercase() }.orEmpty()
        return if (maker.isBlank() || model.startsWith(maker, ignoreCase = true)) model else "$maker $model"
    }

    private fun rows(
        entry: AdbDeviceEntry,
        info: AdbDeviceInfo?,
    ): List<Pair<String, String>> = buildList {
        if (entry.state != DeviceState.READY) add(LABEL_STATE to entry.state.hint)
        info?.androidVersion?.ifBlank { null }?.let { add(LABEL_SYSTEM to "$ANDROID $it") }
        info?.batteryPercent?.let { level ->
            add(LABEL_BATTERY to "$level$PERCENT" + if (info.charging) CHARGING else "")
        }
        add(LABEL_CONNECTION to if (entry.wireless) WIRELESS else USB)
        add(LABEL_ID to entry.serial)
    }

    // "Android 14 · 87 %, charging" for the list; the state for a device not ready
    private fun summary(
        entry: AdbDeviceEntry,
        info: AdbDeviceInfo?,
    ): String {
        if (entry.state != DeviceState.READY) return entry.state.short
        val parts =
            listOfNotNull(
                info?.androidVersion?.ifBlank { null }?.let { "$ANDROID $it" },
                info?.batteryPercent?.let { level -> "$level$PERCENT" + if (info.charging) CHARGING else "" },
            )
        return parts.joinToString(SUMMARY_SEPARATOR)
    }

    private fun serialOf(device: ConnectedDevice): String = device.id.removePrefix(ID_PREFIX)

    companion object {
        /** Provider name stored in its devices. */
        const val PROVIDER = "adb"

        /** Prefix of the device ids of this provider. */
        private const val ID_PREFIX = "$PROVIDER:"

        /** One shell call for the facts, read by [AdbOutput.info]. */
        private const val INFO_COMMAND =
            "getprop ro.product.manufacturer; getprop ro.product.model; " +
                "getprop ro.build.version.release; dumpsys battery"

        private const val LABEL_STATE = "State"
        private const val LABEL_SYSTEM = "System"
        private const val LABEL_BATTERY = "Battery"
        private const val LABEL_CONNECTION = "Connection"
        private const val LABEL_ID = "Id"
        private const val ANDROID = "Android"
        private const val PERCENT = " %"
        private const val CHARGING = ", charging"
        private const val USB = "USB cable"
        private const val WIRELESS = "Wireless"
        private const val SUMMARY_SEPARATOR = " · "
    }
}
