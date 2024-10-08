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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.custom.UserAvatar
import eu.mjdev.desktop.components.sliding.base.VisibilityState
import eu.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.data.Category
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.extensions.Compose.clear
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.extensions.Compose.onMouseEnter
import eu.mjdev.desktop.extensions.Compose.plus
import eu.mjdev.desktop.extensions.Compose.rememberCalculated
import eu.mjdev.desktop.extensions.Compose.rememberComputed
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.extensions.Compose.removeLast
import eu.mjdev.desktop.extensions.Modifier.dropShadow
import eu.mjdev.desktop.helpers.animation.Animations.AppsMenuEnterAnimation
import eu.mjdev.desktop.helpers.animation.Animations.AppsMenuExitAnimation
import eu.mjdev.desktop.helpers.internal.KeyEventHandler.Companion.globalKeyEventHandler
import eu.mjdev.desktop.provider.DesktopScope
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import eu.mjdev.desktop.windows.ChromeWindow
import eu.mjdev.desktop.windows.ChromeWindowState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

@Suppress("FunctionName")
@Composable
fun AppsMenu(
    panelState: VisibilityState = rememberVisibilityState(),
    menuState: VisibilityState = rememberVisibilityState(),
    enterAnimation: EnterTransition = AppsMenuEnterAnimation,
    exitAnimation: ExitTransition = AppsMenuExitAnimation,
    searchTextState: MutableState<String> = rememberState(""),
    onFocusChange: ChromeWindowState.(Boolean) -> Unit = {},
    onAppClick: DesktopScope.(App) -> Unit = { app ->
        startApp(app)
        menuState.hide()
    },
    onAppContextMenuClick: DesktopScope.(App) -> Unit = {},
    onCategoryContextMenuClick: (Category) -> Unit = {},
    onUserAvatarClick: () -> Unit = {}
) = withDesktopScope {
    var category by rememberState("")
    val items by rememberComputed(searchTextState.value, category) {
        when {
            searchTextState.value.isNotEmpty() -> appsProvider.allApps
                .filter { app ->
                    app.desktopFile?.fileData?.contains(searchTextState.value) == true
                }

            category.isNotEmpty() -> appCategoriesAndApps[category]?.sortedBy { app ->
                app.name
            } ?: emptyList<App>()

            else -> appCategories
        }
    }
    val onCategoryClick: (Category) -> Unit = remember {
        { c ->
            category = c.name
        }
    }
    val position by rememberCalculated {
        WindowPosition.Absolute(
            panelState.bounds.x,
            containerSize.height - (panelState.bounds.height + appMenuMinHeight)
        )
    }
    val windowState: ChromeWindowState = rememberChromeWindowState(position = position)
    globalKeyEventHandler(menuState.isVisible && menuState.enabled) {
        onEscape {
            menuState.hide()
            true
        }
        onBack {
            searchTextState.clear()
            true
        }
        onBackSpace {
            searchTextState.removeLast()
            true
        }
        onDelete {
            searchTextState.clear()
            true
        }
        onChar { char ->
            searchTextState + char
            true
        }
    }
    ChromeWindow(
        visible = menuState.isVisible,
        enterAnimation = enterAnimation,
        exitAnimation = exitAnimation,
        windowState = windowState,
        onFocusChange = onFocusChange
    ) {
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
                .onMouseEnter {
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
                        color = borderColor,
                        thickness = 2.dp
                    )
                    AppsBottomBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(backgroundColor),
                        backButtonVisible = items.firstOrNull() is App,
                        searchTextState = searchTextState,
                        onHideMenu = {
                            menuState.hide()
                        },
                        onBackClick = {
                            category = ""
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
                category = ""
            }
        }
    }
}

@Preview
@Composable
fun AppsMenuPreview() = AppsMenu()
