package org.psippr.desktop.managers.display

import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.helpers.compositor.CompositorRequest
import org.psippr.desktop.helpers.compositor.CompositorSocket
import org.psippr.desktop.managers.display.mutter.MutterDisplayBackend
import org.psippr.desktop.managers.theme.linux.session.DesktopSession

/**
 * Screen mode of the main display on the JVM desktop. The backend is picked per call, so it
 * follows where the shell runs: the psippr compositor when its socket exists, mutter under GNOME
 * Shell, none elsewhere (then the display settings only show the size).
 */
@Suppress("unused")
class DisplayManager(
    private val context: IDesktopContext,
) : IDisplayManager {
    override val currentMode: DisplayMode?
        get() = backend()?.currentMode()

    override val modes: List<DisplayMode>
        get() = backend()?.modes().orEmpty()

    override fun setMode(mode: DisplayMode): Result<Boolean> = backend()?.setMode(mode) ?: Result.success(false)

    // a socket file alone is no compositor: a killed run leaves it behind, and taking it for one
    // hid mutter's modes in a plain GNOME run; only a compositor that answers counts
    private fun backend(): IDisplayBackend? = when {
        CompositorSocket.path() != null && CompositorRequest.send(PROBE) != null -> CompositorDisplayBackend()
        DesktopSession.current().hasGnomeShell -> MutterDisplayBackend()
        else -> null
    }

    private companion object {
        /** Cheap request telling whether a compositor answers on the socket. */
        const val PROBE = "{\"cmd\":\"list-modes\"}"
    }
}
