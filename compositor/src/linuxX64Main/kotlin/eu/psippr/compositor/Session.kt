/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

@file:OptIn(ExperimentalForeignApi::class)

package eu.psippr.compositor

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import psippr.compositor.shim.mjc_socket_name
import psippr.compositor.shim.mjc_spawn
import psippr.compositor.shim.mjc_xwayland_display
import platform.posix.setenv

/**
 * Session bootstrap: exports environment to dbus, starts the
 * xdg desktop portals (full session only), the desktop shell and
 * optional extra startup commands. Ends the compositor when the
 * shell exits cleanly or keeps restarting it after crashes.
 */
class Session(
    private val c: Compositor,
) {
    private var shellPid = -1
    private var crashRestarts = 0

    fun onReady() {
        val wayland = mjc_socket_name(c.server)?.toKString()
        val x11 = mjc_xwayland_display(c.server)?.toKString()
        println("psipprc: WAYLAND_DISPLAY=$wayland DISPLAY=${x11 ?: "-"}")

        setenv("XDG_CURRENT_DESKTOP", "psippr", 1)
        if (!c.config.sessionMode) {
            // the nested shell must not change the host desktop's settings, see NestedSessionBus
            setenv(NestedSessionBus.ENV_NESTED, NestedSessionBus.NESTED, 1)
        }
        setenv("XDG_SESSION_TYPE", "wayland", 1)

        // old: exported in nested mode too, which overwrote the HOST session's activation
        // environment: every app D-Bus starts there (gnome-terminal-server, nautilus, ...) then
        // went to the nested display and, once that was gone, no window opened at all
//        mjc_spawn(
//            c.server,
//            "dbus-update-activation-environment --systemd " +
//                "WAYLAND_DISPLAY DISPLAY XDG_CURRENT_DESKTOP XDG_SESSION_TYPE " +
//                "2>/dev/null || true",
//        )
        if (c.config.sessionMode) {
            // only the real session owns the user's bus; nested apps inherit the display directly
            mjc_spawn(
                c.server,
                "dbus-update-activation-environment --systemd " +
                    "WAYLAND_DISPLAY DISPLAY XDG_CURRENT_DESKTOP XDG_SESSION_TYPE " +
                    "2>/dev/null || true",
            )
        }

        if (c.config.sessionMode) {
            // portals deliver live gtk-theme/color-scheme changes to gtk apps;
            // never started in nested mode where the host session owns them
            mjc_spawn(c.server, "/usr/libexec/xdg-desktop-portal-gtk")
            mjc_spawn(c.server, "sleep 1; exec /usr/libexec/xdg-desktop-portal")
        }

        c.config.startupCmds.forEach { mjc_spawn(c.server, it) }
        startShell()
    }

    private fun startShell() {
        val cmd = c.config.shellCmd ?: return
        // old: a nested shell shared the host's session bus, so apps D-Bus starts (gnome-terminal)
        // opened on the host display or, with the host's environment broken, not at all
//        shellPid = mjc_spawn(c.server, cmd)
        shellPid = mjc_spawn(c.server, if (c.config.sessionMode) cmd else NestedSessionBus.wrap(cmd))
        println("psipprc: shell started pid=$shellPid: $cmd")
    }

    fun onChildExit(
        pid: Int,
        status: Int,
    ) {
        if (pid != shellPid || shellPid < 0) {
            return
        }
        val crashed = status != 0
        if (crashed && crashRestarts < MAX_SHELL_RESTARTS) {
            crashRestarts++
            println("psipprc: shell crashed (status=$status), restart $crashRestarts")
            startShell()
        } else {
            println("psipprc: shell exited (status=$status), terminating")
            c.terminate()
        }
    }

    companion object {
        private const val MAX_SHELL_RESTARTS = 3
    }
}
