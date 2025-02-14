package org.mjdev.desktop.extensions

//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.Stable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.Modifier.Node
//import androidx.compose.ui.draw.drawBehind
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.DefaultShadowColor
//import androidx.compose.ui.graphics.NativePaint
//import androidx.compose.ui.graphics.Paint
//import androidx.compose.ui.graphics.RectangleShape
//import androidx.compose.ui.graphics.Shape
//import androidx.compose.ui.graphics.drawOutline
//import androidx.compose.ui.graphics.drawscope.ContentDrawScope
//import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
//import androidx.compose.ui.graphics.toArgb
//import androidx.compose.ui.node.DrawModifierNode
//import androidx.compose.ui.node.ModifierNodeElement
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//
//@Suppress("unused")
//
//@Stable
//@Composable
//fun Modifier.shadow(
//    elevation: Dp,
//    shape: Shape = RectangleShape,
//    color: Color = DefaultShadowColor,
//    clip: Boolean = false,
//): Modifier = shadow(elevation, shape, clip, color, color)
//
//@Stable
//@Composable
//fun Modifier.rectShadow(
//    size: Dp = 8.dp,
//    color: Color = Color.Black,
//    shape: Shape = RectangleShape
//) = shadow(size, shape, color)
//
//@Stable
//@Composable
//fun Modifier.circleShadow(
//    size: Dp = 8.dp,
//    color: Color = Color.Black,
//    shape: Shape = CircleShape
//) = shadow(size, shape, color)
//
//@Stable
//@Composable
//fun Modifier.coloredCircleShadow(
//    color: Color,
//    blurRadius: Float,
//    offsetY: Dp,
//    offsetX: Dp,
//) = then(
//    drawBehind {
//        drawIntoCanvas { canvas ->
//            val paint = Paint()
//            val frameworkPaint = paint.asFrameworkPaint()
//            if (blurRadius != 0f) {
//                frameworkPaint.setMaskFilter(blurRadius)
//            }
//            // todo
////            frameworkPaint.color = color.toArgb()
//            val centerX = size.width / 2 + offsetX.toPx()
//            val centerY = size.height / 2 + offsetY.toPx()
//            val radius = size.width.coerceAtLeast(size.height) / 2
//            canvas.drawCircle(Offset(centerX, centerY), radius, paint)
//        }
//    }
//)
//
//fun Modifier.rightShadow(
//    color: Color,
//    offsetX: Dp = 0.dp,
//    blur: Dp = 8.dp,
//) = dropShadow(
//    shape = RectangleShape,
//    color = color,
//    offsetX = -offsetX,
//    offsetY = 0.dp,
//    blur = blur,
//    spread = offsetX,
//)
//
//fun Modifier.leftShadow(
//    color: Color,
//    offsetX: Dp = 0.dp,
//    blur: Dp = 8.dp
//) = dropShadow(
//    shape = RectangleShape,
//    color = color,
//    offsetX = offsetX,
//    offsetY = 0.dp,
//    blur = blur,
//    spread = offsetX,
//)
//
//fun Modifier.topShadow(
//    color: Color,
//    offsetY: Dp = 0.dp,
//    blur: Dp = 8.dp
//) = dropShadow(
//    shape = RectangleShape,
//    color = color,
//    offsetX = 0.dp,
//    offsetY = -offsetY,
//    blur = blur,
//    spread = offsetY,
//)
//
//fun Modifier.bottomShadow(
//    color: Color,
//    offsetY: Dp = 0.dp,
//    blur: Dp = 8.dp
//) = dropShadow(
//    shape = RectangleShape,
//    color = color,
//    offsetX = 0.dp,
//    offsetY = offsetY,
//    blur = blur,
//    spread = offsetY,
//)
//
//fun Modifier.dropShadow(
//    shape: Shape,
//    color: Color = Color.Black.copy(0.25f),
//    offsetX: Dp = 0.dp,
//    offsetY: Dp = 4.dp,
//    blur: Dp = 4.dp,
//    spread: Dp = 0.dp
//): Modifier {
//    return this then DropShadowNodeElement(shape, color, offsetX, offsetY, blur, spread)
//}
//
//private data class DropShadowNodeElement(
//    val shape: Shape,
//    val color: Color,
//    val offsetX: Dp,
//    val offsetY: Dp,
//    val blur: Dp,
//    val spread: Dp,
//) : ModifierNodeElement<DropShadowNode>() {
//    override fun create() = DropShadowNode(
//        shape,
//        color,
//        offsetX,
//        offsetY,
//        blur,
//        spread
//    )
//
//    override fun update(
//        node: DropShadowNode
//    ) {
//        node.shape = shape
//        node.color = color
//        node.offsetX = offsetX
//        node.offsetY = offsetY
//        node.blur = blur
//        node.spread = spread
//    }
//}
//
//private class DropShadowNode(
//    var shape: Shape,
//    var color: Color,
//    var offsetX: Dp,
//    var offsetY: Dp,
//    var blur: Dp,
//    var spread: Dp,
//) : DrawModifierNode, Node() {
//    override fun ContentDrawScope.draw() {
//        val shadowSize = Size(size.width + spread.toPx(), size.height + spread.toPx())
//        val shadowOutline = shape.createOutline(shadowSize, layoutDirection, this)
//        val rOffsetX = offsetX.toPx() - spread.toPx() / 2
//        val rOffsetY = offsetY.toPx() - spread.toPx() / 2
//        val paint = Paint()
//        paint.color = color
//        if (blur.value > 0) {
//            paint.asFrameworkPaint().apply {
//                setMaskFilter(blur.toPx())
//            }
//        }
//        drawIntoCanvas { canvas ->
//            // Save the canvas state
//            canvas.save()
//            // Translate to specified offsets
//            canvas.translate(rOffsetX, rOffsetY)
//            // Draw the shadow
//            canvas.drawOutline(shadowOutline, paint)
//            // Restore the canvas state
//            canvas.restore()
//        }
//        drawContent()
//    }
//}
//
//fun NativePaint.setMaskFilter(
//    blurRadius: Float
//) {
//    // todo
////    maskFilter = makeBlur(NORMAL, blurRadius / 2, true)
//}

