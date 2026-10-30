package org.psippr.desktop.managers.devices

import kotlinx.coroutines.CoroutineScope
import org.psippr.desktop.managers.base.IDelegate

/**
 * Devices connected to the desktop (phones, tablets, ...) with their info and actions, from
 * every available provider. ADB is the first provider; others plug in behind this interface
 * without changing the devices tab. Calls block (devices answer slowly), so callers run them off
 * the UI thread.
 */
interface IDevicesManager : IDelegate {
    /** Connected devices of all providers the user allowed (connected or remembered). */
    fun devices(): List<ConnectedDevice>

    /**
     * Ids of the devices the user allowed (connected via the question or remembered, not
     * forgotten), plugged in or not; cheap, no device is asked. Empty where unsupported.
     */
    fun allowedIds(): List<String> = emptyList()

    /**
     * Watches for newly connected devices in [scope] and asks about each unknown one with a
     * notification (Connect / Dismiss, "Remember device"); no-op where unsupported.
     */
    fun startWatching(scope: CoroutineScope) {}

    /** Runs [action] on [device]; may open a file dialog first (e.g. to pick the files to send). */
    fun perform(
        device: ConnectedDevice,
        action: DeviceAction,
    ): DeviceActionResult

    companion object {
        /** No-op fallback used when no platform manager is available. */
        val EMPTY =
            object : IDevicesManager {
                override fun devices(): List<ConnectedDevice> = emptyList()

                override fun perform(
                    device: ConnectedDevice,
                    action: DeviceAction,
                ): DeviceActionResult = DeviceActionResult.UNSUPPORTED
            }
    }
}
