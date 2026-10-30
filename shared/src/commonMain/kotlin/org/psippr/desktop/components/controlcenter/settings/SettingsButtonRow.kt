package org.psippr.desktop.components.controlcenter.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.psippr.desktop.components.text.TextAny
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Colors.alpha
import org.psippr.desktop.extensions.Compose.preview

/** Alpha of the border, as on [SettingsSelectRow]. */
private const val BORDER_ALPHA = 0.5f

/** Alpha of the button text while the button is disabled. */
private const val DISABLED_ALPHA = 0.5f

/**
 * A labelled action: [label] on the left, a button reading [action] on the right, drawn like the
 * value box of [SettingsSelectRow] so the settings pages keep one look.
 */
@Suppress("FunctionName")
@Composable
fun SettingsButtonRow(
    label: String,
    action: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) = withDesktopContext {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // the label gives way (wraps) so the button keeps its one line
        TextAny(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            text = label,
            color = textColor,
            fontSize = 14.sp,
        )
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
                    .border(1.dp, borderColor.alpha(BORDER_ALPHA), RoundedCornerShape(8.dp))
                    .clickable(enabled = enabled, onClick = onClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            TextAny(
                text = action,
                color = if (enabled) textColor else textColor.alpha(DISABLED_ALPHA),
                fontSize = 14.sp,
                softWrap = false,
                maxLines = 1,
            )
        }
    }
}

@Preview
@Composable
fun SettingsButtonRowPreview() = preview {
    SettingsButtonRow(label = "Plugin", action = "Install…") {}
}
