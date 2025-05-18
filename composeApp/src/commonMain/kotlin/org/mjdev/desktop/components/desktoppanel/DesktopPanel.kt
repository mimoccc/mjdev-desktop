package org.mjdev.desktop.components.desktoppanel

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.blur.BlurPanel
import org.mjdev.desktop.components.desktoppanel.applets.DesktopMenuIcon
import org.mjdev.desktop.components.desktoppanel.applets.DesktopPanelDateTime
import org.mjdev.desktop.components.desktoppanel.applets.DesktopPanelFavoriteApps
import org.mjdev.desktop.components.desktoppanel.applets.DesktopPanelLanguage
import org.mjdev.desktop.components.desktoppanel.applets.DesktopPanelTray
import org.mjdev.desktop.components.sliding.SlidingPanel
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Colors.lighter
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import org.mjdev.desktop.components.sliding.base.VisibilityState
import org.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.mjdev.desktop.context.DesktopContextScope
import org.mjdev.desktop.extensions.LaunchedEffect.runAsync
import org.mjdev.desktop.extensions.MutableStateExt.rememberComputed
import org.mjdev.desktop.extensions.PaddingValues.height
import org.mjdev.desktop.helpers.animation.Animations.DesktopPanelEnterAnimation
import org.mjdev.desktop.helpers.animation.Animations.DesktopPanelExitAnimation
import org.mjdev.desktop.interfaces.IApp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.extensions.Compose.preview

@Composable
fun DesktopPanel(
    iconSize: DpSize = DpSize(56.dp, 56.dp),
    iconPadding: PaddingValues = PaddingValues(2.dp),
    iconOuterPadding: PaddingValues = PaddingValues(2.dp),
    showMenuIcon: Boolean = true,
    panelState: VisibilityState = rememberVisibilityState(),
    enterAnimation: EnterTransition = DesktopPanelEnterAnimation,
    exitAnimation: ExitTransition = DesktopPanelExitAnimation,
    onFocusChange: (focused: Boolean) -> Unit = {},
    onMenuIconClicked: () -> Unit = {},
    onMenuIconContextMenuClicked: () -> Unit = {},
    onAppClick: DesktopContextScope.(IApp) -> Unit = {},
    onAppContextMenuClick: (IApp) -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onTooltip: (item: Any?) -> Unit = {},
) = withDesktopContext {
    val panelHeight: (visible: Boolean) -> Dp = { visible ->
        if (visible) {
            iconSize.height +
                    iconPadding.height.times(2) +
                    iconOuterPadding.height.times(2) +
                    theme.panelContentPadding.times(2)
        } else panelDividerWidth
    }
    val size by rememberComputed(
        panelState.isVisible,
        panelState.enabled,
        containerSize.width,
        containerSize.height
    ) {
        DpSize(
            containerSize.width,
            panelHeight(
                if (panelState.enabled) panelState.isVisible
                else true
            )
        )
    }
    val panelShape by rememberComputed(size) {
        RoundedCornerShape(size.height.div(2))
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        SlidingPanel(
            modifier = Modifier.fillMaxWidth()
                .height(size.height),
            orientation = Vertical,
            state = panelState,
            enterAnimation = enterAnimation,
            exitAnimation = exitAnimation,
            onPointerEnter = {
                onFocusChange(true)
            }) { isVisible ->
            if (!isVisible) {
                Divider(
                    modifier = Modifier.fillMaxWidth()
                        .height(panelDividerWidth),
                    color = if (isDebug) Color.Red else Color.Transparent,
                    thickness = panelDividerWidth
                )
            } else {
                BoxWithConstraints(
                    modifier = Modifier.wrapContentSize()
                        .onPlaced {
                            runAsync {
                                panelState.updateSize(DpSize(size.width, size.height / 2))
                            }
                        }
                ) {
                    Column {
                        BlurPanel(
                            modifier = Modifier.fillMaxWidth()
                                .wrapContentHeight()
                                .clip(panelShape)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .wrapContentHeight()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                backgroundColor.alpha(0.2f),
                                                backgroundColor.alpha(0.4f),
                                                backgroundColor.alpha(0.5f),
                                            )
                                        )
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            this@BoxWithConstraints.constraints.maxHeight.div(
                                                2
                                            ).dp
                                        )
                                    )
                                    .shadow(
                                        shape = panelShape,
                                        ambientColor = borderColor.alpha(0.3f),
                                        spotColor = borderColor.alpha(0.3f),
                                        elevation = 4.dp
                                    ).padding(
                                        start = 4.dp,
                                        end = 4.dp,
                                        bottom = 4.dp
                                    ),
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .padding(theme.panelContentPadding)
                                ) {
                                    if (showMenuIcon) {
                                        DesktopMenuIcon(
                                            modifier = Modifier.align(Alignment.CenterStart),
                                            iconColor = borderColor,
                                            iconBackgroundColor = iconsTintColor,
                                            iconSize = iconSize,
                                            iconPadding = iconPadding,
                                            iconOuterPadding = iconOuterPadding,
                                            onTooltip = onTooltip,
                                            onClick = {
                                                onMenuIconClicked()
                                            },
                                            onContextMenuClick = onMenuIconContextMenuClicked
                                        )
                                    }
                                    DesktopPanelFavoriteApps(
                                        modifier = Modifier.align(Alignment.Center),
                                        iconColor = borderColor,
                                        iconBackgroundColor = iconsTintColor,
                                        iconColorRunning = iconsTintColor.lighter(0.3f),
                                        iconSize = iconSize,
                                        iconPadding = iconPadding,
                                        iconOuterPadding = iconOuterPadding,
                                        onTooltip = onTooltip,
                                        onAppClick = { app -> onAppClick(app) },
                                        onContextMenuClick = onAppContextMenuClick
                                    )
                                    DesktopPanelTray(
                                        modifier = Modifier.align(Alignment.CenterEnd)
                                            .padding(end = 16.dp),
                                    ) {
                                        DesktopPanelLanguage(
                                            onTooltip = onTooltip,
                                            onClick = onLanguageClick
                                        )
                                        DesktopPanelDateTime(
                                            onTooltip = onTooltip,
                                            onClick = {
                                                ai.ask("Whats current time?")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun DesktopPanelPreview() = preview {
    DesktopPanel(
        panelState = rememberVisibilityState(true)
    )
}