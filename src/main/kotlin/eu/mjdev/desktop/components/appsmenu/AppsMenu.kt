package eu.mjdev.desktop.components.appsmenu

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import eu.mjdev.desktop.components.custom.UserAvatar
import eu.mjdev.desktop.components.slidemenu.VisibilityState
import eu.mjdev.desktop.components.slidemenu.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.extensions.Compose.setWindowBounds
import eu.mjdev.desktop.helpers.WindowFocusState.Companion.windowFocusHandler
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.data.App
import eu.mjdev.desktop.provider.data.Category
import eu.mjdev.desktop.windows.TopWindow

@Composable
fun AppsMenu(
    modifier: Modifier = Modifier,
    api: DesktopProvider = LocalDesktop.current,
    appMenuExpandedWidth: Dp = 480.dp, // todo
    appMenuMinHeight: Dp = 640.dp, // todo
    bottomY: Dp = 64.dp, // todo panel height
    backgroundColor: Color = api.currentUser.theme.backgroundColor,
    menuPadding: PaddingValues = PaddingValues(2.dp), // todo
    menuState: VisibilityState = rememberVisibilityState(),
    items: MutableState<List<Any>> = remember { mutableStateOf(api.appsProvider.appCategories) },
    onVisibilityChange: (visible: Boolean) -> Unit = {},
) {
    val windowState: WindowState = rememberWindowState(
        placement = WindowPlacement.Floating,
        position = WindowPosition.Aligned(api.currentUser.theme.panelLocation.alignment),
        size = api.containerSize.copy(
            width = if (menuState.isVisible) appMenuMinHeight else 0.dp
        ),
        isMinimized = false
    )
    TopWindow(
        windowState = windowState,
    ) {
        windowFocusHandler { hasFocus ->
            if (!hasFocus) {
                menuState.hide()
            }
        }
        AnimatedVisibility(
            menuState.isVisible,
            modifier = modifier,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            Box(
                modifier = modifier
                    .padding(bottom = bottomY)
                    .width(appMenuExpandedWidth)
                    .heightIn(
                        min = appMenuMinHeight,
                        max = appMenuMinHeight
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(menuPadding)
                        .background(backgroundColor.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, backgroundColor, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        UserAvatar(
                            avatarSize = 64.dp,
                            backgroundColor = backgroundColor,
                            orientation = Orientation.Horizontal
                        )
                        LazyColumn(
                            modifier = Modifier
                                .padding(bottom = 48.dp)
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            when (items.value.firstOrNull()) {
                                is Category -> {
                                    items(items.value) { item ->
                                        AppsMenuCategory(
                                            category = item as Category,
                                            backgroundColor = backgroundColor,
                                            iconTint = Color.White
                                        ) { category ->
                                            items.value = api.appsProvider.categoriesAndApps[category.name]
                                                ?: emptyList()
                                        }
                                    }
                                }

                                is App -> {
                                    items(items.value) { item ->
                                        AppsMenuApp(
                                            app = item as App,
                                            backgroundColor = backgroundColor,
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
                    AppsBottomBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.BottomStart)
                            .background(backgroundColor)
                    )
                }
            }
            launchedEffect(menuState.isVisible) { isVisible ->
                val x = 0.dp
                val y = when (isVisible) {
                    true -> (api.containerSize.height - appMenuMinHeight)
                    else -> api.containerSize.height
                }
                val width = appMenuExpandedWidth
                val height = when (isVisible) {
                    true -> appMenuMinHeight
                    else -> 0.dp
                }
                window.setWindowBounds(x, y, width, height)
                if (!isVisible) {
                    items.value = api.appsProvider.appCategories
                }
                onVisibilityChange(isVisible)
            }
        }
    }
}