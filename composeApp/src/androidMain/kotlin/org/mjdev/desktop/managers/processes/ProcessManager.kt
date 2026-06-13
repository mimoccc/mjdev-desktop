/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.processes

import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.interfaces.IApp
import org.mjdev.desktop.interfaces.IProcessListener
import org.mjdev.desktop.managers.process.IProcessManager

// todo dbus
// todo strange error :  index (71) is out of bound of [0, 0)
@Suppress("unused")
class ProcessManager(
    private val context: IDesktopContext,
    private val checkDelay: Long = 1000L,
) : IProcessManager {
    private val listeners = mutableListOf<IProcessListener>()

    override val size: Int get() = 0

    override fun addListener(listener: IProcessListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: IProcessListener) {
        listeners.remove(listener)
    }

    override fun dispose() {
        listeners.clear()
    }

    // todo
    override fun hasAppProcess(app: IApp?): Boolean = false
}
