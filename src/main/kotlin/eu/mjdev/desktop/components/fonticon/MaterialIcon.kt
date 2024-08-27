@file:Suppress("unused")

package eu.mjdev.desktop.components.fonticon

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.components.icon.ShapedIcon
import eu.mjdev.desktop.components.text.AutoResizeText
import eu.mjdev.desktop.components.text.FontSizeRange
import eu.mjdev.desktop.extensions.Compose.sp
import eu.mjdev.desktop.extensions.Compose.width
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

@Composable
fun MaterialIcon(
    iconicFont: MaterialIconFont = LocalDesktop.current.currentUser.theme.iconSet,
    iconId: Int,
    iconSize: DpSize = DpSize(32.dp, 32.dp),
    iconColor: Color = Color.Black,
    iconBackgroundColor: Color = Color.White,
    innerPadding: PaddingValues = PaddingValues(4.dp),
    contentDescription: String = "",
    outerPadding: PaddingValues = PaddingValues(2.dp),
    iconShape: Shape = CircleShape,
    onClick: () -> Unit
) = ShapedIcon(
    iconSize = iconSize,
    iconColor = iconColor,
    iconBackgroundColor = iconBackgroundColor,
    contentDescription = contentDescription,
    iconShape = iconShape,
    innerPadding = innerPadding,
    outerPadding = outerPadding,
    customContent = {
        AutoResizeText(
            text = iconId.toChar().toString(),
            modifier = Modifier.align(Alignment.Center),
            color = iconColor,
            fontSizeRange = FontSizeRange(
                min = 16.sp,
                max = (iconSize.height - (innerPadding.width + outerPadding.width)).sp,
            ),
            fontFamily = iconicFont.fontFile.fontFamily,
            textAlign = TextAlign.Center
        )
    },
    onClick = onClick
)
