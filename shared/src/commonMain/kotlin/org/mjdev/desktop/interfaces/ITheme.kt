package org.mjdev.desktop.interfaces

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.data.ControlCenterLocation
import org.mjdev.desktop.data.PanelLocation
import org.mjdev.desktop.extensions.Colors.SuperDarkGray

@Suppress("UNUSED_PARAMETER")
interface ITheme : IDisposable {
    var iconSet: IFont
    var backgroundColor: Color
    var backgroundRotationDelay: Long

    var panelLocation: PanelLocation
    var panelDividerWidth: Dp
    var panelContentPadding: Dp
    var panelHideDelay: Long

    var controlCenterLocation: ControlCenterLocation
    var controlPanelHideDelay: Long
    var controlCenterExpandedWidthPercent: Int
    var controlCenterDividerColor: Color
    var controlCenterDividerWidth: Dp
    var controlCenterIconColor: Color
    var controlCenterIconSize: DpSize
    var controlCenterBackgroundAlpha: Float

    var appMenuMinWidthRatio: Float
    var appMenuMinHeightRatio: Float
    var appMenuOuterPadding: Dp

    companion object {
        val DEFAULT =
            object : ITheme {
                override var iconSet: IFont = IFont.Empty
                override var backgroundColor: Color = Color.SuperDarkGray
                override var backgroundRotationDelay: Long = 60000
                override var panelLocation: PanelLocation = PanelLocation.Bottom
                override var panelDividerWidth: Dp = 2.dp
                override var panelContentPadding: Dp = 8.dp
                override var panelHideDelay: Long = 0
                override var controlCenterLocation: ControlCenterLocation = ControlCenterLocation.Right
                override var controlPanelHideDelay: Long = 10000
                override var controlCenterExpandedWidthPercent: Int = 35
                override var controlCenterDividerColor: Color = Color.SuperDarkGray
                override var controlCenterDividerWidth: Dp = 2.dp
                override var controlCenterIconColor: Color = Color.White
                override var controlCenterIconSize: DpSize = DpSize(32.dp, 32.dp)
                override var controlCenterBackgroundAlpha: Float = 0.4f
                override var appMenuMinWidthRatio: Float = 0.3f
                override var appMenuMinHeightRatio: Float = 0.8f
                override var appMenuOuterPadding: Dp = 4.dp

                override fun dispose() {
                }
            }

        fun load(user: IUser?): ITheme = DEFAULT
    }
}
