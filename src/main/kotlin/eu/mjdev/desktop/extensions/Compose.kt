@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package eu.mjdev.desktop.extensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import eu.mjdev.desktop.provider.DesktopProvider.Companion.desktopLocalProvider
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
    fun launchedEffect(
        key: Any?,
        block: suspend CoroutineScope.() -> Unit
    ) = LaunchedEffect(key, block)

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
    fun FullScreenWindow(
        visible: Boolean = true,
        title: String = "",
        icon: Painter? = null,
        focusable: Boolean = true,
        alwaysOnTop: Boolean = false,
        onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
        onKeyEvent: (KeyEvent) -> Boolean = { false },
        onCloseRequest: () -> Unit = {},
        content: @Composable FrameWindowScope.() -> Unit
    ) = Window(
        state = rememberWindowState(
            placement = WindowPlacement.Fullscreen
        ),
        resizable = false,
        undecorated = true,
        transparent = true,
        focusable = focusable,
        alwaysOnTop = alwaysOnTop,
        visible = visible,
        title = title,
        icon = icon,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
        onCloseRequest = onCloseRequest,
        content = {
            MaterialTheme {
                desktopLocalProvider {
                    content()
                }
            }
        }
    )

    @Composable
    fun TopWindow(
        // todo
        @Suppress("UNUSED_PARAMETER") movable: Boolean = false,
        resizable: Boolean = false,
        undecorated: Boolean = true,
        visible: Boolean = true,
        title: String = "",
        icon: Painter? = null,
        focusable: Boolean = true,
        onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
        onKeyEvent: (KeyEvent) -> Boolean = { false },
        onCloseRequest: () -> Unit = {},
        content: @Composable FrameWindowScope.() -> Unit
    ) = Window(
        state = rememberWindowState(
            placement = WindowPlacement.Floating
        ),
        resizable = resizable,
        undecorated = undecorated,
        onCloseRequest = onCloseRequest,
        visible = visible,
        transparent = true,
        title = title,
        icon = icon,
        focusable = focusable,
        alwaysOnTop = true,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
        content = {
            MaterialTheme {
                desktopLocalProvider {
                    content()
                }
            }
        }
    )

}
