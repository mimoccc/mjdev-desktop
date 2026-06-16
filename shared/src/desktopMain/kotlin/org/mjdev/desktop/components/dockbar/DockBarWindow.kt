package org.mjdev.desktop.components.dockbar

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
        // Bottom anchor that survives the bottom-right moveBy in ChromeWindowState.size:
        // start at (containerW, containerH); the first 0 -> size grow's moveBy lands it at
        // (containerW - width, containerH - height) = (0, containerH - height).
        DpOffset(
            containerSize.width,
            containerSize.height,
        )
    }
    val mouseRange by rememberCalculated(
        containerSize,
        size,
        position,
    ) {
        MouseRange(
            x = 0.dp,
            y = containerSize.height - controlCenterDividerWidth,
            width = containerSize.width,
            height = size.height,
        )
    }
    // bounds of the *expanded* dock — used to autohide on pointer-leave (decoupled from focus,
    // per the docking UX spec). The mouseRange above is only the thin bottom reveal hotspot, so
    // it cannot tell when the pointer has left the visible dock; this range can.
    val leaveRange by rememberCalculated(containerSize) {
        val expandedHeight = panelHeight(true)
        MouseRange(
            x = 0.dp,
            y = containerSize.height - expandedHeight,
            width = containerSize.width,
            height = expandedHeight,
        )
    }
    ChromeWindow(
        name = "DockBar",
        visible = true,
        position = position,
        size = size,
        onFocusChange = onFocusChange,
        windowState = panelState,
        onCreated = {
            panelState.position = position
            panelState.size = size
        },
        isGlobalKeyHandlerEnabled = {
            panelState.isVisible && panelState.enabled
        },
        onGlobalKey = {
            onEscape {
                runAsync {
                    menuState.hide()
                    panelState.hide()
                }
                true
            }
            onMenuKey {
                runAsync {
                    panelState.showOrFocus()
                    menuState.showOrFocus()
                }
                true
            }
        },
        isGlobalMouseHandlerEnabled = {
            isUserLoggedIn && panelState.enabled
        },
        onGlobalMouse = {
            onPointerEnter(mouseRange) {
                runAsync {
                    // show() only on a real reveal (not showOrFocus): re-focusing an already-open
                    // dock churns focus-follows-mouse and flip-flops the size, drifting geometry.
                    if (menuState.isNotVisible && panelState.isNotVisible) {
                        panelState.show()
                    }
                }
            }
            // autohide when the pointer leaves the expanded dock (and no menu is open, which
            // must keep the dock alive). Driven by pointer position, not focus, so it no longer
            // gets stuck open over the non-focusable desktop.
            onPointerLeave(leaveRange) {
                runAsync {
                    if (panelState.isVisible && menuState.isNotVisible) {
                        panelState.hide()
                    }
                }
            }
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
            onFocusChange = { focused ->
                // Only re-show on a real transition into focus while hidden; re-showing an already
                // visible dock on every focus flicker (focus-follows-mouse) was a churn source.
                if (focused && panelState.isNotVisible) {
                    runAsync {
                        panelState.show()
                        if (menuState.isVisible) {
                            menuState.focus()
                        }
                    }
                }
            },
        )
    }
    LaunchedEffect(size, position) {
        panelState.size = size
    }
}

// todo
@Preview
@Composable
fun PreviewDockBarWindow() =
    preview {
        DockBarWindow()
    }
