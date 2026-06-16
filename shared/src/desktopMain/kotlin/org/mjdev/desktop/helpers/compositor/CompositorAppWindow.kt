package org.mjdev.desktop.helpers.compositor

data class CompositorAppWindow(
    val id: Long,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val minimized: Boolean,
) {
    val isVisibleApp: Boolean
        get() = !minimized && width > 0 && height > 0
}

internal fun rectsOverlap(
    ax: Int,
    ay: Int,
    aw: Int,
    ah: Int,
    bx: Int,
    by: Int,
    bw: Int,
    bh: Int,
): Boolean {
    if (aw <= 0 || ah <= 0 || bw <= 0 || bh <= 0) return false
    val aRight = ax + aw
    val aBottom = ay + ah
    val bRight = bx + bw
    val bBottom = by + bh
    return ax < bRight && aRight > bx && ay < bBottom && aBottom > by
}

internal fun anyAppOccludesDockZone(
    windows: List<CompositorAppWindow>,
    screenWidth: Int,
    screenHeight: Int,
    dockZoneTopY: Int,
): Boolean {
    if (screenWidth <= 0 || screenHeight <= 0 || dockZoneTopY >= screenHeight) return false
    val visible = windows.filter { it.isVisibleApp }
    if (visible.isEmpty()) return false
    val dockH = screenHeight - dockZoneTopY
    return visible.any { w ->
        rectsOverlap(w.x, w.y, w.width, w.height, 0, dockZoneTopY, screenWidth, dockH)
    }
}
