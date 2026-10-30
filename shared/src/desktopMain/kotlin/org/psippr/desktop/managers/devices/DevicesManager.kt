package org.psippr.desktop.managers.devices

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.log.Log
import org.psippr.desktop.managers.devices.adb.AdbDeviceProvider
import org.psippr.desktop.managers.notifications.NotificationAction
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

    private val notifications = context.notificationsManager
    private val store = KnownDevicesStore(context.currentUser)
    private val gate = DeviceGate(store.load().remembered)

    // old: every connected device was shown, nobody was asked
//    override fun devices(): List<ConnectedDevice> = providers.flatMap { provider ->
//        runCatching { provider.devices() }
//            .onFailure { e -> Log.w("devices of ${provider.name} failed: ${e.message}") }
//            .getOrDefault(emptyList())
//    }
    override fun devices(): List<ConnectedDevice> = gate.visible(allDevices())

    override fun startWatching(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                val all = allDevices()
                gate.forgetGone(all.map { it.id })
                gate.toAsk(all).forEach { device -> withContext(Dispatchers.Main) { ask(device) } }
                delay(WATCH_MS)
            }
        }
    }

    // a notification with Connect / Dismiss and the "Remember device" checkbox
    private fun ask(device: ConnectedDevice) {
        notifications.ask(
            title = DeviceQuestionLabels.TITLE,
            message = device.name + DeviceQuestionLabels.MESSAGE,
            actions =
                listOf(
                    NotificationAction(DeviceQuestionLabels.ACTION_CONNECT, DeviceQuestionLabels.CONNECT),
                    NotificationAction(DeviceQuestionLabels.ACTION_DISMISS, DeviceQuestionLabels.DISMISS),
                ),
            checkbox = DeviceQuestionLabels.REMEMBER,
        ) { actionId, remember ->
            if (actionId == DeviceQuestionLabels.ACTION_CONNECT) {
                gate.connect(device.id, remember)
                if (remember) store.save(KnownDevices(gate.rememberedIds().toMutableList()))
            } else {
                gate.dismiss(device.id)
            }
        }
    }

    // every connected device of every provider, allowed or not
    private fun allDevices(): List<ConnectedDevice> = providers.flatMap { provider ->
        runCatching { provider.devices() }
            .onFailure { e -> Log.w("devices of ${provider.name} failed: ${e.message}") }
            .getOrDefault(emptyList())
    }

    private companion object {
        /** Pause between two looks for new devices. */
        const val WATCH_MS = 3_000L
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
