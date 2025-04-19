package org.mjdev.desktop.components.greeter

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpOffset
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.MutableStateExt.rememberState
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.LaunchedEffect.runAsync
import org.mjdev.desktop.interfaces.IUser
import org.mjdev.desktop.windows.ChromeWindow

// todo : all users
@Suppress("FunctionName", "unused")
@Composable
fun GreeterWindow() = withDesktopContext {
    val user: IUser? by rememberState(context.currentUser)
    val isUserLoggedIn by rememberState(user?.isLoggedIn)
    val onLogin: (user:IUser, password: String) -> Unit = { u, p ->
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
        size = context.containerSize
    ) {
        Greeter(
            onLogin = onLogin
        )
    }
}

@Preview
@Composable
fun GreeterWindowPreview() = preview {
    GreeterWindow()
}
