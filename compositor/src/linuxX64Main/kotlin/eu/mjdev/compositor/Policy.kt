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
import mjdev.compositor.shim.MJC_LAYER_BACKGROUND
import mjdev.compositor.shim.MJC_LAYER_TOP
import mjdev.compositor.shim.MJC_MOD_ALT
import mjdev.compositor.shim.mjc_view_close
import mjdev.compositor.shim.mjc_view_focus
import mjdev.compositor.shim.mjc_view_set_chrome
import mjdev.compositor.shim.mjc_view_set_focusable
import mjdev.compositor.shim.mjc_view_set_layer

/**
 * Window management policy:
 * - windows of the desktop shell app (recognized by WM_CLASS/app_id, or
 *   a "mjdev::<role>" title prefix) are placed into dedicated layers
 * - everything else is a normal app window
 */
class Policy(private val c: Compositor) {

    private val shellClasses = setOf(
        "mjdev-desktop",
        "org.mjdev.desktop",
        "eu.mjdev.desktop",
        // default AWT WM_CLASS of the Compose desktop main class
        "eu-mjdev-desktop-mainkt",
    )

    fun apply(info: WindowInfo) {
        val appId = info.appId?.lowercase().orEmpty()
        val titleRole = info.title
            ?.takeIf { it.startsWith(TITLE_PREFIX) }
            ?.removePrefix(TITLE_PREFIX)
            ?.substringBefore(' ')
            ?.lowercase()
        val isShell = appId in shellClasses || titleRole != null
        info.shell = isShell
        if (!isShell) {
            return
        }
        info.role = titleRole ?: "window"
        // shell surfaces (wallpaper, panels, menus, control center) are our own UI and
        // must never get a server-side frame; app windows stay decorated
        mjc_view_set_chrome(info.ptr, true)
        when (info.role) {
            // ChromeWindow names: DesktopWindow/FullScreenWindow carry the wallpaper
            "wallpaper", "background", "desktop",
            "desktopwindow", "fullscreenwindow" -> {
                mjc_view_set_layer(info.ptr, MJC_LAYER_BACKGROUND)
                mjc_view_set_focusable(info.ptr, false)
            }

            // the panel is click only - keeping it unfocusable means opening
            // it never steals keyboard focus from the menu or from apps
            "desktoppanel", "panel", "tooltip" -> {
                mjc_view_set_layer(info.ptr, MJC_LAYER_TOP)
                mjc_view_set_focusable(info.ptr, false)
            }

            // menus, control center & co stay above app windows
            else -> {
                mjc_view_set_layer(info.ptr, MJC_LAYER_TOP)
            }
        }
    }

    fun handleKey(keysym: UInt, modifiers: UInt, pressed: Boolean): Boolean {
        if (!pressed) {
            return false
        }
        val alt = modifiers and MJC_MOD_ALT.toUInt() != 0u
        return when {
            alt && keysym == XKB_KEY_TAB -> {
                focusNext()
                true
            }

            alt && keysym == XKB_KEY_F4 -> {
                c.windows.all().firstOrNull { it.focused }?.let {
                    mjc_view_close(it.ptr)
                }
                true
            }

            else -> false
        }
    }

    private fun focusNext() {
        val cycle = c.windows.all().filter {
            it.mapped && !it.minimized && !it.shell
        }
        if (cycle.isEmpty()) {
            return
        }
        val current = cycle.indexOfFirst { it.focused }
        val next = cycle[(current + 1).mod(cycle.size)]
        mjc_view_focus(next.ptr)
    }

    companion object {
        const val TITLE_PREFIX = "mjdev::"
        val XKB_KEY_TAB = 0xff09u
        val XKB_KEY_F4 = 0xffc1u
    }
}
