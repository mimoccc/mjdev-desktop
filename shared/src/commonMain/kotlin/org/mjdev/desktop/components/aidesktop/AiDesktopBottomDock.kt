package org.mjdev.desktop.components.aidesktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.blur.BlurPanel
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.interfaces.IApp

@Composable
fun BoxScope.AiDesktopBottomDock(
    modifier: Modifier = Modifier,
    favoriteApps: List<IApp>,
    onMenuClick: () -> Unit,
    onAppLaunched: () -> Unit,
) = withDesktopContext {
    BlurPanel(
        modifier =
            modifier
                .align(Alignment.BottomCenter)
                .padding(AiDesktopMetrics.DesktopPadding)
                .height(AiDesktopMetrics.DockHeight)
                .widthIn(min = containerSize.width * 0.58f, max = containerSize.width - 32.dp)
                .clip(RoundedCornerShape(AiDesktopMetrics.DockCornerRadius))
                .background(backgroundColor.alpha(0.46f)),
    ) {
        Row(
            modifier = Modifier.padding(AiDesktopMetrics.DockContentPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AiDesktopDockIcon(
                iconName = "menu",
                contentDescription = AiDesktopText.Menu,
                iconColor = backgroundColor,
                iconBackgroundColor = borderColor,
                onClick = onMenuClick,
            )
            favoriteApps.take(AiDesktopTextLimits.FavoriteDockApps).forEach { app ->
                AiDesktopDockIcon(
                    iconName = app.name,
                    contentDescription = app.name,
                    iconColor = if (app.isRunning) backgroundColor else textColor,
                    iconBackgroundColor = if (app.isRunning) borderColor else backgroundColor,
                    onClick = {
                        runAsync {
                            app.start()
                            onAppLaunched()
                        }
                    },
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            AiDesktopTray()
        }
    }
}
