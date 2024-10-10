package eu.mjdev.desktop.components.custom

//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp

//@Composable
//fun NeonCircle(
//    modifier: Modifier = Modifier,
//    colorCircle: Color = Color.White,
//    colorStroke: Color = Color.Red,
//    padding: Dp = 20.dp,
//    strokeWidth: Dp = 30.dp,
//    circleWidth: Dp = 3.dp
//) {
//    val paint = remember {
//        Paint().apply {
//            style = PaintingStyle.Stroke
//            this.strokeWidth = strokeWidth.value
//        }
//    }
//    val frameworkPaint = remember {
//        paint.asFrameworkPaint().apply {
//            setColor(colorStroke.copy(alpha = 0f).toArgb())
//        }
//    }
//    BoxWithConstraints(
//        modifier = modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        val baseSize = min(maxWidth, maxHeight)
//        val size = min(maxWidth, maxHeight) - (circleWidth + padding)
//        val pad = padding / 2
//        Canvas(
//            modifier = Modifier.size(baseSize, baseSize)
//        ) {
//            drawIntoCanvas { canvas ->
//                frameworkPaint.setShadowLayer(
//                    circleWidth.toPx(),
//                    pad.toPx(),
//                    pad.toPx(),
//                    colorStroke.copy(alpha = .5f).toArgb()
//                )
//                canvas.drawOval(
//                    left = 0f,
//                    top = 0f,
//                    right = size.toPx(),
//                    bottom = size.toPx(),
//                    paint = paint
//                )
//                drawOval(
//                    colorCircle,
//                    topLeft = Offset(pad.toPx(), pad.toPx()),
//                    size = Size(size.toPx(), size.toPx()),
//                    alpha = 1f,
//                    colorFilter = null,
//                    style = Stroke(width = circleWidth.toPx())
//                )
//            }
//        }
//    }
//}
