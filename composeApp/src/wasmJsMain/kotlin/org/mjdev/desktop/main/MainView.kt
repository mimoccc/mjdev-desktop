package org.mjdev.desktop.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.background.BackgroundImage
import org.mjdev.desktop.components.qrcode.QrCodeView

@Composable
fun MainView() = withDesktopContext {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        BackgroundImage(
            modifier = Modifier.fillMaxSize(),
            switchDelay = theme.backgroundRotationDelay,
            images = backgrounds,
            onChange = { src ->
                palette.apply {
                    update(src)
                }.also { p ->
                    context.currentUser.theme.backgroundColor = p.backgroundColor
                }
            }
        )
        Box(
            modifier = Modifier.size(256.dp, 256.dp),
            contentAlignment = Alignment.Center
        ) {
            Column {
                QrCodeView(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
//    val tooltipState: TooltipState = rememberTooltipState()
//    val appsMenuState = rememberAppsMenuState(
//        visible = isDesign
//    )
//    val controlCenterState = rememberVisibilityState(
//        visible = isDesign
//    )
//    val panelState = rememberVisibilityState(
//        visible = isDesign || !panelAutoHideEnabled,
//        enabled = panelAutoHideEnabled,
//        autoHideDelay = panelHideDelay
//    )
//    val bottomPadding by rememberCalculated(
//        panelState.enabled, panelState.height
//    ) {
//        if (panelState.enabled) 0.dp
//        else max(0.dp, panelState.height)
//    }
////    val installWindowState = rememberVisibilityState()
////    val infoWindowState = rememberVisibilityState(false) //(api.isFirstStart || api.isDebug) // todo
//    val onTooltip: (item: Any?) -> Unit = { item ->
//        println("Showing tooltip : $item")
//        tooltipState.show(item)
//    }
//    // todo not showing nothing
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        // todo, something prevent preview
//        if (!isDesign) {
//            Desktop(
//                tooltipState = tooltipState,
//                onTooltip = onTooltip,
//                padding = PaddingValues(
//                    bottom = bottomPadding
//                ),
//                widgets = {
//                    MemoryChart(
//                        modifier = Modifier
//                            .size(350.dp, 300.dp)
//                            .align(Alignment.BottomEnd)
//                    )
//                }, onLeftMouseClick = {
//                    runAsync {
//                        panelState.hide()
//                        appsMenuState.hide()
//                        controlCenterState.hide()
//                    }
//                }, onRightMouseClick = {
////                contextMenuState.show()
//                })
//        }
//        DesktopPanel(
//            panelState = panelState,
//            onTooltip = onTooltip,
//            onMenuIconClicked = {
//                runAsync {
//                    appsMenuState.show()
//                }
//            }
//        )
//        ControlCenter(
//            modifier = Modifier
//                .fillMaxHeight()
//                .align(Alignment.TopEnd),
//            controlCenterState = controlCenterState,
//            onTooltip = onTooltip,
//        )
//        AppsMenu(
//            modifier = Modifier
//                .wrapContentSize()
//                .align(Alignment.BottomStart),
//            appsMenuState = appsMenuState,
//            panelState = panelState,
//            onAppClick = { app ->
//                runAsync {
//                    app.start()
//                    appsMenuState.hide()
//                }
//            },
//            onAppContextMenuClick = {},
//            onCategoryContextMenuClick = {},
//            onUserAvatarClick = {},
//            onActionClick = {},
//            onTooltip = onTooltip,
//        )
//    }
}

@Preview
@Composable
fun MainViewPreview() = preview {
    MainView()
}