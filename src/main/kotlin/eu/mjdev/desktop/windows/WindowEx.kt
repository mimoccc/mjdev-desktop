package eu.mjdev.desktop.windows

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import eu.mjdev.desktop.windows.ListenerOnWindowRef.Companion.componentListenerRef
import eu.mjdev.desktop.windows.ListenerOnWindowRef.Companion.windowListenerRef
import eu.mjdev.desktop.windows.ListenerOnWindowRef.Companion.windowStateListenerRef
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import javax.swing.JFrame
import kotlin.math.roundToInt

@Composable
fun WindowEx(
    onCloseRequest: () -> Unit,
    state: WindowState = rememberWindowState(),
    visible: Boolean = true,
    title: String = "Untitled",
    icon: Painter? = null,
    undecorated: Boolean = false,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    onUpdate: () -> Unit = {},
    content: @Composable FrameWindowScope.() -> Unit = {}
) {
    val currentState by rememberUpdatedState(state)
    val currentTitle by rememberUpdatedState(title)
    val currentIcon by rememberUpdatedState(icon)
    val currentUndecorated by rememberUpdatedState(undecorated)
    val currentTransparent by rememberUpdatedState(transparent)
    val currentResizable by rememberUpdatedState(resizable)
    val currentEnabled by rememberUpdatedState(enabled)
    val currentFocusable by rememberUpdatedState(focusable)
    val currentAlwaysOnTop by rememberUpdatedState(alwaysOnTop)
    val currentOnCloseRequest by rememberUpdatedState(onCloseRequest)

    val updater = remember(::ComponentUpdater)

    val appliedState = remember {
        object {
            var size: DpSize? = null
            var position: WindowPosition? = null
            var placement: WindowPlacement? = null
            var isMinimized: Boolean? = null
        }
    }

    val listeners = remember {
        object {
            var windowListenerRef = windowListenerRef()
            var windowStateListenerRef = windowStateListenerRef()
            var componentListenerRef = componentListenerRef()

            fun removeFromAndClear(window: ComposeWindow) {
                windowListenerRef.unregisterFromAndClear(window)
                windowStateListenerRef.unregisterFromAndClear(window)
                componentListenerRef.unregisterFromAndClear(window)
            }
        }
    }

    Window(
        visible = visible,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
        create = {
            val graphicsConfiguration = WindowLocationTracker.lastActiveGraphicsConfiguration
            ComposeWindow(graphicsConfiguration = graphicsConfiguration).apply {
                defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
                listeners.windowListenerRef.registerWithAndSet(
                    this,
                    object : WindowAdapter() {
                        override fun windowClosing(e: WindowEvent) {
                            currentOnCloseRequest()
                        }
                    }
                )
                listeners.windowStateListenerRef.registerWithAndSet(this) {
                    currentState.placement = placement
                    currentState.isMinimized = isMinimized
                    appliedState.placement = currentState.placement
                    appliedState.isMinimized = currentState.isMinimized
                }
                listeners.componentListenerRef.registerWithAndSet(
                    this,
                    object : ComponentAdapter() {
                        override fun componentResized(e: ComponentEvent) {
                            currentState.placement = placement
                            currentState.size = DpSize(width.dp, height.dp)
                            appliedState.placement = currentState.placement
                            appliedState.size = currentState.size
                        }

                        override fun componentMoved(e: ComponentEvent) {
                            currentState.position = WindowPosition(x.dp, y.dp)
                            appliedState.position = currentState.position
                        }
                    }
                )
                WindowLocationTracker.onWindowCreated(this)
            }
        },
        dispose = {
            WindowLocationTracker.onWindowDisposed(it)
            listeners.removeFromAndClear(it)
            it.dispose()
        },
        update = { window ->
            updater.update {
                set(currentTitle, window::setTitle)
                set(currentIcon, window::setIcon)
                set(currentUndecorated, window::setUndecoratedSafely)
                set(currentTransparent, window::isTransparent::set)
                set(currentResizable, window::setResizable)
                set(currentEnabled, window::setEnabled)
                set(currentFocusable, window::setFocusableWindowState)
                set(currentAlwaysOnTop, window::setAlwaysOnTop)
            }
            if (state.size != appliedState.size) {
                window.setSizeSafely(state.size, state.placement)
                appliedState.size = state.size
            }
            if (state.position != appliedState.position) {
                window.setPositionSafely(
                    state.position,
                    state.placement,
                    platformDefaultPosition = { WindowLocationTracker.getCascadeLocationFor(window) }
                )
                appliedState.position = state.position
            }
            if (state.placement != appliedState.placement) {
                window.placement = state.placement
                appliedState.placement = state.placement
            }
            if (state.isMinimized != appliedState.isMinimized) {
                window.isMinimized = state.isMinimized
                appliedState.isMinimized = state.isMinimized
            }
            onUpdate()
        },
        content = content
    )
}

