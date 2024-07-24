package eu.mjdev.desktop.components.shadow

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray

@Suppress("unused")
@Preview
@Composable
fun RightShadow(
    modifier: Modifier = Modifier,
    alpha: Float = 0.5f,
    height: Dp = 8.dp,
    color: Color = Color.Black,
    contentBackgroundColor: Color = Color.SuperDarkGray,
    content: @Composable () -> Unit = {}
) = Row(
    modifier = modifier,
    verticalAlignment = Alignment.Top,
    horizontalArrangement = Arrangement.Start
) {
    Box(
        modifier = Modifier.background(contentBackgroundColor)
    ) {
        content()
    }
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(height)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        color.copy(alpha = alpha),
                        Color.Transparent,
                    )
                )
            )
    )
}
