package eu.mjdev.desktop.provider.data

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.fonticon.MaterialIconFont
import eu.mjdev.desktop.components.fonticon.MaterialSymbolsSharp
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// todo all customizable
@Suppress("unused", "MemberVisibilityCanBePrivate")
class Theme(
    val scope: CoroutineScope?,
    backgroundColor: Color,
    backgroundRotationDelay: Long,

    panelLocation: PanelLocation,
    panelHideDelay: Long,

    controlCenterLocation: ControlCenterLocation,
    controlPanelHideDelay: Long,
    controlCenterExpandedWidth: Dp,
    controlCenterDividerColor: Color = backgroundColor,
    controlCenterDividerWidth: Dp,
    controlCenterIconColor: Color,
    controlCenterIconSize: DpSize,
    controlCenterBackgroundAlpha: Float,

    iconSet: MaterialIconFont,
) {

    val backgroundColorState = Animatable(backgroundColor)
    val panelLocationState = mutableStateOf(panelLocation)
    val panelHideDelayState = mutableStateOf(panelHideDelay)
    val controlCenterLocationState = mutableStateOf(controlCenterLocation)
    val controlPanelHideDelayState = mutableStateOf(controlPanelHideDelay)
    var controlCenterExpandedWidthState = mutableStateOf(controlCenterExpandedWidth)
    val controlCenterDividerColorState = mutableStateOf(controlCenterDividerColor)
    val controlCenterDividerWidthState = mutableStateOf(controlCenterDividerWidth)
    val controlCenterIconColorState = mutableStateOf(controlCenterIconColor)
    val controlCenterIconSizeState = mutableStateOf(controlCenterIconSize)
    val iconSetState = mutableStateOf(iconSet)
    val backgroundRotationDelayState = mutableStateOf(backgroundRotationDelay)
    val controlCenterBackgroundAlphaState = mutableStateOf(controlCenterBackgroundAlpha)

    var backgroundColor
        get() = backgroundColorState.value
        set(value) {
            scope?.launch {
                backgroundColorState.animateTo(value, animationSpec = tween(1500))
            } ?: {
                println("Can not set background color")
            }
        }
    val panelLocation get() = panelLocationState.value
    val panelHideDelay get() = panelHideDelayState.value
    val controlCenterLocation get() = controlCenterLocationState.value
    val controlPanelHideDelay get() = controlPanelHideDelayState.value
    var controlCenterExpandedWidth
        get() = controlCenterExpandedWidthState.value
        set(value) {
            controlCenterExpandedWidthState.value = value
        }
    val controlCenterDividerColor get() = controlCenterDividerColorState.value
    val controlCenterDividerWidth get() = controlCenterDividerWidthState.value
    val controlCenterIconColor get() = controlCenterIconColorState.value
    val controlCenterIconSize get() = controlCenterIconSizeState.value
    val controlCenterBackgroundAlpha get() = controlCenterBackgroundAlphaState.value
    val iconSet get() = iconSetState.value
    val backgroundRotationDelay get() = backgroundRotationDelayState.value

    companion object {
        @Suppress("FunctionName")
        fun Default(
            scope: CoroutineScope?
        ) = Theme(
            scope = scope,
            iconSet = MaterialSymbolsSharp,

            backgroundColor = Color.SuperDarkGray,

            panelLocation = PanelLocation.Bottom,
            panelHideDelay = 5000L,

            controlCenterLocation = ControlCenterLocation.Right,
            controlPanelHideDelay = 5000L,
            controlCenterExpandedWidth = 480.dp,
            controlCenterDividerColor = Color.SuperDarkGray,
            controlCenterDividerWidth = 2.dp,
            controlCenterIconColor = Color.White,
            controlCenterIconSize = DpSize(32.dp, 32.dp),
            controlCenterBackgroundAlpha = 0.8f,

            backgroundRotationDelay = 10000
        )
    }
}
