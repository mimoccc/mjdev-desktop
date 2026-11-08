package org.psippr.desktop.helpers.greetd

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Regression test: session files become runnable greetd sessions and the last choice is kept. */
class LoginSessionsTest {
    @Test
    fun waylandSessionFileIsParsed() {
        val lines =
            listOf(
                "[Desktop Entry]",
                "Name=Ubuntu",
                "Exec=env GNOME_SHELL_SESSION_MODE=ubuntu /usr/bin/gnome-session --session=ubuntu",
                "DesktopNames=ubuntu;GNOME;",
            )
        val session = LoginSessions.parse(LoginSessionKind.Wayland, "ubuntu", lines)
        assertEquals("Ubuntu", session?.name)
        assertEquals("/bin/sh", session?.cmd?.first())
        assertTrue(session?.env.orEmpty().contains("XDG_CURRENT_DESKTOP=ubuntu:GNOME"))
        assertTrue(session?.env.orEmpty().contains("XDG_SESSION_TYPE=wayland"))
    }

    @Test
    fun hiddenSessionIsSkipped() {
        assertNull(LoginSessions.parse(LoginSessionKind.Wayland, "x", listOf("Name=X", "Exec=x", "Hidden=true")))
    }

    @Test
    fun psipprFallbackStartsPsipprSession() {
        assertEquals(listOf(GreetdSetting.SessionCommand.value), LoginSessions.fallback.cmd)
    }

    @Test
    fun lastLoginIsRemembered() {
        val file = Files.createTempDirectory("last-login").resolve("last.properties").toFile()
        LastLoginStore(file).remember("psippr", "Ubuntu")
        val reread = LastLoginStore(file)
        assertEquals("psippr", reread.lastUser)
        assertEquals("Ubuntu", reread.lastSession("psippr"))
        file.parentFile.deleteRecursively()
    }
}
