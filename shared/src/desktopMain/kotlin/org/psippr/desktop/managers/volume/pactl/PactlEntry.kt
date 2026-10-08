package org.psippr.desktop.managers.volume.pactl

/**
 * One line of a `Ports` / `Profiles` section:
 * `analog-output-speaker: Speakers (type: Speaker, priority: 10000, availability unknown)`.
 *
 * @property id system id before `": "`
 * @property label description before the details in parentheses
 * @property available false when pactl marks the entry unavailable (unplugged / unusable)
 */
data class PactlEntry(
    val id: String,
    val label: String,
    val available: Boolean,
) {
    companion object {
        /** Separator between id and description (ids themselves may contain plain `:`). */
        private const val ID_SEPARATOR = ": "

        /** Start of the details in parentheses. */
        private const val DETAILS_START = " ("

        /** Port marker of an unplugged port. */
        private const val PORT_NOT_AVAILABLE = "not available"

        /** Profile marker of an unusable profile. */
        private const val PROFILE_NOT_AVAILABLE = "available: no"

        /** Entry of [line], null when it is not an entry line. */
        fun parse(line: String): PactlEntry? {
            if (!line.contains(ID_SEPARATOR)) return null
            val rest = line.substringAfter(ID_SEPARATOR)
            val details = rest.substringAfterLast(DETAILS_START, "")
            return PactlEntry(
                id = line.substringBefore(ID_SEPARATOR).trim(),
                label = rest.substringBeforeLast(DETAILS_START).trim(),
                available = !details.contains(PORT_NOT_AVAILABLE) && !details.contains(PROFILE_NOT_AVAILABLE),
            )
        }
    }
}
