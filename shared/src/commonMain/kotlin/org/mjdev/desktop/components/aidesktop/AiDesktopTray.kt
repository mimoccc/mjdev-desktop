package org.mjdev.desktop.components.aidesktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext

@Composable
fun AiDesktopTray(
    modifier: Modifier = Modifier,
) = withDesktopContext {
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AiDesktopDockIcon(iconName = "wifi", contentDescription = AiDesktopText.ControlCenter)
        AiDesktopDockIcon(iconName = "sound", contentDescription = AiDesktopText.ControlCenter)
        Text(
            text = AiDesktopText.Keyboard,
            color = textColor,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = AiDesktopText.Time,
            color = textColor,
            fontWeight = FontWeight.Bold,
        )
    }
}
