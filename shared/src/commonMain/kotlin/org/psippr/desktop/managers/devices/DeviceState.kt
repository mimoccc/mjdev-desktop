package org.psippr.desktop.managers.devices

/**
 * Whether a connected device can be used.
 *
 * @property hint what the user has to do, empty when nothing
 */
enum class DeviceState(
    val hint: String,
) {
    /** Connected and allowed: info and actions work. */
    READY(""),

    /** Connected, but the device has not allowed this computer yet. */
    UNAUTHORIZED("Allow this computer on the device (the debugging prompt on its screen)."),

    /** Seen, but not answering. */
    OFFLINE("Not answering. Reconnect the cable or restart debugging on the device."),
}
