package eu.mjdev.desktop.components.appsmenu

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.custom.UserAvatar
import eu.mjdev.desktop.components.sliding.VisibilityState
import eu.mjdev.desktop.components.sliding.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.data.App
import eu.mjdev.desktop.provider.data.Category
import eu.mjdev.desktop.windows.ChromeWindow

@Composable
fun AppsMenu(
    api: DesktopProvider = LocalDesktop.current,
    panelState: VisibilityState = rememberVisibilityState(),
    menuState: VisibilityState = rememberVisibilityState(),
    appMenuMinWidth: Dp = api.currentUser.theme.appMenuMinWidth,
    appMenuMinHeight: Dp = api.currentUser.theme.appMenuMinHeight,
    appMenuBackgroundColor: Color = api.currentUser.theme.backgroundColor,
    menuPadding: PaddingValues = PaddingValues(api.currentUser.theme.appMenuOuterPadding),// todo
    enterAnimation: EnterTransition = fadeIn() + slideInVertically(initialOffsetY = { it }),
    exitAnimation: ExitTransition = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    items: MutableState<List<Any>> = remember(api.appsProvider) { mutableStateOf(api.appsProvider.appCategories) },
    onFocusChange: (Boolean) -> Unit = {},
) = ChromeWindow(
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
                    orientation = Orientation.Horizontal
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
                    when (items.value.firstOrNull()) {
                        is Category -> {
                            items(items.value) { item ->
                                AppsMenuCategory(
                                    category = item as Category,
                                    backgroundColor = appMenuBackgroundColor,
                                    iconTint = Color.White
                                ) { category ->
                                    items.value = api.appsProvider.categoriesAndApps[category.name]
                                        ?.sortedBy { it.name } ?: emptyList()
                                }
                            }
                        }

                        is App -> {
                            items(items.value) { item ->
                                AppsMenuApp(
                                    app = item as App,
                                    backgroundColor = appMenuBackgroundColor,
                                    iconTint = Color.White
                                ) { app ->
                                    app?.start()
                                    menuState.hide()
                                }
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
                    backButtonVisible = items.value.first() is App,// todo
                    onBackClick = {
                        items.value = api.appsProvider.appCategories
                    }
                )
            }
        }
    }
    launchedEffect(menuState.isVisible) {
        if (!menuState.isVisible) {
            items.value = api.appsProvider.appCategories
        }
    }
}