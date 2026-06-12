/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.window

import org.mjdev.desktop.interfaces.IApp
import org.mjdev.desktop.interfaces.IDisposable
import org.mjdev.desktop.managers.base.IDelegate

interface IWindowsManager : IDisposable, IDelegate {
    val isCompositorAvailable: Boolean
    val windows: List<SystemWindow>

    fun findWindows(app: IApp? = null): List<SystemWindow>

    fun hasWindow(app: IApp): Boolean

    fun isWindowFocus(app: IApp): Boolean

    fun requestWindowsFocus(app: IApp): Boolean

    fun minimizeWindows(app: IApp): Boolean

    fun closeWindows(app: IApp): Boolean

    fun activateWindow(window: SystemWindow)

    fun minimizeWindow(window: SystemWindow)

    fun closeWindow(window: SystemWindow)

    /** windows matching given app, exposed for taskbar style uses */
    fun windowsOf(app: IApp): List<SystemWindow> = findWindows(app)

    data class SystemWindow(
        val id: Long,
        val pid: Long,
        val windowClass: String,
        val desktop: Long,
        val command: String,
        val title: String = "",
        val minimized: Boolean = false,
        val focused: Boolean = false,
    )

    companion object {
        val EMPTY = object : IWindowsManager {
            override val isCompositorAvailable: Boolean = false
            override val windows: List<SystemWindow> = emptyList()
            override fun findWindows(app: IApp?): List<SystemWindow> = emptyList()
            override fun hasWindow(app: IApp): Boolean = false
            override fun isWindowFocus(app: IApp): Boolean = false
            override fun requestWindowsFocus(app: IApp): Boolean = false
            override fun minimizeWindows(app: IApp): Boolean = false
            override fun closeWindows(app: IApp): Boolean = false
            override fun activateWindow(window: SystemWindow) {}
            override fun minimizeWindow(window: SystemWindow) {}
            override fun closeWindow(window: SystemWindow) {}
            override fun dispose() {}
        }
    }
}
