package org.psippr.desktop.managers.process

import org.psippr.desktop.interfaces.IApp
import org.psippr.desktop.interfaces.IDisposable
import org.psippr.desktop.interfaces.IProcessListener
import org.psippr.desktop.managers.base.IDelegate

interface IProcessManager :
    IDisposable,
    IDelegate {
    val size: Int

    fun hasAppProcess(app: IApp?): Boolean

    fun addListener(listener: IProcessListener)

    fun removeListener(listener: IProcessListener)

    companion object {
        val EMPTY =
            object : IProcessManager {
                override val size: Int = 0

                override fun hasAppProcess(app: IApp?): Boolean = false

                override fun addListener(listener: IProcessListener) {}

                override fun removeListener(listener: IProcessListener) {}

                override fun dispose() {}
            }
    }
}
