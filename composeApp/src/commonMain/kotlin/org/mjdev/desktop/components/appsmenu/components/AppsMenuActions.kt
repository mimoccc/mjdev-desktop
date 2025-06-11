/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.appsmenu.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.icon.ShapedIcon
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.SuperDarkGray
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.Modifier.circleBorder
import org.mjdev.desktop.icons.system.Logout
import org.mjdev.desktop.icons.system.PowerOff
import org.mjdev.desktop.icons.system.RestartAlt
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("UNUSED_PARAMETER")
@Composable
fun AppsMenuActions(
    // todo theme
    iconRestart: ImageVector = RestartAlt,
    // todo theme
    iconPowerOff: ImageVector = PowerOff,
    // todo theme
    iconLogout: ImageVector = Logout,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = {},
    onTooltip: (item: Any?) -> Unit = {}
) = withDesktopContext {
    Row(
        modifier = modifier.wrapContentWidth()
    ) {
        ShapedIcon(
            modifier = Modifier.circleBorder(2.dp, textColor.alpha(0.5f)),
            imageVector = iconRestart,
            iconColor = borderColor,
            iconBackgroundColor = iconsTintColor,
            onRightClick = {},
            onClick = {
                onActionClick()
                // todo : dialog
                runAsync {
                    context.restart()
                }
            }
        )
        ShapedIcon(
            modifier = Modifier.circleBorder(2.dp, textColor.alpha(0.5f)),
            imageVector = iconPowerOff,
            iconColor = borderColor,
            iconBackgroundColor = iconsTintColor,
            onRightClick = {},
            onClick = {
                onActionClick()
                // todo : dialog
                runAsync {
                    context.suspend()
                }
            }
        )
        ShapedIcon(
            modifier = Modifier.circleBorder(2.dp, textColor.alpha(0.5f)),
            imageVector = iconLogout,
            iconColor = borderColor,
            iconBackgroundColor = iconsTintColor,
            onRightClick = {},
            onClick = {
                onActionClick()
                // todo : dialog
                runAsync {
                    context.logOut()
                }
            }
        )
    }
}

@Preview
@Composable
fun PreviewAppsMenuActions() = preview {
    AppsMenuActions(
        modifier = Modifier
            .background(Color.SuperDarkGray, RoundedCornerShape(16.dp))
            .padding(8.dp),
    )
}
