package eu.mjdev.desktop.components.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.extensions.Compose.size

@Composable
fun ShapedIcon(
    imageVector: ImageVector = Icons.Filled.Apps,
    iconBackgroundColor: Color = Color.White,
    iconColor: Color = Color.Black,
    iconSize: DpSize = DpSize(32.dp, 32.dp),
    contentDescription: String = "",
    customContent: (@Composable BoxScope.() -> Unit)? = null,
    iconShape: Shape = CircleShape,
    innerPadding: PaddingValues = PaddingValues(4.dp),
    outerPadding: PaddingValues = PaddingValues(2.dp),
) = Box(
    modifier = Modifier
        .padding(outerPadding)
        .size(iconSize + innerPadding.size + outerPadding.size)
        .background(iconBackgroundColor, iconShape)
        .clip(iconShape)
        .padding(innerPadding)
) {
    if (customContent != null) {
        customContent()
    } else {
        Icon(
            modifier = Modifier.fillMaxSize(),
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = iconColor
        )
    }
}


