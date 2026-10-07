package org.mjdev.desktop.managers.theme.linux.gtk

/**
 * Window chrome rules for the mjdev compositor session only: the compositor draws the frame and
 * the min / max / close buttons, so client-side titlebuttons and frame shadows are hidden.
 * Never written under GNOME Shell - there CSD apps would lose their close button.
 */
object CompositorChromeRules {
    /** Rules (moved from the previous generator unchanged). */
    val CSS: String =
        """
        /* App content follows the wallpaper palette (compositor draws the outer frame). */
        window {
            background-image: none;
            border-radius: 0;
            box-shadow: none;
            border: none;
            margin: 0;
            padding: 0;
        }

        /* Compositor draws min/max/close - hide client-side titlebuttons only. */
        headerbar button.titlebutton,
        headerbar .titlebutton {
            opacity: 0;
            min-width: 0;
            min-height: 0;
            padding: 0;
            margin: 0;
            border: none;
        }

        window.ssd headerbar {
            min-height: 0;
            padding: 0;
            margin: 0;
            border: none;
            box-shadow: none;
            background-image: none;
        }
        """.trimIndent()
}
