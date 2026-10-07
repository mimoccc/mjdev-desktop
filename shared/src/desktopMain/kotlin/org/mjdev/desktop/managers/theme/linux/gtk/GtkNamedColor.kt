package org.mjdev.desktop.managers.theme.linux.gtk

import androidx.compose.ui.graphics.Color

/**
 * GTK `@define-color` names written to every generated stylesheet.
 *
 * Covers the names libadwaita 1.x maps its CSS variables from (so `~/.config/gtk-4.0/gtk.css`
 * recolors every libadwaita app), the GTK3 `theme_*` names Firefox / Chromium / Electron / LibreOffice
 * read, and the legacy names apps and old extensions still reference.
 *
 * @property cssName name after `@define-color`
 * @property resolve color of this name in the given theme
 */
enum class GtkNamedColor(
    val cssName: String,
    val resolve: (GtkThemeColors) -> Color,
) {
    // libadwaita: accent and status

    /** libadwaita accent as text. */
    ACCENT("accent_color", { c -> c.accent }),

    /** libadwaita accent fill. */
    ACCENT_BG("accent_bg_color", { c -> c.accentBg }),

    /** libadwaita text on accent. */
    ACCENT_FG("accent_fg_color", { c -> c.accentFg }),

    /** Destructive as text. */
    DESTRUCTIVE("destructive_color", { _ -> GtkStatusColor.DESTRUCTIVE.bg }),

    /** Destructive fill. */
    DESTRUCTIVE_BG("destructive_bg_color", { _ -> GtkStatusColor.DESTRUCTIVE.bg }),

    /** Text on destructive. */
    DESTRUCTIVE_FG("destructive_fg_color", { _ -> GtkStatusColor.DESTRUCTIVE.fg }),

    /** Success as text. */
    SUCCESS("success_color", { _ -> GtkStatusColor.SUCCESS.bg }),

    /** Success fill. */
    SUCCESS_BG("success_bg_color", { _ -> GtkStatusColor.SUCCESS.bg }),

    /** Text on success. */
    SUCCESS_FG("success_fg_color", { _ -> GtkStatusColor.SUCCESS.fg }),

    /** Warning as text. */
    WARNING("warning_color", { _ -> GtkStatusColor.WARNING.bg }),

    /** Warning fill. */
    WARNING_BG("warning_bg_color", { _ -> GtkStatusColor.WARNING.bg }),

    /** Text on warning. */
    WARNING_FG("warning_fg_color", { _ -> GtkStatusColor.WARNING.fg }),

    /** Error as text. */
    ERROR("error_color", { _ -> GtkStatusColor.ERROR.bg }),

    /** Error fill. */
    ERROR_BG("error_bg_color", { _ -> GtkStatusColor.ERROR.bg }),

    /** Text on error. */
    ERROR_FG("error_fg_color", { _ -> GtkStatusColor.ERROR.fg }),

    // libadwaita: surfaces

    /** Window background. */
    WINDOW_BG("window_bg_color", { c -> c.windowBg }),

    /** Window text. */
    WINDOW_FG("window_fg_color", { c -> c.windowFg }),

    /** View background. */
    VIEW_BG("view_bg_color", { c -> c.viewBg }),

    /** View text. */
    VIEW_FG("view_fg_color", { c -> c.windowFg }),

    /** Header bar background. */
    HEADERBAR_BG("headerbar_bg_color", { c -> c.headerbarBg }),

    /** Header bar text. */
    HEADERBAR_FG("headerbar_fg_color", { c -> c.windowFg }),

    /** Header bar border. */
    HEADERBAR_BORDER("headerbar_border_color", { c -> c.windowFg }),

    /** Header bar of an unfocused window. */
    HEADERBAR_BACKDROP("headerbar_backdrop_color", { c -> c.windowBg }),

    /** Header bar shadow. */
    HEADERBAR_SHADE("headerbar_shade_color", { c -> c.shade }),

    /** Header bar shadow, stronger variant. */
    HEADERBAR_DARKER_SHADE("headerbar_darker_shade_color", { c -> c.shade }),

    /** Sidebar background. */
    SIDEBAR_BG("sidebar_bg_color", { c -> c.sidebarBg }),

    /** Sidebar text. */
    SIDEBAR_FG("sidebar_fg_color", { c -> c.windowFg }),

    /** Sidebar of an unfocused window. */
    SIDEBAR_BACKDROP("sidebar_backdrop_color", { c -> c.windowBg }),

    /** Sidebar border. */
    SIDEBAR_BORDER("sidebar_border_color", { c -> c.border }),

    /** Sidebar shadow. */
    SIDEBAR_SHADE("sidebar_shade_color", { c -> c.shade }),

    /** Secondary sidebar background. */
    SECONDARY_SIDEBAR_BG("secondary_sidebar_bg_color", { c -> c.secondarySidebarBg }),

    /** Secondary sidebar text. */
    SECONDARY_SIDEBAR_FG("secondary_sidebar_fg_color", { c -> c.windowFg }),

    /** Secondary sidebar of an unfocused window. */
    SECONDARY_SIDEBAR_BACKDROP("secondary_sidebar_backdrop_color", { c -> c.windowBg }),

    /** Secondary sidebar border. */
    SECONDARY_SIDEBAR_BORDER("secondary_sidebar_border_color", { c -> c.border }),

    /** Secondary sidebar shadow. */
    SECONDARY_SIDEBAR_SHADE("secondary_sidebar_shade_color", { c -> c.shade }),

    /** Card background. */
    CARD_BG("card_bg_color", { c -> c.cardBg }),

    /** Card text. */
    CARD_FG("card_fg_color", { c -> c.windowFg }),

    /** Card shadow. */
    CARD_SHADE("card_shade_color", { c -> c.shade }),

    /** Thumbnail background. */
    THUMBNAIL_BG("thumbnail_bg_color", { c -> c.cardBg }),

    /** Thumbnail text. */
    THUMBNAIL_FG("thumbnail_fg_color", { c -> c.windowFg }),

    /** Dialog background. */
    DIALOG_BG("dialog_bg_color", { c -> c.popoverBg }),

    /** Dialog text. */
    DIALOG_FG("dialog_fg_color", { c -> c.windowFg }),

    /** Popover background. */
    POPOVER_BG("popover_bg_color", { c -> c.popoverBg }),

    /** Popover text. */
    POPOVER_FG("popover_fg_color", { c -> c.windowFg }),

    /** Popover shadow. */
    POPOVER_SHADE("popover_shade_color", { c -> c.shade }),

    /** Generic shadow. */
    SHADE("shade_color", { c -> c.shade }),

    /** Scrollbar outline. */
    SCROLLBAR_OUTLINE("scrollbar_outline_color", { c -> c.shade }),

    // GTK3 theme colors (read by GTK3 apps, Firefox, Chromium, Electron, LibreOffice)

    /** GTK3 window text. */
    THEME_FG("theme_fg_color", { c -> c.windowFg }),

    /** GTK3 view text. */
    THEME_TEXT("theme_text_color", { c -> c.windowFg }),

    /** GTK3 window background. */
    THEME_BG("theme_bg_color", { c -> c.windowBg }),

    /** GTK3 view background. */
    THEME_BASE("theme_base_color", { c -> c.viewBg }),

    /** GTK3 selection fill. */
    THEME_SELECTED_BG("theme_selected_bg_color", { c -> c.accentBg }),

    /** GTK3 selected text. */
    THEME_SELECTED_FG("theme_selected_fg_color", { c -> c.accentFg }),

    /** GTK3 disabled background. */
    INSENSITIVE_BG("insensitive_bg_color", { c -> c.windowBg }),

    /** GTK3 disabled text. */
    INSENSITIVE_FG("insensitive_fg_color", { c -> c.insensitiveFg }),

    /** GTK3 disabled view background. */
    INSENSITIVE_BASE("insensitive_base_color", { c -> c.viewBg }),

    /** GTK3 unfocused window text. */
    THEME_UNFOCUSED_FG("theme_unfocused_fg_color", { c -> c.insensitiveFg }),

    /** GTK3 unfocused view text. */
    THEME_UNFOCUSED_TEXT("theme_unfocused_text_color", { c -> c.windowFg }),

    /** GTK3 unfocused window background. */
    THEME_UNFOCUSED_BG("theme_unfocused_bg_color", { c -> c.windowBg }),

    /** GTK3 unfocused view background. */
    THEME_UNFOCUSED_BASE("theme_unfocused_base_color", { c -> c.viewBg }),

    /** GTK3 unfocused selection fill. */
    THEME_UNFOCUSED_SELECTED_BG("theme_unfocused_selected_bg_color", { c -> c.accentBg }),

    /** GTK3 unfocused selected text. */
    THEME_UNFOCUSED_SELECTED_FG("theme_unfocused_selected_fg_color", { c -> c.accentFg }),

    /** GTK3 unfocused disabled text. */
    UNFOCUSED_INSENSITIVE("unfocused_insensitive_color", { c -> c.insensitiveFg }),

    /** GTK3 borders. */
    BORDERS("borders", { c -> c.border }),

    /** GTK3 unfocused borders. */
    UNFOCUSED_BORDERS("unfocused_borders", { c -> c.border }),

    /** GTK3 content view background. */
    CONTENT_VIEW_BG("content_view_bg", { c -> c.viewBg }),

    /** GTK3 text view background. */
    TEXT_VIEW_BG("text_view_bg", { c -> c.viewBg }),

    // legacy names (GTK2-era, used by older apps and the previous generator)

    /** Legacy window background. */
    BG("bg_color", { c -> c.windowBg }),

    /** Legacy window text. */
    FG("fg_color", { c -> c.windowFg }),

    /** Legacy view background. */
    BASE("base_color", { c -> c.viewBg }),

    /** Legacy view text. */
    TEXT("text_color", { c -> c.windowFg }),

    /** Legacy selection fill. */
    SELECTED_BG("selected_bg_color", { c -> c.accentBg }),

    /** Legacy selected text. */
    SELECTED_FG("selected_fg_color", { c -> c.accentFg }),

    /** Tooltip background. */
    TOOLTIP_BG("tooltip_bg_color", { c -> c.tooltipBg }),

    /** Tooltip text. */
    TOOLTIP_FG("tooltip_fg_color", { c -> c.tooltipFg }),

    /** Link color. */
    LINK("link_color", { c -> c.accent }),

    /** Visited link color. */
    LINK_VISITED("link_visited_color", { c -> c.accent }),

    /** OSD background. */
    OSD_BG("osd_bg_color", { c -> c.popoverBg }),

    /** OSD text. */
    OSD_FG("osd_fg_color", { c -> c.windowFg }),

    /** Focus ring. */
    FOCUS("focus_color", { c -> c.accentBg }),

    // generator helpers (referenced by GtkWidgetRules)

    /** Regular button fill. */
    BUTTON_BG("button_bg_color", { c -> c.buttonBg }),

    /** Hovered button / menu item fill. */
    BUTTON_HOVER_BG("button_hover_bg_color", { c -> c.buttonHoverBg }),

    /** Pressed button fill. */
    BUTTON_ACTIVE_BG("button_active_bg_color", { c -> c.buttonActiveBg }),

    /** Scrollbar slider. */
    SCROLLBAR_SLIDER("scrollbar_slider_color", { c -> c.scrollbar }),
    ;

    /** `@define-color` line for this name in the given theme. */
    fun define(colors: GtkThemeColors): String = "@define-color $cssName ${CssColor.css(resolve(colors))};"
}
