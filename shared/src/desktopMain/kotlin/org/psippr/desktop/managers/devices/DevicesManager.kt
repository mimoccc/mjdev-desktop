package org.psippr.desktop.managers.devices

import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.log.Log
import org.psippr.desktop.managers.devices.adb.AdbDeviceProvider
import java.io.File

/**
 * Connected devices on the JVM desktop: every [IDeviceProvider] asked in turn. A provider that
 * fails (tool missing, device gone) only drops its own devices; the others still show.
 */
@Suppress("unused")
class DevicesManager(
    context: IDesktopContext,
) : IDevicesManager {
    private val providers: List<IDeviceProvider> =
        listOf(
            AdbDeviceProvider(
                picturesDir =
                    File(
                        context.currentUser.userDirs.picturesDirectory
                            .toString(),
                    ),
            ),
        )

    override fun devices(): List<ConnectedDevice> = providers.flatMap { provider ->
        runCatching { provider.devices() }
            .onFailure { e -> Log.w("devices of ${provider.name} failed: ${e.message}") }
            .getOrDefault(emptyList())
    }

    override fun perform(
        device: ConnectedDevice,
        action: DeviceAction,
    ): DeviceActionResult {
        val provider = providers.firstOrNull { it.name == device.provider } ?: return DeviceActionResult.UNSUPPORTED
        return runCatching { provider.perform(device, action) }
            .getOrElse { e -> DeviceActionResult(done = false, message = e.message ?: e.javaClass.simpleName) }
    }
}
