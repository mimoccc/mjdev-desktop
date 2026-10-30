package org.psippr.desktop.managers.devices

import org.psippr.desktop.managers.base.IDelegate

/**
 * Devices connected to the desktop (phones, tablets, ...) with their info and actions, from
 * every available provider. ADB is the first provider; others plug in behind this interface
 * without changing the devices tab. Calls block (devices answer slowly), so callers run them off
 * the UI thread.
 */
interface IDevicesManager : IDelegate {
    /** All connected devices of all providers. */
    fun devices(): List<ConnectedDevice>

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
