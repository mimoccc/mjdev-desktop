@file:Suppress("CanBeParameter")

package org.mjdev.desktop.components.tooltip

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.data.TooltipData
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.interfaces.IApp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("FunctionName")
@Composable
fun Tooltip(
    tooltipState: TooltipState= rememberTooltipState(),
) = withDesktopContext {
    if (tooltipState.value != null) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            with(tooltipState.convertedValue) {
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
    } else Unit
}

class TooltipState(
    val converter: (Any?) -> TooltipData = { item ->
        when (item) {
            is IApp -> TooltipData(title = item.name, description = item.comment)
            else -> TooltipData(description = item.toString())
        }
    }
) {
    private var valueState: MutableState<Any?> = mutableStateOf(null)

    val convertedValue: TooltipData
        get() = converter(value)

    var value
        get() = valueState.value
        set(value) {
            this.valueState.value = value
        }

    fun show(what: Any?) {
        value = what
    }

    fun clear() {
        value = null
    }
}

@Composable
fun rememberTooltipState() = remember { TooltipState() }

@Preview
@Composable
fun TooltipPreview() = preview {
    Tooltip(
        tooltipState = rememberTooltipState().apply{ show("test tooltip") }
    )
}
