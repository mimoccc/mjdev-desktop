package org.mjdev.desktop.components.aidesktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.blur.BlurPanel
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.data.Category
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.interfaces.IApp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BoxScope.AiDesktopMenuPanel(
    modifier: Modifier = Modifier,
    mode: AiDesktopMenuMode,
    categories: List<Category>,
    apps: List<IApp>,
    onModeChange: (AiDesktopMenuMode) -> Unit,
    onClose: () -> Unit,
) = withDesktopContext {
    var query by remember { mutableStateOf("") }
    val visibleApps =
        remember(apps, query) {
            apps
                .filter { app ->
                    query.isBlank() ||
                        app.name.contains(query, ignoreCase = true) ||
                        app.comment.contains(query, ignoreCase = true)
                }.take(AiDesktopTextLimits.MenuPreviewApps)
        }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    val windowRows =
        remember(categories, visibleApps, selectedCategory) {
            selectedCategory
                ?.let { category -> visibleApps.filter { app -> category in app.categories } }
                ?: emptyList()
        }
    val panelModifier =
        when (mode) {
            AiDesktopMenuMode.Window ->
                modifier
                    .align(Alignment.BottomStart)
                    .padding(start = AiDesktopMetrics.DesktopPadding, bottom = AiDesktopMetrics.DockHeight + 32.dp)
                    .width(AiDesktopMetrics.MenuWidth)
                    .heightIn(max = AiDesktopMetrics.MenuHeight)
            AiDesktopMenuMode.Fullscreen ->
                modifier
                    .align(Alignment.Center)
                    .padding(AiDesktopMetrics.FullscreenMenuPadding)
                    .fillMaxSize()
        }
    BlurPanel(
        modifier =
            panelModifier
                .clip(RoundedCornerShape(AiDesktopMetrics.PanelCornerRadius))
                .background(backgroundColor.alpha(0.64f)),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(AiDesktopMetrics.MenuPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedCategory?.name ?: AiDesktopText.Applications,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AiDesktopDockIcon(
                        iconName = if (mode == AiDesktopMenuMode.Window) AiDesktopIconName.List else AiDesktopIconName.Menu,
                        contentDescription = AiDesktopText.WindowMenu,
                        onClick = { onModeChange(AiDesktopMenuMode.Window) },
                    )
                    AiDesktopDockIcon(
                        iconName = if (mode == AiDesktopMenuMode.Fullscreen) AiDesktopIconName.Apps else AiDesktopIconName.Grid,
                        contentDescription = AiDesktopText.TouchMenu,
                        onClick = { onModeChange(AiDesktopMenuMode.Fullscreen) },
                    )
                }
            }
            if (mode == AiDesktopMenuMode.Window) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (selectedCategory == null) {
                        items(categories) { category ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(backgroundColor.alpha(0.24f))
                                        .clickable { selectedCategory = category }
                                        .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                AiDesktopDockIcon(iconName = category.name, contentDescription = category.name)
                                Text(text = category.name, color = textColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        items(windowRows.ifEmpty { visibleApps }) { app ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(backgroundColor.alpha(0.24f))
                                        .clickable {
                                            runAsync {
                                                app.start()
                                                onClose()
                                            }
                                        }.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                AiDesktopDockIcon(iconName = app.name, contentDescription = app.name)
                                Column {
                                    Text(text = app.name, color = textColor, fontWeight = FontWeight.Bold)
                                    Text(text = app.comment, color = textColor.alpha(0.78f))
                                }
                            }
                        }
                    }
                }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categories.take(8).forEach { category ->
                        Text(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(borderColor.alpha(0.22f))
                                    .clickable { selectedCategory = category }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            text = category.name,
                            color = textColor,
                        )
                    }
                }
                LazyVerticalGrid(
                    modifier = Modifier.weight(1f),
                    columns = GridCells.Adaptive(112.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(visibleApps) { app ->
                        Column(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(backgroundColor.alpha(0.28f))
                                    .clickable {
                                        runAsync {
                                            app.start()
                                            onClose()
                                        }
                                    }.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            AiDesktopDockIcon(
                                iconName = app.name,
                                iconSize = AiDesktopMetrics.DockIconSize,
                                contentDescription = app.name,
                            )
                            Text(
                                modifier = Modifier.size(width = 96.dp, height = 38.dp),
                                text = app.name,
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    label = { Text(AiDesktopText.SearchPlaceholder) },
                )
                AiDesktopDockIcon(iconName = AiDesktopIconName.Sleep, contentDescription = AiDesktopText.Sleep)
                AiDesktopDockIcon(iconName = AiDesktopIconName.Restart, contentDescription = AiDesktopText.Restart)
                AiDesktopDockIcon(iconName = AiDesktopIconName.Power, contentDescription = AiDesktopText.PowerOff)
            }
        }
    }
}
