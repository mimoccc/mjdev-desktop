/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.psippr.compositor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Regression: a nested shell runs on its own bus, so its apps never use the host's bus. */
class NestedSessionBusTest {
    @Test
    fun shellRunsOnItsOwnBusWithTheNestedDisplay() {
        val wrapped = NestedSessionBus.wrap("/opt/psippr-desktop/bin/psippr-desktop")
        assertEquals(
            "exec dbus-run-session -- /bin/sh -c 'dbus-update-activation-environment WAYLAND_DISPLAY DISPLAY " +
                "XDG_CURRENT_DESKTOP XDG_SESSION_TYPE 2>/dev/null; exec /opt/psippr-desktop/bin/psippr-desktop'",
            wrapped,
        )
    }

    @Test
    fun quotesInTheCommandStayOneArgument() {
        val wrapped = NestedSessionBus.wrap("/path/it's here/app")
        assertTrue(wrapped.endsWith("exec /path/it'\\''s here/app'"), wrapped)
    }

    @Test
    fun hostBusIsNeverExportedTo() {
        assertTrue("--systemd" !in NestedSessionBus.wrap("app"))
    }
}
