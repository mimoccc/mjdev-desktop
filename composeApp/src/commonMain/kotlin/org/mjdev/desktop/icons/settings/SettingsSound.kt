package org.mjdev.desktop.icons.settings

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val SettingsSound: ImageVector
    get() {
        if (_headphones != null) {
            return _headphones!!
        }
        _headphones =
            materialIcon(name = "Filled.Headphones") {
                materialPath {
                    moveTo(12.0f, 3.0f)
                    curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
                    verticalLineToRelative(7.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(4.0f)
                    verticalLineToRelative(-8.0f)
                    horizontalLineTo(5.0f)
                    verticalLineToRelative(-1.0f)
                    curveToRelative(0.0f, -3.87f, 3.13f, -7.0f, 7.0f, -7.0f)
                    reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
                    verticalLineToRelative(1.0f)
                    horizontalLineToRelative(-4.0f)
                    verticalLineToRelative(8.0f)
                    horizontalLineToRelative(4.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineToRelative(-7.0f)
                    curveTo(21.0f, 7.03f, 16.97f, 3.0f, 12.0f, 3.0f)
                    close()
                }
            }
        return _headphones!!
    }

private var _headphones: ImageVector? = null
