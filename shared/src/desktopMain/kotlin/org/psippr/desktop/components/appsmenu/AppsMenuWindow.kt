package org.psippr.desktop.components.appsmenu

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import org.psippr.desktop.components.appsmenu.AppsMenuState.Companion.rememberAppsMenuState
import org.psippr.desktop.context.DesktopContextScope
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.data.Category
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.extensions.LaunchedEffect.runAsync
import org.psippr.desktop.extensions.MutableStateExt.clear
import org.psippr.desktop.extensions.MutableStateExt.plus
import org.psippr.desktop.extensions.MutableStateExt.rememberComputed
import org.psippr.desktop.extensions.MutableStateExt.rememberState
import org.psippr.desktop.extensions.MutableStateExt.removeLast
import org.psippr.desktop.helpers.animation.Animations.AppsMenuEnterAnimation
import org.psippr.desktop.helpers.animation.Animations.AppsMenuExitAnimation
import org.psippr.desktop.interfaces.IApp
import org.psippr.desktop.windows.ChromeWindow
import org.psippr.desktop.windows.ChromeWindowState
import org.psippr.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

@Suppress("FunctionName")
@Composable
fun AppsMenuWindow(
    panelState: ChromeWindowState = rememberChromeWindowState(),
    menuState: ChromeWindowState = rememberChromeWindowState(),
    appsMenuState: AppsMenuState = rememberAppsMenuState(),
    categoryState: MutableState<Category?> = rememberState(null),
    enterAnimation: EnterTransition = AppsMenuEnterAnimation,
    exitAnimation: ExitTransition = AppsMenuExitAnimation,
    searchTextState: MutableState<String> = rememberState(""),
    onFocusChange: ChromeWindowState.(Boolean) -> Unit = {},
    // Close the menu through the desktop's single authority (DesktopState) so its intent stays in
    // sync; callers wire this to desktopState.closeMenu(). Defaults to a direct hide for previews.
    onCloseMenu: () -> Unit = { runAsync { menuState.hide() } },
    onAppClick: DesktopContextScope.(IApp) -> Unit = { app ->
        runAsync {
            app.start()
            searchTextState.clear()
            onCloseMenu()
        }
    },
    onAppContextMenuClick: DesktopContextScope.(IApp) -> Unit = {},
    onCategoryContextMenuClick: DesktopContextScope.(Category) -> Unit = {},
    onUserAvatarClick: () -> Unit = {},
    onActionClick: () -> Unit = { onCloseMenu() },
    onTooltip: (item: Any?) -> Unit = {},
) = withDesktopContext {
    val size by rememberComputed(
        appMenuMinHeight,
        appMenuMinWidth,
        panelState.x,
        panelState.height,
        menuState.isVisible,
        appsMenuState.isVisible,
    ) {
        DpSize(appMenuMinWidth, appMenuMinHeight)
    }
    val position by rememberComputed(
        panelState.x,
        panelState.height,
        menuState.isVisible,
        appsMenuState.isVisible,
        size,
    ) {
        DpOffset(
            panelState.x,
            containerSize.height - (panelState.height + appMenuMinHeight),
        ).apply {
//            println("Menu position: $this")
//            println("Menu size: $size")
        }
    }
    ChromeWindow(
        name = "AppsMenu",
        visible = menuState.isVisible,
        blur = true,
        // Not alwaysOnTop: DesktopState's focusOnShow (see MainWindow's register call) already
        // raises + focuses this window on open — a normal toFront(), not a permanent OS-level
        // pin that would keep it above external apps too.
        size = size,
        position = position,
        windowState = menuState,
        onCreated = {
            menuState.size = size
            menuState.position = position
        },
        isGlobalKeyHandlerEnabled = {
            (menuState.isVisible || appsMenuState.isVisible) &&
                (menuState.enabled || appsMenuState.enabled)
        },
        onGlobalKey = {
            // Escape and the menu/Super key are owned by DesktopState now (see DesktopStateDriver);
            // here we only handle in-menu search typing while the menu is open.
            onBack {
                appsMenuState.searchTextState.clear()
                true
            }
            onBackSpace {
                appsMenuState.searchTextState.removeLast()
                true
            }
            onDelete {
                appsMenuState.searchTextState.clear()
                true
            }
            onChar { char ->
                appsMenuState.searchTextState + char
                true
            }
        },
        onFocusChange = onFocusChange,
    ) {
        AppsMenu(
            appsMenuState = appsMenuState,
            panelState = panelState,
            enterAnimation = enterAnimation,
            exitAnimation = exitAnimation,
            onAppClick = onAppClick,
            onAppContextMenuClick = onAppContextMenuClick,
            onCategoryContextMenuClick = onCategoryContextMenuClick,
            onUserAvatarClick = onUserAvatarClick,
            onActionClick = onActionClick,
            onTooltip = onTooltip,
        )
        LaunchedEffect(menuState.isVisible) {
            if (!menuState.isVisible) {
                categoryState.value = null
            }
        }
    }
}

@Suppress("unused")
// @Preview
@Composable
fun PreviewAppsMenuWindow() =
    preview {
        AppsMenuWindow()
    }
