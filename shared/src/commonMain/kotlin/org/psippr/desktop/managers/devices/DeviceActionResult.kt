package org.psippr.desktop.managers.devices

/**
 * Outcome of a [DeviceAction].
 *
 * @property done true when the action finished; false when it failed or the user cancelled
 * @property message what happened, shown under the device; empty for a silent cancel
 */
data class DeviceActionResult(
    val done: Boolean,
    val message: String = "",
) {
    companion object {
        /** The user closed a file dialog: nothing happened, nothing to report. */
        val CANCELLED = DeviceActionResult(done = false)

        /** Result of a platform without device support. */
        val UNSUPPORTED = DeviceActionResult(done = false, message = "Not supported here.")
    }
}
