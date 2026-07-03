package org.mjdev.desktop.managers.remote

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import org.mjdev.desktop.managers.base.IDelegate

/**
 * Serves the running desktop over a standardized remote-desktop protocol (RFB / VNC) so any
 * VNC viewer can connect by IP address and see + control it. Platform-agnostic contract; the
 * actual framebuffer capture + input injection live in the platform implementation
 * (desktopMain uses java.awt.Robot, so it works in plain `runDesktop` with no compositor).
 */
interface IRemoteDesktopManager : IDelegate {
    /** Reactive running flag so the control-center toggle reflects the true server state. */
    val runningState: State<Boolean>
    val isRunning: Boolean get() = runningState.value

    /** TCP port the RFB server listens on (5900 = VNC display :0). */
    val port: Int

    /** LAN IPv4 addresses a viewer can connect to, as `ip:port` strings. */
    val addresses: List<String>

    suspend fun start()

    suspend fun stop()

    suspend fun toggle() {
        if (isRunning) stop() else start()
    }

    companion object {
        val EMPTY =
            object : IRemoteDesktopManager {
                override val runningState = mutableStateOf(false)
                override val port = 5900
                override val addresses = emptyList<String>()

                override suspend fun start() = Unit

                override suspend fun stop() = Unit
            }
    }
}
