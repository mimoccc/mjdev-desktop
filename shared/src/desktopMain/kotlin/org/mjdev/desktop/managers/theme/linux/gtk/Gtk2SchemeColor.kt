package org.mjdev.desktop.managers.theme.linux.gtk

import androidx.compose.ui.graphics.Color

/**
 * Symbolic colors of GTK2 `gtk-color-scheme` - every name the stock Adwaita GTK2 rc files use.
 *
 * @property rcName name inside `gtk-color-scheme`
 * @property resolve color of this name in the given theme
 */
enum class Gtk2SchemeColor(
    val rcName: String,
    val resolve: (GtkThemeColors) -> Color,
) {
    /** View text. */
    TEXT("text_color", { c -> c.windowFg }),

    /** View background. */
    BASE("base_color", { c -> c.viewBg }),

    /** Window text. */
    FG("fg_color", { c -> c.windowFg }),

    /** Window background. */
    BG("bg_color", { c -> c.windowBg }),

    /** Selected text. */
    SELECTED_FG("selected_fg_color", { c -> c.accentFg }),

    /** Selection fill. */
    SELECTED_BG("selected_bg_color", { c -> c.accentBg }),

    /** Disabled text. */
    INSENSITIVE_FG("insensitive_fg_color", { c -> c.insensitiveFg }),

    /** Disabled background. */
    INSENSITIVE_BG("insensitive_bg_color", { c -> c.windowBg }),

    /** Menu background. */
    MENU("menu_color", { c -> c.popoverBg }),

    /** Link. */
    LINK("link_color", { c -> c.accent }),

    /** Visited link. */
    VISITED_LINK("visited_link_color", { c -> c.accent }),

    /** Tree view column header text. */
    COLUMN_HEADER("column_header_color", { c -> c.insensitiveFg }),

    /** Hovered tree view column header text. */
    HOVER_COLUMN_HEADER("hover_column_header_color", { c -> c.windowFg }),

    /** Window decoration. */
    WINDOW("window_color", { c -> c.headerbarBg }),

    /** Tooltip text. */
    TOOLTIP_FG("tooltip_fg_color", { c -> c.tooltipFg }),

    /** Tooltip background. */
    TOOLTIP_BG("tooltip_bg_color", { c -> c.tooltipBg }),
    ;

    /** `name:#rrggbb` entry (GTK2 does not understand alpha). */
    fun entry(colors: GtkThemeColors): String = "$rcName:${CssColor.css(resolve(colors).copy(alpha = 1f))}"
}
