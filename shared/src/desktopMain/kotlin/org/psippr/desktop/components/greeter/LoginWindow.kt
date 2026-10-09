package org.psippr.desktop.components.greeter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.psippr.desktop.helpers.greetd.GreetdLogin
import org.psippr.desktop.helpers.greetd.LastLoginStore
import org.psippr.desktop.helpers.greetd.LoginSessions
import org.psippr.desktop.helpers.greetd.LoginUsers
import org.psippr.desktop.interfaces.IUser
import org.psippr.desktop.windows.FullScreenWindow

/**
 * Full screen window of the greetd greeter (`psippr::LoginWindow`, a top layer surface under
 * psipprc), showing the psippr [Greeter] for the picked account with the user and desktop
 * switches below it. [onLoggedIn] must end the process: greetd starts the session after that.
 */
@Composable
fun LoginWindow(onLoggedIn: () -> Unit) {
    val users = remember { LoginUsers.users() }
    val sessions = remember { LoginSessions.all() }
    val store = remember { LastLoginStore() }
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf(users.firstOrNull { it.userName == store.lastUser } ?: users.firstOrNull()) }
    var session by remember(user) {
        mutableStateOf(
            sessions.firstOrNull { it.name == store.lastSession(user?.userName.orEmpty()) } ?: sessions.first(),
        )
    }
    var message by remember { mutableStateOf(if (users.isEmpty()) LoginLabels.NoUsers.text else "") }
    var busy by remember { mutableStateOf(false) }
    val login: (IUser, String) -> Unit = { u, password ->
        if (!busy) {
            busy = true
            message = LoginLabels.CheckingPassword.text
            val chosen = session
            scope.launch {
                val result = withContext(Dispatchers.IO) { GreetdLogin.login(u.userName, password, chosen) }
                busy = false
                result
                    .onSuccess {
                        store.remember(u.userName, chosen.name)
                        onLoggedIn()
                    }.onFailure { e -> message = LoginLabels.LoginFailed.text + e.message.orEmpty() }
            }
        }
    }
    // old: the desktop switch sat in the footer under the password field
//    FullScreenWindow(name = WINDOW_NAME) {
//        Greeter(
//            onLogin = login,
//            loginUser = user,
//            users = users,
//            onUserSelect = { picked -> user = picked },
//            message = message,
//            footer = {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically,
//                ) {
//                    // old: separate user switch icon, the shown user is clicked instead now
////                    user?.takeIf { users.size > 1 }?.let { selected ->
////                        UserSwitch(users = users, selected = selected, onSelect = { picked -> user = picked })
////                    }
//                    SessionSwitch(sessions = sessions, selected = session, onSelect = { picked -> session = picked })
//                }
//            },
//        )
//    }
    FullScreenWindow(name = WINDOW_NAME) {
        Box(modifier = Modifier.fillMaxSize()) {
            Greeter(
                onLogin = login,
                loginUser = user,
                users = users,
                onUserSelect = { picked -> user = picked },
                message = message,
            )
            // the desktop switch in the bottom right corner of the screen
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(SESSION_SWITCH_MARGIN),
            ) {
                SessionSwitch(sessions = sessions, selected = session, onSelect = { picked -> session = picked })
            }
        }
    }
}

/** Distance of the desktop switch from the bottom right corner of the screen. */
private val SESSION_SWITCH_MARGIN = 24.dp

private const val WINDOW_NAME = "LoginWindow"
