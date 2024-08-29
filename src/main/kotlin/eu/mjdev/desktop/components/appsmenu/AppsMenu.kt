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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.custom.UserAvatar
import eu.mjdev.desktop.components.slidemenu.VisibilityState
import eu.mjdev.desktop.components.slidemenu.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.data.App
import eu.mjdev.desktop.provider.data.Category
import eu.mjdev.desktop.windows.TopWindow
import eu.mjdev.desktop.windows.TopWindowState
import eu.mjdev.desktop.windows.TopWindowState.Companion.rememberTopWindowState
import eu.mjdev.desktop.windows.WindowBounds

@Composable
fun AppsMenu(
    modifier: Modifier = Modifier,
    api: DesktopProvider = LocalDesktop.current,
    appMenuMinWidth: Dp = 480.dp, // todo
    appMenuMinHeight: Dp = 640.dp, // todo
    bottomY: Dp = 64.dp, // todo panel height
    backgroundColor: Color = api.currentUser.theme.backgroundColor,
    menuPadding: PaddingValues = PaddingValues(2.dp), // todo
    menuState: VisibilityState = rememberVisibilityState(),
    panelState: VisibilityState = rememberVisibilityState(),
    items: MutableState<List<Any>> = remember { mutableStateOf(api.appsProvider.appCategories) },
    enter: EnterTransition = fadeIn() + slideInVertically(initialOffsetY = { it }),
    exit: ExitTransition = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    windowState: TopWindowState = rememberTopWindowState(
        position = api.currentUser.theme.panelLocation.alignment,
        size = DpSize(appMenuMinWidth, appMenuMinHeight),
        exit = exit,
        enter = enter,
        visible = menuState.isVisible,
        computeBounds = { isVisible ->
            val containerHeight = api.containerSize.height
            WindowBounds(
                0.dp,
                if (isVisible) (containerHeight - appMenuMinHeight) else containerHeight,
                appMenuMinWidth,
                if (isVisible) appMenuMinHeight else 0.dp
            )
        }
    )
) = TopWindow(
    windowState = windowState,
    onFocusChange = { hasFocus -> if (!hasFocus) menuState.hide() }
) {
    Box(
        modifier = modifier
            .padding(bottom = bottomY)
            .width(appMenuMinWidth)
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
        launchedEffect(panelState.isVisible) { isVisible ->
            if (!isVisible) menuState.isVisible = false
        }
        launchedEffect(menuState.isVisible) { isVisible ->
            if (!isVisible) {
                items.value = api.appsProvider.appCategories
            }
        }
    }
}