package eu.mjdev.desktop.components.desktoppanel.applets

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.components.custom.DateTime
import eu.mjdev.desktop.extensions.Compose.color
import eu.mjdev.desktop.extensions.Compose.noElevation
import eu.mjdev.desktop.extensions.Compose.onMouseEnter
import eu.mjdev.desktop.extensions.Compose.onMouseLeave
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Composable
fun DesktopPanelDateTime(
    modifier: Modifier = Modifier,
    buttonState: MutableState<Boolean> = rememberState(false),
    padding: Dp = 4.dp,
    backgroundHover: Color = Color.White.copy(alpha = 0.4f),
    timeTextSize: TextUnit = 14.sp,
    dateTextSize: TextUnit = 10.sp,
    onTooltip: (item: Any?) -> Unit = {},
    onClick: () -> Unit = {},
) = withDesktopScope {
    Box(
        modifier = modifier
            .wrapContentSize()
            .onMouseEnter {
                buttonState.value = true
                onTooltip(null) // todo
            }
            .onMouseLeave {
                buttonState.value = false
            }
    ) {
        Button(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = 4.dp),
            onClick = onClick,
            colors = ButtonDefaults.color(if (buttonState.value) backgroundHover else Color.Transparent),
            elevation = ButtonDefaults.noElevation()
        ) {
            DateTime(
                backgroundColor = Color.Transparent,
                padding = 1.dp,
                dateTextSize = dateTextSize,
                timeTextSize = timeTextSize,
                timeTextColor = textColor,
                dateTextColor = textColor,
            )
        }
    }
}

@Preview
@Composable
fun DesktopPanelDateTimePreview() = DesktopPanelDateTime()