package org.psippr.desktop.components.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.psippr.desktop.components.text.TextAny
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Colors.alpha
import org.psippr.desktop.managers.notifications.DesktopNotification

/** Alpha of the time and of the close button, as secondary text. */
private const val SECONDARY_ALPHA = 0.7f

/** Text of the close button for screen readers. */
private const val CLOSE = "Dismiss"

/**
 * One [notification] as a card, drawn like the tooltip bubble (palette background and border):
 * title, message and time, with the close button ("x") in the top right corner.
 */
@Suppress("FunctionName")
@Composable
fun NotificationCard(
    notification: DesktopNotification,
    time: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) = withDesktopContext {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                .padding(8.dp),
    ) {
        // the right padding keeps the text clear of the close button
        Column(modifier = Modifier.fillMaxWidth().padding(end = 24.dp)) {
            if (notification.title.isNotBlank()) {
                TextAny(text = notification.title, color = textColor, fontSize = 14.sp)
            }
            if (notification.message.isNotBlank()) {
                TextAny(text = notification.message, color = textColor, fontSize = 12.sp, maxLines = 4)
            }
            TextAny(text = time, color = textColor.alpha(SECONDARY_ALPHA), fontSize = 10.sp)
        }
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = CLOSE,
            tint = textColor.alpha(SECONDARY_ALPHA),
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onDismiss),
        )
    }
}
