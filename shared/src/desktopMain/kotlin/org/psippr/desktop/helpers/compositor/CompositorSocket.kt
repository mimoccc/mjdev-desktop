package org.psippr.desktop.helpers.compositor

import java.nio.file.Files
import java.nio.file.Path

/** Location of the psippr compositor's IPC socket (psipprc `IpcServer`). */
object CompositorSocket {
    /** Environment variable of the per-user runtime dir holding the socket. */
    private const val RUNTIME_DIR_ENV = "XDG_RUNTIME_DIR"

    /** File name of the socket inside the runtime dir. */
    private const val SOCKET_NAME = "psippr-compositor.sock"

    /** The socket path, null when no compositor runs (plain JVM run). */
    fun path(): Path? {
        val dir = System.getenv(RUNTIME_DIR_ENV) ?: return null
        val path = Path.of(dir, SOCKET_NAME)
        return if (Files.exists(path)) path else null
    }
}
