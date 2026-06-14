package org.mjdev.desktop.icons.settings

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val SettingsMonitor: ImageVector
    get() {
        if (_screenshotMonitor != null) {
            return _screenshotMonitor!!
        }
        _screenshotMonitor =
            materialIcon(name = "Filled.ScreenshotMonitor") {
                materialPath {
                    moveTo(20.0f, 3.0f)
                    horizontalLineTo(4.0f)
                    curveTo(2.89f, 3.0f, 2.0f, 3.89f, 2.0f, 5.0f)
                    verticalLineToRelative(12.0f)
                    curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(4.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineToRelative(8.0f)
                    verticalLineToRelative(-2.0f)
                    horizontalLineToRelative(4.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(5.0f)
                    curveTo(22.0f, 3.89f, 21.1f, 3.0f, 20.0f, 3.0f)
                    close()
                    moveTo(20.0f, 17.0f)
                    horizontalLineTo(4.0f)
                    verticalLineTo(5.0f)
                    horizontalLineToRelative(16.0f)
                    verticalLineTo(17.0f)
                    close()
                }
                materialPath {
                    moveTo(6.5f, 7.5f)
                    lineToRelative(2.5f, 0.0f)
                    lineToRelative(0.0f, -1.5f)
                    lineToRelative(-4.0f, 0.0f)
                    lineToRelative(0.0f, 4.0f)
                    lineToRelative(1.5f, 0.0f)
                    close()
                }
                materialPath {
                    moveTo(19.0f, 12.0f)
                    lineToRelative(-1.5f, 0.0f)
                    lineToRelative(0.0f, 2.5f)
                    lineToRelative(-2.5f, 0.0f)
                    lineToRelative(0.0f, 1.5f)
                    lineToRelative(4.0f, 0.0f)
                    close()
                }
            }
        return _screenshotMonitor!!
    }

private var _screenshotMonitor: ImageVector? = null
