package org.mjdev.desktop.plugins.music

/** Constants of the music player widget: what is playable, how deep to scan and how it is laid out. */
object MusicDefaults {
    /** File extensions (lower case, no dot) the widget treats as playable music. */
    val EXTENSIONS: Set<String> = setOf("mp3", "flac", "ogg", "wav", "m4a", "aac", "opus")

    /** How many sub-folder levels of the music folder are scanned. */
    const val MAX_SCAN_DEPTH = 3

    /** Upper bound of tracks read from the music folder, protects against huge libraries. */
    const val MAX_TRACKS = 5_000

    /** Widget width in dp. */
    const val WIDTH_DP = 320f

    /** Widget height in dp. */
    const val HEIGHT_DP = 170f

    /** Corner radius of the widget card in dp. */
    const val CORNER_DP = 20

    /** Inner padding of the widget card in dp. */
    const val PADDING_DP = 14

    /** Gap between rows of the widget in dp. */
    const val SPACING_DP = 6

    /** Size of the transport buttons in dp. */
    const val BUTTON_DP = 44

    /** Alpha of the card background so the wallpaper shows through. */
    const val CARD_ALPHA = 0.55f

    /** Title text size in sp. */
    const val TITLE_SP = 16

    /** Secondary text size in sp. */
    const val SUBTITLE_SP = 12

    /** The player counts a track as finished when it stops this close (ms) to its end. */
    const val END_TOLERANCE_MS = 1_500L

    /** Milliseconds in one second, used to format the time labels. */
    const val MILLIS_IN_SECOND = 1_000L

    /** Seconds in one minute, used to format the time labels. */
    const val SECONDS_IN_MINUTE = 60L

    /** Label shown when the music folder holds no playable file. */
    const val EMPTY_TITLE = "No music found"

    /** Label of the previous-track button (accessibility). */
    const val PREVIOUS_LABEL = "Previous"

    /** Label of the play button (accessibility). */
    const val PLAY_LABEL = "Play"

    /** Label of the pause button (accessibility). */
    const val PAUSE_LABEL = "Pause"

    /** Label of the next-track button (accessibility). */
    const val NEXT_LABEL = "Next"
}
