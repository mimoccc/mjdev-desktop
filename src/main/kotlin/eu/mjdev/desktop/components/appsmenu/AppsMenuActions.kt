/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.appsmenu

import androidx.compose.desktop.ui.tooling.preview.Preview
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
import eu.mjdev.desktop.components.icon.ShapedIcon
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.extensions.Compose.runAsync
import eu.mjdev.desktop.extensions.Modifier.circleBorder
import eu.mjdev.desktop.icons.Icons
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import kotlinx.coroutines.Dispatchers

@Composable
fun AppsMenuActions(
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
            modifier = Modifier.circleBorder(2.dp, textColor.alpha(0.5f)),
            imageVector = iconRestart,
            iconColor = borderColor,
            iconBackgroundColor = iconsTintColor,
            onRightClick = {},
            onClick = {
                onActionClick()
                // todo : dialog
                runAsync(Dispatchers.IO) {
                    api.restart()
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
                runAsync(Dispatchers.IO) {
                    api.suspend()
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
                runAsync(Dispatchers.IO) {
                    api.logOut()
                }
            }
        )
    }
}

@Preview
@Composable
fun AppsMenuActionsPreview() = preview {
    AppsMenuActions(
        modifier = Modifier.background(Color.SuperDarkGray, RoundedCornerShape(16.dp)).padding(8.dp),
    )
}
