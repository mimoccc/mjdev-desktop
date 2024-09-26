package eu.mjdev.desktop.components.appsmenu

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.custom.UserAvatar
import eu.mjdev.desktop.components.sliding.base.VisibilityState
import eu.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.data.Category
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.extensions.Compose.rememberCalculated
import eu.mjdev.desktop.extensions.Modifier.dropShadow
import eu.mjdev.desktop.helpers.animation.Animations.AppsMenuEnterAnimation
import eu.mjdev.desktop.helpers.animation.Animations.AppsMenuExitAnimation
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import eu.mjdev.desktop.windows.ChromeWindow
import eu.mjdev.desktop.windows.ChromeWindowState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("FunctionName", "LocalVariableName")
@Composable
fun AppsMenu(
    panelState: VisibilityState = rememberVisibilityState(),
    menuState: VisibilityState = rememberVisibilityState(),
    enterAnimation: EnterTransition = AppsMenuEnterAnimation,
    exitAnimation: ExitTransition = AppsMenuExitAnimation,
    onFocusChange: (Boolean) -> Unit = {},
    onAppClick: (App, DesktopProvider) -> Unit = { app, api ->
        api.appsProvider.startApp(app)
    },
    onAppContextMenuClick: (App) -> Unit = {
        // todo
    },
    onCategoryContextMenuClick: (Category) -> Unit = {
        // todo
    },
    onUserAvatarClick: () -> Unit = {
        // todo
    }
) = withDesktopScope {
    val appMenuMinWidth by remember { api.currentUser.theme.appMenuMinWidthState }
    val appMenuMinHeight by remember { api.currentUser.theme.appMenuMinHeightState }
    val menuPadding by remember { api.currentUser.theme.appMenuOuterPaddingState }
    // todo
    var items: List<Any> by remember(api.appsProvider) { mutableStateOf(api.appsProvider.appCategories) }
    val onCategoryClick: (Category) -> Unit = { category ->
        items = api.appsProvider.categoriesAndApps[category.name]?.sortedBy { it.name } ?: emptyList()
    }
    val position by rememberCalculated {
        WindowPosition.Absolute(
            panelState.bounds.x,
            api.containerSize.height - (panelState.bounds.height + appMenuMinHeight)
        )
    }
    val windowState: ChromeWindowState = rememberChromeWindowState(position = position)
    val Container: @Composable (content: @Composable () -> Unit) -> Unit = { content ->
        if (isDebug) {
            content()
        } else {
            ChromeWindow(
                visible = menuState.isVisible,
                enterAnimation = enterAnimation,
                exitAnimation = exitAnimation,
                windowState = windowState,
                onFocusChange = { focused ->
                    menuState.onFocusChange(focused)
                    onFocusChange(focused)
                },
                content = { content() }
            )
        }
    }
    Container {
        Box(
            modifier = Modifier
                .width(appMenuMinWidth)
                .heightIn(
                    min = appMenuMinHeight,
                    max = appMenuMinHeight
                )
                .dropShadow(
                    RoundedCornerShape(24.dp),
                    borderColor.alpha(0.5f),
                    0.dp,
                    0.dp,
                    4.dp,
                    0.dp
                )
                .onPlaced(menuState::onPlaced)
                .onPointerEvent(PointerEventType.Enter) {
                    windowState.requestFocus()
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(menuPadding)
                    .background(backgroundColor.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                    .border(2.dp, borderColor, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    UserAvatar(
                        modifier = Modifier.background(backgroundColor),
                        avatarSize = 64.dp,
                        orientation = Orientation.Horizontal,
                        onUserAvatarClick = onUserAvatarClick
                    )
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .padding(
                                start = 2.dp,
                                end = 2.dp
                            ),
                        color = borderColor,
                        thickness = 2.dp
                    )
                    LazyColumn(
                        modifier = Modifier
                            .padding(bottom = 62.dp)
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        when (items.firstOrNull()) {
                            is Category -> {
                                items(items) { item ->
                                    AppsMenuCategory(
                                        category = item as Category,
                                        backgroundColor = backgroundColor,
                                        iconTint = textColor,
                                        textColor = textColor,
                                        onClick = { onCategoryClick(item) },
                                        onContextMenuClick = { onCategoryContextMenuClick(item) }
                                    )
                                }
                            }

                            is App -> {
                                items(items) { item ->
                                    AppsMenuApp(
                                        app = item as App,
                                        backgroundColor = backgroundColor,
                                        iconTint = textColor,
                                        textColor = textColor,
                                        onClick = {
                                            onAppClick(item, api)
                                        },
                                        onContextMenuClick = {
                                            onAppContextMenuClick(item)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.BottomStart),
                ) {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .padding(
                                start = 2.dp,
                                end = 2.dp
                            ),
                        color = borderColor,
                        thickness = 2.dp
                    )
                    AppsBottomBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(backgroundColor),
                        iconColor = borderColor,
                        iconBackgroundColor = iconsTintColor,
                        backButtonVisible = items.first() is App,
                        onBackClick = {
                            items = api.appsProvider.appCategories
                        },
                        onContextMenuClick = {
                            // todo : context menu
                        }
                    )
                }
            }
        }
        launchedEffect(menuState.isVisible) {
            if (!menuState.isVisible) {
                items = api.appsProvider.appCategories
            }
        }
    }
}

@Preview
@Composable
fun AppsMenuPreview() = AppsMenu()
