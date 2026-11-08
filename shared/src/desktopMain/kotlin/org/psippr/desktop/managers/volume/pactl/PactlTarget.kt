package org.psippr.desktop.managers.volume.pactl

import org.psippr.desktop.managers.volume.AudioDeviceKind

/**
 * pactl vocabulary per device kind.
 *
 * @property kind device kind
 * @property listType object type for `pactl list`
 * @property defaultAlias alias of the default device
 * @property getDefault command reading the default device name
 * @property setDefault command selecting the default device
 * @property setPort command selecting the active port
 * @property getVolume command reading the volume
 * @property setVolume command writing the volume
 * @property getMute command reading the mute state
 * @property setMute command writing the mute state
 */
enum class PactlTarget(
    val kind: AudioDeviceKind,
    val listType: String,
    val defaultAlias: String,
    val getDefault: String,
    val setDefault: String,
    val setPort: String,
    val getVolume: String,
    val setVolume: String,
    val getMute: String,
    val setMute: String,
) {
    /** Sinks. */
    SINK(
        AudioDeviceKind.OUTPUT,
        "sinks",
        "@DEFAULT_SINK@",
        "get-default-sink",
        "set-default-sink",
        "set-sink-port",
        "get-sink-volume",
        "set-sink-volume",
        "get-sink-mute",
        "set-sink-mute",
    ),

    /** Sources. */
    SOURCE(
        AudioDeviceKind.INPUT,
        "sources",
        "@DEFAULT_SOURCE@",
        "get-default-source",
        "set-default-source",
        "set-source-port",
        "get-source-volume",
        "set-source-volume",
        "get-source-mute",
        "set-source-mute",
    ),
    ;

    companion object {
        /** Target of [kind]. */
        fun of(kind: AudioDeviceKind): PactlTarget = entries.first { target -> target.kind == kind }
    }
}
