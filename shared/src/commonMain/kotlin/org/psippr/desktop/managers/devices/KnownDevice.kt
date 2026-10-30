package org.psippr.desktop.managers.devices

/**
 * A device the user remembered (Connect with "Remember device").
 *
 * @property id the device id ([ConnectedDevice.id])
 * @property name its name when it was remembered, shown while it is not connected
 * @property provider provider that handles it ([ConnectedDevice.provider])
 * @property forgotten true after "Forget this device"; entries are flagged, never deleted
 */
data class KnownDevice(
    val id: String = "",
    val name: String = "",
    val provider: String = "",
    val forgotten: Boolean = false,
)
