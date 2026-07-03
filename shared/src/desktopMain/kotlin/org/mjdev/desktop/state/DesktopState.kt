package org.mjdev.desktop.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.mjdev.desktop.windows.ChromeWindowState

/**
 * The brain of the desktop. Surfaces (bottom bar, apps menu, control center) *register*
 * themselves with a window state and lambdas describing their on-screen rectangle and their
 * hover-reveal zone; nothing about the individual surfaces is hardcoded here. The state then
 * turns raw input — pointer moves, clicks, Escape, the menu button, and window-overlap news
 * from the compositor — into intent, runs it through the pure [DesktopPolicy], and applies the
 * resulting visibility to each registered window.
 *
 * Because overlap is only known under the compositor, plain `runDesktop` reports "nothing
 * overlaps", so the bar is simply always visible there — no fragile global-pointer reveal needed.
 *
 * Add a new surface or rule by registering another kind and extending [DesktopPolicy]; the wiring
 * here stays the same.
 */
class DesktopState(
    private val scope: CoroutineScope,
) {
    /** An axis-aligned screen rectangle in pixels (pointer + window coords are 1:1 px↔dp). */
    data class ScreenRect(
        val left: Double,
        val top: Double,
        val right: Double,
        val bottom: Double,
    ) {
        fun contains(
            x: Double,
            y: Double,
        ) = x in left..right && y in top..bottom
    }

    private class Surface(
        val window: ChromeWindowState,
        val bounds: () -> ScreenRect,
        val revealHotspot: () -> ScreenRect?,
        val focusOnShow: Boolean,
        val onApply: (visible: Boolean) -> Unit,
    )

    private val surfaces = linkedMapOf<SurfaceKind, Surface>()

    // user intent (what the user asked to be open)
    private var menuOpen = false
    private var controlCenterOpen = false

    // facts about the world
    private var barOverlapped = false
    private var pointerInBarZone = false

    fun register(
        kind: SurfaceKind,
        window: ChromeWindowState,
        bounds: () -> ScreenRect,
        revealHotspot: () -> ScreenRect? = { null },
        focusOnShow: Boolean = false,
        onApply: (visible: Boolean) -> Unit = {},
    ) {
        surfaces[kind] = Surface(window, bounds, revealHotspot, focusOnShow, onApply)
        reconcile()
    }

    // ---- input events ------------------------------------------------------

    /** The global pointer moved. Reveals the control center on its edge, tracks the bar zone. */
    fun onPointerMove(
        x: Int,
        y: Int,
    ) {
        val px = x.toDouble()
        val py = y.toDouble()
        // control center opens when the pointer reaches its reveal edge (stays open until dismissed)
        surfaces[SurfaceKind.ControlCenter]?.revealHotspot?.invoke()?.let { hotspot ->
            if (hotspot.contains(px, py)) controlCenterOpen = true
        }
        // the bar's zone = the bar itself plus its bottom reveal strip; only matters when overlapped
        pointerInBarZone =
            surfaces[SurfaceKind.Bar]?.let { bar ->
                bar.bounds().contains(px, py) || bar.revealHotspot()?.contains(px, py) == true
            } ?: false
        reconcile()
    }

    /** A global click. Anything outside every open surface dismisses the menu + control center. */
    fun onClick(
        x: Int,
        y: Int,
    ) {
        val px = x.toDouble()
        val py = y.toDouble()
        val insideAnOpenSurface =
            surfaces.values.any { s -> s.window.isVisible && s.bounds().contains(px, py) }
        if (!insideAnOpenSurface) {
            menuOpen = false
            controlCenterOpen = false
            reconcile()
        }
    }

    fun onEscape() = dismissTransients()

    /** Dismiss the menu + control center (e.g. a click on empty desktop, or Escape). */
    fun dismissTransients() {
        menuOpen = false
        controlCenterOpen = false
        reconcile()
    }

    fun toggleMenu() {
        menuOpen = !menuOpen
        if (menuOpen) controlCenterOpen = false
        reconcile()
    }

    fun openMenu() {
        menuOpen = true
        controlCenterOpen = false
        reconcile()
    }

    fun closeMenu() {
        menuOpen = false
        reconcile()
    }

    fun openControlCenter() {
        controlCenterOpen = true
        reconcile()
    }

    /** The compositor tells us whether a real window now overlaps the bar strip. */
    fun setBarOverlapped(overlapped: Boolean) {
        if (barOverlapped != overlapped) {
            barOverlapped = overlapped
            reconcile()
        }
    }

    // ---- decision + apply --------------------------------------------------

    private fun reconcile() {
        val decision =
            DesktopPolicy.decide(
                DesktopInputs(
                    menuOpen = menuOpen,
                    controlCenterOpen = controlCenterOpen,
                    barOverlapped = barOverlapped,
                    pointerInBarRevealZone = pointerInBarZone,
                ),
            )
        scope.launch {
            // control center first (it gates the others), then menu, then bar
            apply(SurfaceKind.ControlCenter, decision.controlCenter)
            apply(SurfaceKind.Menu, decision.menu)
            apply(SurfaceKind.Bar, decision.bar)
        }
    }

    private suspend fun apply(
        kind: SurfaceKind,
        visible: Boolean,
    ) {
        val surface = surfaces[kind] ?: return
        if (surface.window.isVisible != visible) {
            if (visible) {
                surface.window.show()
                // grab keyboard focus on open so Escape reaches it right away (control center /
                // menu). The bar never steals focus.
                if (surface.focusOnShow) surface.window.focus()
            } else {
                surface.window.hide(force = true)
            }
        }
        surface.onApply(visible)
    }
}
