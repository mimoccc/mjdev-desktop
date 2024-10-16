/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.processes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.extensions.Custom.orElse
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import kotlinx.coroutines.*
import kotlin.jvm.optionals.getOrNull

// todo dbus
@Suppress("unused")
class ProcessManager(
    private val checkDelay: Long = 1000L
) : AutoCloseable {
    val size: Int get() = processes.size
    private val scope = CoroutineScope(Dispatchers.IO)
    private val listeners = mutableListOf<ProcessListener>()
    private var job: Job? = null
    private val processes = mutableStateListOf<ProcessWrapper>()

    init {
        job = scope.launch(Dispatchers.IO) {
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
        processes.iterator().apply {
            while (hasNext()) {
                val pw = next()
                if (pw.processHandle?.isAlive != true) {
                    remove()
                    listeners.filterIsInstance<ProcessRemoveListener>().forEach { l ->
                        l.onProcessRemoved(pw.processHandle)
                    }
                }
            }
        }
    }

    fun addListener(listener: ProcessListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ProcessListener) {
        listeners.remove(listener)
    }

    override fun close() {
        job?.cancel()
    }

    fun dispose() = close()

    // todo : something raises error
    fun hasAppProcess(app: App?): Boolean = runCatching {
        val appCmd = app?.cmd
        val appName = app?.name
        val appFullName = app?.fullAppName
        processes.any { pw ->
            (appName != null && pw.command.contains(appName)) ||
                    (appCmd != null && pw.command.contains(appCmd)) ||
                    (appFullName != null && pw.command.contains(appFullName)) ||
                    (appName != null && pw.commandLine.contains(appName)) ||
                    (appCmd != null && pw.commandLine.contains(appCmd)) ||
                    (appFullName != null && pw.commandLine.contains(appFullName))

        }
    }.getOrNull() ?: false

    companion object {
        fun SnapshotStateList<ProcessWrapper>.containsProcess(
            ph: ProcessHandle
        ) = ph.pid().let { pid ->
            any { p -> p.pid == pid }
        }

        fun SnapshotStateList<ProcessWrapper>.containsProcess(
            ph: ProcessWrapper
        ) = any { p -> p.pid == ph.pid }

        @Composable
        fun rememberProcessManager(
            checkDelay: Long = 1000L
        ) = remember { ProcessManager(checkDelay) }

        @Composable
        fun processManagerListener(
            onChanged: ProcessManager.(processHandle: ProcessHandle?) -> Unit = {},
        ) = processManagerListener(onChanged, onChanged)

        @Composable
        fun processManagerListener(
            onAdd: ProcessManager.(processHandle: ProcessHandle?) -> Unit,
            onRemove: ProcessManager.(processHandle: ProcessHandle?) -> Unit,
            api: DesktopProvider = LocalDesktop.current,
            processManger: ProcessManager = api.processManager
        ): ProcessManager {
            val addListener = object : ProcessAddListener {
                override fun onProcessAdded(processHandle: ProcessHandle?) {
                    onAdd.invoke(processManger, processHandle)
                }
            }
            val removeListener = object : ProcessRemoveListener {
                override fun onProcessRemoved(processHandle: ProcessHandle?) {
                    onRemove.invoke(processManger, processHandle)
                }
            }
            DisposableEffect(Unit) {
                processManger.addListener(addListener)
                processManger.addListener(removeListener)
                onDispose {
                    processManger.removeListener(addListener)
                    processManger.removeListener(removeListener)
                }
            }
            return processManger
        }
    }

    interface ProcessListener

    interface ProcessAddListener : ProcessListener {
        fun onProcessAdded(processHandle: ProcessHandle?)
    }

    interface ProcessRemoveListener : ProcessListener {
        fun onProcessRemoved(processHandle: ProcessHandle?)
    }

    class ProcessWrapper(
        val processHandle: ProcessHandle?,
        val pid: Long = processHandle?.pid().orElse { -1L },
        val info: ProcessHandle.Info? = processHandle?.info(),
        val command: String = info?.command()?.getOrNull().orEmpty(),
        val commandLine: String = info?.commandLine()?.getOrNull().orEmpty()
    ) {
        override fun toString(): String {
            return "$pid | $command | $commandLine"
        }
    }
}