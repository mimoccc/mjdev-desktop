package org.psippr.desktop.managers.theme.linux.gtk

/**
 * How far a surface is mixed from the window background toward the text color.
 *
 * Mirrors the Yaru / Adwaita layering (headerbar, sidebar, cards and popovers sit slightly
 * apart from the window) so recolored apps keep the stock Ubuntu depth on any palette.
 *
 * @property fraction 0 = window background, 1 = text color
 */
enum class GtkSurfaceBlend(
    val fraction: Float,
) {
    /** Content views (lists, text views) - shifted away from the text color, see [GtkThemeColors]. */
    VIEW(0.04f),

    /** Header bars, toolbars, menu bars. */
    HEADERBAR(0.05f),

    /** Primary sidebars (Files places, Settings). */
    SIDEBAR(0.03f),

    /** Secondary sidebars (split views). */
    SECONDARY_SIDEBAR(0.02f),

    /** Cards, boxed lists, message bubbles. */
    CARD(0.06f),

    /** Popovers, menus, dialogs. */
    POPOVER(0.08f),

    /** Regular (non flat) buttons. */
    BUTTON(0.10f),

    /** Hovered buttons and menu items. */
    BUTTON_HOVER(0.16f),

    /** Pressed buttons. */
    BUTTON_ACTIVE(0.22f),

    /** Borders and separators. */
    BORDER(0.18f),

    /** Disabled text. */
    INSENSITIVE(0.50f),

    /** Scrollbar slider. */
    SCROLLBAR(0.40f),
}
