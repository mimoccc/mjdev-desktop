package org.psippr.desktop.managers.devices

/**
 * Which connected devices the desktop shows, and which it still has to ask about.
 *
 * A device is shown when it is remembered, or when the user pressed Connect for it while it was
 * plugged in. Dismiss hides it. Decisions of this session end when the device goes away, so
 * plugging it in again asks again; only "Remember device" makes Connect last, until "Forget".
 * Thread safe: the watcher and the notification answers run on different threads.
 */
class DeviceGate(
    known: Collection<KnownDevice>,
) {
    private val known = known.associateBy { it.id }.toMutableMap()
    private val connected = mutableSetOf<String>()
    private val dismissed = mutableSetOf<String>()
    private val asked = mutableSetOf<String>()

    /** Gate of the first format: remembered device ids only (names unknown, the id stands in). */
    constructor(rememberedIds: List<String>) : this(rememberedIds.map { id -> KnownDevice(id = id, name = id) })

    /** Every remembered entry, forgotten ones flagged, to store. */
    @Synchronized
    fun knownDevices(): List<KnownDevice> = known.values.toList()

    /** Ids of the devices remembered and not forgotten. */
    @Synchronized
    fun rememberedIds(): List<String> = known.values.filterNot { it.forgotten }.map { it.id }

    /** The devices of [devices] the user allowed. */
    @Synchronized
    fun visible(devices: List<ConnectedDevice>): List<ConnectedDevice> = devices.filter {
        isRemembered(it.id) ||
            it.id in connected
    }

    /** Remembered devices that are not among [presentIds] (unplugged), to list them for Forget. */
    @Synchronized
    fun rememberedAbsent(presentIds: Collection<String>): List<KnownDevice> =
        known.values.filter { !it.forgotten && it.id !in presentIds }

    /**
     * Devices of [devices] to ask about now: neither remembered, connected, dismissed nor asked
     * already. They count as asked from here on.
     */
    @Synchronized
    fun toAsk(devices: List<ConnectedDevice>): List<ConnectedDevice> = devices
        .filter { !isRemembered(it.id) && it.id !in connected && it.id !in dismissed && it.id !in asked }
        .onEach { asked.add(it.id) }

    /** Connect was pressed for [device]; with [remember] it is shown at once next time too. */
    @Synchronized
    fun connect(
        device: ConnectedDevice,
        remember: Boolean,
    ) {
        asked.remove(device.id)
        connected.add(device.id)
        if (remember) known[device.id] = KnownDevice(device.id, device.name, device.provider, forgotten = false)
    }

    /** Connect for the device [id] whose name is not at hand (the id stands in for the name). */
    @Synchronized
    fun connect(
        id: String,
        remember: Boolean,
    ) = connect(ConnectedDevice(id = id, provider = "", name = id, state = DeviceState.READY), remember)

    /** Dismiss was pressed for [id] (or its question closed): it stays hidden while plugged in. */
    @Synchronized
    fun dismiss(id: String) {
        asked.remove(id)
        dismissed.add(id)
    }

    /**
     * "Forget this device": no longer remembered (flagged) nor connected. It stays hidden while
     * plugged in; plugging it in again asks again.
     */
    @Synchronized
    fun forget(id: String) {
        known[id]?.let { entry -> known[id] = entry.copy(forgotten = true) }
        connected.remove(id)
        asked.remove(id)
        dismissed.add(id)
    }

    /**
     * Stores the current names of remembered devices among [devices] (an entry of the first
     * format only knows the id); true when a name changed and the entries should be saved.
     */
    @Synchronized
    fun updateNames(devices: List<ConnectedDevice>): Boolean {
        var changed = false
        devices.forEach { device ->
            val entry = known[device.id] ?: return@forEach
            if (entry.name != device.name || entry.provider != device.provider) {
                known[device.id] = entry.copy(name = device.name, provider = device.provider)
                changed = true
            }
        }
        return changed
    }

    /** Forgets the session decisions of devices no longer in [presentIds] (unplugged). */
    @Synchronized
    fun forgetGone(presentIds: Collection<String>) {
        val present = presentIds.toSet()
        connected.retainAll(present)
        dismissed.retainAll(present)
        asked.retainAll(present)
    }

    private fun isRemembered(id: String): Boolean = known[id]?.forgotten == false
}
