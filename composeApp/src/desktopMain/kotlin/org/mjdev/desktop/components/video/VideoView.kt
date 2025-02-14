package org.mjdev.desktop.components.video

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import org.mjdev.desktop.components.web.ComposeWebView
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.log.Log
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext

@Composable
fun VideoView(
    modifier: Modifier = Modifier,
    src: Any? = null,
    alignment: Alignment = Alignment.Center,
    alpha: Float = 1f,
    contentDescription: String? = "",
    colorFilter: ColorFilter? = null,
    contentScale: ContentScale = ContentScale.Crop,
    onFail: (Throwable) -> Unit = { e -> Log.e(e) },
    onVideoFinish: () -> Unit = {}
) = withDesktopContext {
    Box(
        modifier = modifier,
        contentAlignment = alignment
    ) {
        val url = remember(src) {
            src.toString().let {
                if (it.startsWith("http://") || it.startsWith("https://")) it
                else "file://$it"
            }
        }
        println("Playing: $url")
        ComposeWebView(
            modifier = Modifier.fillMaxSize(),
            url = url,
        )
    }
}

@Preview
@Composable
fun VideoViewPreview() = preview {
    VideoView()
}
