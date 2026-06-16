package org.mjdev.desktop.components.aidesktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.mjdev.desktop.components.blur.BlurPanel
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.alpha

@Composable
fun BoxScope.AiDesktopRightDock(
    modifier: Modifier = Modifier,
    onControlCenterClick: () -> Unit,
) = withDesktopContext {
    BlurPanel(
        modifier =
            modifier
                .align(Alignment.CenterEnd)
                .padding(end = AiDesktopMetrics.DesktopPadding)
                .width(AiDesktopMetrics.RightDockWidth)
                .clip(RoundedCornerShape(AiDesktopMetrics.DockCornerRadius))
                .background(backgroundColor.alpha(0.44f)),
    ) {
        Column(
            modifier = Modifier.padding(AiDesktopMetrics.DockContentPadding),
            verticalArrangement = Arrangement.spacedBy(AiDesktopMetrics.RightDockItemSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AiDesktopDockIcon(
                iconName = "settings",
                contentDescription = AiDesktopText.ControlCenter,
                iconColor = backgroundColor,
                iconBackgroundColor = borderColor,
                onClick = onControlCenterClick,
            )
            AiDesktopDockIcon(iconName = "widgets", contentDescription = AiDesktopText.Widgets)
            AiDesktopDockIcon(iconName = "palette", contentDescription = AiDesktopText.Theme)
        }
    }
}
