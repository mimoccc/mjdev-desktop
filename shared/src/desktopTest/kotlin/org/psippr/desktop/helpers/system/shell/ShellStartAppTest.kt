package org.psippr.desktop.helpers.system.shell

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okio.Path.Companion.toOkioPath
import org.psippr.desktop.data.App
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Regression test: an app writing more than the pipe buffer (~64 KiB) used to hang forever,
 * because its output was only read after it exited.
 */
class ShellStartAppTest {
    /** Bytes the fake app prints, well above the Linux pipe buffer. */
    private val outputBytes = 256 * 1024

    /** Upper bound for the fake app to finish when its output is not blocked. */
    private val timeoutMs = 20_000L

    @Test
    fun chattyAppDoesNotHang() {
        // dex is a runtime dependency of the desktop; without it there is nothing to launch
        if (!File(DEX_PATH).canExecute()) return
        val desktopFile = File.createTempFile("psippr-chatty", ".desktop").apply {
            deleteOnExit()
            writeText(
                """
                [Desktop Entry]
                Type=Application
                Name=Chatty
                Exec=sh -c "head -c $outputBytes /dev/zero | tr '\\0' x"
                """.trimIndent(),
            )
        }
        var stopped = false
        runBlocking {
            withTimeout(timeoutMs) {
                Shell {}.startApp(App(desktopFile.toOkioPath())) { stopped = true }
            }
        }
        assertTrue(stopped)
    }

    private companion object {
        /** Location of dex on Debian / Ubuntu. */
        const val DEX_PATH = "/usr/bin/dex"
    }
}
