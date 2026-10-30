package org.psippr.desktop.managers.display

import org.psippr.desktop.context.IDesktopContext
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

    private fun backend(): IDisplayBackend? = when {
        CompositorSocket.path() != null -> CompositorDisplayBackend()
        DesktopSession.current().hasGnomeShell -> MutterDisplayBackend()
        else -> null
    }
}
