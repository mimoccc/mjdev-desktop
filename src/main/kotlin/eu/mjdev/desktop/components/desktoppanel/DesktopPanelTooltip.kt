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
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

@Composable
fun DesktopPanelTooltip(
    item: Any?,
    api: DesktopProvider = LocalDesktop.current,
    converter: (Any?) -> TooltipData = { TooltipData(description = it.toString()) }
) = Box(
    modifier = Modifier
        .wrapContentSize()
        .background(api.currentUser.theme.backgroundColor, RoundedCornerShape(8.dp))
        .border(2.dp, Color.White.copy(0.2f), RoundedCornerShape(8.dp))
        .padding(8.dp)
) {
    if (item != null) {
        with(converter(item)) {
            Column {
                if (title.isNotEmpty()) {
                    TextAny(
                        text = title,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
                if (description.isNotEmpty()) {
                    TextAny(
                        text = description,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

data class TooltipData(
    val title: String = "",
    val description: String = ""
)