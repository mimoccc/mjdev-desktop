package org.mjdev.desktop.managers.volume.pactl

/**
 * Field / section names of `pactl list` output (C locale).
 *
 * @property key name before the colon
 */
enum class PactlField(
    val key: String,
) {
    /** System name of the object. */
    NAME("Name"),

    /** Human readable description. */
    DESCRIPTION("Description"),

    /** Ports of a sink / source. */
    PORTS("Ports"),

    /** Active port of a sink / source. */
    ACTIVE_PORT("Active Port"),

    /** Profiles of a card. */
    PROFILES("Profiles"),

    /** Active profile of a card. */
    ACTIVE_PROFILE("Active Profile"),

    /** Properties section. */
    PROPERTIES("Properties"),
}
