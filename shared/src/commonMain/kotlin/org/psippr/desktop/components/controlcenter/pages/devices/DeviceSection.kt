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
import org.psippr.desktop.components.controlcenter.settings.SettingsInfoRow
import org.psippr.desktop.components.controlcenter.settings.SettingsSection
import org.psippr.desktop.managers.devices.ConnectedDevice
import org.psippr.desktop.managers.devices.DeviceAction
import org.psippr.desktop.managers.devices.IDevicesManager

/** Actions that need a second click: restarting a device by a misclick would be annoying. */
private val CONFIRMED_ACTIONS = setOf(DeviceAction.REBOOT, DeviceAction.REBOOT_RECOVERY, DeviceAction.REBOOT_BOOTLOADER)

/**
 * One connected [device]: its facts, then one row per action it offers, and the message of the
 * last action. Actions run off the UI thread through [manager]; one at a time per device.
 */
@Suppress("FunctionName")
@Composable
fun DeviceSection(
    device: ConnectedDevice,
    manager: IDevicesManager,
) {
    val scope = rememberCoroutineScope()
    var running by remember(device.id) { mutableStateOf<DeviceAction?>(null) }
    var armed by remember(device.id) { mutableStateOf<DeviceAction?>(null) }
    var message by remember(device.id) { mutableStateOf("") }
    SettingsSection(title = device.name) {
        device.info.forEach { (label, value) -> SettingsInfoRow(label = label, value = value) }
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
                    message = withContext(Dispatchers.IO) { manager.perform(device, action) }.message
                    running = null
                }
            }
        }
        if (message.isNotBlank()) {
            SettingsInfoRow(label = message, value = "")
        }
    }
}
