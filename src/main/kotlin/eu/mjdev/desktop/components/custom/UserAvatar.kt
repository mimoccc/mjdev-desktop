package eu.mjdev.desktop.components.custom

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
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.appbar.AppBar
import eu.mjdev.desktop.components.button.TransparentButton
import eu.mjdev.desktop.components.image.ImageAny
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import eu.mjdev.desktop.extensions.Modifier.circleBorder
import eu.mjdev.desktop.extensions.Modifier.clipCircle
import eu.mjdev.desktop.helpers.compose.Orientation
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Suppress("FunctionName")
@Composable
fun UserAvatar(
    modifier: Modifier = Modifier,
    avatarSize: Dp = 64.dp,
    orientation: Orientation = Orientation.Vertical,
    onUserAvatarClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) = withDesktopScope {
    val content: @Composable (orientation: Orientation) -> Unit = {
        AppBar(
            icon = {
                TransparentButton(
                    modifier = Modifier
                        .size(avatarSize)
                        .clipCircle(),
                    onClick = onUserAvatarClick,
                ) {
                    ImageAny(
                        modifier = Modifier
                            .size(avatarSize)
                            .circleBorder(2.dp, iconsTintColor),
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
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = if (orientation == Orientation.Vertical) Alignment.CenterHorizontally
                    else Alignment.Start
                ) {
                    TextAny(
                        text = api.currentUser.userName,
                        color = textColor,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Bold
                    )
                    TextAny(
                        text = api.machineName,
                        color = textColor,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Medium
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
                content = { content(orientation) }
            )

            Orientation.Horizontal -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.Start,
                content = { content(orientation) }
            )
        }
    }
}

@Preview
@Composable
fun UserAvatarPreview() {
    Column {
        UserAvatar(
            modifier = Modifier.fillMaxWidth()
                .background(Color.SuperDarkGray),
            orientation = Orientation.Vertical
        )
        UserAvatar(
            modifier = Modifier.fillMaxWidth()
                .background(Color.SuperDarkGray),
            orientation = Orientation.Horizontal
        )
    }
}