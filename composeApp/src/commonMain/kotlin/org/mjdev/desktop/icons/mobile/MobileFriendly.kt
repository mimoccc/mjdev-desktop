package org.mjdev.desktop.icons.mobile

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MobileFriendly: ImageVector
    get() {
        if (_mobileFriendly != null) {
            return _mobileFriendly!!
        }
        _mobileFriendly = materialIcon(name = "Filled.MobileFriendly") {
            materialPath {
                moveTo(19.0f, 1.0f)
                horizontalLineTo(9.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(16.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(3.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(7.01f, 13.47f)
                lineToRelative(-2.55f, -2.55f)
                lineToRelative(-1.27f, 1.27f)
                lineTo(7.0f, 16.0f)
                lineToRelative(7.19f, -7.19f)
                lineToRelative(-1.27f, -1.27f)
                close()
            }
        }
        return _mobileFriendly!!
    }

private var _mobileFriendly: ImageVector? = null
