package org.mjdev.desktop.managers.theme.linux.gtk

/** Where a generated GTK stylesheet goes; decides how much it contains. */
enum class GtkCssLayer {
    /**
     * `~/.themes/Mjdev/gtk-x.0/gtk.css` - a full theme: stock base import + palette colors + recolor
     * rules. GTK re-reads it on a `gtk-theme` change, which is how open windows get the new colors.
     */
    THEME,

    /**
     * `~/.config/gtk-x.0/gtk.css` - loaded on top of any theme and by libadwaita apps: palette
     * named colors only, so libadwaita / Yaru keep their own widget styling.
     */
    USER,
}
