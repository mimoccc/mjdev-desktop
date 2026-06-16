package org.mjdev.desktop.components.aidesktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.blur.BlurPanel
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.alpha

@Composable
fun BoxScope.AiDesktopControlCenterPanel(
    modifier: Modifier = Modifier,
    selectedTab: AiControlCenterTab,
    onTabSelected: (AiControlCenterTab) -> Unit,
) = withDesktopContext {
    BlurPanel(
        modifier =
            modifier
                .align(Alignment.CenterEnd)
                .padding(AiDesktopMetrics.DesktopPadding)
                .width(AiDesktopMetrics.ControlCenterWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(AiDesktopMetrics.PanelCornerRadius))
                .background(backgroundColor.alpha(0.68f)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(AiDesktopMetrics.MenuPadding),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AiControlCenterTab.entries.forEach { tab ->
                    val selected = tab == selectedTab
                    Column(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (selected) borderColor.alpha(0.36f) else backgroundColor.alpha(0.2f))
                                .clickable { onTabSelected(tab) }
                                .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AiDesktopDockIcon(
                            iconName = tab.iconName,
                            contentDescription = tab.title,
                            iconColor = if (selected) backgroundColor else textColor,
                            iconBackgroundColor = if (selected) borderColor else backgroundColor,
                        )
                        Text(text = tab.title, color = textColor)
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = selectedTab.title,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                )
                HorizontalDivider(color = borderColor.alpha(0.4f))
                Text(
                    text = AiDesktopText.DesktopVision,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = AiDesktopText.DesktopVisionDetail,
                    color = textColor.alpha(0.82f),
                )
                repeat(4) { index ->
                    Text(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(borderColor.alpha(0.16f))
                                .padding(14.dp),
                        text = "${selectedTab.title} setting ${index + 1}",
                        color = textColor,
                    )
                }
            }
        }
    }
}
