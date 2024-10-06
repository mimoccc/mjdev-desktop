package eu.mjdev.desktop.data

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.fonts.MaterialIconFont
import eu.mjdev.desktop.fonts.MaterialSymbolsSharp
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray

// todo all customizable
@Suppress("unused", "MemberVisibilityCanBePrivate")
class Theme(
    backgroundColor: Color,
    backgroundRotationDelay: Long,

    panelLocation: PanelLocation,
    panelDividerWidth: Dp,
    panelContentPadding: Dp,
    panelHideDelay: Long,

    controlCenterLocation: ControlCenterLocation,
    controlPanelHideDelay: Long,
    controlCenterExpandedWidthPercent: Int,
    controlCenterDividerColor: Color = backgroundColor,
    controlCenterDividerWidth: Dp,
    controlCenterIconColor: Color,
    controlCenterIconSize: DpSize,
    controlCenterBackgroundAlpha: Float,

    iconSet: MaterialIconFont,

    appMenuMinWidth: Dp,
    appMenuMinHeight: Dp,
    appMenuOuterPadding: Dp,
) {

    val backgroundColorState = mutableStateOf(backgroundColor)
    val backgroundRotationDelayState = mutableStateOf(backgroundRotationDelay)

    val panelLocationState = mutableStateOf(panelLocation)
    val panelHideDelayState = mutableStateOf(panelHideDelay)
    val panelDividerWidthState = mutableStateOf(panelDividerWidth)
    val panelContentPaddingState = mutableStateOf(panelContentPadding)

    val controlCenterLocationState = mutableStateOf(controlCenterLocation)
    val controlPanelHideDelayState = mutableStateOf(controlPanelHideDelay)
    var controlCenterExpandedWidthState = mutableStateOf(controlCenterExpandedWidthPercent)
    val controlCenterDividerColorState = mutableStateOf(controlCenterDividerColor)
    val controlCenterDividerWidthState = mutableStateOf(controlCenterDividerWidth)
    val controlCenterIconColorState = mutableStateOf(controlCenterIconColor)
    val controlCenterIconSizeState = mutableStateOf(controlCenterIconSize)

    val iconSetState = mutableStateOf(iconSet)

    val appMenuMinWidthState = mutableStateOf(appMenuMinWidth)
    val appMenuMinHeightState = mutableStateOf(appMenuMinHeight)
    val appMenuOuterPaddingState = mutableStateOf(appMenuOuterPadding)

    var backgroundColor
        get() = backgroundColorState.value
        set(value) {
            backgroundColorState.value = value
        }

    val panelLocation get() = panelLocationState.value
    val panelDividerWidth get() = panelDividerWidthState.value
    val panelContentPadding get() = panelContentPaddingState.value
    val panelHideDelay get() = panelHideDelayState.value

    val controlCenterBackgroundAlphaState = mutableStateOf(controlCenterBackgroundAlpha)
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

    val appMenuMinWidth get() = appMenuMinWidthState.value
    val appMenuMinHeight get() = appMenuMinHeightState.value
    val appMenuOuterPadding get() = appMenuOuterPaddingState.value

    companion object {
        val Default by lazy {
            Theme(
                iconSet = MaterialSymbolsSharp,

                backgroundColor = Color.SuperDarkGray,

                panelLocation = PanelLocation.Bottom,
                panelDividerWidth = 16.dp,
                panelContentPadding = 4.dp,
                panelHideDelay = 5000L,

                controlCenterLocation = ControlCenterLocation.Right,
                controlPanelHideDelay = 5000L,
                controlCenterExpandedWidthPercent = 25,
                controlCenterDividerColor = Color.SuperDarkGray,
                controlCenterDividerWidth = 4.dp,
                controlCenterIconColor = Color.White,
                controlCenterIconSize = DpSize(32.dp, 32.dp),
                controlCenterBackgroundAlpha = 0.8f,

                backgroundRotationDelay = 60000,

                appMenuMinWidth = 480.dp,
                appMenuMinHeight = 640.dp,
                appMenuOuterPadding = 2.dp
            )
        }
    }
}
