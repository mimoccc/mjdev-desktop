package eu.mjdev.desktop.components.desktoppanel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.components.text.TextAny

@Composable
fun DesktopPanelTooltip(
    item: Any? = null,
    textColor: Color = Color.White,
    borderColor: Color = Color.White,
    backgroundColor: Color = Color.Transparent,
    converter: (Any?) -> TooltipData = { TooltipData(description = it.toString()) }
) = if (item != null) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {

        with(converter(item)) {
            Column {
                if (title.isNotEmpty()) {
                    TextAny(
                        text = title,
                        color = textColor,
                        fontSize = 14.sp
                    )
                }
                if (description.isNotEmpty()) {
                    TextAny(
                        text = description,
                        color = textColor,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
} else Unit

data class TooltipData(
    val title: String = "",
    val description: String = ""
)