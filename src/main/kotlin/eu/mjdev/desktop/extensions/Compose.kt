@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package eu.mjdev.desktop.extensions

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter

object Compose {

    val Color.Companion.SuperDarkGray: Color
        get() = Color(0xff202020)

    val Color.Companion.DarkDarkGray: Color
        get() = Color(0xff404040)

    val Color.Companion.MediumDarkGray: Color
        get() = Color(0xff808080)

    val Color.Companion.LiteDarkGray: Color
        get() = Color(0xffc0c0c0)

    fun Modifier.conditional(
        condition: Boolean,
        onFalse: (Modifier.() -> Modifier)? = null,
        onTrue: Modifier.() -> Modifier,
    ): Modifier = if (condition) {
        then(onTrue(Modifier))
    } else if (onFalse != null) {
        then(onFalse(Modifier))
    } else {
        this
    }

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
    fun launchedEffect(
        block: suspend CoroutineScope.() -> Unit
    ) = LaunchedEffect(Unit, block)

    @Composable
    fun launchedEffect(
        key: Any?,
        block: suspend CoroutineScope.() -> Unit
    ) = LaunchedEffect(key, block)

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

    @Stable
    fun Modifier.shadow(
        elevation: Dp,
        shape: Shape = RectangleShape,
        color: Color = DefaultShadowColor,
        clip: Boolean = false,
    ): Modifier = shadow(elevation, shape, clip, color, color)

    @Composable
    fun Modifier.rectShadow(
        size: Dp = 8.dp,
        color: Color = Color.Black,
        shape: Shape = RectangleShape
    ) = shadow(size, shape, color)

    @Composable
    fun Modifier.circleShadow(
        size: Dp = 8.dp,
        color: Color = Color.Black,
        shape: Shape = CircleShape
    ) = shadow(size, shape, color)

    @Composable
    fun Modifier.clipCircle() = clip(CircleShape)

    @Composable
    fun Modifier.circleBorder(
        width: Dp = 2.dp,
        color: Color = Color.White
    ) = clipCircle().border(width, color, CircleShape)

    @Composable
    fun Modifier.coloredCircleShadow(
        color: Color,
        blurRadius: Float,
        offsetY: Dp,
        offsetX: Dp,
    ) = then(
        drawBehind {
            drawIntoCanvas { canvas ->
                val paint = Paint()
                val frameworkPaint = paint.asFrameworkPaint()
                if (blurRadius != 0f) {
                    frameworkPaint.setMaskFilter(blurRadius)
                }
                frameworkPaint.color = color.toArgb()
                val centerX = size.width / 2 + offsetX.toPx()
                val centerY = size.height / 2 + offsetY.toPx()
                val radius = size.width.coerceAtLeast(size.height) / 2
                canvas.drawCircle(Offset(centerX, centerY), radius, paint)
            }
        }
    )

    fun NativePaint.setMaskFilter(
        blurRadius: Float
    ) {
        this.maskFilter = MaskFilter.makeBlur(FilterBlurMode.NORMAL, blurRadius / 2, true)
    }

    @Composable
    fun fullScreenWindow(
        onCloseRequest: () -> Unit = {},
        content: @Composable FrameWindowScope.() -> Unit
    ) = Window(
        state = rememberWindowState(
            placement = WindowPlacement.Fullscreen
        ),
        resizable = false,
        undecorated = true,
        onCloseRequest = onCloseRequest,
        content = content
    )

}
