package org.mjdev.desktop.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import org.mjdev.desktop.helpers.keyevents.GlobalKeyListener
import org.mjdev.desktop.helpers.mouseevents.CompositorMouseSource
import org.mjdev.desktop.helpers.mouseevents.GlobalClickListener
import org.mjdev.desktop.helpers.mouseevents.GlobalMouseListener
import java.awt.event.KeyEvent

/**
 * Feeds raw global input into [DesktopState]: pointer moves (compositor feed when running, AWT
 * polling otherwise), clicks (for dismiss-on-click-outside), and Escape. One place wires the
 * whole desktop's input, replacing the per-window global handlers that used to fight each other.
 */
@Suppress("FunctionName")
@Composable
fun DesktopStateDriver(desktopState: DesktopState) {
    val scope = rememberCoroutineScope()
    DisposableEffect(desktopState) {
        // pointer: prefer the compositor's reliable feed, fall back to AWT global polling
        val ipc = CompositorMouseSource.connect(scope) { p -> desktopState.onPointerMove(p.x, p.y) }
        val awtPointer =
            if (ipc == null) {
                GlobalMouseListener(scope = scope) { p -> desktopState.onPointerMove(p.x, p.y) }
            } else {
                null
            }
        val clicks = GlobalClickListener { x, y, _ -> desktopState.onClick(x, y) }
        val keys =
            GlobalKeyListener { event ->
                // act on press (fires first; dismiss/toggle are idempotent so a duplicate is fine)
                if (event.id != KeyEvent.KEY_PRESSED) return@GlobalKeyListener
                when (event.keyCode) {
                    // Escape closes whatever is open (menu or control center); the bar stays if
                    // nothing overlaps it — DesktopPolicy works that out.
                    KeyEvent.VK_ESCAPE -> desktopState.onEscape()
                    // Super / Menu key toggles the apps menu: opening it also closes the control
                    // center and keeps the bar visible.
                    KeyEvent.VK_WINDOWS, KeyEvent.VK_CONTEXT_MENU -> desktopState.toggleMenu()
                }
            }
        onDispose {
            ipc?.dispose()
            awtPointer?.dispose()
            clicks.dispose()
            keys.dispose()
        }
    }
}
