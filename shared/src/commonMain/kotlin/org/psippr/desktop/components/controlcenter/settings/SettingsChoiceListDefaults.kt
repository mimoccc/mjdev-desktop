package org.psippr.desktop.components.controlcenter.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing / alpha constants of [SettingsChoiceList]. */
object SettingsChoiceListDefaults {
    /** Vertical padding of the whole list. */
    val listPadding: Dp = 6.dp

    /** Corner radius of one option row. */
    val rowCorner: Dp = 8.dp

    /** Inner horizontal padding of one option row. */
    val rowPadding: Dp = 4.dp

    /** Gap between option rows. */
    val rowSpacing: Dp = 2.dp

    /** Background alpha of the selected row. */
    const val SELECTED_ALPHA: Float = 0.12f

    /** Max lines of an option label. */
    const val LABEL_MAX_LINES: Int = 2
}
