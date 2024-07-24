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
import eu.mjdev.desktop.components.SlideMenuState
import eu.mjdev.desktop.components.SlideMenuState.Companion.rememberSlideMenuState
import eu.mjdev.desktop.components.SlidingMenu
import eu.mjdev.desktop.components.UserAvatar
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.LocalDesktop
import eu.mjdev.desktop.provider.data.App
import eu.mjdev.desktop.provider.data.Category

@Composable
fun AppsMenu(
    modifier: Modifier = Modifier,
    appMenuExpandedWidth: Dp = 480.dp,
    appMenuMinHeight: Dp = 640.dp,
    bottomY: Dp = 0.dp,
    backgroundColor: Color = Color.SuperDarkGray,
    menuPadding: PaddingValues = PaddingValues(2.dp),
    state: SlideMenuState = rememberSlideMenuState(),
    api: DesktopProvider = LocalDesktop.current,
    items: MutableState<List<Any>> = remember { mutableStateOf(api.appsProvider.appCategories) },
    onVisibilityChange: (visible: Boolean) -> Unit = {},
) {
    SlidingMenu(
        modifier = modifier,
        orientation = Orientation.Vertical,
        state = state,
        onVisibilityChange = onVisibilityChange
    ) { isVisible ->
        AnimatedVisibility(
            isVisible,
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
                                            category = item as Category
                                        ) { category ->
                                            items.value =
                                                api.appsProvider.categoriesAndApps[category.name] ?: emptyList()
                                        }
                                    }
                                }

                                is App -> {
                                    items(items.value) { item ->
                                        AppsMenuApp(
                                            app = item as App
                                        ) { app ->
                                            app?.start()
                                            state.hide(0)

                                        }
                                    }
                                }
                            }
                        }
                    }
                    AppsBottomBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .align(Alignment.BottomStart)
                            .background(backgroundColor)
                    )
                }
            }
        }
    }
    launchedEffect(state.isVisible) {
        if (!state.isVisible) {
            items.value = api.appsProvider.appCategories
        }
    }
}

@Composable
fun AppsBottomBar(
    modifier: Modifier = Modifier
) = Box(
    modifier = modifier
) {
}
