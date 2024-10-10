/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.appsmenu

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import eu.mjdev.desktop.components.icon.ShapedIcon
import eu.mjdev.desktop.icons.Icons
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Composable
fun AppActions(
    // todo theme
    iconRestart: ImageVector = Icons.RestartComputer,
    // todo theme
    iconPowerOff: ImageVector = Icons.PowerOffComputer,
    // todo theme
    iconLogout: ImageVector = Icons.LogOutUser,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = {},
) = withDesktopScope {
    Row(
        modifier = modifier.wrapContentWidth()
    ) {
        ShapedIcon(
            imageVector = iconRestart,
            iconColor = borderColor,
            iconBackgroundColor = iconsTintColor,
            onRightClick = {},
            onClick = {
                onActionClick()
                // todo : dialog
                api.restart()
            }
        )
        ShapedIcon(
            imageVector = iconPowerOff,
            iconColor = borderColor,
            iconBackgroundColor = iconsTintColor,
            onRightClick = {},
            onClick = {
                onActionClick()
                // todo : dialog
                api.suspend()
            }
        )
        ShapedIcon(
            imageVector = iconLogout,
            iconColor = borderColor,
            iconBackgroundColor = iconsTintColor,
            onRightClick = {},
            onClick = {
                onActionClick()
                // todo : dialog
                api.logOut()
            }
        )
    }
}

@Preview
@Composable
fun AppActionsPreview() = AppActions()
