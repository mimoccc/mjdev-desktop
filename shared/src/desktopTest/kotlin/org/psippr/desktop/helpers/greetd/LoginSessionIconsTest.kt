package org.psippr.desktop.helpers.greetd

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Terminal
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression tests: every login session gets a picture, logo files win over the vector fallback. */
class LoginSessionIconsTest {
    @Test
    fun variantFallsBackToBaseNameAndDesktopNames() {
        assertEquals(
            listOf("gnome-wayland", "gnome", "ubuntu"),
            LoginSessionIcons.candidates("gnome-wayland", listOf("GNOME", "ubuntu")),
        )
    }

    @Test
    fun psipprLooksForItsPackageIconFirst() {
        assertEquals("psippr-desktop", LoginSessionIcons.candidates("psippr").first())
    }

    @Test
    fun existingIconKeyPathIsUsed() {
        val logo = File.createTempFile(TEMP_PREFIX, TEMP_SUFFIX)
        try {
            assertEquals(logo, LoginSessionIcons.resolve(UNKNOWN_ID, logo.absolutePath))
        } finally {
            logo.delete()
        }
    }

    @Test
    fun cssStyledSvgIsSkipped() {
        val logo = File.createTempFile(TEMP_PREFIX, TEMP_SUFFIX)
        try {
            logo.writeText(CSS_SVG)
            assertFalse(LoginSessionIcons.isDrawable(logo))
            assertEquals(Icons.Filled.DesktopWindows, LoginSessionIcons.resolve(UNKNOWN_ID, logo.absolutePath))
            logo.writeText(PLAIN_SVG)
            assertTrue(LoginSessionIcons.isDrawable(logo))
        } finally {
            logo.delete()
        }
    }

    @Test
    fun sessionWithoutLogoGetsVectorIcon() {
        assertEquals(Icons.Filled.DesktopWindows, LoginSessionIcons.resolve(UNKNOWN_ID))
        assertEquals(Icons.Filled.Terminal, LoginSessionIcons.resolve(UNKNOWN_TERMINAL_ID))
    }

    private companion object {
        /** Session id no icon dir has a file for. */
        const val UNKNOWN_ID = "zz-no-such-session-4711"

        /** Session id without a logo that names a terminal. */
        const val UNKNOWN_TERMINAL_ID = "zz-terminal-4711"

        /** Temp logo file name parts. */
        const val TEMP_PREFIX = "session-logo"
        const val TEMP_SUFFIX = ".svg"

        /** SVG coloring its shapes by CSS classes, like Ubuntu's logo. */
        const val CSS_SVG = "<svg><defs><style>.a{fill:#e95420;}</style></defs><rect class=\"a\"/></svg>"

        /** SVG with plain fill attributes. */
        const val PLAIN_SVG = "<svg><rect fill=\"#e95420\"/></svg>"
    }
}
