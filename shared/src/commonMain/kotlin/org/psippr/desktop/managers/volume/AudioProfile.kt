package org.psippr.desktop.managers.volume

/** One configuration (profile) of a sound card, like "Configuration" in Ubuntu Settings Sound. */
data class AudioProfile(
    /** System id of the card. */
    val cardId: String,
    /** System id of the profile. */
    val id: String,
    /** Human readable name shown to the user. */
    val label: String = id,
    /** True when the profile is active on its card. */
    val isActive: Boolean = false,
)
