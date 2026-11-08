package org.psippr.desktop.components.greeter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.psippr.desktop.components.input.PasswordTextView
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Colors.alpha
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.interfaces.IUser

/** Width of the password field and the status line. */
private val FIELD_WIDTH = 256.dp

/** Vertical gap between the user row, the password field, the status and the footer. */
private val ITEM_GAP = 16.dp

/** Opacity of the screen background over the wallpaper. */
private const val SCREEN_ALPHA = 0.9f

/** Index step of the Left arrow key in the user row. */
private const val STEP_PREVIOUS = -1

/** Index step of the Right arrow key in the user row. */
private const val STEP_NEXT = 1

/** Keys that submit the typed password. */
private val SUBMIT_KEYS = setOf(Key.Enter, Key.NumPadEnter)

// old signature: always the current user, no room for status or the desktop switch
// fun Greeter(onLogin: (user: IUser, password: String) -> Unit = { _, _ -> }) = withDesktopContext {
//    val user: IUser? by rememberState(context.currentUser)

// old: one avatar, a click on it opened a text dropdown of the users and only then the password
// field - the users were not visible and the password field was hidden until a click
// @Composable
// fun Greeter(
//    onLogin: (user: IUser, password: String) -> Unit = { _, _ -> },
//    loginUser: IUser? = null,
//    users: List<IUser> = emptyList(),
//    onUserSelect: (IUser) -> Unit = {},
//    message: String = "",
//    footer: @Composable () -> Unit = {},
// ) = withDesktopContext {
//    val user: IUser? = loginUser ?: context.currentUser
//    var passwordVisible by rememberState(isDesign)
//    var usersExpanded by rememberState(false)
//    Box(
//        modifier = Modifier.fillMaxSize().background(backgroundColor.alpha(0.9f)),
//        contentAlignment = Alignment.Center,
//    ) {
//        Column(modifier = Modifier.width(256.dp)) {
//            Box {
//                UserAvatar(
//                    modifier = Modifier,
//                    user = user,
//                    avatarSize = 128.dp,
//                    orientation = Orientation.Vertical,
//                    textAlign = TextAlign.Center,
//                    circleBorder = 4.dp,
//                    onUserAvatarClick = {
//                        if (users.size > 1) usersExpanded = true else passwordVisible = true
//                    },
//                )
//                DropdownMenu(expanded = usersExpanded, onDismissRequest = { usersExpanded = false }) {
//                    users.forEach { item ->
//                        DropdownMenuItem(
//                            onClick = {
//                                usersExpanded = false
//                                onUserSelect(item)
//                                passwordVisible = true
//                            },
//                        ) {
//                            Text(
//                                text = item.userName,
//                                fontWeight = if (item.userName == user?.userName) FontWeight.Bold else FontWeight.Normal,
//                            )
//                        }
//                    }
//                }
//            }
//            if (passwordVisible) {
//                PasswordTextView(
//                    modifier = Modifier.width(256.dp),
//                    onDone = { password -> user?.let { u -> onLogin(u, password) } },
//                )
//            }
//            if (message.isNotEmpty()) {
//                Text(modifier = Modifier.width(256.dp), text = message, color = textColor, textAlign = TextAlign.Center)
//            }
//            footer()
//        }
//    }
// }

/**
 * The psippr login screen: every one of [users] as an avatar in a horizontal, scrollable row
 * (the current user when the list is empty) with [loginUser] selected, and the password field
 * always visible and focused below. A click on an avatar, or the Left / Right arrow keys while
 * the password field is empty, select another user and report it to [onUserSelect]; [onLogin]
 * gets the selected user and the typed password. [message] shows the login state below,
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
    val shownUsers = users.ifEmpty { listOf(loginUser ?: context.currentUser) }
    val selected = shownUsers.indexOfFirst { it.userName == loginUser?.userName }.coerceAtLeast(0)
    val password = remember { mutableStateOf("") }
    val passwordFocus = remember { FocusRequester() }
    val select: (Int) -> Unit = { index ->
        shownUsers.getOrNull(index)?.let { user ->
            password.value = ""
            onUserSelect(user)
        }
    }
    val submit: () -> Unit = {
        shownUsers.getOrNull(selected)?.let { u -> onLogin(u, password.value) }
    }
    LaunchedEffect(selected) {
        runCatching { passwordFocus.requestFocus() }
    }
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(backgroundColor.alpha(SCREEN_ALPHA))
                .onPreviewKeyEvent { event ->
                    // Enter submits: the password field itself only submits from its send icon
                    val submits = event.key in SUBMIT_KEYS && password.value.isNotEmpty()
                    if (submits) {
                        if (event.type == KeyEventType.KeyDown) submit()
                        return@onPreviewKeyEvent true
                    }
                    val step =
                        when (event.key) {
                            Key.DirectionLeft -> STEP_PREVIOUS
                            Key.DirectionRight -> STEP_NEXT
                            else -> null
                        }
                    // arrows move the caret once something is typed, so they switch users only
                    // while the password field is empty
                    val switches = step != null && password.value.isEmpty() && shownUsers.size > 1
                    if (switches && event.type == KeyEventType.KeyDown) {
                        select((selected + step).coerceIn(shownUsers.indices))
                    }
                    switches
                },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ITEM_GAP),
        ) {
            GreeterUserRow(
                users = shownUsers,
                selected = selected,
                onSelect = select,
            )
            PasswordTextView(
                modifier = Modifier.width(FIELD_WIDTH).focusRequester(passwordFocus),
                password = password,
                onDone = { submit() },
            )
            if (message.isNotEmpty()) {
                Text(
                    modifier = Modifier.width(FIELD_WIDTH),
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
