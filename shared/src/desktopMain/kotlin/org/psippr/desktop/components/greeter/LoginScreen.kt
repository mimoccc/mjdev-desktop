package org.psippr.desktop.components.greeter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.psippr.desktop.components.input.PasswordTextView
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Colors.alpha
import org.psippr.desktop.helpers.greetd.LoginSession

/** Width of the login column. */
private val COLUMN_WIDTH = 256.dp

/** Gap between the login column items. */
private val ITEM_GAP = 12.dp

/** Padding inside a user name chip. */
private val CHIP_PADDING = 8.dp

/** Size of the user name text. */
private val NAME_SIZE = 20.sp

/** Background opacity of the screen and of the selected user chip. */
private const val SCREEN_ALPHA = 0.9f
private const val SELECTED_ALPHA = 0.2f

/**
 * Login screen of the greetd greeter: pick a user and a desktop, type the password; [login] runs
 * the greetd dialog off the UI thread and [onLoggedIn] ends the greeter so greetd starts the
 * session. [initialUser] and [initialSession] preselect the last login.
 */
@Composable
fun LoginScreen(
    users: List<String>,
    sessions: List<LoginSession>,
    initialUser: String?,
    initialSession: (user: String) -> LoginSession,
    login: (user: String, password: String, session: LoginSession) -> Result<Unit>,
    onLoggedIn: (user: String, session: LoginSession) -> Unit,
) = withDesktopContext {
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf(initialUser?.takeIf { it in users } ?: users.firstOrNull()) }
    var session by remember(selected) { mutableStateOf(initialSession(selected.orEmpty())) }
    var message by remember { mutableStateOf(if (users.isEmpty()) LoginLabels.NoUsers.text else "") }
    var busy by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize().background(backgroundColor.alpha(SCREEN_ALPHA)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.width(COLUMN_WIDTH),
            verticalArrangement = Arrangement.spacedBy(ITEM_GAP),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            users.forEach { user ->
                Text(
                    text = user,
                    color = textColor,
                    fontSize = NAME_SIZE,
                    fontWeight = if (user == selected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .background(
                                if (user == selected) textColor.alpha(SELECTED_ALPHA) else backgroundColor.alpha(0f),
                                RoundedCornerShape(CHIP_PADDING),
                            ).clickable { selected = user }
                            .padding(CHIP_PADDING),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                SessionSwitch(sessions = sessions, selected = session, onSelect = { picked -> session = picked })
                Text(text = session.name, color = textColor)
            }
            PasswordTextView(
                modifier = Modifier.width(COLUMN_WIDTH),
                onDone = { password ->
                    val user = selected
                    if (user != null && !busy) {
                        busy = true
                        message = LoginLabels.CheckingPassword.text
                        val chosen = session
                        scope.launch {
                            val result = withContext(Dispatchers.IO) { login(user, password, chosen) }
                            busy = false
                            result
                                .onSuccess { onLoggedIn(user, chosen) }
                                .onFailure { e -> message = LoginLabels.LoginFailed.text + e.message.orEmpty() }
                        }
                    }
                },
            )
            Text(text = message, color = textColor, textAlign = TextAlign.Center)
        }
    }
}
