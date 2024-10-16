package eu.mjdev.desktop.components.appsmenu

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.blur.BlurPanel
import eu.mjdev.desktop.components.user.UserAvatar
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.data.Category
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.extensions.ColorUtils.darker
import eu.mjdev.desktop.extensions.Compose.clear
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.extensions.Compose.onMouseEnter
import eu.mjdev.desktop.extensions.Compose.plus
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.extensions.Compose.rememberCalculated
import eu.mjdev.desktop.extensions.Compose.rememberComputed
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.extensions.Compose.removeLast
import eu.mjdev.desktop.extensions.Custom.flowBlock
import eu.mjdev.desktop.extensions.Custom.sortByRelevance
import eu.mjdev.desktop.extensions.Custom.trimIsNotEmpty
import eu.mjdev.desktop.extensions.Modifier.dropShadow
import eu.mjdev.desktop.helpers.animation.Animations.AppsMenuEnterAnimation
import eu.mjdev.desktop.helpers.animation.Animations.AppsMenuExitAnimation
import eu.mjdev.desktop.helpers.compose.Orientation
import eu.mjdev.desktop.helpers.compose.rememberForeverLazyListState
import eu.mjdev.desktop.helpers.internal.KeyEventHandler.Companion.globalKeyEventHandler
import eu.mjdev.desktop.helpers.shape.BarShape
import eu.mjdev.desktop.provider.DesktopScope
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import eu.mjdev.desktop.windows.ChromeWindow
import eu.mjdev.desktop.windows.ChromeWindowState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

@Suppress("FunctionName")
@Composable
fun AppsMenu(
    panelState: ChromeWindowState = rememberChromeWindowState(),
    menuState: ChromeWindowState = rememberChromeWindowState(),
    enterAnimation: EnterTransition = AppsMenuEnterAnimation,
    exitAnimation: ExitTransition = AppsMenuExitAnimation,
    searchTextState: MutableState<String> = rememberState(""),
    listState: LazyListState = rememberForeverLazyListState("AppsMenu"),
    onFocusChange: ChromeWindowState.(Boolean) -> Unit = {},
    onAppClick: DesktopScope.(App) -> Unit = { app ->
        app.start()
        searchTextState.clear()
        menuState.hide()
    },
    onAppContextMenuClick: DesktopScope.(App) -> Unit = {},
    onCategoryContextMenuClick: DesktopScope.(Category) -> Unit = {},
    onUserAvatarClick: () -> Unit = {}
) = withDesktopScope {
    var category by rememberState("")
    val appCategories by flowBlock(emptyList()) { appCategories }
    val items by rememberComputed(searchTextState.value, category) {
        when {
            // todo fuzzy sort, simlify
            searchTextState.trimIsNotEmpty() -> allApps.filter { app ->
                app.desktopData.contains(searchTextState.value, ignoreCase = true)
            }.sortByRelevance(searchTextState.value) { name }

            category.isNotEmpty() -> allApps.filter { app ->
                app.categories.contains(category)
            }.sortedBy { app ->
                app.name
            }

            else -> appCategories
        }
    }
    val position by rememberCalculated {
        WindowPosition.Absolute(
            panelState.bounds.x,
            containerSize.height - (panelState.bounds.height + appMenuMinHeight)
        )
    }
    val windowState: ChromeWindowState = rememberChromeWindowState(position = position)
    globalKeyEventHandler(
        isEnabled = { menuState.isVisible && menuState.enabled }
    ) {
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
            BlurPanel(
                modifier = Modifier.fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(menuPadding)
                    .background(backgroundColor.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .border(2.dp, borderColor, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier.clip(RectangleShape),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        val shape = BarShape(
                            offset = 52f,
                            circleRadius = 24.dp,
                            cornerRadius = 4.dp,
                            circleGap = 8.dp,
                        )
                        val brush = Brush.horizontalGradient(
                            listOf(
                                backgroundColor.darker(0.1f),
                                backgroundColor.darker(0.1f),
                                backgroundColor.darker(0.1f),
                                backgroundColor.alpha(0.9f),
                                backgroundColor.alpha(0.7f),
                                backgroundColor.alpha(0.3f),
                            )
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .height(64.dp)
                                .background(
                                    brush = brush,
                                    shape = shape
                                )
                        )
                        UserAvatar(
                            avatarSize = 64.dp,
                            orientation = Orientation.Horizontal,
                            titleVerticalAlignment = Alignment.Bottom,
                            iconPadding = PaddingValues(bottom = 16.dp),
                            titlePadding = PaddingValues(top = 24.dp, start = 20.dp),
                            onUserAvatarClick = onUserAvatarClick,
                        )
                    }
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
                    AppsList(
                        modifier = Modifier
                            .padding(bottom = 62.dp)
                            .fillMaxSize()
                            .padding(24.dp),
                        category = category,
                        listState = listState,
                        onCategoryClick = { c -> category = c.name },
                        onAppClick = onAppClick,
                        onAppContextMenuClick = onAppContextMenuClick,
                        onCategoryContextMenuClick = onCategoryContextMenuClick,
                        items = items
                    )
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
                        onActionClick = {
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
fun AppsMenuPreview() = preview {
    AppsMenu()
}
