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
import eu.mjdev.desktop.provider.DesktopScope
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
    onAppClick: DesktopScope.(App) -> Unit = { app -> startApp(app) },
    onAppContextMenuClick: DesktopScope.(App) -> Unit = {},
    onCategoryContextMenuClick: (Category) -> Unit = {},
    onUserAvatarClick: () -> Unit = {}
) = withDesktopScope {
    // todo remove those three
    val appMenuMinWidth by remember { theme.appMenuMinWidthState }
    val appMenuMinHeight by remember { theme.appMenuMinHeightState }
    val menuPadding by remember { theme.appMenuOuterPaddingState }

    var items: List<Any> by remember(appCategories) { mutableStateOf(appCategories) }
    val onCategoryClick: (Category) -> Unit = { category ->
        items = appCategoriesAndApps[category.name]?.sortedBy { it.name } ?: emptyList()
    }
    val position by rememberCalculated {
        WindowPosition.Absolute(
            panelState.bounds.x,
            containerSize.height - (panelState.bounds.height + appMenuMinHeight)
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
                        backButtonVisible = items.first() is App,
                        onBackClick = {
                            items = appCategories
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
                items = appCategories
            }
        }
    }
}

@Preview
@Composable
fun AppsMenuPreview() = AppsMenu()
