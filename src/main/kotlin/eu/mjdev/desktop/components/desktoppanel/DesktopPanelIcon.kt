package eu.mjdev.desktop.components.desktoppanel

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.fonticon.FontIcon
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.extensions.Compose.color
import eu.mjdev.desktop.extensions.Compose.noElevation
import eu.mjdev.desktop.extensions.Modifier.clipRect
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun DesktopPanelIcon(
    api: DesktopProvider = LocalDesktop.current,
    hapticFeedback: HapticFeedback = LocalHapticFeedback.current,
    app: App? = null,
    isRunning: Boolean = false,
    isStarting: Boolean = false,
    icon: String? = null,
    iconColor: Color = Color.Black,
    iconBackgroundColor: Color = Color.White,
    iconBackgroundHover: Color = Color.Red,
    iconSize: DpSize = DpSize(48.dp, 48.dp),
    iconPadding: PaddingValues = PaddingValues(4.dp),
    iconOuterPadding: PaddingValues = PaddingValues(2.dp),
    iconState: MutableState<Boolean> = remember { mutableStateOf(false) },
    contentDescription: String? = null,
    onToolTip: (item: Any?) -> Unit = {},
    onClick: () -> Unit,
) {
    val iconName = app?.name ?: icon
    val materialIcon = api.currentUser.theme.iconSet.iconForName(iconName) ?: "?".toInt()
    val background = when {
        (iconState.value || isRunning) -> iconBackgroundHover
        else -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .size(iconSize)
            .clipRect()
            .onPointerEvent(PointerEventType.Enter) {
                iconState.value = true
                onToolTip(app ?: contentDescription)
            }
            .onPointerEvent(PointerEventType.Exit) {
                iconState.value = false
            }
            .onPointerEvent(PointerEventType.Press) {
                iconState.value = false
            }
    ) {
        Button(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp),
            onClick = onClick,
            colors = ButtonDefaults.color(background),
            elevation = ButtonDefaults.noElevation()
        ) {
            FontIcon(
                iconId = materialIcon,
                iconColor = iconColor,
                iconBackgroundColor = iconBackgroundColor,
                iconSize = iconSize,
                innerPadding = iconPadding,
                outerPadding = iconOuterPadding,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
        }
        if (isStarting) {
            CircularProgressIndicator(
                modifier = Modifier.size(iconSize),
                color = Color.White,
                strokeWidth = 10.dp,
                backgroundColor = Color.Transparent
            )
        }
    }
}