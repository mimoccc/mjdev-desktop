package org.psippr.desktop.windows

/**
 * Encodes what the shell wants the compositor to know about a window into its (never visible)
 * title: `psippr::<name>` for the role, optionally followed by space separated flags, e.g.
 * `psippr::ControlCenter blur`. The compositor parses the same format in its Policy.
 */
object WindowTitle {
    /** Prefix that marks a window as part of the desktop shell and names its role. */
    const val PREFIX = "psippr::"

    /** Flag asking the compositor to blur everything rendered below the window. */
    const val FLAG_BLUR = "blur"

    /** Builds the title for a window called [name]; null when the window has no name. */
    fun encode(
        name: String?,
        blur: Boolean,
    ): String = name
        ?.let { role -> listOfNotNull(PREFIX + role, FLAG_BLUR.takeIf { blur }).joinToString(" ") }
        .orEmpty()
}
