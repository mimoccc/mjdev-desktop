package org.psippr.desktop.components.greeter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.helpers.greetd.LoginSession

/** Picture size of the selected desktop on the switch button. */
private val BUTTON_ICON_SIZE = 32.dp

/** Picture size of a desktop in the menu. */
private val MENU_ICON_SIZE = 24.dp

/** Gap between a desktop's picture and its name in the menu. */
private val MENU_ICON_GAP = 12.dp

/**
 * Desktop switch of the login screen: the picture of the preselected desktop, a click opens the
 * vertical menu of the installed desktops, each with its picture and name (shown even when psippr
 * is the only one).
 */
@Composable
fun SessionSwitch(
    sessions: List<LoginSession>,
    selected: LoginSession,
    onSelect: (LoginSession) -> Unit,
) = withDesktopContext {
    // old: the same generic monitor icon for every desktop, menu entries were names only
//    var expanded by remember { mutableStateOf(false) }
//    Box {
//        IconButton(onClick = { expanded = true }) {
//            Icon(imageVector = Icons.Filled.DesktopWindows, contentDescription = selected.name, tint = textColor)
//        }
//        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
//            sessions.forEach { session ->
//                DropdownMenuItem(
//                    onClick = {
//                        expanded = false
//                        onSelect(session)
//                    },
//                ) {
//                    Text(
//                        text = session.name,
//                        fontWeight = if (session == selected) FontWeight.Bold else FontWeight.Normal,
//                    )
//                }
//            }
//        }
//    }
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            SessionIcon(session = selected, size = BUTTON_ICON_SIZE, tint = textColor)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            sessions.forEach { session ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelect(session)
                    },
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MENU_ICON_GAP),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SessionIcon(session = session, size = MENU_ICON_SIZE)
                        Text(
                            text = session.name,
                            fontWeight = if (session == selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}
