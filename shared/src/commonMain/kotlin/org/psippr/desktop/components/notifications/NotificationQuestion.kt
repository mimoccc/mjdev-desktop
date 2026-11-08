package org.psippr.desktop.components.notifications

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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

/** Alpha of the button borders and of the unchecked checkbox. */
private const val SECONDARY_ALPHA = 0.6f

/**
 * The question part of a notification card: its checkbox (when it has one) and its buttons in
 * a row. [onAction] gets the id of the pressed button.
 */
@Suppress("FunctionName")
@Composable
fun NotificationQuestion(
    notification: DesktopNotification,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onAction: (actionId: String) -> Unit,
) = withDesktopContext {
    Column(modifier = Modifier.padding(top = 6.dp)) {
        if (notification.checkbox.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onCheckedChange(!checked) },
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors =
                        CheckboxDefaults.colors(
                            checkedColor = iconsTintColor,
                            uncheckedColor = textColor.alpha(SECONDARY_ALPHA),
                            checkmarkColor = backgroundColor,
                        ),
                )
                TextAny(text = notification.checkbox, color = textColor, fontSize = 12.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            notification.actions.forEach { action ->
                TextAny(
                    text = action.label,
                    color = textColor,
                    fontSize = 13.sp,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, borderColor.alpha(SECONDARY_ALPHA), RoundedCornerShape(8.dp))
                            .clickable { onAction(action.id) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
        }
    }
}
