/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.processes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.*
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.context.LocalDesktopContext
import org.mjdev.desktop.extensions.Compose.orElse
import org.mjdev.desktop.interfaces.IApp
import org.mjdev.desktop.interfaces.IProcessListener
import org.mjdev.desktop.log.Log
import org.mjdev.desktop.managers.process.IProcessManager
import kotlin.jvm.optionals.getOrNull

// todo dbus
@Suppress("unused")
class ProcessManager(
    context: IDesktopContext,
    private val checkDelay: Long = 1000L,
) : IProcessManager {
    private val listeners = mutableListOf<IProcessListener>()

    override val size: Int
        get() =
            runCatching {
                processes.size
            }.onFailure { e ->
                Log.e(e)
            }.getOrElse { 0 }

    private var job: Job? = null

    private val processes = mutableStateListOf<ProcessWrapper>()

    init {
        job =
            context.scope.launch(Dispatchers.Default) {
                while (isActive) {
                    ProcessHandle.allProcesses().toList().filterNotNull().let { phList ->
                        phList.forEach { ph ->
                            if (!processes.containsProcess(ph)) {
                                ProcessWrapper(ph).also { pw ->
                                    processes.add(pw)
                                }
                                listeners.filterIsInstance<ProcessAddListener>().forEach { l ->
                                    l.onProcessAdded(ph)
                                }
                            }
                        }
                        cleanup()
                    }
                    delay(checkDelay)
                }
            }
    }

    private fun cleanup() {
        processes.toList().forEach { pw ->
            if (pw.processHandle?.isAlive != true) {
                processes.remove(pw)
                listeners.filterIsInstance<ProcessRemoveListener>().forEach { l ->
                    l.onProcessRemoved(pw.processHandle)
                }
            }
        }
    }

    override fun addListener(listener: IProcessListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: IProcessListener) {
        listeners.remove(listener)
    }

    override fun dispose() {
        job?.cancel()
    }

    // todo : something raises error
    override fun hasAppProcess(app: IApp?): Boolean =
        runCatching {
            val appCmd = app?.cmd
            val appName = app?.name
            val appFullName = app?.fullAppName
            processes.toList().any { pw ->
                (appName != null && pw.command.contains(appName)) ||
                    (appCmd != null && pw.command.contains(appCmd)) ||
                    (appFullName != null && pw.command.contains(appFullName)) ||
                    (appName != null && pw.commandLine.contains(appName)) ||
                    (appCmd != null && pw.commandLine.contains(appCmd)) ||
                    (appFullName != null && pw.commandLine.contains(appFullName))
            }
        }.onFailure { e ->
            Log.e(e)
        }.getOrNull() ?: false

    companion object {
        fun SnapshotStateList<ProcessWrapper>.containsProcess(ph: ProcessHandle) =
            ph.pid().let { pid ->
                toList().any { p -> p.pid == pid }
            }

        fun SnapshotStateList<ProcessWrapper>.containsProcess(ph: ProcessWrapper) =
            toList().any { p -> p.pid == ph.pid }

        @Composable
        fun processManagerListener(onChanged: IProcessManager?.(processHandle: ProcessHandle?) -> Unit = {}) =
            processManagerListener(onChanged, onChanged)

        @Composable
        fun processManagerListener(
            onAdd: IProcessManager?.(processHandle: ProcessHandle?) -> Unit,
            onRemove: IProcessManager?.(processHandle: ProcessHandle?) -> Unit,
            context: IDesktopContext = LocalDesktopContext.current,
            processManger: IProcessManager? = context.processManager, // todo
        ): IProcessManager? {
            val addListener =
                object : ProcessAddListener {
                    override fun onProcessAdded(processHandle: ProcessHandle?) {
                        onAdd.invoke(processManger, processHandle)
                    }
                }
            val removeListener =
                object : ProcessRemoveListener {
                    override fun onProcessRemoved(processHandle: ProcessHandle?) {
                        onRemove.invoke(processManger, processHandle)
                    }
                }
            DisposableEffect(Unit) {
                processManger?.addListener(addListener)
                processManger?.addListener(removeListener)
                onDispose {
                    processManger?.removeListener(addListener)
                    processManger?.removeListener(removeListener)
                }
            }
            return processManger
        }
    }

    interface ProcessAddListener : IProcessListener {
        fun onProcessAdded(processHandle: ProcessHandle?)
    }

    interface ProcessRemoveListener : IProcessListener {
        fun onProcessRemoved(processHandle: ProcessHandle?)
    }

    class ProcessWrapper(
        val processHandle: ProcessHandle?,
        val pid: Long = processHandle?.pid().orElse { -1L },
        val info: ProcessHandle.Info? = processHandle?.info(),
        val command: String = info?.command()?.getOrNull().orEmpty(),
        val commandLine: String = info?.commandLine()?.getOrNull().orEmpty(),
    ) {
        override fun toString(): String = "$pid | $command | $commandLine"
    }
}
