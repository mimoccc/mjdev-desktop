package org.psippr.desktop.components.greeter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.extensions.MutableStateExt.rememberState
import org.psippr.desktop.interfaces.IUser
import org.psippr.desktop.windows.ChromeWindow

// todo : all users
@Suppress("FunctionName", "unused")
@Composable
fun GreeterWindow() = withDesktopContext {
    val user: IUser? by rememberState(context.currentUser)
    val isUserLoggedIn by rememberState(user?.isLoggedIn)
    val onLogin: (user: IUser, password: String) -> Unit = { u, p ->
        runAsync {
//            context.currentUser.login( password).collectLatest { logState ->
//                isUserLoggedIn = logState
//            }
        }
    }
    ChromeWindow(
        position = DpOffset.Zero,
        visible = (isUserLoggedIn ?: false).not(),
        enabled = true,
        transparent = true,
        resizable = false,
        alwaysOnTop = true,
        size = context.containerSize,
    ) {
        Greeter(
            onLogin = onLogin,
        )
    }
}

@Preview
@Composable
fun PreviewGreeterWindow() = preview {
    GreeterWindow()
}
