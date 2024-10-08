@file:Suppress("unused")

package eu.mjdev.desktop.components.fonticon

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.icon.ShapedIcon
import eu.mjdev.desktop.components.text.AutoResizeText
import eu.mjdev.desktop.fonts.MaterialIconFont
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

@Suppress("FunctionName")
@Composable
fun FontIcon(
    modifier: Modifier = Modifier,
    iconId: Int = 0,
    iconicFont: MaterialIconFont = LocalDesktop.current.currentUser.theme.iconSet,
    iconSize: DpSize = DpSize(32.dp, 32.dp),
    iconColor: Color = Color.Black,
    iconBackgroundColor: Color = Color.White,
    innerPadding: PaddingValues = PaddingValues(4.dp),
    contentDescription: String = "",
    outerPadding: PaddingValues = PaddingValues(2.dp),
    iconShape: Shape = CircleShape,
    onRightClick: () -> Unit = {},
    onClick: () -> Unit = {}
) = ShapedIcon(
    modifier = modifier,
    iconSize = iconSize,
    iconColor = iconColor,
    iconBackgroundColor = iconBackgroundColor,
    contentDescription = contentDescription,
    iconShape = iconShape,
    innerPadding = innerPadding,
    outerPadding = outerPadding,
    customContent = {
        AutoResizeText(
            modifier = Modifier.align(Alignment.Center),
            text = iconId.toChar().toString(),
            color = iconColor,
            fontFamily = iconicFont.fontFamily,
        )
    },
    onClick = onClick,
    onRightClick = onRightClick
)

@Preview
@Composable
fun FontIconPreview() = FontIcon()
