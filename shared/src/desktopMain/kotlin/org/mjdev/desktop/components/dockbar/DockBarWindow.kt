package org.mjdev.desktop.components.dockbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.desktoppanel.DesktopPanel
import org.mjdev.desktop.context.DesktopContextScope
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.LaunchedEffect.runAsync
import org.mjdev.desktop.extensions.MutableStateExt.rememberCalculated
import org.mjdev.desktop.extensions.MutableStateExt.rememberComputed
import org.mjdev.desktop.extensions.PaddingValues.height
import org.mjdev.desktop.helpers.mouseevents.MouseRange
import org.mjdev.desktop.interfaces.IApp
import org.mjdev.desktop.windows.ChromeWindow
import org.mjdev.desktop.windows.ChromeWindowState
import org.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

/**
 * Dock bar window — a separate copy of
 * [org.mjdev.desktop.components.desktoppanel.DesktopPanelWindow] so the fragile original stays
 * untouched. It keeps the original's proven mechanics 1:1 (slide, hotspot, menu coupling) and
 * changes only the horizontal anchor.
 *
 * Why x = containerSize.width: [ChromeWindowState.size] pins the bottom-right corner on resize via
 * moveBy. The original started at x = 9.dp, so the first 0 -> width grow ran moveBy(width, ..) and
 * dragged the bar to 9 - width (≈ -1271) off-screen left. Starting at x = containerSize.width makes
 * that same moveBy land at width - width = 0 (left edge) — exactly mirroring how y = containerSize.height
 * lands at containerH - height (bottom edge), which already worked.
 *
 * For now this is bottom-only (full width, anchored to the bottom edge); per-edge docking via
 * panelLocation is a later step.
 */
@Suppress("FunctionName")
@Composable
fun DockBarWindow(
    iconSize: DpSize = DpSize(48.dp, 48.dp),
    iconPadding: PaddingValues = PaddingValues(4.dp),
    iconOuterPadding: PaddingValues = PaddingValues(2.dp),
    showMenuIcon: Boolean = true,
    panelState: ChromeWindowState = rememberChromeWindowState(),
    menuState: ChromeWindowState = rememberChromeWindowState(),
    controlCenterState: ChromeWindowState = rememberChromeWindowState(),
    onMenuIconClicked: () -> Unit = {
        runAsync {
            menuState.showOrFocus()
        }
    },
    onMenuIconContextMenuClicked: () -> Unit = {},
    onFocusChange: ChromeWindowState.(Boolean) -> Unit = {},
    onAppClick: DesktopContextScope.(IApp) -> Unit = { app ->
        runAsync {
            app.start()
            // Launching from the dock dismisses the apps menu, mirroring AppsMenuWindow's own
            // onAppClick — the menu should not linger over a freshly started app.
            menuState.hide()
        }
    },
    onAppContextMenuClick: (IApp) -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onTooltip: (item: Any?) -> Unit = {},
) = withDesktopContext {
    val panelHeight: (visible: Boolean) -> Dp = { visible ->
        if (visible) {
            iconSize.height +
                iconPadding.height.times(2) +
                iconOuterPadding.height.times(2) +
                theme.panelContentPadding.times(2)
        } else {
            panelDividerWidth
        }
    }
    val size by rememberComputed(
        panelState.isVisible,
        panelState.enabled,
        containerSize.width,
        containerSize.height,
    ) {
        DpSize(
            containerSize.width,
            panelHeight(
                if (panelState.enabled) {
                    panelState.isVisible
                } else {
                    true
                },
            ),
        )
    }
    val position by rememberComputed(size) {
        // True absolute bottom-anchored top-left: (0, containerH - height). Applied atomically
        // via panelState.applyBounds so it never races the size change (no relative moveBy).
        DpOffset(
            0.dp,
            containerSize.height - size.height,
        )
    }
    ChromeWindow(
        name = "DockBar",
        visible = true,
        // Always on top: the bar (and its thin reveal handle when collapsed) must sit above the
        // desktop window, or a click that raises the desktop would bury the handle and it stops
        // working.
        alwaysOnTop = true,
        position = position,
        size = size,
        onFocusChange = onFocusChange,
        windowState = panelState,
        onCreated = {
            runAsync { panelState.applyBounds(position, size) }
        },
    ) {
        DesktopPanel(
            iconSize = iconSize,
            iconPadding = iconPadding,
            iconOuterPadding = iconOuterPadding,
            showMenuIcon = showMenuIcon,
            panelState = panelState,
            onMenuIconClicked = {
                onMenuIconClicked()
            },
            onMenuIconContextMenuClicked = onMenuIconContextMenuClicked,
            onAppClick = onAppClick,
            onAppContextMenuClick = onAppContextMenuClick,
            onLanguageClick = onLanguageClick,
            onTooltip = onTooltip,
            onFocusChange = {},
        )
    }
    // Single atomic driver for the dock's geometry: position + size applied absolutely together
    // on every change, so the "grew to full height but stayed at the collapsed anchor -> only a
    // 12px line visible" race can't happen.
    LaunchedEffect(size, position) {
        panelState.applyBounds(position, size)
    }
}

// todo
@Preview
@Composable
fun PreviewDockBarWindow() = preview {
    DockBarWindow()
}
