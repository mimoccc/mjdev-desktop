package org.mjdev.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import coil3.ImageLoader
import kotlinx.browser.document
import org.mjdev.desktop.components.background.BackgroundImage
import org.mjdev.desktop.components.qrcode.QrCodeView
import org.mjdev.desktop.extensions.Modifier.onMousePress
import org.mjdev.desktop.helpers.image.ImageLoader.asyncImageLoader

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val imageLoader: ImageLoader = asyncImageLoader()
        MainView(
            imageLoader = imageLoader
        )
    }
}

@Composable
fun MainView(
    imageLoader: ImageLoader = asyncImageLoader()
) = Box(
    modifier = Modifier.fillMaxSize()
) {
    BackgroundImage(
        imageLoader = imageLoader,
        modifier = Modifier
            .fillMaxSize()
            .onMousePress {
//                    onLeftClick {
//                        panelState.hide()
//                        menuState.hide()
//                        controlCenterState.hide()
//                    }
//                    onRightClick {
//                        contextMenuState.show()
//                    }
            },
//            switchDelay = theme.backgroundRotationDelay,
//            images = backgrounds,
//            onChange = { src ->
//                api.palette.apply {
//                    update(src)
//                }.also { p ->
//                    api.currentUser.theme.backgroundColor = p.backgroundColor
//                }
//            }
    )
    Box(
        modifier = Modifier.size(256.dp, 256.dp).align(Alignment.Center)
    ) {
        Column {
            QrCodeView(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}