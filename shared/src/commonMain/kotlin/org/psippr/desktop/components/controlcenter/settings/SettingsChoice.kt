package org.psippr.desktop.components.controlcenter.settings

/** One option of a [SettingsChoiceList]. */
data class SettingsChoice(
    /** Stable key identifying the option. */
    val key: String,
    /** Text shown to the user. */
    val label: String,
    /** True when this option is the current one. */
    val selected: Boolean,
)
