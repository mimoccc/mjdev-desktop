package org.psippr.desktop.components.greeter

import androidx.compose.foundation.layout.Box
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.helpers.greetd.LoginSession

/**
 * Desktop switch of the login screen: an icon with the preselected desktop's name, a click opens
 * the menu of the installed desktops (shown even when psippr is the only one).
 */
@Composable
fun SessionSwitch(
    sessions: List<LoginSession>,
    selected: LoginSession,
    onSelect: (LoginSession) -> Unit,
) = withDesktopContext {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(imageVector = Icons.Filled.DesktopWindows, contentDescription = selected.name, tint = textColor)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            sessions.forEach { session ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelect(session)
                    },
                ) {
                    Text(
                        text = session.name,
                        fontWeight = if (session == selected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}
