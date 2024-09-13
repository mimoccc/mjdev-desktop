package eu.mjdev.desktop.windows

import java.awt.*
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener

object WindowLocationTracker {
    private val cascadeOffset = Point(48, 48)
    private var lastFocusedWindows = mutableSetOf<Window>()

    private val focusListener = object : WindowFocusListener {
        override fun windowGainedFocus(e: WindowEvent) {
            with(lastFocusedWindows) {
                remove(e.window)
                add(e.window)
            }
        }

        override fun windowLostFocus(e: WindowEvent) = Unit
    }

    val lastActiveGraphicsConfiguration: GraphicsConfiguration?
        get() = lastFocusedWindows.lastOrNull()?.graphicsConfiguration

    fun onWindowCreated(window: Window) {
        window.addWindowFocusListener(focusListener)
    }

    fun onWindowDisposed(window: Window) {
        window.removeWindowFocusListener(focusListener)
        lastFocusedWindows.remove(window)
    }

    fun getCascadeLocationFor(window: Window): Point {
        val lastWindow = lastFocusedWindows.lastOrNull()
        val graphicsConfiguration = lastWindow?.graphicsConfiguration
            ?: GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice?.defaultConfiguration
        return if (graphicsConfiguration != null) {
            val screenBounds = graphicsConfiguration.bounds
            val screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration)
            val screenLeftTop = screenBounds.leftTop + Point(screenInsets.left, screenInsets.top)
            val screenRightBottom = screenBounds.rightBottom - Point(screenInsets.right, screenInsets.bottom)
            val lastLocation = lastWindow?.location ?: screenLeftTop
            var location = lastLocation + cascadeOffset
            val rightBottom = location + window.size.rightBottom
            if (rightBottom.x > screenRightBottom.x || rightBottom.y > screenRightBottom.y) {
                location = screenLeftTop + cascadeOffset
            }
            location
        } else {
            cascadeOffset
        }
    }

    val Dimension.rightBottom get() = Point(width, height)

    val Rectangle.leftTop get() = Point(x, y)
    val Rectangle.rightBottom get() = Point(x + width, y + height)

    operator fun Point.plus(other: Point) = Point(x + other.x, y + other.y)
    operator fun Point.minus(other: Point) = Point(x - other.x, y - other.y)
}