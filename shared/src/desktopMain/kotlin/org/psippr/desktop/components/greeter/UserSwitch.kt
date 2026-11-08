package org.psippr.desktop.components.greeter

import androidx.compose.foundation.layout.Box
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.interfaces.IUser

/** User switch of the login screen: an icon, a click opens the menu of the accounts. */
@Composable
fun UserSwitch(
    users: List<IUser>,
    selected: IUser,
    onSelect: (IUser) -> Unit,
) = withDesktopContext {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(imageVector = Icons.Filled.People, contentDescription = selected.userName, tint = textColor)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            users.forEach { user ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelect(user)
                    },
                ) {
                    Text(
                        text = user.userName,
                        fontWeight = if (user.userName == selected.userName) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}
