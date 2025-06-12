package org.mjdev.desktop.components.appsmenu

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.mjdev.desktop.components.appsmenu.AppsMenuState.Companion.rememberAppsMenuState
import org.mjdev.desktop.components.appsmenu.components.AppsBottomBar
import org.mjdev.desktop.components.appsmenu.components.AppsList
import org.mjdev.desktop.components.blur.BlurPanel
import org.mjdev.desktop.components.sliding.SlidingPanel
import org.mjdev.desktop.components.sliding.base.VisibilityState
import org.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.mjdev.desktop.components.user.UserAvatar
import org.mjdev.desktop.data.Category
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Colors.darker
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.MutableStateExt.rememberComputed
import org.mjdev.desktop.extensions.MutableStateExt.rememberState
import org.mjdev.desktop.helpers.compose.Orientation
import org.mjdev.desktop.helpers.shape.BarShape
import org.mjdev.desktop.context.DesktopContextScope
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.sortByRelevance
import org.mjdev.desktop.extensions.Compose.trimIsNotEmpty
import org.mjdev.desktop.extensions.LaunchedEffect.flowBlock
import org.mjdev.desktop.helpers.animation.Animations.AppsMenuEnterAnimation
import org.mjdev.desktop.helpers.animation.Animations.AppsMenuExitAnimation
import org.mjdev.desktop.interfaces.IApp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("FunctionName")
@Composable
fun AppsMenu(
    modifier: Modifier = Modifier,
    appsMenuState: AppsMenuState = rememberAppsMenuState(),
    panelState: VisibilityState = rememberVisibilityState(),
    enterAnimation: EnterTransition = AppsMenuEnterAnimation,
    exitAnimation: ExitTransition = AppsMenuExitAnimation,
    onAppClick: DesktopContextScope.(IApp) -> Unit = { app ->
        runAsync {
            appsManager.startApp(app)
        }
    },
    onAppContextMenuClick: DesktopContextScope.(IApp) -> Unit = {},
    onCategoryContextMenuClick: DesktopContextScope.(Category) -> Unit = {},
    onUserAvatarClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
    onTooltip: (item: Any?) -> Unit = {}
) = withDesktopContext {
    val appCategories by flowBlock(emptyList()) { appsManager.categories }
    val items by rememberComputed(
        appsMenuState.searchTextState.value,
        appsMenuState.categoryState.value
    ) {
        when {
            // todo fuzzy sort, simplify
            appsMenuState.searchTextState.trimIsNotEmpty() -> appsManager.allApps.filter { app ->
                app.fullTextString.contains(appsMenuState.searchTextState.value, ignoreCase = true)
            }.sortByRelevance(appsMenuState.searchTextState.value) { name }

            appsMenuState.categoryState.value != null -> appsManager.allApps.filter { app ->
                app.categories.any { it.name.contentEquals(appsMenuState.categoryState.value?.name) }
            }.sortedBy { app ->
                app.name
            }

            else -> appCategories
        }
    }
    SlidingPanel(
        modifier = modifier
            .size(
                appMenuMinWidth,
                appMenuMinHeight
            )
//            .widthIn(
//                min = appMenuMinWidth,
//                max = containerSize.width
//            )
//            .heightIn(
//                min = appMenuMinHeight,
//                max = containerSize.height
//            )
            .padding(
                bottom = panelState.height
            ),
        enterAnimation = enterAnimation,
        exitAnimation = exitAnimation,
        state = appsMenuState
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
                        offset = 50.dp,
                        circleRadius = 32.dp,
                        cornerRadius = 4.dp,
                        circleGap = 4.dp,
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
                        iconPadding = PaddingValues(
                            bottom = 16.dp
                        ),
                        titlePadding = PaddingValues(
                            top = 24.dp,
                            start = 20.dp
                        ),
                        onUserAvatarClick = onUserAvatarClick,
                        onTooltip = onTooltip
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(
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
                    category = appsMenuState.categoryState.value,
                    listState = appsMenuState.listState,
                    onCategoryClick = { c -> appsMenuState.categoryState.value = c },
                    onAppClick = onAppClick,
                    onAppContextMenuClick = onAppContextMenuClick,
                    onCategoryContextMenuClick = onCategoryContextMenuClick,
                    onTooltip = onTooltip,
                    items = items,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomStart),
            ) {
                HorizontalDivider(
                    modifier = Modifier.padding(
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
                    onTooltip = onTooltip,
                    backButtonVisible = items.firstOrNull() is IApp,
                    searchTextState = appsMenuState.searchTextState,
                    onActionClick = onActionClick,
                    onBackClick = {
                        appsMenuState.categoryState.value = null
                    },
                    onContextMenuClick = {
                        // todo : context menu
                    }
                )
            }
        }
    }
    LaunchedEffect(appsMenuState.isVisible) {
        if (!appsMenuState.isVisible) {
            appsMenuState.categoryState.value = null
        }
    }
}

class AppsMenuState(
    visible: Boolean = false,
    enabled: Boolean = true,
    hideDelay: Long = 0L,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val listState: LazyListState = LazyListState(),
    val categoryState: MutableState<Category?> = mutableStateOf(null),
    val searchTextState: MutableState<String> = mutableStateOf(""),
    val position: DpOffset = DpOffset.Zero
) : VisibilityState(visible, enabled, hideDelay, scope) {
    companion object {
        @Composable
        fun rememberAppsMenuState(
            visible: Boolean = false,
            enabled: Boolean = true,
            hideDelay: Long = 0L,
            scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
            listState: LazyListState = rememberLazyListState(),
            categoryState: MutableState<Category?> = rememberState(null),
            searchTextState: MutableState<String> = rememberState(""),
        ) = AppsMenuState(
            visible,
            enabled,
            hideDelay,
            scope,
            listState,
            categoryState,
            searchTextState
        )
    }
}

@Preview
@Composable
fun PreviewAppsMenu() = preview {
    AppsMenu(
        appsMenuState = rememberAppsMenuState(true),
        panelState = rememberVisibilityState(true)
    )
}
