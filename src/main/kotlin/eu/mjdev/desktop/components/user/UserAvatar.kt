/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.user

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.components.appbar.AppBar
import eu.mjdev.desktop.components.button.TransparentButton
import eu.mjdev.desktop.components.image.ImageAny
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.extensions.Modifier.circleBorder
import eu.mjdev.desktop.extensions.Modifier.clipCircle
import eu.mjdev.desktop.extensions.Modifier.conditional
import eu.mjdev.desktop.helpers.compose.Orientation
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

// todo move back orientation for greeter
@Suppress("FunctionName")
@Composable
fun UserAvatar(
    modifier: Modifier = Modifier,
    avatarSize: Dp = 64.dp,
    titleTextSize: TextUnit = 16.sp,
    detailTextSize: TextUnit = 14.sp,
    cicleBorder: Dp = 4.dp,
    iconVerticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    titleVerticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    actionsVerticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    iconPadding: PaddingValues = PaddingValues(),
    titlePadding: PaddingValues = PaddingValues(),
    actionsPadding: PaddingValues = PaddingValues(),
    orientation: Orientation = Orientation.Horizontal,
    textAlign: TextAlign = TextAlign.Start,
    onUserAvatarClick: () -> Unit = {},
    actions: @Composable() (RowScope.() -> Unit) = {},
) = withDesktopScope {
    val content: @Composable () -> Unit = {
        AppBar(
            iconVerticalAlignment = iconVerticalAlignment,
            titleVerticalAlignment = titleVerticalAlignment,
            actionsVerticalAlignment = actionsVerticalAlignment,
            titleHorizontalArrangement = if (orientation == Orientation.Horizontal) Arrangement.Start
            else Arrangement.Center,
            iconHorizontalArrangement = if (orientation == Orientation.Horizontal) Arrangement.Start
            else Arrangement.Center,
            actionsHorizontalArrangement = if (orientation == Orientation.Horizontal) Arrangement.Start
            else Arrangement.Center,
            orientation = orientation,
            contentAlignment = Alignment.Bottom,
            iconPadding = iconPadding,
            titlePadding = titlePadding,
            actionsPadding = actionsPadding,
            fillCenter = orientation == Orientation.Horizontal,
            icon = {
                TransparentButton(
                    modifier = Modifier
                        .size(avatarSize + cicleBorder)
                        .clipCircle(),
                    onClick = onUserAvatarClick,
                ) {
                    ImageAny(
                        modifier = Modifier
                            .size(avatarSize)
                            .circleBorder(cicleBorder, textColor.alpha(0.5f)),
                        src = api.currentUser.picture,
                        colorFilter = if (api.currentUser.picture is ImageVector) ColorFilter.tint(iconsTintColor)
                        else null,
                        contentDescription = ""
                    )
                }
            },
            title = {
                Column(
                    modifier = Modifier.padding(start = 8.dp)
                        .conditional(orientation == Orientation.Horizontal) {
                            fillMaxWidth()
                        }
                        .conditional(orientation == Orientation.Vertical) {
                            wrapContentSize()
                        }
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    TextAny(
                        modifier = Modifier.fillMaxWidth(),
                        text = api.currentUser.userName,
                        color = textColor,
                        textAlign = textAlign,
                        fontWeight = FontWeight.Bold,
                        fontSize = titleTextSize
                    )
                    TextAny(
                        modifier = Modifier.fillMaxWidth(),
                        text = api.machineName,
                        color = textColor,
                        textAlign = textAlign,
                        fontWeight = FontWeight.Medium,
                        fontSize = detailTextSize
                    )
                }
            },
            actions = actions
        )
    }
    Box(
        modifier = modifier
            .wrapContentWidth()
            .padding(16.dp)
    ) {
        when (orientation) {
            Orientation.Vertical -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = { content() }
            )

            Orientation.Horizontal -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.Center,
                content = { content() }
            )
        }
    }
}

@Preview
@Composable
fun UserAvatarHorizontalFillPreview() = preview {
    UserAvatar(
        modifier = Modifier.fillMaxWidth()
            .wrapContentHeight()
            .background(Color.SuperDarkGray),
        orientation = Orientation.Horizontal,
    )
}

@Preview
@Composable
fun UserAvatarVerticalPreview() = preview {
    UserAvatar(
        modifier = Modifier.fillMaxWidth()
            .wrapContentHeight()
            .background(Color.SuperDarkGray),
        orientation = Orientation.Vertical,
        textAlign = TextAlign.Center,
    )
}