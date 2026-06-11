/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

@file:OptIn(ExperimentalForeignApi::class)

package eu.mjdev.compositor

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import mjdev.compositor.shim.mjc_socket_name
import mjdev.compositor.shim.mjc_spawn
import mjdev.compositor.shim.mjc_xwayland_display
import platform.posix.setenv

/**
 * Session bootstrap: exports environment to dbus, starts the
 * xdg desktop portals (full session only), the desktop shell and
 * optional extra startup commands. Ends the compositor when the
 * shell exits cleanly or keeps restarting it after crashes.
 */
class Session(private val c: Compositor) {

    private var shellPid = -1
    private var crashRestarts = 0

    fun onReady() {
        val wayland = mjc_socket_name(c.server)?.toKString()
        val x11 = mjc_xwayland_display(c.server)?.toKString()
        println("mjdevc: WAYLAND_DISPLAY=$wayland DISPLAY=${x11 ?: "-"}")

        setenv("XDG_CURRENT_DESKTOP", "mjdev", 1)
        setenv("XDG_SESSION_TYPE", "wayland", 1)

        mjc_spawn(
            c.server,
            "dbus-update-activation-environment --systemd " +
                    "WAYLAND_DISPLAY DISPLAY XDG_CURRENT_DESKTOP XDG_SESSION_TYPE " +
                    "2>/dev/null || true"
        )

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
        shellPid = mjc_spawn(c.server, cmd)
        println("mjdevc: shell started pid=$shellPid: $cmd")
    }

    fun onChildExit(pid: Int, status: Int) {
        if (pid != shellPid || shellPid < 0) {
            return
        }
        val crashed = status != 0
        if (crashed && crashRestarts < MAX_SHELL_RESTARTS) {
            crashRestarts++
            println("mjdevc: shell crashed (status=$status), restart $crashRestarts")
            startShell()
        } else {
            println("mjdevc: shell exited (status=$status), terminating")
            c.terminate()
        }
    }

    companion object {
        private const val MAX_SHELL_RESTARTS = 3
    }
}
