package eu.mjdev.desktop.extensions

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.extensions.Custom.setMaskFilter

@Suppress("unused")
object Modifier {

    @Stable
    @Composable
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

    @Stable
    @Composable
    fun Modifier.shadow(
        elevation: Dp,
        shape: Shape = RectangleShape,
        color: Color = DefaultShadowColor,
        clip: Boolean = false,
    ): Modifier = shadow(elevation, shape, clip, color, color)

    @Stable
    @Composable
    fun Modifier.rectShadow(
        size: Dp = 8.dp,
        color: Color = Color.Black,
        shape: Shape = RectangleShape
    ) = shadow(size, shape, color)

    @Stable
    @Composable
    fun Modifier.circleShadow(
        size: Dp = 8.dp,
        color: Color = Color.Black,
        shape: Shape = CircleShape
    ) = shadow(size, shape, color)

    @Stable
    @Composable
    fun Modifier.clipCircle() = clip(CircleShape)

    @Stable
    @Composable
    fun Modifier.clipRect() = clip(RectangleShape)

    @Stable
    @Composable
    fun Modifier.clipRoundRect(round: Dp) = clip(RoundedCornerShape(round))

    @Stable
    @Composable
    fun Modifier.circleBorder(
        width: Dp = 2.dp,
        color: Color = Color.White
    ) = clipCircle().border(width, color, CircleShape)

    @Stable
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

}