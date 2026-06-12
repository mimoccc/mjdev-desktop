package org.mjdev.desktop.icons.text

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val TextSnippet: ImageVector
    get() {
        if (_textSnippet != null) {
            return _textSnippet!!
        }
        _textSnippet = materialIcon(name = "AutoMirrored.Filled.TextSnippet", autoMirror = true) {
            materialPath {
                moveTo(20.41f, 8.41f)
                lineToRelative(-4.83f, -4.83f)
                curveTo(15.21f, 3.21f, 14.7f, 3.0f, 14.17f, 3.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(9.83f)
                curveTo(21.0f, 9.3f, 20.79f, 8.79f, 20.41f, 8.41f)
                close()
                moveTo(7.0f, 7.0f)
                horizontalLineToRelative(7.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(7.0f)
                verticalLineTo(7.0f)
                close()
                moveTo(17.0f, 17.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(10.0f)
                verticalLineTo(17.0f)
                close()
                moveTo(17.0f, 13.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(10.0f)
                verticalLineTo(13.0f)
                close()
            }
        }
        return _textSnippet!!
    }

private var _textSnippet: ImageVector? = null
