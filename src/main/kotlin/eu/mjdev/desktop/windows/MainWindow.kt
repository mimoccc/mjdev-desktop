package eu.mjdev.desktop.windows

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import eu.mjdev.desktop.components.background.BackgroundImage
import eu.mjdev.desktop.components.slidemenu.VisibilityState
import eu.mjdev.desktop.components.window.FullScreenWindow
import eu.mjdev.desktop.helpers.Palette
import eu.mjdev.desktop.helpers.Palette.Companion.rememberPalette
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("FunctionName")
@Preview
@Composable
fun MainWindow(
    controlCenterState: VisibilityState,
    panelState: VisibilityState,
    menuState: VisibilityState,
    api: DesktopProvider = LocalDesktop.current,
    palette: Palette = rememberPalette(api.currentUser.theme.backgroundColor)
) = FullScreenWindow {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(api.currentUser.theme.backgroundColor)
    ) {
        BackgroundImage(
            modifier = Modifier
                .fillMaxSize()
                .onPointerEvent(PointerEventType.Press) {
                    panelState.hide()
                    menuState.hide()
                    controlCenterState.hide()
                },
            backgroundColor = api.currentUser.theme.backgroundColor,
            backgrounds = api.appsProvider.backgrounds + api.currentUser.config.desktopBackgroundUrls,
            switchDelay = api.currentUser.theme.backgroundRotationDelay,
            onChange = { src ->
                palette.update(src)
                api.currentUser.theme.backgroundColor = palette.backgroundColor
            }
        )
//        WidgetsPanel(
//            modifier = Modifier
//                .fillMaxSize()
//        )
    }
//    launchedEffect {
//        runCatching {
//            Notification("Hello!", "I just wanted to say hello :)").apply {
//                setUrgency(Notification.UrgencyCritical)
//            }.send()
//        }
//    }
}
