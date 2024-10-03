@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package eu.mjdev.desktop.extensions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.relocation.BringIntoViewResponder
import androidx.compose.foundation.relocation.bringIntoViewResponder
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.*
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.svg.SvgDecoder
import eu.mjdev.desktop.helpers.compose.FocusHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Compose {

    val Color.Companion.SuperDarkGray
        get() = Color(0xff202020)

    val Color.Companion.DarkDarkGray
        get() = Color(0xff404040)

    val Color.Companion.MediumDarkGray
        get() = Color(0xff808080)

    val Color.Companion.LiteDarkGray
        get() = Color(0xffc0c0c0)

    val Color.Companion.DarkBlue
        get() = Color(0xff202060)

    fun Color.rgbToHex(): String {
        val red = (red * 255).toInt()
        val green = (green * 255).toInt()
        val blue = (blue * 255).toInt()
        return String.format("#%02x%02x%02x", red, green, blue)
    }

    fun Color.rgbaToHex(): String {
        val alpha: Int = (alpha * 255).toInt()
        val red = (red * 255).toInt()
        val green = (green * 255).toInt()
        val blue = (blue * 255).toInt()
        return String.format("#%02x%02x%02x%02x", alpha, red, green, blue)
    }

    val PaddingValues.width: Dp
        get() = calculateLeftPadding(LayoutDirection.Ltr) + calculateRightPadding(LayoutDirection.Ltr)

    val Dp.sp: TextUnit
        get() = value.sp

    val PaddingValues.height: Dp
        get() = calculateTopPadding() + calculateBottomPadding()

    val PaddingValues.size: DpSize
        get() = DpSize(width, height)

    @Composable
    fun launchedEffect(
        block: suspend CoroutineScope.() -> Unit
    ) = LaunchedEffect(Unit, block)

    @Composable
    fun <T> launchedEffect(
        key: T,
        block: suspend CoroutineScope.(key: T) -> Unit
    ) = LaunchedEffect(key) { block(key) }

    @Composable
    fun <T, E> launchedEffect(
        key1: T,
        key2: E,
        block: suspend CoroutineScope.(key1: T, key2: E) -> Unit
    ) = LaunchedEffect(key1, key2) { block(key1, key2) }

    @Composable
    fun <T> textFrom(text: T?): String = when (text) {
        is MutableState<*> -> textFrom(text.value)
        else -> text.toString()
    }.toString()

    @Composable
    fun ButtonDefaults.noElevation() =
        elevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)

    @Composable
    fun ButtonDefaults.color(color: Color) = buttonColors(
        backgroundColor = color,
        contentColor = color,
        disabledContentColor = color,
        disabledBackgroundColor = color
    )

    @Composable
    fun ButtonDefaults.transparent() = buttonColors(
        backgroundColor = Color.Transparent,
        contentColor = Color.Transparent,
        disabledContentColor = Color.Transparent,
        disabledBackgroundColor = Color.Transparent
    )

    @Composable
    fun imageLoaderContext() = LocalPlatformContext.current

    @Composable
    fun imageLoaderMemoryCache() = MemoryCache.Builder()
        .maxSizePercent(imageLoaderContext(), 0.3)
        .strongReferencesEnabled(true)
        .build()

    @Composable
    fun asyncImageLoader(
        imageLoaderContext: PlatformContext = imageLoaderContext(),
        imageLoaderMemoryCache: MemoryCache = imageLoaderMemoryCache()
    ) = ImageLoader.Builder(imageLoaderContext)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .components {
            add(SvgDecoder.Factory())
//            add(GifDecoder.Factory())
        }
        .memoryCache { imageLoaderMemoryCache }
        .build()

    @Composable
    fun <T> rememberState(
        value: T,
        policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy()
    ) = remember { mutableStateOf(value, policy) }

    @Composable
    fun <T> rememberCalculated(
        vararg key: Any?,
        calculation: () -> T
    ) = remember(*key) { derivedStateOf { calculation() } }

    @OptIn(ExperimentalComposeUiApi::class)
    fun Modifier.onMouseEnter(
        pass: PointerEventPass = PointerEventPass.Main,
        onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
    ): Modifier = onPointerEvent(
        eventType = PointerEventType.Enter,
        pass = pass,
        onEvent = onEvent
    )

    @OptIn(ExperimentalComposeUiApi::class)
    fun Modifier.onMouseLeave(
        pass: PointerEventPass = PointerEventPass.Main,
        onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
    ): Modifier = onPointerEvent(
        eventType = PointerEventType.Exit,
        pass = pass,
        onEvent = onEvent
    )

    @OptIn(ExperimentalComposeUiApi::class)
    fun Modifier.onMousePress(
        pass: PointerEventPass = PointerEventPass.Main,
        onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
    ): Modifier = onPointerEvent(
        eventType = PointerEventType.Press,
        pass = pass,
        onEvent = onEvent
    )

    @Suppress("UNUSED_PARAMETER")
    fun Modifier.onMouseLongPress(
        pass: PointerEventPass = PointerEventPass.Main,
        onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
    ): Modifier {
//        return onPointerEvent(
//            eventType = PointerEventType.Press,
//            pass = pass,
//            onEvent = onEvent
//        )
        return this
    }

    @OptIn(ExperimentalComposeUiApi::class)
    val AwaitPointerEventScope.isPrimary
        get() = currentEvent.button.isPrimary

    @OptIn(ExperimentalComposeUiApi::class)
    val AwaitPointerEventScope.isSecondary
        get() = currentEvent.button.isSecondary

    fun AwaitPointerEventScope.onLeftClick(
        block: AwaitPointerEventScope.() -> Unit
    ) {
        if (isPrimary) block()
    }

    fun AwaitPointerEventScope.onRightClick(
        block: AwaitPointerEventScope.() -> Unit
    ) {
        if (isSecondary) block()
    }

    fun Modifier.onKey(
        keyCode: Key,
        action: KeyEventType = KeyEventType.KeyDown,
        block: () -> Unit
    ): Modifier = this then onKeyEvent { ev ->
        if (ev.type == action) {
            if (ev.key == keyCode) {
                block()
                true
            } else false
        } else false
    }

    @Composable
    fun Modifier.requestFocusOnTouch(
        focusRequester: FocusRequester,
        requestFocus: Boolean = true,
        scope: CoroutineScope = rememberCoroutineScope(),
        onTouch: (() -> Unit)? = null
    ): Modifier = this then focusRequester(
        focusRequester
    ).pointerInput(this) {
        detectTapGestures(
            onTap = {
                try {
                    scope.launch {
                        onTouch?.invoke()
                        if (requestFocus) {
                            focusRequester.requestFocus()
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    fun Modifier.bringIntoViewIfChildrenAreFocused(): Modifier = composed(
        inspectorInfo = debugInspectorInfo { name = "bringIntoViewIfChildrenAreFocused" },
        factory = {
            var myRect: Rect = Rect.Zero
            this.onSizeChanged {
                myRect = Rect(Offset.Zero, Offset(it.width.toFloat(), it.height.toFloat()))
            }.bringIntoViewResponder(
                remember {
                    object : BringIntoViewResponder {
                        @ExperimentalFoundationApi
                        override fun calculateRectForParent(localRect: Rect): Rect = myRect

                        @ExperimentalFoundationApi
                        override suspend fun bringChildIntoView(localRect: () -> Rect?) {
                        }
                    }
                }
            )
        }
    )

    @Composable
    fun rememberFocusRequester(
        key: Any? = Unit
    ) = remember(key) { FocusRequester() }

    @Composable
    fun <T> produceStateInCoroutine(
        initialValue: T,
        key: Any? = null,
        block: suspend () -> T
    ) = produceState(
        initialValue = initialValue,
        key1 = key
    ) {
        withContext(Dispatchers.IO) {
            value = block()
        }
    }

    @Composable
    fun <T> rememberDerivedState(
        key: Any? = Unit,
        initialValue: T,
        block: suspend () -> T
    ) = produceStateInCoroutine(initialValue, key, block)

    @Composable
    fun <T> rememberDerivedStateOf(
        function: () -> T,
        key: Any? = function
    ): State<T> = remember(key) { derivedStateOf(function) }

    @Composable
    fun rememberFocusState(
        initial: FocusState = FocusHelper(false)
    ) = remember(initial) {
        mutableStateOf(initial)
    }

    @Composable
    fun rememberFocusState(
        key: Any?,
        initial: FocusState = FocusHelper(false),
    ) = remember(key) {
        mutableStateOf(initial)
    }

    fun Modifier.focusState(
        focusState: MutableState<FocusState>
    ): Modifier = onFocusChanged { state ->
        focusState.value = state
    }

    val MutableState<FocusState>.isFocused
        get() = value.isFocused || value.hasFocus

    val MutableState<FocusState>.isNotFocused
        get() = !isFocused

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    val FocusState.isFocused
        get() = this.isFocused || this.hasFocus

    val FocusState.isNotFocused
        get() = !isFocused

//    Toolkit.getDefaultToolkit().addAWTEventListener({ event ->
//        }, AWTEvent.MOUSE_EVENT_MASK or AWTEvent.FOCUS_EVENT_MASK
//    )

//    Toolkit.getDefaultToolkit().addAWTEventListener(object:AWTEventListener {
//        override fun eventDispatched(event: AWTEvent?) {
//            println(event)
//        }
//    }, AWTEvent.WINDOW_EVENT_MASK)

}
