package org.psippr.desktop.managers.devices

/**
 * Something the user can do with a connected device. A provider lists the actions its device
 * supports; the devices tab shows one row per action.
 *
 * @property label what the action does, shown as the row label
 * @property button text of the row's button
 */
enum class DeviceAction(
    val label: String,
    val button: String,
) {
    /** Copy files from the desktop to the device's Download folder. */
    SEND_FILES("Send files to its Download folder", "Send…"),

    /** Install an app package on the device. */
    INSTALL_APP("Install an app (.apk)", "Install…"),

    /** Save a screenshot of the device screen into the Pictures folder. */
    SCREENSHOT("Screenshot to Pictures", "Take"),

    /** Open a shell of the device in a terminal window. */
    OPEN_SHELL("Shell in a terminal", "Open"),

    /** Switch the device to a wireless connection, so the cable can be unplugged. */
    ENABLE_WIRELESS("Switch to wireless", "Switch"),

    /** End a wireless connection. */
    DISCONNECT("Disconnect", "Disconnect"),

    /** Restart the device. */
    REBOOT("Restart", "Restart"),

    /** Restart the device into its recovery system. */
    REBOOT_RECOVERY("Restart into recovery", "Restart"),

    /** Restart the device into its bootloader. */
    REBOOT_BOOTLOADER("Restart into bootloader", "Restart"),

    /** Connect again to a remembered wireless device that is not connected now. */
    RECONNECT("Reconnect over the network", "Reconnect"),

    /** Stop remembering the device and hide it; connecting it again asks again. */
    FORGET("Forget this device", "Forget"),
}
