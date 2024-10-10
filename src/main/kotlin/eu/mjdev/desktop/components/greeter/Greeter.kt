package eu.mjdev.desktop.components.greeter

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.custom.UserAvatar
import eu.mjdev.desktop.components.input.PasswordTextView
import eu.mjdev.desktop.data.User
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import eu.mjdev.desktop.windows.ChromeWindow
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.helpers.compose.Orientation

// todo : all users
@Suppress("FunctionName", "unused")
@Composable
fun Greeter() = withDesktopScope {
    val user: User by rememberState(api.currentUser)
    var isUserLoggedIn by rememberState(user.isLoggedIn)
    var passwordVisible by rememberState(false)
    val onDone: (password: String) -> Unit = { password ->
        isUserLoggedIn = user.login(api, password)
    }
    ChromeWindow(
        position = WindowPosition.Aligned(Alignment.Center),
        visible = isUserLoggedIn.not(),
        enabled = true,
        transparent = true,
        resizable = false,
        alwaysOnTop = true,
        size = api.containerSize
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(backgroundColor.alpha(0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.width(256.dp)
            ) {
                UserAvatar(
                    modifier = Modifier,
                    avatarSize = 128.dp,
                    orientation = Orientation.Vertical,
                    onUserAvatarClick = {
                        passwordVisible = true
                    }
                )
                if (passwordVisible) {
                    PasswordTextView(
                        modifier = Modifier.width(256.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = iconsTintColor.copy(alpha = 0.1f),
                            textColor = iconsTintColor.copy(alpha = 0.9f),
                            cursorColor = iconsTintColor,
                            focusedBorderColor = iconsTintColor,
                            unfocusedBorderColor = iconsTintColor
                        ),
                        onDone = onDone
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun GreeterPreview() = Greeter()
