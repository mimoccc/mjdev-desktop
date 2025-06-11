package org.mjdev.desktop.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import org.mjdev.desktop.components.appsmenu.AppsMenu
import org.mjdev.desktop.components.appsmenu.AppsMenuState.Companion.rememberAppsMenuState
import org.mjdev.desktop.components.controlcenter.ControlCenter
import org.mjdev.desktop.components.desktop.Desktop
import org.mjdev.desktop.components.desktoppanel.DesktopPanel
import org.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.mjdev.desktop.components.tooltip.TooltipState
import org.mjdev.desktop.components.tooltip.rememberTooltipState
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.isDesign
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.MutableStateExt.rememberCalculated
import org.mjdev.desktop.components.desktop.widgets.MemoryChart

@SuppressLint("ComposableNaming")
@Composable
fun MainView() = withDesktopContext {
    val tooltipState: TooltipState = rememberTooltipState()
    val appsMenuState = rememberAppsMenuState(
        visible = isDesign
    )
    val controlCenterState = rememberVisibilityState(
        visible = isDesign
    )
    val panelState = rememberVisibilityState(
        visible = isDesign || !panelAutoHideEnabled,
        enabled = panelAutoHideEnabled,
        autoHideDelay = panelHideDelay
    )
    val bottomPadding by rememberCalculated(
        panelState.enabled, panelState.height
    ) {
        if (panelState.enabled) 0.dp
        else max(0.dp, panelState.height)
    }
//    val installWindowState = rememberVisibilityState()
//    val infoWindowState = rememberVisibilityState(false) //(api.isFirstStart || api.isDebug) // todo
    val onTooltip: (item: Any?) -> Unit = { item ->
        println("Showing tooltip : $item")
        tooltipState.show(item)
    }
    // todo not showing nothing
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // todo, something prevent preview
        if (!isDesign) {
            Desktop(
                tooltipState = tooltipState,
                onTooltip = onTooltip,
                padding = PaddingValues(
                    bottom = bottomPadding
                ),
                widgets = {
                    MemoryChart(
                        modifier = Modifier
                            .size(350.dp, 300.dp)
                            .align(Alignment.BottomEnd)
                    )
                }, onLeftMouseClick = {
                    runAsync {
                        panelState.hide()
                        appsMenuState.hide()
                        controlCenterState.hide()
                    }
                }, onRightMouseClick = {
//                contextMenuState.show()
                })
        }
        DesktopPanel(panelState = panelState, onTooltip = onTooltip, onMenuIconClicked = {
            runAsync {
                appsMenuState.show()
            }
        })
        ControlCenter(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.TopEnd),
            controlCenterState = controlCenterState,
            onTooltip = onTooltip,
        )
        AppsMenu(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.BottomStart),
            appsMenuState = appsMenuState,
            panelState = panelState,
            onAppClick = { app ->
                runAsync {
                    app.start()
                    appsMenuState.hide()
                }
            },
            onAppContextMenuClick = {},
            onCategoryContextMenuClick = {},
            onUserAvatarClick = {},
            onActionClick = {},
            onTooltip = onTooltip,
        )
    }
}

@Preview(device = Devices.TABLET)
@Composable
fun PreviewMainView() = preview {
    MainView()
}