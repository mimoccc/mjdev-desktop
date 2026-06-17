package org.mjdev.desktop.components.controlcenter.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.preview

/** A titled, rounded container that groups related desktop settings rows. */
@Suppress("FunctionName")
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) = withDesktopContext {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor.alpha(0.3f))
                .padding(12.dp),
    ) {
        TextAny(
            modifier = Modifier.padding(bottom = 8.dp),
            text = title,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        content()
    }
}

@Preview
@Composable
fun PreviewSettingsSection() =
    preview {
        SettingsSection(title = "Background") {
            TextAny(text = "content")
        }
    }
