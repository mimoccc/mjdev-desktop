package org.mjdev.desktop.icons.settings

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val SettingsEthernet: ImageVector
    get() {
        if (_settingsEthernet != null) {
            return _settingsEthernet!!
        }
        _settingsEthernet =
            materialIcon(name = "Filled.SettingsEthernet") {
                materialPath {
                    moveTo(7.77f, 6.76f)
                    lineTo(6.23f, 5.48f)
                    lineTo(0.82f, 12.0f)
                    lineToRelative(5.41f, 6.52f)
                    lineToRelative(1.54f, -1.28f)
                    lineTo(3.42f, 12.0f)
                    lineToRelative(4.35f, -5.24f)
                    close()
                    moveTo(7.0f, 13.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineToRelative(-2.0f)
                    lineTo(7.0f, 11.0f)
                    verticalLineToRelative(2.0f)
                    close()
                    moveTo(17.0f, 11.0f)
                    horizontalLineToRelative(-2.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineToRelative(-2.0f)
                    close()
                    moveTo(11.0f, 13.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineToRelative(-2.0f)
                    horizontalLineToRelative(-2.0f)
                    verticalLineToRelative(2.0f)
                    close()
                    moveTo(17.77f, 5.48f)
                    lineToRelative(-1.54f, 1.28f)
                    lineTo(20.58f, 12.0f)
                    lineToRelative(-4.35f, 5.24f)
                    lineToRelative(1.54f, 1.28f)
                    lineTo(23.18f, 12.0f)
                    lineToRelative(-5.41f, -6.52f)
                    close()
                }
            }
        return _settingsEthernet!!
    }

private var _settingsEthernet: ImageVector? = null
