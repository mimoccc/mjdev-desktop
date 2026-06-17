package org.mjdev.desktop.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.helpers.compose.ImagesProvider
import org.mjdev.desktop.interfaces.ITheme
import org.mjdev.desktop.interfaces.IUser
import kotlin.math.roundToInt

/**
 * Serializable snapshot of the whole desktop configuration — every tunable that the desktop
 * shell exposes ([ITheme]) plus the background [ProviderConfig] list. Persisted as JSON via
 * [DesktopConfigStore] at `~/.mjdev/desktop/config.json` and bridged to the live [ITheme]
 * with [applyTo] / [fromTheme].
 *
 * Colors are stored as packed ARGB ints and Dp values as their raw float so the JSON stays
 * plain and portable.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
data class DesktopConfigData(
    // background
    var backgroundColorArgb: Int = Color(0xFF202020).toArgbInt(),
    var backgroundRotationDelay: Long = 60_000L,
    // panel
    var panelLocation: String = PanelLocation.Bottom.name,
    var panelDividerWidth: Float = 16f,
    var panelContentPadding: Float = 4f,
    var panelHideDelay: Long = 2_000L,
    // control center
    var controlCenterLocation: String = ControlCenterLocation.Right.name,
    var controlPanelHideDelay: Long = 2_000L,
    var controlCenterExpandedWidthPercent: Int = 25,
    var controlCenterDividerWidth: Float = 4f,
    var controlCenterIconSize: Float = 32f,
    var controlCenterBackgroundAlpha: Float = 0.6f,
    // app menu
    var appMenuMinWidthRatio: Float = 0.3f,
    var appMenuMinHeightRatio: Float = 0.8f,
    var appMenuOuterPadding: Float = 2f,
    // background sources
    var providers: MutableList<ProviderConfig> = ProviderConfig.defaults(),
) {
    /** Writes every value back onto the live [theme] so the running desktop picks them up. */
    fun applyTo(theme: ITheme) {
        theme.backgroundColor = Color(backgroundColorArgb)
        theme.backgroundRotationDelay = backgroundRotationDelay
        theme.panelLocation = panelLocation.toEnum(PanelLocation.Bottom)
        theme.panelDividerWidth = panelDividerWidth.dp
        theme.panelContentPadding = panelContentPadding.dp
        theme.panelHideDelay = panelHideDelay
        theme.controlCenterLocation = controlCenterLocation.toEnum(ControlCenterLocation.Right)
        theme.controlPanelHideDelay = controlPanelHideDelay
        theme.controlCenterExpandedWidthPercent = controlCenterExpandedWidthPercent
        theme.controlCenterDividerWidth = controlCenterDividerWidth.dp
        theme.controlCenterIconSize = DpSize(controlCenterIconSize.dp, controlCenterIconSize.dp)
        theme.controlCenterBackgroundAlpha = controlCenterBackgroundAlpha
        theme.appMenuMinWidthRatio = appMenuMinWidthRatio
        theme.appMenuMinHeightRatio = appMenuMinHeightRatio
        theme.appMenuOuterPadding = appMenuOuterPadding.dp
    }

    /** Builds the enabled background providers for this config. */
    fun buildProviders(user: IUser): List<ImagesProvider> = providers.mapNotNull { it.build(user) }

    private inline fun <reified T : Enum<T>> String.toEnum(default: T): T = enumValues<T>().firstOrNull { it.name == this } ?: default

    companion object {
        /** Snapshots the current live [theme] into a persistable config, keeping [providers]. */
        fun fromTheme(
            theme: ITheme,
            providers: MutableList<ProviderConfig> = ProviderConfig.defaults(),
        ): DesktopConfigData =
            DesktopConfigData(
                backgroundColorArgb = theme.backgroundColor.toArgbInt(),
                backgroundRotationDelay = theme.backgroundRotationDelay,
                panelLocation = theme.panelLocation.name,
                panelDividerWidth = theme.panelDividerWidth.value,
                panelContentPadding = theme.panelContentPadding.value,
                panelHideDelay = theme.panelHideDelay,
                controlCenterLocation = theme.controlCenterLocation.name,
                controlPanelHideDelay = theme.controlPanelHideDelay,
                controlCenterExpandedWidthPercent = theme.controlCenterExpandedWidthPercent,
                controlCenterDividerWidth = theme.controlCenterDividerWidth.value,
                controlCenterIconSize = theme.controlCenterIconSize.width.value,
                controlCenterBackgroundAlpha = theme.controlCenterBackgroundAlpha,
                appMenuMinWidthRatio = theme.appMenuMinWidthRatio,
                appMenuMinHeightRatio = theme.appMenuMinHeightRatio,
                appMenuOuterPadding = theme.appMenuOuterPadding.value,
                providers = providers,
            )

        private fun Color.toArgbInt(): Int {
            val a = (alpha * 255f).roundToInt() and 0xFF
            val r = (red * 255f).roundToInt() and 0xFF
            val g = (green * 255f).roundToInt() and 0xFF
            val b = (blue * 255f).roundToInt() and 0xFF
            return (a shl 24) or (r shl 16) or (g shl 8) or b
        }
    }
}
