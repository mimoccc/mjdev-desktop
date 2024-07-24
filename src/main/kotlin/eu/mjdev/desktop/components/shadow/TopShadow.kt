package eu.mjdev.desktop.components.shadow

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray

@Suppress("unused")
@Preview
@Composable
fun TopShadow(
    modifier: Modifier = Modifier,
    alpha: Float = 0.5f,
    height: Dp = 8.dp,
    color: Color = Color.Black,
    contentBackgroundColor: Color = Color.SuperDarkGray,
    content: @Composable () -> Unit = {}
) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.Top
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color.copy(alpha = alpha),
                    )
                )
            )
    )
    Box(
        modifier = Modifier.fillMaxWidth().wrapContentHeight().background(contentBackgroundColor)
    ) {
        content()
    }
}
