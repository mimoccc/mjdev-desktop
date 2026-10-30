package org.psippr.desktop.components.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.System.currentTimeMillis

/** Gap between two cards and around the list. */
private val SPACING: Dp = 8.dp

/**
 * The notification history on the control center's home tab: every notification not dismissed
 * yet, newest first, each with its own close button. Scrolls when longer than [maxHeight].
 */
@Suppress("FunctionName")
@Composable
fun NotificationList(
    maxHeight: Dp,
    modifier: Modifier = Modifier,
) = withDesktopContext {
    val manager = context.notificationsManager
    val items = manager.visible
    if (items.isNotEmpty()) {
        val now = currentTimeMillis
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(SPACING),
            verticalArrangement = Arrangement.spacedBy(SPACING),
        ) {
            items.forEach { notification ->
                NotificationCard(
                    notification = notification,
                    time = NotificationTime.format(notification.createdAt, now),
                    onDismiss = { manager.dismiss(notification.id) },
                    answerable = manager.isOpen(notification.id),
                    onAction = { actionId, checked -> manager.answer(notification.id, actionId, checked) },
                )
            }
        }
    }
}
