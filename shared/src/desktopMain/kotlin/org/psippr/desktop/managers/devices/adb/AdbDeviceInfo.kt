package org.psippr.desktop.managers.devices.adb

/**
 * Facts read from an authorized Android device.
 *
 * @property manufacturer maker, e.g. `samsung`
 * @property model model name
 * @property androidVersion Android release, e.g. `14`
 * @property batteryPercent battery level, null when unknown
 * @property charging true while powered by a charger or the cable
 */
data class AdbDeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val batteryPercent: Int?,
    val charging: Boolean,
)
