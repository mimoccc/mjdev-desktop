package org.psippr.desktop.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.psippr.desktop.windows.ChromeWindowState

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
        val raiseOnShow: Boolean,
        val onApply: (visible: Boolean) -> Unit,
        // the strip app windows must not cover (the bar when shown), null for surfaces without one
        val overlapStrip: () -> ScreenRect? = { null },
    )

    private val surfaces = linkedMapOf<SurfaceKind, Surface>()

    // user intent (what the user asked to be open)
    private var menuOpen = false
    private var controlCenterOpen = false

    // facts about the world
    private var barOverlapped = false
    private var notificationShown = false
    private var deviceShown = false
    private var pointerInBarZone = false
    private var pointerWasInBarZone = false

    // the pointer was in the bar zone outside the open control center (edge of BarZoneEntry)
    private var pointerWasInBarZoneBesideCc = false
    private var pointerWasInCcHotspot = false

    fun register(
        kind: SurfaceKind,
        window: ChromeWindowState,
        bounds: () -> ScreenRect,
        revealHotspot: () -> ScreenRect? = { null },
        focusOnShow: Boolean = false,
        raiseOnShow: Boolean = false,
        onApply: (visible: Boolean) -> Unit = {},
        overlapStrip: () -> ScreenRect? = { null },
    ) {
        // old: surfaces had no overlap strip, nothing ever reported the bar as overlapped
//        surfaces[kind] = Surface(window, bounds, revealHotspot, focusOnShow, raiseOnShow, onApply)
        surfaces[kind] = Surface(window, bounds, revealHotspot, focusOnShow, raiseOnShow, onApply, overlapStrip)
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
        // control center opens on a fresh ENTER into its reveal edge (edge-triggered). Level-
        // triggering re-opened it right after a dismiss while the pointer sat in the edge strip;
        // now the pointer must leave and re-enter to reveal again.
        val inCcHotspot =
            surfaces[SurfaceKind.ControlCenter]?.revealHotspot?.invoke()?.contains(px, py) == true
        // opening the control center also closes the menu for real (not just covers it), so it
        // won't reappear when the control center later closes.
        if (inCcHotspot && !pointerWasInCcHotspot) {
            controlCenterOpen = true
            menuOpen = false
        }
        pointerWasInCcHotspot = inCcHotspot
        // the bar's zone = the bar itself plus its bottom reveal strip; only matters when overlapped
        val inBarZone =
            surfaces[SurfaceKind.Bar]?.let { bar ->
                bar.bounds().contains(px, py) || bar.revealHotspot()?.contains(px, py) == true
            } ?: false
        // moving down to the bar while the control center is open hides the control center and
        // brings the bar back (edge-triggered so it only fires on entering the zone).
        // old: also fired at the bottom of the open control center (the bar zone covers it)
//        if (inBarZone && !pointerWasInBarZone && controlCenterOpen) controlCenterOpen = false
        val overControlCenter = surfaces[SurfaceKind.ControlCenter]?.bounds?.invoke()?.contains(px, py) == true
        val inBarZoneBesideCc = inBarZone && !overControlCenter
        val entered = inBarZoneBesideCc && !pointerWasInBarZoneBesideCc
        if (BarZoneEntry.closesControlCenter(entered, controlCenterOpen, overControlCenter)) controlCenterOpen = false
        pointerWasInBarZoneBesideCc = inBarZoneBesideCc
        pointerInBarZone = inBarZone
        pointerWasInBarZone = inBarZone
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

    // old: Escape only closed the surfaces registered here
//    fun onEscape() = dismissTransients()
    fun onEscape() {
        escapeListeners.toList().forEach { listener -> listener() }
        dismissTransients()
    }

    /** Listeners told about Escape, e.g. the desktop menu, which is no registered surface. */
    private val escapeListeners = mutableListOf<() -> Unit>()

    /** Tells [listener] about every Escape until [removeEscapeListener]. */
    fun addEscapeListener(listener: () -> Unit) {
        escapeListeners += listener
    }

    /** Stops telling [listener] about Escape. */
    fun removeEscapeListener(listener: () -> Unit) {
        escapeListeners -= listener
    }

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
        menuOpen = false // opening the control center closes the menu (symmetric with openMenu)
        reconcile()
    }

    /**
     * The app windows on screen changed (compositor feed): the bar counts as overlapped when one
     * of them covers its shown strip.
     */
    fun onAppWindows(windows: List<ScreenRect>) {
        val strip = surfaces[SurfaceKind.Bar]?.overlapStrip?.invoke() ?: return
        setBarOverlapped(BarOverlap.overlaps(windows, strip))
    }

    /**
     * A notification popup appeared ([shown] true) or went away. When it appears the control
     * center and the menu close and the bar steps aside; hovering their edges brings them back.
     */
    fun setNotificationShown(shown: Boolean) {
        if (notificationShown == shown) return
        notificationShown = shown
        if (shown) {
            controlCenterOpen = false
            menuOpen = false
        }
        reconcile()
    }

    /**
     * The screen of a connected device appeared on the desktop ([shown] true) or went away; while
     * shown the bar steps aside, hovering its edge brings it back.
     */
    fun setDeviceShown(shown: Boolean) {
        if (deviceShown == shown) return
        deviceShown = shown
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
                    notificationShown = notificationShown,
                    deviceShown = deviceShown,
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
                // menu). The bar never steals focus, only raises (see raiseOnShow) so an
                // always-on-top pin isn't needed to stay above whatever app was on top before.
                if (surface.focusOnShow) surface.window.focus()
                if (surface.raiseOnShow) surface.window.raise()
            } else {
                surface.window.hide(force = true)
            }
        }
        surface.onApply(visible)
    }
}
