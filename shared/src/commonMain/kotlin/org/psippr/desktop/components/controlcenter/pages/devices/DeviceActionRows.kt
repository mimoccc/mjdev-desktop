package org.psippr.desktop.components.controlcenter.pages.devices

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.psippr.desktop.components.controlcenter.settings.SettingsButtonRow
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.managers.devices.ConnectedDevice
import org.psippr.desktop.managers.devices.DeviceAction
import org.psippr.desktop.managers.devices.IDevicesManager

// old: restarts only
// private val CONFIRMED_ACTIONS = setOf(DeviceAction.REBOOT, DeviceAction.REBOOT_RECOVERY, DeviceAction.REBOOT_BOOTLOADER)

/** Actions that need a second click: restarting or forgetting a device by a misclick would be annoying. */
private val CONFIRMED_ACTIONS =
    setOf(DeviceAction.REBOOT, DeviceAction.REBOOT_RECOVERY, DeviceAction.REBOOT_BOOTLOADER, DeviceAction.FORGET)

/**
 * One row per action [device] offers. Actions run off the UI thread through [manager], one at a
 * time per device; their result is shown as a notification.
 */
@Suppress("FunctionName")
@Composable
fun DeviceActionRows(
    device: ConnectedDevice,
    manager: IDevicesManager,
) = withDesktopContext {
    val scope = rememberCoroutineScope()
    var running by remember(device.id) { mutableStateOf<DeviceAction?>(null) }
    var armed by remember(device.id) { mutableStateOf<DeviceAction?>(null) }
    device.actions.forEach { action ->
        val button =
            when (action) {
                running -> DevicesPageDefaults.RUNNING
                armed -> DevicesPageDefaults.CONFIRM
                else -> action.button
            }
        SettingsButtonRow(label = action.label, action = button, enabled = running == null) {
            if (action in CONFIRMED_ACTIONS && armed != action) {
                armed = action
                return@SettingsButtonRow
            }
            armed = null
            running = action
            scope.launch {
                // the action may open a file dialog, which blocks until it closes
                val result = withContext(Dispatchers.IO) { manager.perform(device, action) }
                if (result.message.isNotBlank()) context.notify(title = device.name, message = result.message)
                running = null
            }
        }
    }
}
