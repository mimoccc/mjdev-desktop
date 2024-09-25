package eu.mjdev.desktop.windows

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState
import java.awt.*
import java.awt.event.*
import java.util.*
import javax.swing.JFrame
import kotlin.math.roundToInt

@Suppress("FunctionName")
@Preview
@Composable
fun Window(
//    onOpen: (window: ComposeWindow) -> Unit = {},
    onCreate: (window: ComposeWindow) -> Unit = {},
    onOpen: (window: ComposeWindow) -> Unit = {},
    onClosing: (window: ComposeWindow) -> Unit = {},
    onClosed: (window: ComposeWindow) -> Unit = {},
    onActivated: (window: ComposeWindow) -> Unit = {},
    onDeactivated: (window: ComposeWindow) -> Unit = {},
    onIconified: (window: ComposeWindow) -> Unit = {},
    onDeIconified: (window: ComposeWindow) -> Unit = {},
    onGainFocus: (window: ComposeWindow) -> Unit = {},
    onLostFocus: (window: ComposeWindow) -> Unit = {},
    onStateChanged: (window: ComposeWindow) -> Unit = {},
    onCloseRequest: () -> Unit,
    state: ChromeWindowState =  rememberChromeWindowState(),
    visible: Boolean = true,
    title: String = "",
    icon: Painter? = null,
    undecorated: Boolean = false,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
//    alwaysOnBottom: Boolean = false,
    closeAction: Int = JFrame.DO_NOTHING_ON_CLOSE,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    content: @Composable FrameWindowScope.() -> Unit
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
//    val currentAlwaysOnBottom by rememberUpdatedState(alwaysOnBottom)
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
                defaultCloseOperation = closeAction
                listeners.windowListenerRef.registerWithAndSet(
                    this,
                    WindowEventsAdapter(
                        this,
                        currentOnCloseRequest,
                        onOpen,
                        onClosing,
                        onClosed,
                        onActivated,
                        onDeactivated,
                        onIconified,
                        onDeIconified,
                        onGainFocus,
                        onLostFocus,
                        onStateChanged,
                    )
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
                onCreate(this)
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
//                if (alwaysOnBottom) {
//                    set(false, window::setFocusableWindowState)
//                    set(false, window::setAlwaysOnTop)
//                    set(false, window::setAutoRequestFocus)
//                } else {
                set(currentFocusable, window::setFocusableWindowState)
                set(currentAlwaysOnTop, window::setAlwaysOnTop)
//                }
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
        },
        content = content
    )
}

