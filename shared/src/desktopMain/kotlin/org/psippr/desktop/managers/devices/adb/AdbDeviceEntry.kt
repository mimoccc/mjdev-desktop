package org.psippr.desktop.managers.devices.adb

import org.psippr.desktop.managers.devices.DeviceState

/**
 * One line of `adb devices -l`.
 *
 * @property serial adb's id of the device (USB serial, or `host:port` / mDNS name when wireless)
 * @property state whether it can be used
 * @property model model adb reports, empty before the device is authorized
 * @property wireless true for a network (wireless debugging) connection
 */
data class AdbDeviceEntry(
    val serial: String,
    val state: DeviceState,
    val model: String,
    val wireless: Boolean,
)
