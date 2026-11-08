package org.psippr.desktop.helpers.files

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Regression: a folder double clicked in the nested session started Nautilus over D-Bus without
 * a window; the app is now run from its Exec line with the file in place of the field code.
 */
class ExecLineTest {
    @Test
    fun fileTakesThePlaceOfTheFieldCode() {
        assertEquals(
            listOf("nautilus", "--new-window", "/home/u"),
            ExecLine.command("nautilus --new-window %U", "/home/u"),
        )
    }

    @Test
    fun otherFieldCodesAreDropped() {
        assertEquals(listOf("app", "/f"), ExecLine.command("app %i %f %k", "/f"))
    }

    @Test
    fun quotedArgumentStaysOne() {
        assertEquals(listOf("sh", "-c", "echo \"hi\"", "/f"), ExecLine.command("sh -c \"echo \\\"hi\\\"\" %f", "/f"))
    }

    @Test
    fun entryWithoutFileCodeStillGetsTheFile() {
        assertEquals(listOf("thunar", "/f"), ExecLine.command("thunar", "/f"))
    }

    @Test
    fun emptyExecIsNoCommand() {
        assertEquals(emptyList(), ExecLine.command("  ", "/f"))
    }
}
