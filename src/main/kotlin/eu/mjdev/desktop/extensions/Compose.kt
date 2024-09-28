@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package eu.mjdev.desktop.extensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.*
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.svg.SvgDecoder
import kotlinx.coroutines.CoroutineScope

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
        val alpha : Int= (alpha * 255).toInt()
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
        null -> ""
        is Unit -> ""
        is Int -> text.toString()
        is String -> text
        is MutableState<*> -> textFrom(text.value)
        else -> text.toString()
    }

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

    fun AwaitPointerEventScope.onRigntClick(
        block: AwaitPointerEventScope.() -> Unit
    ) {
        if (isSecondary) block()
    }

//    Toolkit.getDefaultToolkit().addAWTEventListener({ event ->
//        }, AWTEvent.MOUSE_EVENT_MASK or AWTEvent.FOCUS_EVENT_MASK
//    )

//    Toolkit.getDefaultToolkit().addAWTEventListener(object:AWTEventListener {
//        override fun eventDispatched(event: AWTEvent?) {
//            println(event)
//        }
//    }, AWTEvent.WINDOW_EVENT_MASK)

}
