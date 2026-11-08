package org.psippr.desktop.helpers.mouseevents

import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.geom.AffineTransform
import kotlin.math.floor

/**
 * Converts pointer positions of the psippr compositor (output pixels) into AWT screen
 * coordinates (pixels divided by the AWT UI scale), the unit of every window bound, click and
 * reveal strip of the shell. Without it a 1.12 UI scale stretched the 12 dp control center edge
 * strip to 149 px, so it opened over the buttons and the resize edge of app windows.
 */
object CompositorPointerScale {
    /** Scale used when AWT reports none or an invalid one: pixels are taken as they are. */
    private const val NO_SCALE = 1.0

    /**
     * [x], [y] in compositor pixels to AWT coordinates for the AWT scale [scaleX], [scaleY],
     * floored: a pixel belongs to the unit it starts in.
     */
    fun toAwt(
        x: Int,
        y: Int,
        scaleX: Double,
        scaleY: Double,
    ): Point = Point(floor(x / valid(scaleX)).toInt(), floor(y / valid(scaleY)).toInt())

    /** [x], [y] in compositor pixels to AWT coordinates of the default screen. */
    fun toAwt(
        x: Int,
        y: Int,
    ): Point = toAwt(x, y, scaleX(), scaleY())

    /** Horizontal AWT UI scale of the default screen, 1 when unknown. */
    fun scaleX(): Double = screenTransform()?.scaleX ?: NO_SCALE

    /** Vertical AWT UI scale of the default screen, 1 when unknown. */
    fun scaleY(): Double = screenTransform()?.scaleY ?: NO_SCALE

    private fun screenTransform(): AffineTransform? = runCatching {
        GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .defaultScreenDevice
            .defaultConfiguration
            .defaultTransform
    }.getOrNull()

    // a zero, negative or NaN scale would break the division: keep the pixels instead
    private fun valid(scale: Double): Double = if (scale > 0.0) scale else NO_SCALE
}
