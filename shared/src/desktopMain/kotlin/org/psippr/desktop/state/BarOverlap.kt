package org.psippr.desktop.state

/**
 * Decides whether app windows cover the bottom bar. The bar's *shown* strip is used, never its
 * live bounds: the hidden bar shrinks to a thin divider, so measuring against it would show the
 * bar again at once and make it flicker.
 */
object BarOverlap {
    /** True when any of [windows] shares an area (not just an edge) with the bar [strip]. */
    fun overlaps(
        windows: List<DesktopState.ScreenRect>,
        strip: DesktopState.ScreenRect,
    ): Boolean = windows.any { window -> intersects(window, strip) }

    // touching edges do not count: a window ending exactly where the bar starts leaves it free
    private fun intersects(
        a: DesktopState.ScreenRect,
        b: DesktopState.ScreenRect,
    ): Boolean = a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom
}
