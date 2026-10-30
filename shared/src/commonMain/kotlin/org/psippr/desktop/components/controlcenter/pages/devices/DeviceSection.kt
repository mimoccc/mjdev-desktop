package org.psippr.desktop.components.controlcenter.pages.devices

import androidx.compose.runtime.Composable
import org.psippr.desktop.components.controlcenter.settings.SettingsInfoRow
import org.psippr.desktop.components.controlcenter.settings.SettingsSection
import org.psippr.desktop.managers.devices.ConnectedDevice
import org.psippr.desktop.managers.devices.IDevicesManager

/**
 * One connected [device] as an open section: its facts and its actions. The devices tab now
 * lists devices with [DeviceRow] (details open on a click); this stays for callers that want the
 * whole device at once.
 */
@Suppress("FunctionName")
@Composable
fun DeviceSection(
    device: ConnectedDevice,
    manager: IDevicesManager,
) {
    SettingsSection(title = device.name) {
        device.info.forEach { (label, value) -> SettingsInfoRow(label = label, value = value) }
        DeviceActionRows(device = device, manager = manager)
    }
}

// old: each device as an open section with all details; replaced by DeviceRow (a list whose
// rows open the details) and DeviceActionRows
// /** Actions that need a second click: restarting a device by a misclick would be annoying. */
// private val CONFIRMED_ACTIONS = setOf(DeviceAction.REBOOT, DeviceAction.REBOOT_RECOVERY, DeviceAction.REBOOT_BOOTLOADER)
//
// /**
//  * One connected [device]: its facts, then one row per action it offers. Actions run off the UI
//  * thread through [manager], one at a time per device; their result is shown as a notification.
//  */
// @Suppress("FunctionName")
// @Composable
// fun DeviceSection(
//     device: ConnectedDevice,
//     manager: IDevicesManager,
// ) = withDesktopContext {
//     val scope = rememberCoroutineScope()
//     var running by remember(device.id) { mutableStateOf<DeviceAction?>(null) }
//     var armed by remember(device.id) { mutableStateOf<DeviceAction?>(null) }
//     // old: result of the last action, shown as a line; it is a notification now
// //    var message by remember(device.id) { mutableStateOf("") }
//     SettingsSection(title = device.name) {
//         device.info.forEach { (label, value) -> SettingsInfoRow(label = label, value = value) }
//         device.actions.forEach { action ->
//             val button =
//                 when (action) {
//                     running -> DevicesPageDefaults.RUNNING
//                     armed -> DevicesPageDefaults.CONFIRM
//                     else -> action.button
//                 }
//             SettingsButtonRow(label = action.label, action = button, enabled = running == null) {
//                 if (action in CONFIRMED_ACTIONS && armed != action) {
//                     armed = action
//                     return@SettingsButtonRow
//                 }
//                 armed = null
//                 running = action
//                 scope.launch {
//                     // the action may open a file dialog, which blocks until it closes
//                     // old: the result stayed as a line in the device section
// //                    message = withContext(Dispatchers.IO) { manager.perform(device, action) }.message
//                     val result = withContext(Dispatchers.IO) { manager.perform(device, action) }
//                     if (result.message.isNotBlank()) context.notify(title = device.name, message = result.message)
//                     running = null
//                 }
//             }
//         }
//         // old: the result of the last action as a line here; it is a notification now
// //        if (message.isNotBlank()) {
// //            SettingsInfoRow(label = message, value = "")
// //        }
//     }
// }
