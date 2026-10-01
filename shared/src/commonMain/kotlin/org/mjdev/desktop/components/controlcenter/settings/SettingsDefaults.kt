package org.mjdev.desktop.components.controlcenter.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Layout and alpha constants shared by all control center settings pages and rows. */
object SettingsDefaults {
    /** Padding around the scrollable content of a settings page. */
    val pagePadding: Dp = 16.dp

    /** Extra space kept free at the bottom of a scrollable page. */
    val pageBottomPadding: Dp = 24.dp

    /** Corner radius of cards, chips and swatches. */
    val cornerRadius: Dp = 12.dp

    /** Size of a color swatch in the theme page. */
    val swatchSize: Dp = 32.dp

    /** Border width of a color swatch. */
    val swatchBorderWidth: Dp = 1.dp

    /** Alpha applied to secondary (value) text. */
    const val SECONDARY_ALPHA: Float = 0.7f

    /** Alpha applied to the border of swatches and chips. */
    const val BORDER_ALPHA: Float = 0.5f

    /** Placeholder shown when a value is not available. */
    const val EMPTY_VALUE: String = "-"
}
