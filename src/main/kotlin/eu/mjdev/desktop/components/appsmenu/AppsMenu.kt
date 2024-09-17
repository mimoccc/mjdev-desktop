package eu.mjdev.desktop.components.appsmenu

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.custom.UserAvatar
import eu.mjdev.desktop.components.sliding.VisibilityState
import eu.mjdev.desktop.components.sliding.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.data.Category
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.helpers.Animations.AppsMenuEnterAnimation
import eu.mjdev.desktop.helpers.Animations.AppsMenuExitAnimation
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.windows.ChromeWindow

@Composable
fun AppsMenu(
    api: DesktopProvider = LocalDesktop.current,
    panelState: VisibilityState = rememberVisibilityState(),
    menuState: VisibilityState = rememberVisibilityState(),
    enterAnimation: EnterTransition = AppsMenuEnterAnimation,
    exitAnimation: ExitTransition = AppsMenuExitAnimation,
    onFocusChange: (Boolean) -> Unit = {},
    onAppClick: (App) -> Unit = { app ->
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
) {
    val appMenuMinWidth by remember { api.currentUser.theme.appMenuMinWidthState }
    val appMenuMinHeight by remember { api.currentUser.theme.appMenuMinHeightState }
    val appMenuBackgroundColor by remember { api.currentUser.theme.backgroundColorState }
    val menuPadding by remember { api.currentUser.theme.appMenuOuterPaddingState } // todo
    var items: List<Any> by remember(api.appsProvider) { mutableStateOf(api.appsProvider.appCategories) }
    val onCategoryClick: (Category) -> Unit = { category ->
        items = api.appsProvider.categoriesAndApps[category.name]?.sortedBy { it.name } ?: emptyList()
    }
    ChromeWindow(
        visible = menuState.isVisible,
        enterAnimation = enterAnimation,
        exitAnimation = exitAnimation,
        position = WindowPosition.Absolute(
            panelState.bounds.x,
            api.containerSize.height - (panelState.bounds.height + appMenuMinHeight)
        ),
        onFocusChange = { focused ->
            menuState.onFocusChange(focused)
            onFocusChange(focused)
        }
    ) {
        Box(
            modifier = Modifier
                .width(appMenuMinWidth)
                .heightIn(
                    min = appMenuMinHeight,
                    max = appMenuMinHeight
                )
                .onPlaced(menuState::onPlaced)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(menuPadding)
                    .background(appMenuBackgroundColor.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    UserAvatar(
                        avatarSize = 64.dp,
                        backgroundColor = appMenuBackgroundColor,
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
                        color = Color.White.copy(0.1f),
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
                                        backgroundColor = appMenuBackgroundColor,
                                        iconTint = Color.White,
                                        onClick = { onCategoryClick(item) },
                                        onContextMenuClick = { onCategoryContextMenuClick(item) }
                                    )
                                }
                            }

                            is App -> {
                                items(items) { item ->
                                    AppsMenuApp(
                                        app = item as App,
                                        backgroundColor = appMenuBackgroundColor,
                                        iconTint = Color.White,
                                        onClick = {
                                            onAppClick(item)
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
                        color = Color.White.copy(0.1f),
                        thickness = 2.dp
                    )
                    AppsBottomBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(appMenuBackgroundColor),
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