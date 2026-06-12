package org.mjdev.desktop.data

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.extensions.Colors.SuperDarkGray
import org.mjdev.desktop.helpers.font.MaterialSymbolsSharp
import org.mjdev.desktop.interfaces.IFont
import org.mjdev.desktop.interfaces.ITheme
import org.mjdev.desktop.interfaces.IUser

// todo all components customizable
@Suppress("unused", "MemberVisibilityCanBePrivate")
class Theme(
    backgroundColor: Color,
    backgroundRotationDelay: Long,

    panelLocation: PanelLocation,
    panelDividerWidth: Dp,
    panelContentPadding: Dp,
    panelHideDelay: Long,

    // grace period before a popup window (menu, control center) hides after
    // losing focus; absorbs the focus bounce of the wayland compositor
    windowFocusGraceDelay: Long,

    controlCenterLocation: ControlCenterLocation,
    controlPanelHideDelay: Long,
    controlCenterExpandedWidthPercent: Int,
    controlCenterDividerColor: Color = backgroundColor,
    controlCenterDividerWidth: Dp,
    controlCenterIconColor: Color,
    controlCenterIconSize: DpSize,
    controlCenterBackgroundAlpha: Float,

    iconSet: IFont,

    appMenuMinWidthRatio:Float,
    appMenuMinHeightRatio:Float,
    appMenuOuterPadding: Dp,
) : ITheme {

    val backgroundColorState = mutableStateOf(backgroundColor)
    val backgroundRotationDelayState = mutableStateOf(backgroundRotationDelay)

    val panelLocationState = mutableStateOf(panelLocation)
    val panelHideDelayState = mutableStateOf(panelHideDelay)
    val windowFocusGraceDelayState = mutableStateOf(windowFocusGraceDelay)
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

    val appMenuMinWidthState = mutableStateOf(appMenuMinWidthRatio)
    val appMenuMinHeightState = mutableStateOf(appMenuMinHeightRatio)
    val appMenuOuterPaddingState = mutableStateOf(appMenuOuterPadding)

    val controlCenterBackgroundAlphaState = mutableStateOf(controlCenterBackgroundAlpha)

    override var backgroundColor = backgroundColorState.value

    override var panelLocation = panelLocationState.value

    override var panelDividerWidth = panelDividerWidthState.value

    override var panelContentPadding = panelContentPaddingState.value

    override var panelHideDelay = panelHideDelayState.value

    override var windowFocusGraceDelay = windowFocusGraceDelayState.value

    override var controlCenterLocation = controlCenterLocationState.value

    override var controlPanelHideDelay = controlPanelHideDelayState.value

    override var controlCenterExpandedWidthPercent = controlCenterExpandedWidthState.value

    override var controlCenterDividerColor = controlCenterDividerColorState.value

    override var controlCenterDividerWidth = controlCenterDividerWidthState.value

    override var controlCenterIconColor = controlCenterIconColorState.value

    override var controlCenterIconSize = controlCenterIconSizeState.value

    override var controlCenterBackgroundAlpha = controlCenterBackgroundAlphaState.value

    override var iconSet : IFont = iconSetState.value

    override var backgroundRotationDelay = backgroundRotationDelayState.value

    override var appMenuOuterPadding = appMenuOuterPaddingState.value

    override var appMenuMinWidthRatio: Float = appMenuMinWidthState.value

    override var appMenuMinHeightRatio: Float = appMenuMinHeightState.value

    override fun dispose() {
        // todo
    }

    companion object {
        val themeCache = mutableMapOf<String, Theme>()

        val DEFAULT = Theme(
            iconSet = MaterialSymbolsSharp,

            backgroundColor = Color.SuperDarkGray,
            backgroundRotationDelay = 60000,

            panelLocation = PanelLocation.Bottom,
            panelDividerWidth = 16.dp,
            panelContentPadding = 4.dp,
            panelHideDelay = 2000L,
            windowFocusGraceDelay = 300L,

            controlCenterLocation = ControlCenterLocation.Right,
            controlPanelHideDelay = 2000L,
            controlCenterExpandedWidthPercent = 25,
            controlCenterDividerColor = Color.SuperDarkGray,
            controlCenterDividerWidth = 4.dp,
            controlCenterIconColor = Color.White,
            controlCenterIconSize = DpSize(32.dp, 32.dp),
            controlCenterBackgroundAlpha = 0.6f,

            appMenuMinWidthRatio = 0.3f,
            appMenuMinHeightRatio = 0.8f,
            appMenuOuterPadding = 2.dp
        )

        // todo load from user settings
        fun load(
            user: IUser
        ): Theme = themeCache[user.userName] ?: DEFAULT.apply {
            themeCache[user.userName] = this
        }
    }
}
