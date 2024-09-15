@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package eu.mjdev.desktop.extensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.svg.SvgDecoder
import kotlinx.coroutines.CoroutineScope

object Compose {

    val Color.Companion.SuperDarkGray: Color
        get() = Color(0xff202020)

    val Color.Companion.DarkDarkGray: Color
        get() = Color(0xff404040)

    val Color.Companion.MediumDarkGray: Color
        get() = Color(0xff808080)

    val Color.Companion.LiteDarkGray: Color
        get() = Color(0xffc0c0c0)

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

    //    Toolkit.getDefaultToolkit().addAWTEventListener({ event ->
//        }, AWTEvent.MOUSE_EVENT_MASK or AWTEvent.FOCUS_EVENT_MASK
//    )

//    Toolkit.getDefaultToolkit().addAWTEventListener(object:AWTEventListener {
//        override fun eventDispatched(event: AWTEvent?) {
//            println(event)
//        }
//    }, AWTEvent.WINDOW_EVENT_MASK)

}
