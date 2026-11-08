package org.psippr.desktop.managers.devices

/**
 * One way devices connect to the desktop (ADB today; others later). [DevicesManager] asks every
 * provider for its devices and hands each action to the provider named in the device.
 */
interface IDeviceProvider {
    /** Name stored in [ConnectedDevice.provider], unique among the providers. */
    val name: String

    /** Devices of this provider connected now. */
    fun devices(): List<ConnectedDevice>

    /** Runs [action] on one of this provider's devices. */
    fun perform(
        device: ConnectedDevice,
        action: DeviceAction,
    ): DeviceActionResult
}
