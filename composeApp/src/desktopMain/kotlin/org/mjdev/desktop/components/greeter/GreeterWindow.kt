package org.mjdev.desktop.components.greeter

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpOffset
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.interfaces.IUser
import org.mjdev.desktop.windows.ChromeWindow

// todo : all users
@Suppress("FunctionName", "unused")
@Composable
fun GreeterWindow() = withDesktopContext {
    val authenticated = context.authenticatedState.value
    val onLogin: (user: IUser, password: String) -> Unit = { u, p ->
        runAsync {
            context.authenticate(u, p)
        }
    }
    ChromeWindow(
        name = "Greeter",
        position = DpOffset.Zero,
        // greeter acts as the session lock screen until the password
        // is verified; debug runs skip it to not block development
        visible = !authenticated && !isDebug,
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
fun PreviewGreeterWindow() = preview {
    GreeterWindow()
}
