@file:Suppress("unused")

package org.mjdev.desktop.components.fonticon

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.desktop.components.icon.ShapedIcon
import org.mjdev.desktop.components.text.AutoResizeText
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.interfaces.IFont
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("FunctionName")
@Composable
fun FontIcon(
    modifier: Modifier = Modifier,
    iconId: Int = "x".toInt(),
    iconicFont: IFont? = null,
    iconSize: DpSize = DpSize(32.dp, 32.dp),
    iconColor: Color = Color.Black,
    iconBackgroundColor: Color = Color.White,
    innerPadding: PaddingValues = PaddingValues(4.dp),
    outerPadding: PaddingValues = PaddingValues(4.dp),
    iconShape: Shape = CircleShape,
    contentDescription: String = "",
    onRightClick: () -> Unit = {},
    onClick: () -> Unit = {}
) = withDesktopContext {
    ShapedIcon(
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
                text = iconId.toChar().toString(),
                color = iconColor,
                fontFamily = (iconicFont ?: theme.iconSet).fontFamily,
                minFontSize = (iconSize.height.value * 0.7).sp
            )
        },
        onClick = onClick,
        onRightClick = onRightClick
    )
}

@Composable
fun FontIcon(
    modifier: Modifier = Modifier,
    iconName: String = "",
    iconicFont: IFont? = null,
    iconSize: DpSize = DpSize(32.dp, 32.dp),
    iconColor: Color = Color.Black,
    iconBackgroundColor: Color = Color.White,
    innerPadding: PaddingValues = PaddingValues(4.dp),
    contentDescription: String = "",
    outerPadding: PaddingValues = PaddingValues(2.dp),
    iconShape: Shape = CircleShape,
    onRightClick: () -> Unit = {},
    onClick: () -> Unit = {}
) = withDesktopContext {
    ShapedIcon(
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
                text = (iconSet?.iconForName(iconName) ?: 0).toChar().toString(),
                color = iconColor,
                fontFamily = (iconicFont ?: theme?.iconSet)?.fontFamily,
            )
        },
        onClick = onClick,
        onRightClick = onRightClick
    )
}

@Preview
@Composable
fun FontIconPreview() = preview {
    Column {
        Column {
            FontIcon(iconId = 0)
            FontIcon(iconName = "")
            FontIcon(iconName = "tv")
            FontIcon(iconName = "browser")
        }
        Spacer(modifier = Modifier.height(32.dp).fillMaxWidth())
        Row {
            FontIcon(iconId = 0)
            FontIcon(iconName = "")
            FontIcon(iconName = "tv")
            FontIcon(iconName = "browser")
        }
    }
}
