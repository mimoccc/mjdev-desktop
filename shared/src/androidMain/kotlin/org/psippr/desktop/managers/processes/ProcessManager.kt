/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.psippr.desktop.managers.processes

import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.interfaces.IApp
import org.psippr.desktop.interfaces.IProcessListener
import org.psippr.desktop.managers.process.IProcessManager

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
