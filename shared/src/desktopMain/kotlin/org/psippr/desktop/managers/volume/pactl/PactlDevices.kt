package org.psippr.desktop.managers.volume.pactl

import org.psippr.desktop.managers.volume.AudioDevice
import org.psippr.desktop.managers.volume.AudioProfile

/** Maps parsed pactl blocks to the devices and profiles shown in the Sound settings. */
object PactlDevices {
    /** `device.class` of sink monitors (not real inputs). */
    private const val CLASS_MONITOR = "monitor"

    /** Value of `device.profile.pro` on Pro Audio devices. */
    private const val PRO_TRUE = "true"

    /** Suffix ALSA adds to PCM ids (`HDMI1 (*)`). */
    private const val ALSA_ID_SUFFIX = "(*)"

    /** Separator between port and device in a label, as in Ubuntu Settings. */
    private const val LABEL_SEPARATOR = " – "

    /** Profile turning the card off, never offered (all devices would disappear). */
    private const val PROFILE_OFF = "off"

    /**
     * Devices of [target]: one entry per available port, or one per device without ports.
     * [defaultName] marks the default device; its active port is marked default.
     */
    fun devices(
        blocks: List<PactlBlock>,
        target: PactlTarget,
        defaultName: String,
    ): List<AudioDevice> = blocks
        .filter { block -> block.property(PactlProperty.DEVICE_CLASS) != CLASS_MONITOR }
        .flatMap { block -> devicesOf(block, target, defaultName) }

    /** Selectable profiles of all cards (available ones, never `off`). */
    fun profiles(cards: List<PactlBlock>): List<AudioProfile> = cards.flatMap { card ->
        val cardId = card.field(PactlField.NAME).orEmpty()
        val active = card.field(PactlField.ACTIVE_PROFILE)
        card
            .section(PactlField.PROFILES)
            .mapNotNull { line -> PactlEntry.parse(line) }
            .filter { entry -> entry.available && entry.id != PROFILE_OFF }
            .map { entry -> AudioProfile(cardId, entry.id, entry.label, entry.id == active) }
    }

    private fun devicesOf(
        block: PactlBlock,
        target: PactlTarget,
        defaultName: String,
    ): List<AudioDevice> {
        val id = block.field(PactlField.NAME) ?: return emptyList()
        val isDefault = id == defaultName
        val name = deviceName(block)
        val ports =
            block
                .section(PactlField.PORTS)
                .mapNotNull { line -> PactlEntry.parse(line) }
                .filter { port -> port.available }
        if (ports.isEmpty()) return listOf(AudioDevice(target.kind, id, null, name, isDefault))
        val activePort = block.field(PactlField.ACTIVE_PORT)
        return ports.map { port ->
            AudioDevice(
                kind = target.kind,
                id = id,
                port = port.id,
                label = port.label + LABEL_SEPARATOR + name,
                isDefault = isDefault && port.id == activePort,
            )
        }
    }

    // Pro Audio devices all share the card description; their ALSA PCM id (HDMI1, DMIC…) tells them apart
    private fun deviceName(block: PactlBlock): String {
        val description = block.field(PactlField.DESCRIPTION).orEmpty()
        val alsaId = block.property(PactlProperty.ALSA_ID)?.removeSuffix(ALSA_ID_SUFFIX)?.trim()
        val isPro = block.property(PactlProperty.PROFILE_PRO) == PRO_TRUE
        val card = block.property(PactlProperty.DEVICE_DESCRIPTION) ?: description
        return when {
            isPro && !alsaId.isNullOrEmpty() -> alsaId + LABEL_SEPARATOR + card
            description.isNotEmpty() -> description
            else -> block.field(PactlField.NAME).orEmpty()
        }
    }
}
