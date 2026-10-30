package org.psippr.desktop.helpers.compositor

import org.psippr.desktop.state.DesktopState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompositorWindowListTest {
    /** A list-windows reply as psipprc sends it: one app window, one shell window. */
    private val reply =
        """{"ok":true,"windows":[""" +
            """{"id":13,"app_id":"org.gnome.TextEditor","mapped":true,"minimized":false,"maximized":false,""" +
            """"x":64,"y":36,"width":1152,"height":648,"focused":true,"shell":false,"blurred":false},""" +
            """{"id":7,"app_id":"org-psippr-desktop-main-MainKt","mapped":true,"minimized":false,""" +
            """"x":0,"y":640,"width":1280,"height":80,"shell":true}]}"""

    @Test
    fun appWindowIsReadAndShellWindowSkipped() {
        assertEquals(
            listOf(DesktopState.ScreenRect(64.0, 36.0, 1216.0, 684.0)),
            CompositorWindowList.appWindows(reply, NO_SCALE, NO_SCALE),
        )
    }

    @Test
    fun minimizedWindowIsSkipped() {
        val minimized = reply.replace("\"minimized\":false,\"maximized\"", "\"minimized\":true,\"maximized\"")
        assertTrue(CompositorWindowList.appWindows(minimized, NO_SCALE, NO_SCALE).isEmpty())
    }

    @Test
    fun pixelsAreScaledToAwtUnits() {
        val rect = CompositorWindowList.appWindows(reply, DOUBLE_SCALE, DOUBLE_SCALE).single()
        assertEquals(DesktopState.ScreenRect(32.0, 18.0, 608.0, 342.0), rect)
    }

    @Test
    fun brokenReplyGivesNoWindows() {
        assertTrue(CompositorWindowList.appWindows("{not json", NO_SCALE, NO_SCALE).isEmpty())
    }

    private companion object {
        /** AWT scale without UI scaling. */
        const val NO_SCALE = 1.0

        /** AWT scale of a 200 % screen. */
        const val DOUBLE_SCALE = 2.0
    }
}
