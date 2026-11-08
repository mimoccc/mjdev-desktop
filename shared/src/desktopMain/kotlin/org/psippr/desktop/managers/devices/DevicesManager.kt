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

    // old: remembered ids only, no names and no way to forget
//    private val gate = DeviceGate(store.load().remembered)
    private val gate = DeviceGate(store.load().entries())

    // old: every connected device was shown, nobody was asked
//    override fun devices(): List<ConnectedDevice> = providers.flatMap { provider ->
//        runCatching { provider.devices() }
//            .onFailure { e -> Log.w("devices of ${provider.name} failed: ${e.message}") }
//            .getOrDefault(emptyList())
//    }
    // old: connected devices only, a remembered device could not be forgotten
//    override fun devices(): List<ConnectedDevice> = gate.visible(allDevices())

    /**
     * Allowed connected devices (with Forget added), then remembered devices that are not
     * connected now, so they can be forgotten (or reconnected over the network) at any time.
     */
    override fun devices(): List<ConnectedDevice> {
        val all = allDevices()
        // Forget first: forgetting devices is what this list is mostly for
        val connected =
            gate.visible(all).map { device ->
                device.copy(actions = listOf(DeviceAction.FORGET) + device.actions)
            }
        val absent = gate.rememberedAbsent(all.map { it.id }).map(::absentDevice)
        return connected + absent
    }

    // a remembered device that is not plugged in: Forget, and Reconnect for network devices
    private fun absentDevice(known: KnownDevice): ConnectedDevice = ConnectedDevice(
        id = known.id,
        provider = known.provider,
        name = known.name,
        state = DeviceState.NOT_CONNECTED,
        summary = DeviceState.NOT_CONNECTED.short,
        actions =
            listOfNotNull(
                DeviceAction.RECONNECT.takeIf { known.id.substringAfter(ID_SEPARATOR).contains(PORT_SEPARATOR) },
                DeviceAction.FORGET,
            ),
    )

    override fun allowedIds(): List<String> = gate.allowedIds()

    override fun startWatching(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                val all = allDevices()
                gate.forgetGone(all.map { it.id })
                if (gate.updateNames(all)) save()
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
                // old: stored ids only
//                gate.connect(device.id, remember)
//                if (remember) store.save(KnownDevices(gate.rememberedIds().toMutableList()))
                gate.connect(device, remember)
                if (remember) save()
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

        /** Message after Forget. */
        const val FORGOTTEN = "Forgotten. Connecting it again will ask first."

        /** Separator between provider and the provider's own id in a device id. */
        const val ID_SEPARATOR = ":"

        /** Separator of host and port in the id of a network device. */
        const val PORT_SEPARATOR = ":"
    }

    // every entry, forgotten ones flagged; the id list of the first format kept in step
    private fun save() =
        store.save(KnownDevices(gate.rememberedIds().toMutableList(), gate.knownDevices().toMutableList()))

    override fun perform(
        device: ConnectedDevice,
        action: DeviceAction,
    ): DeviceActionResult {
        if (action == DeviceAction.FORGET) {
            gate.forget(device.id)
            save()
            return DeviceActionResult(done = true, message = FORGOTTEN)
        }
        val provider = providers.firstOrNull { it.name == device.provider } ?: return DeviceActionResult.UNSUPPORTED
        return runCatching { provider.perform(device, action) }
            .getOrElse { e -> DeviceActionResult(done = false, message = e.message ?: e.javaClass.simpleName) }
    }
}
