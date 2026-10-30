package org.psippr.desktop.managers.devices

/**
 * Which connected devices the desktop shows, and which it still has to ask about.
 *
 * A device is shown when it is remembered, or when the user pressed Connect for it while it was
 * plugged in. Dismiss hides it. Decisions of this session end when the device goes away, so
 * plugging it in again asks again; only "Remember device" makes Connect last.
 * Thread safe: the watcher and the notification answers run on different threads.
 */
class DeviceGate(
    remembered: Collection<String>,
) {
    private val remembered = remembered.toMutableSet()
    private val connected = mutableSetOf<String>()
    private val dismissed = mutableSetOf<String>()
    private val asked = mutableSetOf<String>()

    /** Ids of the remembered devices, to store. */
    @Synchronized
    fun rememberedIds(): List<String> = remembered.toList()

    /** The devices of [devices] the user allowed. */
    @Synchronized
    fun visible(devices: List<ConnectedDevice>): List<ConnectedDevice> = devices.filter {
        it.id in remembered ||
            it.id in connected
    }

    /**
     * Devices of [devices] to ask about now: neither remembered, connected, dismissed nor asked
     * already. They count as asked from here on.
     */
    @Synchronized
    fun toAsk(devices: List<ConnectedDevice>): List<ConnectedDevice> = devices
        .filter { it.id !in remembered && it.id !in connected && it.id !in dismissed && it.id !in asked }
        .onEach { asked.add(it.id) }

    /** Connect was pressed for [id]; with [remember] it is shown at once next time too. */
    @Synchronized
    fun connect(
        id: String,
        remember: Boolean,
    ) {
        asked.remove(id)
        connected.add(id)
        if (remember) remembered.add(id)
    }

    /** Dismiss was pressed for [id] (or its question closed): it stays hidden while plugged in. */
    @Synchronized
    fun dismiss(id: String) {
        asked.remove(id)
        dismissed.add(id)
    }

    /** Forgets the session decisions of devices no longer in [presentIds] (unplugged). */
    @Synchronized
    fun forgetGone(presentIds: Collection<String>) {
        connected.retainAll(presentIds.toSet())
        dismissed.retainAll(presentIds.toSet())
        asked.retainAll(presentIds.toSet())
    }
}
