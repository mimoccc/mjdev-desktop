package org.mjdev.desktop.components.greeter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.input.PasswordTextView
import org.mjdev.desktop.components.user.UserAvatar
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.isDesign
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.MutableStateExt.rememberState
import org.mjdev.desktop.helpers.compose.Orientation
import org.mjdev.desktop.interfaces.IUser

@Composable
fun Greeter(onLogin: (user: IUser, password: String) -> Unit = { _, _ -> }) =
    withDesktopContext {
        val user: IUser? by rememberState(context.currentUser)
        var passwordVisible by rememberState(isDesign)
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(backgroundColor.alpha(0.9f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.width(256.dp),
            ) {
                UserAvatar(
                    modifier = Modifier,
                    avatarSize = 128.dp,
                    orientation = Orientation.Vertical,
                    textAlign = TextAlign.Center,
                    circleBorder = 4.dp,
                    onUserAvatarClick = {
                        passwordVisible = true
                    },
                )
                if (passwordVisible) {
                    PasswordTextView(
                        modifier = Modifier.width(256.dp),
//                        colors = TextFieldDefaults.outlinedTextFieldColors(
//                            backgroundColor = iconsTintColor.copy(alpha = 0.1f),
//                            textColor = iconsTintColor.copy(alpha = 0.9f),
//                            cursorColor = iconsTintColor,
//                            focusedBorderColor = iconsTintColor,
//                            unfocusedBorderColor = iconsTintColor
//                        ),
                        onDone = { password ->
                            if (user != null) {
                                onLogin(user!!, password)
                            }
                        },
                    )
                }
            }
        }
    }

@Preview
@Composable
fun PreviewGreeter() =
    preview(320, 320) {
        Greeter()
    }
