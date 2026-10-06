package org.mjdev.desktop.helpers.font.guard

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Regression tests of the startup freeze: fontconfig entries without a family (stale `.woff` /
 * `.woff2` cache entries) made Skia spin forever, so exactly those must be found and hidden.
 */
class FontGuardTest {
    @Test
    fun findsOnlyFontsWithoutFamily() {
        val output =
            listOf(
                "Victor Mono\t$GOOD_FILE",
                "\t$BROKEN_FILE",
                "\t$BROKEN_FILE_WITH_SPACE",
                "",
            ).joinToString(LINE_BREAK)
        assertEquals(listOf(BROKEN_FILE, BROKEN_FILE_WITH_SPACE), BrokenFontScanner.parse(output))
    }

    @Test
    fun ignoresDuplicatesAndMalformedLines() {
        val output = listOf("\t$BROKEN_FILE", "\t$BROKEN_FILE", "no separator", "\t").joinToString(LINE_BREAK)
        assertEquals(listOf(BROKEN_FILE), BrokenFontScanner.parse(output))
    }

    @Test
    fun configRejectsEachFileByFullPathAndIncludesBase() {
        val config = FontGuardConfig.render(BASE_CONFIG, listOf(BROKEN_FILE, BROKEN_FILE_WITH_SPACE))
        assertTrue(config.contains("<glob>$BROKEN_FILE</glob>"))
        assertTrue(config.contains("<glob>$BROKEN_FILE_WITH_SPACE</glob>"))
        assertTrue(config.contains("<include ignore_missing=\"no\">$BASE_CONFIG</include>"))
        assertFalse(config.contains(GOOD_FILE))
    }

    @Test
    fun configEscapesXmlInPaths() {
        val config = FontGuardConfig.render(BASE_CONFIG, listOf(XML_FILE))
        assertTrue(config.contains("<glob>/fonts/A&amp;B &lt;1&gt;.woff</glob>"))
    }

    private companion object {
        /** Sample font files. */
        const val GOOD_FILE = "/fonts/VictorMono/TTF/VictorMono-Thin.ttf"
        const val BROKEN_FILE = "/fonts/VictorMono/WOFF2/VictorMono-Thin.woff2"
        const val BROKEN_FILE_WITH_SPACE = "/fonts/Roboto Thin/Roboto_Thin_Regular.woff2"
        const val XML_FILE = "/fonts/A&B <1>.woff"

        /** Sample base fontconfig file. */
        const val BASE_CONFIG = "/etc/fonts/fonts.conf"

        /** Line separator of the fc-list output. */
        const val LINE_BREAK = "\n"
    }
}
