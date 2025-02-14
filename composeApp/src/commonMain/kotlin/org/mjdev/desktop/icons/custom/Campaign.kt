package org.mjdev.desktop.icons.custom

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Campaign: ImageVector
    get() {
        if (_campaign != null) {
            return _campaign!!
        }
        _campaign = materialIcon(name = "Filled.Campaign") {
            materialPath {
                moveTo(18.0f, 11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-4.0f)
                close()
                moveTo(16.0f, 17.61f)
                curveToRelative(0.96f, 0.71f, 2.21f, 1.65f, 3.2f, 2.39f)
                curveToRelative(0.4f, -0.53f, 0.8f, -1.07f, 1.2f, -1.6f)
                curveToRelative(-0.99f, -0.74f, -2.24f, -1.68f, -3.2f, -2.4f)
                curveToRelative(-0.4f, 0.54f, -0.8f, 1.08f, -1.2f, 1.61f)
                close()
                moveTo(20.4f, 5.6f)
                curveToRelative(-0.4f, -0.53f, -0.8f, -1.07f, -1.2f, -1.6f)
                curveToRelative(-0.99f, 0.74f, -2.24f, 1.68f, -3.2f, 2.4f)
                curveToRelative(0.4f, 0.53f, 0.8f, 1.07f, 1.2f, 1.6f)
                curveToRelative(0.96f, -0.72f, 2.21f, -1.65f, 3.2f, -2.4f)
                close()
                moveTo(4.0f, 9.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(1.0f)
                lineToRelative(5.0f, 3.0f)
                lineTo(13.0f, 6.0f)
                lineTo(8.0f, 9.0f)
                lineTo(4.0f, 9.0f)
                close()
                moveTo(15.5f, 12.0f)
                curveToRelative(0.0f, -1.33f, -0.58f, -2.53f, -1.5f, -3.35f)
                verticalLineToRelative(6.69f)
                curveToRelative(0.92f, -0.81f, 1.5f, -2.01f, 1.5f, -3.34f)
                close()
            }
        }
        return _campaign!!
    }

private var _campaign: ImageVector? = null