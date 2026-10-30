package org.psippr.desktop.managers.devices

/**
 * Devices the user chose to remember (Connect with "Remember device"): they are shown and used
 * at once when they connect, without asking. Stored as JSON; entries are only ever added or
 * flagged, never removed.
 *
 * @property remembered ids of the remembered devices (the first format, kept so older files are
 * read; [devices] is the full record now)
 * @property devices every device ever remembered, with name and forgotten flag
 */
data class KnownDevices(
    val remembered: MutableList<String> = mutableListOf(),
    val devices: MutableList<KnownDevice> = mutableListOf(),
) {
    /**
     * Every entry, with ids of the first format that have no entry yet taken over (their name is
     * not known, the id stands in until the device connects again).
     */
    fun entries(): List<KnownDevice> = devices +
        remembered
            .filter { id -> devices.none { it.id == id } }
            .map { id -> KnownDevice(id = id, name = id, provider = id.substringBefore(PROVIDER_SEPARATOR)) }

    private companion object {
        /** Separator between provider and the provider's own id in a device id. */
        const val PROVIDER_SEPARATOR = ":"
    }
}
