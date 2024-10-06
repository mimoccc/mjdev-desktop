package eu.mjdev.desktop.components.tooltip

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.data.TooltipData
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import kotlinx.coroutines.delay

@Suppress("FunctionName")
@Composable
fun Tooltip(
    textColor: Color = Color.White,
    borderColor: Color = Color.White,
    backgroundColor: Color = Color.Transparent,
    hideDelay: Long = 2000,
    state: MutableState<Any?> = remember { mutableStateOf(null) },
    converter: (Any?) -> TooltipData = { TooltipData(description = it.toString()) }
) = if (state.value != null) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        with(converter(state.value)) {
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
                        fontSize = 12.sp,
                        minLines = 1,
                        maxLines = 2
                    )
                }
            }
        }
    }
    launchedEffect(state) {
        delay(hideDelay)
        state.value = null
    }
} else Unit

@Preview
@Composable
fun TooltipPreview() = Tooltip()
