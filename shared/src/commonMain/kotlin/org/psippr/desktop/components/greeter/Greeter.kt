package org.psippr.desktop.components.greeter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.psippr.desktop.components.input.PasswordTextView
import org.psippr.desktop.components.user.UserAvatar
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Colors.alpha
import org.psippr.desktop.extensions.Compose.isDesign
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.extensions.MutableStateExt.rememberState
import org.psippr.desktop.helpers.compose.Orientation
import org.psippr.desktop.interfaces.IUser

// old signature: always the current user, no room for status or the desktop switch
//fun Greeter(onLogin: (user: IUser, password: String) -> Unit = { _, _ -> }) = withDesktopContext {
//    val user: IUser? by rememberState(context.currentUser)

/**
 * The psippr login screen: avatar of [loginUser] (default the current user), a click on it shows
 * the password field, [onLogin] gets the typed password. With several [users] the click opens
 * the user list first, [onUserSelect] gets the picked one. [message] shows the login state below,
 * [footer] holds extra controls (the desktop switch of the greetd login screen).
 */
@Composable
fun Greeter(
    onLogin: (user: IUser, password: String) -> Unit = { _, _ -> },
    loginUser: IUser? = null,
    users: List<IUser> = emptyList(),
    onUserSelect: (IUser) -> Unit = {},
    message: String = "",
    footer: @Composable () -> Unit = {},
) = withDesktopContext {
    val user: IUser? = loginUser ?: context.currentUser
    var passwordVisible by rememberState(isDesign)
    var usersExpanded by rememberState(false)
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
            Box {
                UserAvatar(
                    modifier = Modifier,
                    user = user,
                    avatarSize = 128.dp,
                    orientation = Orientation.Vertical,
                    textAlign = TextAlign.Center,
                    circleBorder = 4.dp,
                    // old: only showed the password field
//                    onUserAvatarClick = {
//                        passwordVisible = true
//                    },
                    onUserAvatarClick = {
                        if (users.size > 1) usersExpanded = true else passwordVisible = true
                    },
                )
                DropdownMenu(expanded = usersExpanded, onDismissRequest = { usersExpanded = false }) {
                    users.forEach { item ->
                        DropdownMenuItem(
                            onClick = {
                                usersExpanded = false
                                onUserSelect(item)
                                passwordVisible = true
                            },
                        ) {
                            Text(
                                text = item.userName,
                                fontWeight = if (item.userName == user?.userName) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
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
                    // old: not-null assertion on a delegated state
//                    onDone = { password ->
//                        if (user != null) {
//                            onLogin(user!!, password)
//                        }
//                    },
                    onDone = { password -> user?.let { u -> onLogin(u, password) } },
                )
            }
            if (message.isNotEmpty()) {
                Text(
                    modifier = Modifier.width(256.dp),
                    text = message,
                    color = textColor,
                    textAlign = TextAlign.Center,
                )
            }
            footer()
        }
    }
}

@Preview
@Composable
fun PreviewGreeter() = preview(320, 320) {
    Greeter()
}
