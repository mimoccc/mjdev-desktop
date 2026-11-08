package org.psippr.desktop.managers.theme.linux

import org.psippr.desktop.log.Log
import org.psippr.desktop.managers.theme.linux.gsettings.GSettings
import org.psippr.desktop.managers.theme.linux.gsettings.GSettingsKey
import org.psippr.desktop.managers.theme.linux.gtk.GtkThemeColors
import org.psippr.desktop.managers.theme.linux.shell.GnomeAccent
import org.psippr.desktop.managers.theme.linux.shell.UserThemeExtension
import java.io.File

/**
 * Switches the running desktop to the generated theme and pushes palette changes into windows
 * that are already open:
 * - GTK3 / plain GTK4 apps: `gtk-theme` hop away and back -> GTK re-reads `~/.themes/<name>`
 * - libadwaita apps (also flatpak): `color-scheme` + nearest `accent-color`, applied live by libadwaita
 * - GNOME Shell: User Themes `name` hop -> the shell reloads `gnome-shell.css`
 *
 * Values found before the first switch are remembered and put back by [restore].
 *
 * @param themeName generated theme name
 * @param fallbackGtkTheme stock theme used for the reload hop
 * @param shellSchemaDir User Themes schema dir result, failure when the shell theme cannot be switched
 */
class LiveThemeSwitcher(
    private val themeName: String,
    private val fallbackGtkTheme: String,
    private val shellSchemaDir: Result<String?>,
) {
    /** Values before the first switch, per key. */
    private val previous = mutableMapOf<GSettingsKey, String?>()

    /** Shell schema dir, or null with [shellAvailable] false. */
    private val schemaDir: String? = shellSchemaDir.getOrNull()

    /** True when the GNOME Shell theme can be switched. */
    val shellAvailable: Boolean = shellSchemaDir.isSuccess

    /** Applies color scheme + accent and reloads the changed themes in running apps / shell. */
    fun apply(
        colors: GtkThemeColors,
        gtkChanged: Boolean,
        shellChanged: Boolean,
    ) {
        set(GSettingsKey.COLOR_SCHEME, if (colors.isDark) COLOR_SCHEME_DARK else COLOR_SCHEME_LIGHT)
        set(GSettingsKey.ACCENT_COLOR, GnomeAccent.nearest(colors.accentBg).key)
        if (gtkChanged) {
            reload(GSettingsKey.GTK_THEME, fallbackGtkTheme)
        }
        if (shellChanged && shellAvailable) {
            reload(GSettingsKey.SHELL_THEME, SHELL_DEFAULT_THEME)
        }
        Log.d("Live theme applied: $themeName dark=${colors.isDark} gtk=$gtkChanged shell=$shellChanged")
    }

    /** Switches the icon theme to [name]; the previous one comes back with [restore]. */
    fun applyIconTheme(name: String) {
        set(GSettingsKey.ICON_THEME, name)
        Log.d("Icon theme applied: $name")
    }

    /** Puts back every value changed by [apply]; a value that already was ours is reset to default. */
    fun restore() {
        previous.forEach { (key, value) ->
            val dir = schemaDirFor(key)
            // old: only our own theme name, a generated icon theme ("Psippr-...") was put back
//            if (value == null || value == themeName) {
            if (value == null || value == themeName || value.startsWith("$themeName$DERIVED_SEPARATOR")) {
                GSettings.reset(key, dir)
            } else {
                GSettings.set(key, value, dir)
            }
        }
        previous.clear()
    }

    // writes the value, remembering the original one on first touch
    private fun set(
        key: GSettingsKey,
        value: String,
    ) {
        val dir = schemaDirFor(key)
        if (key !in previous) {
            previous[key] = GSettings.get(key, dir)
        }
        GSettings.set(key, value, dir)
    }

    // hop to another value and back: setting an unchanged value emits no change, so nothing reloads
    private fun reload(
        key: GSettingsKey,
        hopValue: String,
    ) {
        set(key, hopValue)
        set(key, themeName)
    }

    private fun schemaDirFor(key: GSettingsKey): String? = if (key.needsExtensionSchema) schemaDir else null

    companion object {
        /** `color-scheme` value for dark palettes. */
        const val COLOR_SCHEME_DARK = "prefer-dark"

        /** `color-scheme` value for light palettes. */
        const val COLOR_SCHEME_LIGHT = "prefer-light"

        /** Joins our theme name and a part in names derived from it (`Psippr-MaterialSymbolsSharp`). */
        const val DERIVED_SEPARATOR = "-"

        /** Empty User Themes name = stock shell theme, used for the reload hop. */
        const val SHELL_DEFAULT_THEME = ""

        /** Switcher with the User Themes extension looked up in [homeDir]. */
        fun create(
            themeName: String,
            fallbackGtkTheme: String,
            homeDir: File,
            shellSupported: Boolean,
        ): LiveThemeSwitcher {
            val schemaDir =
                if (shellSupported) {
                    UserThemeExtension.schemaDir(homeDir)
                } else {
                    Result.failure(IllegalStateException("No GNOME Shell in this session"))
                }
            schemaDir.onFailure { e -> Log.w("GNOME Shell theme not switched: ${e.message}") }
            return LiveThemeSwitcher(themeName, fallbackGtkTheme, schemaDir)
        }
    }
}
