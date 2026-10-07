package org.mjdev.desktop.plugins.music

/** Constants of the music player widget: what is playable, how deep to scan and how it is laid out. */
object MusicDefaults {
    /** File extensions (lower case, no dot) the widget treats as playable music. */
    val EXTENSIONS: Set<String> = setOf("mp3", "flac", "ogg", "wav", "m4a", "aac", "opus")

    /** How many sub-folder levels of the music folder are scanned. */
    const val MAX_SCAN_DEPTH = 3

    /** Upper bound of tracks read from the music folder, protects against huge libraries. */
    const val MAX_TRACKS = 5_000

    // old size of the simple card, kept for history; the card layout uses CARD_WIDTH_DP / CARD_HEIGHT_DP

    /** Widget width in dp. */
    const val WIDTH_DP = 320f

    /** Widget height in dp. */
    const val HEIGHT_DP = 170f

    /** Width of the card layout in dp. */
    const val CARD_WIDTH_DP = 320f

    /** Height of the card layout in dp. */
    const val CARD_HEIGHT_DP = 276f

    /** Gap between the music card and the memory widget below it, in dp. */
    const val STACK_GAP_DP = 16f

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

    /** Alpha of the card background in the card layout (nearly solid, like the design). */
    const val CARD_BG_ALPHA = 0.9f

    /** Name shown in the card header. */
    const val HEADER_TITLE = "music"

    /** Label above the track lines of the inner card. */
    const val NOW_PLAYING_LABEL = "• NOW PLAYING"

    /** Placeholder line when the queue has no previous / next track. */
    const val NO_TRACK_LINE = ""

    /** Header row height in dp. */
    const val HEADER_DP = 40

    /** Size of the round header icon in dp. */
    const val HEADER_ICON_DP = 26

    /** Padding of the note inside the round header icon in dp. */
    const val HEADER_ICON_PADDING_DP = 4

    /** Header title text size in sp. */
    const val HEADER_SP = 18

    /** Horizontal padding of the time tab in dp. */
    const val TIME_TAB_PADDING_DP = 14

    /** Corner radius of the time tab's inner (bottom-start) corner in dp. */
    const val TIME_TAB_CORNER_DP = 18

    /** Time tab text size in sp. */
    const val TIME_TAB_SP = 14

    /** Height of the raised inner card in dp. */
    const val INNER_CARD_DP = 86

    /** Corner radius of the raised inner card in dp. */
    const val INNER_CORNER_DP = 14

    /** How much lighter / darker the inner card is than the outer one (0..1). */
    const val INNER_CARD_BLEND = 0.08f

    /** Border width of the inner card in dp. */
    const val INNER_BORDER_DP = 1

    /** Alpha of the inner card border. */
    const val INNER_BORDER_ALPHA = 0.15f

    /** Size of the cover placeholder in dp. */
    const val COVER_DP = 60

    /** Corner radius of the cover placeholder in dp. */
    const val COVER_CORNER_DP = 8

    /** Alpha of the cover placeholder background. */
    const val COVER_ALPHA = 0.25f

    /** Size of the small badge icon of the inner card in dp. */
    const val BADGE_DP = 16

    /** Text size of the inner card label in sp. */
    const val LABEL_SP = 11

    /** Text size of the track lines in the inner card in sp. */
    const val LINE_SP = 12

    /** Alpha of the previous / next track lines (faded like lyrics around the current one). */
    const val FADED_LINE_ALPHA = 0.35f

    /** Height of the progress bar touch area in dp. */
    const val PROGRESS_TOUCH_DP = 16

    /** Thickness of the progress track in dp. */
    const val PROGRESS_TRACK_DP = 3

    /** Diameter of the progress thumb in dp. */
    const val PROGRESS_THUMB_DP = 9

    /** Alpha of the unplayed part of the progress track. */
    const val PROGRESS_REST_ALPHA = 0.25f

    /** Time labels text size in sp. */
    const val TIME_SP = 11

    /** Alpha of the secondary texts (folder name, times). */
    const val SECONDARY_ALPHA = 0.6f

    /** Size of the round play / pause button in dp. */
    const val PLAY_BUTTON_DP = 36

    /** Gap between the transport buttons in dp. */
    const val TRANSPORT_GAP_DP = 22
}
