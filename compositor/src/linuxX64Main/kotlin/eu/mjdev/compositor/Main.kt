/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

@file:OptIn(ExperimentalForeignApi::class)

package eu.mjdev.compositor

import cnames.structs.mjc_server
import cnames.structs.mjc_view
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import mjdev.compositor.shim.mjc_callbacks
import mjdev.compositor.shim.mjc_create
import mjdev.compositor.shim.mjc_destroy
import mjdev.compositor.shim.mjc_run
import mjdev.compositor.shim.mjc_start
import mjdev.compositor.shim.mjc_terminate
import platform.posix.SIGPIPE
import platform.posix.SIG_IGN
import platform.posix.getenv
import platform.posix.signal

class Config(
    val shellCmd: String?,
    val startupCmds: List<String>,
    val sessionMode: Boolean,
    val socketPath: String,
) {
    companion object {
        fun parse(args: Array<String>): Config {
            var shellCmd: String? = null
            val startup = mutableListOf<String>()
            var session = false
            var socket: String? = null
            var i = 0
            while (i < args.size) {
                when (args[i]) {
                    "--shell-cmd" -> shellCmd = args[++i]
                    "--startup" -> startup += args[++i]
                    "--session" -> session = true
                    "--socket" -> socket = args[++i]
                    "--help", "-h" -> {
                        println(
                            "mjdevc [--shell-cmd CMD] [--startup CMD]... [--session] [--socket PATH]"
                        )
                        platform.posix.exit(0)
                    }
                }
                i++
            }
            val runtimeDir = getenv("XDG_RUNTIME_DIR")?.toKString() ?: "/tmp"
            return Config(
                shellCmd = shellCmd,
                startupCmds = startup,
                sessionMode = session,
                socketPath = socket ?: "$runtimeDir/mjdev-compositor.sock",
            )
        }
    }
}

class Compositor(val config: Config) {
    val server: CPointer<mjc_server> = mjc_create()
        ?: error("mjdevc: cannot allocate server")
    val windows = WindowModel()
    val policy = Policy(this)
    val ipc = IpcServer(this)
    val session = Session(this)
    val geometry = GeometryStore()

    private var self: StableRef<Compositor>? = null

    fun run(): Boolean {
        signal(SIGPIPE, SIG_IGN)
        val ref = StableRef.create(this)
        self = ref
        val started = memScoped {
            val cbs = alloc<mjc_callbacks>()
            cbs.view_new = staticCFunction(::cbViewNew)
            cbs.view_map = staticCFunction(::cbViewMap)
            cbs.view_unmap = staticCFunction(::cbViewUnmap)
            cbs.view_destroy = staticCFunction(::cbViewDestroy)
            cbs.view_title = staticCFunction(::cbViewTitle)
            cbs.view_app_id = staticCFunction(::cbViewAppId)
            cbs.focus_change = staticCFunction(::cbFocusChange)
            cbs.key = staticCFunction(::cbKey)
            cbs.fd_event = staticCFunction(::cbFdEvent)
            cbs.child_exit = staticCFunction(::cbChildExit)
            cbs.ready = staticCFunction(::cbReady)
            cbs.pointer = staticCFunction(::cbPointer)
            mjc_start(server, cbs.ptr, ref.asCPointer())
        }
        if (!started) {
            Clog.log("backend failed to start")
            ref.dispose()
            return false
        }
        Clog.log("backend started (session=${config.sessionMode}, socket=${config.socketPath})")
        ipc.start()
        mjc_run(server)
        Clog.log("event loop ended, shutting down")
        ipc.stop()
        mjc_destroy(server)
        ref.dispose()
        self = null
        return true
    }

    fun terminate() {
        Clog.log("terminate requested")
        mjc_terminate(server)
    }

    // callback dispatch ------------------------------------------------

    fun onViewNew(view: CPointer<mjc_view>, xwayland: Boolean) {
        val info = windows.add(view, xwayland)
        Clog.v("view new: ${info.describe()}")
    }

    fun onViewMap(view: CPointer<mjc_view>) {
        val info = windows.get(view) ?: return
        info.refresh()
        info.mapped = true
        policy.apply(info)
        if (!info.shell) {
            geometry.restore(info)
        }
        Clog.v("view mapped: ${info.describe()} role=${info.role ?: "-"}")
        ipc.broadcastEvent("window-opened", info)
    }

