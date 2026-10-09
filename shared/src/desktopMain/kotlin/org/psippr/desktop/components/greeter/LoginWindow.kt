package org.psippr.desktop.components.greeter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.psippr.desktop.helpers.greetd.GreetdLogin
import org.psippr.desktop.helpers.greetd.LastLoginStore
import org.psippr.desktop.helpers.greetd.LoginSessions
import org.psippr.desktop.helpers.greetd.LoginUsers
import org.psippr.desktop.windows.FullScreenWindow

/**
 * Full screen window of the greetd greeter (`psippr::LoginWindow`, a top layer surface under
 * psipprc). [onLoggedIn] must end the process: greetd starts the user session after that.
 */
@Composable
fun LoginWindow(onLoggedIn: () -> Unit) {
    val users = remember { LoginUsers.names() }
    val sessions = remember { LoginSessions.all() }
    val store = remember { LastLoginStore() }
    FullScreenWindow(name = WINDOW_NAME) {
        LoginScreen(
            users = users,
            sessions = sessions,
            initialUser = store.lastUser,
            initialSession = { user -> sessions.firstOrNull { it.name == store.lastSession(user) } ?: sessions.first() },
            login = GreetdLogin::login,
            onLoggedIn = { user, session ->
                store.remember(user, session.name)
                onLoggedIn()
            },
        )
    }
}

/** Window role name, turned into the `psippr::<name>` title the compositor layers by. */
private const val WINDOW_NAME = "LoginWindow"
