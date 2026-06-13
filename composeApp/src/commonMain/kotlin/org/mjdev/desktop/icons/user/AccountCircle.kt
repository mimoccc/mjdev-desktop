package org.mjdev.desktop.icons.user

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val AccountCircle: ImageVector
    get() {
        if (_accountCircle != null) {
            return _accountCircle!!
        }
        _accountCircle =
            materialIcon(name = "Filled.AccountCircle") {
                materialPath {
                    moveTo(12.0f, 2.0f)
                    curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                    reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                    reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                    reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                    close()
                    moveTo(12.0f, 6.0f)
                    curveToRelative(1.93f, 0.0f, 3.5f, 1.57f, 3.5f, 3.5f)
                    reflectiveCurveTo(13.93f, 13.0f, 12.0f, 13.0f)
                    reflectiveCurveToRelative(-3.5f, -1.57f, -3.5f, -3.5f)
                    reflectiveCurveTo(10.07f, 6.0f, 12.0f, 6.0f)
                    close()
                    moveTo(12.0f, 20.0f)
                    curveToRelative(-2.03f, 0.0f, -4.43f, -0.82f, -6.14f, -2.88f)
                    curveTo(7.55f, 15.8f, 9.68f, 15.0f, 12.0f, 15.0f)
                    reflectiveCurveToRelative(4.45f, 0.8f, 6.14f, 2.12f)
                    curveTo(16.43f, 19.18f, 14.03f, 20.0f, 12.0f, 20.0f)
                    close()
                }
            }
        return _accountCircle!!
    }

private var _accountCircle: ImageVector? = null
