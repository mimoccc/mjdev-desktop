package org.psippr.desktop.managers.devices

/**
 * Devices the user chose to remember (Connect with "Remember device"): they are shown and used
 * at once when they connect, without asking. Stored as JSON; entries are only ever added.
 *
 * @property remembered device ids ([ConnectedDevice.id]) of the remembered devices
 */
data class KnownDevices(
    val remembered: MutableList<String> = mutableListOf(),
)