    fun onViewUnmap(view: CPointer<mjc_view>) {
        val info = windows.get(view) ?: return
        info.mapped = false
        if (!info.shell) {
            geometry.remember(info)
        }
        Clog.v("view unmapped: ${info.describe()}")
        ipc.broadcastEvent("window-closed", info)
    }

    fun onViewDestroy(view: CPointer<mjc_view>) {
        val info = windows.remove(view) ?: return
        Clog.v("view destroyed: ${info.describe()}")
        if (info.mapped) {
            ipc.broadcastEvent("window-closed", info)
        }
    }

    fun onViewTitle(view: CPointer<mjc_view>, title: String?) {
        val info = windows.get(view) ?: return
        info.title = title
        policy.apply(info)
        if (info.mapped) {
            ipc.broadcastEvent("window-title", info)
        }
    }

    fun onViewAppId(view: CPointer<mjc_view>, appId: String?) {
        val info = windows.get(view) ?: return
        info.appId = appId
        policy.apply(info)
    }

    fun onFocusChange(view: CPointer<mjc_view>?) {
        windows.all().forEach { it.focused = false }
        val info = view?.let { windows.get(it) }
        info?.focused = true
        Clog.v("focus changed -> ${info?.describe() ?: "none"}")
        ipc.broadcastEvent("focus-changed", info)
    }

    fun onKey(keysym: UInt, modifiers: UInt, pressed: Boolean): Boolean =
        policy.handleKey(keysym, modifiers, pressed)

    fun onFdEvent(fd: Int, mask: UInt): Int = ipc.handleFd(fd, mask)

    fun onChildExit(pid: Int, status: Int) = session.onChildExit(pid, status)

    fun onReady() = session.onReady()

    fun onPointer(x: Int, y: Int) {
        Clog.pointer(x, y)
        ipc.broadcastPointer(x, y)
    }
}

private fun ctx(ud: COpaquePointer?): Compositor =
    ud!!.asStableRef<Compositor>().get()

private fun cbViewNew(ud: COpaquePointer?, view: CPointer<mjc_view>?, xwayland: Boolean) {
    ctx(ud).onViewNew(view ?: return, xwayland)
}

private fun cbViewMap(ud: COpaquePointer?, view: CPointer<mjc_view>?) {
    ctx(ud).onViewMap(view ?: return)
}

private fun cbViewUnmap(ud: COpaquePointer?, view: CPointer<mjc_view>?) {
    ctx(ud).onViewUnmap(view ?: return)
}

private fun cbViewDestroy(ud: COpaquePointer?, view: CPointer<mjc_view>?) {
    ctx(ud).onViewDestroy(view ?: return)
}

private fun cbViewTitle(
    ud: COpaquePointer?,
    view: CPointer<mjc_view>?,
    title: CPointer<kotlinx.cinterop.ByteVar>?
) {
    ctx(ud).onViewTitle(view ?: return, title?.toKString())
}

private fun cbViewAppId(
    ud: COpaquePointer?,
    view: CPointer<mjc_view>?,
    appId: CPointer<kotlinx.cinterop.ByteVar>?
) {
    ctx(ud).onViewAppId(view ?: return, appId?.toKString())
}

private fun cbFocusChange(ud: COpaquePointer?, view: CPointer<mjc_view>?) {
    ctx(ud).onFocusChange(view)
}

private fun cbKey(
    ud: COpaquePointer?,
    keysym: UInt,
    modifiers: UInt,
    pressed: Boolean
): Boolean = ctx(ud).onKey(keysym, modifiers, pressed)

private fun cbFdEvent(ud: COpaquePointer?, fd: Int, mask: UInt): Int =
    ctx(ud).onFdEvent(fd, mask)

private fun cbChildExit(ud: COpaquePointer?, pid: Int, status: Int) {
    ctx(ud).onChildExit(pid, status)
}

private fun cbReady(ud: COpaquePointer?) {
    ctx(ud).onReady()
}

private fun cbPointer(ud: COpaquePointer?, x: Int, y: Int) {
    ctx(ud).onPointer(x, y)
}

fun main(args: Array<String>) {
    val compositor = Compositor(Config.parse(args))
    if (!compositor.run()) {
        platform.posix.exit(1)
    }
}
