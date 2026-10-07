package org.mjdev.desktop.managers.theme.linux

import androidx.compose.ui.graphics.Color
import org.mjdev.desktop.managers.theme.linux.gtk.GtkCss
import org.mjdev.desktop.managers.theme.linux.gtk.GtkCssLayer
import org.mjdev.desktop.managers.theme.linux.gtk.GtkNamedColor
import org.mjdev.desktop.managers.theme.linux.gtk.GtkThemeColors
import org.mjdev.desktop.managers.theme.linux.gtk.GtkToolkit
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression tests: generated GTK themes must keep a stock base and recolor every toolkit. */
class GtkCssTest {
    private val dark =
        GtkThemeColors(
            Color(0xFF202838),
            Color(0xFFD0D8E8),
            Color(0xFF3584E4),
            Color.Black,
            Color.White,
        )
    private val light =
        GtkThemeColors(
            Color(0xFFF0F0E8),
            Color(0xFF202020),
            Color(0xFFE95420),
            Color.White,
            Color.Black,
        )
    private val noThemes = File("/nonexistent-themes")

    @Test
    fun themeLayerImportsBuiltinBaseWhenYaruMissing() {
        val gtk3 = GtkCss.build(GtkToolkit.GTK3, GtkCssLayer.THEME, dark, false, noThemes)
        val gtk4 = GtkCss.build(GtkToolkit.GTK4, GtkCssLayer.THEME, light, false, noThemes)
        assertTrue(gtk3.contains("@import url(\"${GtkToolkit.GTK3.builtinDark}\")"))
        assertTrue(gtk4.contains("@import url(\"${GtkToolkit.GTK4.builtinLight}\")"))
    }

    @Test
    fun themeLayerPrefersInstalledYaru() {
        val themes = createTempDirectory().toFile()
        File(themes, "Yaru-dark/gtk-3.0").mkdirs()
        val yaru = File(themes, "Yaru-dark/gtk-3.0/gtk.css").apply { writeText("") }
        val css = GtkCss.build(GtkToolkit.GTK3, GtkCssLayer.THEME, dark, false, themes)
        assertTrue(css.contains("@import url(\"file://${yaru.absolutePath}\")"))
        themes.deleteRecursively()
    }

    @Test
    fun everyLayerDefinesAllNamedColors() {
        GtkToolkit.entries.forEach { toolkit ->
            GtkCssLayer.entries.forEach { layer ->
                val css = GtkCss.build(toolkit, layer, dark, false, noThemes)
                GtkNamedColor.entries.forEach { named ->
                    assertTrue(css.contains("@define-color ${named.cssName} "), "${named.cssName} in $toolkit $layer")
                }
            }
        }
    }

    @Test
    fun userLayerKeepsLibadwaitaStylingAndHasNoImport() {
        val css = GtkCss.build(GtkToolkit.GTK4, GtkCssLayer.USER, dark, false, noThemes)
        assertFalse(css.contains("@import"))
        assertFalse(css.contains("button {"))
    }

    @Test
    fun titlebuttonsHiddenOnlyWhenCompositorDrawsFrame() {
        val gnome = GtkCss.build(GtkToolkit.GTK3, GtkCssLayer.USER, dark, false, noThemes)
        val mjdev = GtkCss.build(GtkToolkit.GTK3, GtkCssLayer.USER, dark, true, noThemes)
        assertFalse(gnome.contains("titlebutton"))
        assertTrue(mjdev.contains("titlebutton"))
    }

    @Test
    fun gtk3OutputAvoidsGtk4OnlySyntax() {
        val css = GtkCss.build(GtkToolkit.GTK3, GtkCssLayer.THEME, dark, true, noThemes)
        listOf(":root", "var(--", ":focus-within", "color-mix(").forEach { syntax ->
            assertFalse(css.contains(syntax), syntax)
        }
    }

    @Test
    fun paletteColorsReachTheStylesheet() {
        val css = GtkCss.build(GtkToolkit.GTK4, GtkCssLayer.USER, dark, false, noThemes)
        assertTrue(css.contains("@define-color window_bg_color #202838;", ignoreCase = true))
        assertTrue(css.contains("@define-color accent_bg_color #3584E4;", ignoreCase = true))
    }
}