fun Window.setPositionSafely(
    position: WindowPosition,
    placement: WindowPlacement,
    platformDefaultPosition: () -> Point = { WindowLocationTracker.getCascadeLocationFor(this) }
) {
    if (!isVisible || (placement == WindowPlacement.Floating)) {
        setPositionImpl(position, platformDefaultPosition)
    }
}

fun Window.align(alignment: Alignment) {
    val screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration)
    val screenBounds = graphicsConfiguration.bounds
    val size = IntSize(size.width, size.height)
    val screenSize = IntSize(
        screenBounds.width - screenInsets.left - screenInsets.right,
        screenBounds.height - screenInsets.top - screenInsets.bottom
    )
    val location = alignment.align(size, screenSize, LayoutDirection.Ltr)

    setLocation(
        screenBounds.x + screenInsets.left + location.x,
        screenBounds.y + screenInsets.top + location.y
    )
}

fun Window.setPositionImpl(
    position: WindowPosition,
    platformDefaultPosition: () -> Point
) = when (position) {
    WindowPosition.PlatformDefault -> location = platformDefaultPosition()
    is WindowPosition.Aligned -> align(position.alignment)
    is WindowPosition.Absolute -> setLocation(
        position.x.value.roundToInt(),
        position.y.value.roundToInt()
    )
}

fun Frame.setUndecoratedSafely(value: Boolean) {
    if (this.isUndecorated != value) {
        this.isUndecorated = value
    }
}

val ComponentOrientation.layoutDirection: LayoutDirection
    get() = when {
        isLeftToRight -> LayoutDirection.Ltr
        isHorizontal -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }

val Locale.layoutDirection: LayoutDirection
    get() = ComponentOrientation.getOrientation(this).layoutDirection

fun layoutDirectionFor(component: Component): LayoutDirection {
    val orientation = component.componentOrientation
    return if (orientation != ComponentOrientation.UNKNOWN) {
        orientation.layoutDirection
    } else {
        return component.locale.layoutDirection
    }
}

fun Window.setIcon(
    painter: Painter?,
    width: Float = 192f,
    height: Float = 192f
) {
    setIconImage(
        painter?.toAwtImage(
            graphicsConfiguration.let {
                Density(
                    it.defaultTransform.scaleX.toFloat(),
                    fontScale = 1f
                )
            },
            layoutDirectionFor(this), Size(width, height)
        )
    )
}

fun Window.setSizeImpl(size: DpSize) {
    val availableSize by lazy {
        val screenBounds = graphicsConfiguration.bounds
        val screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration)

        IntSize(
            width = screenBounds.width - screenInsets.left - screenInsets.right,
            height = screenBounds.height - screenInsets.top - screenInsets.bottom
        )
    }
    val isWidthSpecified = size.isSpecified && size.width.isSpecified
    val isHeightSpecified = size.isSpecified && size.height.isSpecified
    val width = if (isWidthSpecified) {
        size.width.value.roundToInt().coerceAtLeast(0)
    } else {
        availableSize.width
    }
    val height = if (isHeightSpecified) {
        size.height.value.roundToInt().coerceAtLeast(0)
    } else {
        availableSize.height
    }
    var computedPreferredSize: Dimension? = null
    if (!isWidthSpecified || !isHeightSpecified) {
        preferredSize = Dimension(width, height)
        pack()  // Makes it displayable

        // We set preferred size to null, and then call getPreferredSize, which will compute the
        // actual preferred size determined by the content (see the description of setPreferredSize)
        preferredSize = null
        computedPreferredSize = preferredSize
    }
    if (!isDisplayable) {
        // Pack to allow drawing the first frame
        preferredSize = Dimension(width, height)
        pack()
    }
    setSize(
        if (isWidthSpecified) width else computedPreferredSize!!.width,
        if (isHeightSpecified) height else computedPreferredSize!!.height,
    )
    revalidate()  // Calls doLayout on the ComposeLayer, causing it to update its size
}

fun Window.setSizeSafely(size: DpSize, placement: WindowPlacement) {
    if (!isVisible || (placement == WindowPlacement.Floating)) {
        setSizeImpl(size)
    }
}



