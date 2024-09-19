@file:Suppress("MemberVisibilityCanBePrivate")

package eu.mjdev.desktop.extensions

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Modifier.Node
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
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

    fun Modifier.scaleOnPress(
        interactionSource: InteractionSource,
        pressScale: Float = 0.95f,
        leaveScale: Float = 1f
    ) = composed {
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(if (isPressed) pressScale else leaveScale)
        this.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
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

    fun Modifier.rightShadow(
        color: Color,
        offsetX: Dp = 0.dp,
        blur: Dp = 8.dp,
    ) = dropShadow(
        shape = RectangleShape,
        color = color,
        offsetX = -offsetX,
        offsetY = 0.dp,
        blur = blur,
        spread = offsetX,
    )

    fun Modifier.leftShadow(
        color: Color,
        offsetX: Dp = 0.dp,
        blur: Dp = 8.dp
    ) = dropShadow(
        shape = RectangleShape,
        color = color,
        offsetX = offsetX,
        offsetY = 0.dp,
        blur = blur,
        spread = offsetX,
    )

    fun Modifier.topShadow(
        color: Color,
        offsetY: Dp = 0.dp,
        blur: Dp = 8.dp
    ) = dropShadow(
        shape = RectangleShape,
        color = color,
        offsetX = 0.dp,
        offsetY = -offsetY,
        blur = blur,
        spread = offsetY,
    )

    fun Modifier.bottomShadow(
        color: Color,
        offsetY: Dp = 0.dp,
        blur: Dp = 8.dp
    ) = dropShadow(
        shape = RectangleShape,
        color = color,
        offsetX = 0.dp,
        offsetY = offsetY,
        blur = blur,
        spread = offsetY,
    )

    fun Modifier.dropShadow(
        shape: Shape,
        color: Color = Color.Black.copy(0.25f),
        offsetX: Dp = 0.dp,
        offsetY: Dp = 4.dp,
        blur: Dp = 4.dp,
        spread: Dp = 0.dp
    ): Modifier {
        return this then DropShadowNodeElement(shape, color, offsetX, offsetY, blur, spread)
    }

    private data class DropShadowNodeElement(
        val shape: Shape,
        val color: Color,
        val offsetX: Dp,
        val offsetY: Dp,
        val blur: Dp,
        val spread: Dp,
    ) : ModifierNodeElement<DropShadowNode>() {
        override fun create() = DropShadowNode(
            shape,
            color,
            offsetX,
            offsetY,
            blur,
            spread
        )

        override fun update(
            node: DropShadowNode
        ) {
            node.shape = shape
            node.color = color
            node.offsetX = offsetX
            node.offsetY = offsetY
            node.blur = blur
            node.spread = spread
        }
    }

    private class DropShadowNode(
        var shape: Shape,
        var color: Color,
        var offsetX: Dp,
        var offsetY: Dp,
        var blur: Dp,
        var spread: Dp,
    ) : DrawModifierNode, Node() {
        override fun ContentDrawScope.draw() {
            val shadowSize = Size(size.width + spread.toPx(), size.height + spread.toPx())
            val shadowOutline = shape.createOutline(shadowSize, layoutDirection, this)
            val rOffsetX = offsetX.toPx() - spread.toPx() / 2
            val rOffsetY = offsetY.toPx() - spread.toPx() / 2
            val paint = Paint()
            paint.color = color
            if (blur.value > 0) {
                paint.asFrameworkPaint().apply {
                    setMaskFilter(blur.toPx())
                }
            }
            drawIntoCanvas { canvas ->
                // Save the canvas state
                canvas.save()
                // Translate to specified offsets
                canvas.translate(rOffsetX, rOffsetY)
                // Draw the shadow
                canvas.drawOutline(shadowOutline, paint)
                // Restore the canvas state
                canvas.restore()
            }
            drawContent()
        }
    }

}