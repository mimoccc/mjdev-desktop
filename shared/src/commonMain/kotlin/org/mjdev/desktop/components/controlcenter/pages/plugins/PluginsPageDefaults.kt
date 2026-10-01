package org.mjdev.desktop.components.controlcenter.pages.plugins

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Constants used by [PluginsSettingsPage] and [PluginRow]. */
object PluginsPageDefaults {
    /** Size of the plugin image. */
    val iconSize: Dp = 40.dp

    /** Padding inside a plugin row. */
    val rowPadding: Dp = 8.dp

    /** Gap between the elements of a plugin row. */
    val rowSpacing: Dp = 12.dp

    /** Corner shape of a plugin row and its image. */
    val rowShape = RoundedCornerShape(10.dp)

    /** Image shown for plugins that ship none. */
    val fallbackIcon: ImageVector = Icons.Filled.Build

    /** Alpha of secondary text and the unchecked switch. */
    const val SECONDARY_ALPHA: Float = 0.7f

    /** Alpha of the checked switch track. */
    const val TRACK_ALPHA: Float = 0.4f

    const val SECTION_TITLE = "Desktop widgets"
    const val FOLDER_TITLE = "Plugins folder"
    const val EMPTY_HINT = "Drop plugin .jar files into the folder above, then reopen this tab."
    const val NO_DESCRIPTION = "No description."
    const val LABEL_VERSION = "Version"
    const val LABEL_AUTHOR = "Author"
    const val LABEL_ID = "Id"
    const val LABEL_PATH = "Path"
    const val LABEL_SOURCE = "Source"
    const val LABEL_ERROR = "Error"
}