internal fun Window.align(alignment: Alignment) {
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

internal fun Window.setPositionImpl(
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

internal fun Window.setPositionSafely(
    position: WindowPosition,
    placement: WindowPlacement,
    platformDefaultPosition: () -> Point
) {
    if (!isVisible || (placement == WindowPlacement.Floating)) {
        setPositionImpl(position, platformDefaultPosition)
    }
}

private fun Window.setSizeImpl(size: DpSize) {
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
        preferredSize = null
        computedPreferredSize = preferredSize
    }
    if (!isDisplayable) {
        preferredSize = Dimension(width, height)
        pack()
    }
    setSize(
        if (isWidthSpecified) width else computedPreferredSize!!.width,
        if (isHeightSpecified) height else computedPreferredSize!!.height,
    )
    revalidate()
}

internal fun Window.setSizeSafely(size: DpSize, placement: WindowPlacement) {
    if (!isVisible || (placement == WindowPlacement.Floating)) {
        setSizeImpl(size)
    }
}

internal class ListenerOnWindowRef<T>(
    private val register: Window.(T) -> Unit,
    private val unregister: Window.(T) -> Unit
) {
    private var value: T? = null

    fun registerWithAndSet(window: Window, listener: T) {
        window.register(listener)
        value = listener
    }

    fun unregisterFromAndClear(window: Window) {
        value?.let {
            window.unregister(it)
            value = null
        }
    }
}

internal fun windowStateListenerRef() = ListenerOnWindowRef<WindowStateListener>(
    register = Window::addWindowStateListener,
    unregister = Window::removeWindowStateListener
)

internal fun windowListenerRef() = ListenerOnWindowRef<WindowListener>(
    register = Window::addWindowListener,
    unregister = Window::removeWindowListener
)

internal fun componentListenerRef() = ListenerOnWindowRef<ComponentListener>(
    register = Component::addComponentListener,
    unregister = Component::removeComponentListener
)

internal val GraphicsConfiguration.density: Density
    get() = Density(
        defaultTransform.scaleX.toFloat(),
        fontScale = 1f
    )

internal val Locale.layoutDirection: LayoutDirection
    get() = ComponentOrientation.getOrientation(this).layoutDirection

internal val ComponentOrientation.layoutDirection: LayoutDirection
    get() = when {
        isLeftToRight -> LayoutDirection.Ltr
        isHorizontal -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }

internal fun layoutDirectionFor(component: Component): LayoutDirection {
    val orientation = component.componentOrientation
    return if (orientation != ComponentOrientation.UNKNOWN) {
        orientation.layoutDirection
    } else {
        return component.locale.layoutDirection
    }
}

val Component.density: Density get() = graphicsConfiguration.density

fun Window.setIcon(painter: Painter?) {
    setIconImage(
        painter?.toAwtImage(
            density,
            layoutDirectionFor(this),
            Size(192f, 192f)
        )
    )
}

fun Frame.setUndecoratedSafely(value: Boolean) {
    if (this.isUndecorated != value) {
        this.isUndecorated = value
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("unused", "FunctionName")
@Composable
fun Window(
    visible: Boolean = true,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    create: () -> ComposeWindow,
    dispose: (ComposeWindow) -> Unit,
    update: (ComposeWindow) -> Unit = {},
    content: @Composable FrameWindowScope.() -> Unit
) {
    val compositionLocalContext by rememberUpdatedState(currentCompositionLocalContext)
    val windowExceptionHandlerFactory by rememberUpdatedState(
        LocalWindowExceptionHandlerFactory.current
    )
    val layoutDirection = LocalLayoutDirection.current
    AwtWindow(
        visible = visible,
        create = {
            create().apply {
                this.compositionLocalContext = compositionLocalContext
                this.exceptionHandler = windowExceptionHandlerFactory.exceptionHandler(this)
                setContent(onPreviewKeyEvent, onKeyEvent, content)
            }
        },
        dispose = {
            dispose(it)
        },
        update = { component ->
            component.compositionLocalContext = compositionLocalContext
            component.exceptionHandler = windowExceptionHandlerFactory.exceptionHandler(component)
            component.componentOrientation = layoutDirection.componentOrientation
            val wasDisplayable = component.isDisplayable
            update(component)
            if (!wasDisplayable && component.isDisplayable) {
                component.contentPane.paint(component.contentPane.graphics)
            }
        },
    )
}

val LayoutDirection.componentOrientation: ComponentOrientation
    get() = when (this) {
        LayoutDirection.Ltr -> ComponentOrientation.LEFT_TO_RIGHT
        LayoutDirection.Rtl -> ComponentOrientation.RIGHT_TO_LEFT
    }

class WindowEventsAdapter(
    val window: ComposeWindow,
    val currentOnCloseRequest: () -> Unit,
    val onOpen: (window: ComposeWindow) -> Unit = {},
    val onClosing: (window: ComposeWindow) -> Unit = {},
    val onClosed: (window: ComposeWindow) -> Unit = {},
    val onActivated: (window: ComposeWindow) -> Unit = {},
    val onDeactivated: (window: ComposeWindow) -> Unit = {},
    val onIconified: (window: ComposeWindow) -> Unit = {},
    val onDeIconified: (window: ComposeWindow) -> Unit = {},
    val onGainFocus: (window: ComposeWindow) -> Unit = {},
    val onLostFocus: (window: ComposeWindow) -> Unit = {},
    val onStateChanged: (window: ComposeWindow) -> Unit = {},
) : WindowAdapter() {
    override fun windowOpened(e: WindowEvent?) {
        onOpen(window)
    }

    override fun windowActivated(e: WindowEvent?) {
        onActivated(window)
    }

    override fun windowClosed(e: WindowEvent?) {
        onClosed(window)
    }

    override fun windowDeactivated(e: WindowEvent?) {
        onDeactivated(window)
    }

    override fun windowDeiconified(e: WindowEvent?) {
        onDeIconified(window)
    }

    override fun windowIconified(e: WindowEvent?) {
        onIconified(window)
    }

    override fun windowGainedFocus(e: WindowEvent?) {
        onGainFocus(window)
    }

    override fun windowLostFocus(e: WindowEvent?) {
        onLostFocus(window)
    }

    override fun windowStateChanged(e: WindowEvent?) {
        onStateChanged(window)
    }

    override fun windowClosing(e: WindowEvent) {
        onClosing(window)
        currentOnCloseRequest()
    }
}
