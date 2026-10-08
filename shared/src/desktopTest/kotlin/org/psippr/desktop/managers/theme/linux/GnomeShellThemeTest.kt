package org.psippr.desktop.managers.theme.linux

import androidx.compose.ui.graphics.Color
import org.psippr.desktop.managers.theme.linux.gtk.Gtk2Rc
import org.psippr.desktop.managers.theme.linux.gtk.Gtk2SchemeColor
import org.psippr.desktop.managers.theme.linux.gtk.GtkThemeColors
import org.psippr.desktop.managers.theme.linux.session.DesktopSession
import org.psippr.desktop.managers.theme.linux.shell.GnomeAccent
import org.psippr.desktop.managers.theme.linux.shell.GnomeShellBase
import org.psippr.desktop.managers.theme.linux.shell.GnomeShellCss
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Regression tests: GNOME Shell / GTK2 theme generation, accent mapping and session detection. */
class GnomeShellThemeTest {
    private val dark =
        GtkThemeColors(
            Color(0xFF202838),
            Color(0xFFD0D8E8),
            Color(0xFF3584E4),
            Color.Black,
            Color.White,
        )

    @Test
    fun shellCssImportsStockYaruAndUsesPalette() {
        val css = GnomeShellCss.build(dark, GnomeShellBase.YARU)
        assertTrue(css.contains("@import url(\"${GnomeShellBase.YARU.dark}\")"))
        assertTrue(css.contains("#3584E4", ignoreCase = true))
        assertTrue(css.contains("#panel"))
    }

    @Test
    fun accentMapsToNearestGnomeAccent() {
        assertEquals(GnomeAccent.BLUE, GnomeAccent.nearest(Color(0xFF2060F0)))
        assertEquals(GnomeAccent.RED, GnomeAccent.nearest(Color(0xFFE01010)))
        assertEquals(GnomeAccent.GREEN, GnomeAccent.nearest(Color(0xFF30A040)))
        assertEquals(GnomeAccent.SLATE, GnomeAccent.nearest(Color(0xFF808080)))
    }

    @Test
    fun sessionDetection() {
        assertEquals(DesktopSession.UBUNTU_GNOME, DesktopSession.detect("ubuntu:GNOME"))
        assertEquals(DesktopSession.GNOME, DesktopSession.detect("GNOME"))
        assertEquals(DesktopSession.PSIPPR, DesktopSession.detect("psippr"))
        assertEquals(DesktopSession.OTHER, DesktopSession.detect(null))
    }

    @Test
    fun gtk2SchemeComesBeforeStockIncludes() {
        val rc = Gtk2Rc.build(dark)
        val scheme = rc.indexOf("gtk-color-scheme")
        val include = rc.indexOf("include")
        assertTrue(scheme in 0 until include)
        Gtk2SchemeColor.entries.forEach { color -> assertTrue(rc.contains("${color.rcName}:#"), color.rcName) }
    }
}
