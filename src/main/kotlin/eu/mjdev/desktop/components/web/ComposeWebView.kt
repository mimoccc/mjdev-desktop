package eu.mjdev.desktop.components.web

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.jsbridge.IJsMessageHandler
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.jsbridge.rememberWebViewJsBridge
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebContent
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewState
import eu.mjdev.desktop.components.draggable.DraggableView
import eu.mjdev.desktop.extensions.Compose.DarkBlue
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

// todo improve
@Suppress("LocalVariableName", "FunctionName", "unused")
@Composable
fun ComposeWebView(
    modifier: Modifier = Modifier,
    url: String = "https://www.youtube.com/watch?v=BwY6LD7KAQk",
    contentPadding: PaddingValues = PaddingValues(top = 16.dp, start = 2.dp, end = 2.dp, bottom = 2.dp),
    dragEnabled: Boolean = false,
    backgroundColor: Color = Color.Transparent,
    dragBackgroundColor: Color = Color.White.copy(alpha = 0.3f),
    loadingLineColor: Color = Color.DarkBlue,
    jsBridge: IJsMessageHandler? = null,
    disablePopupWindows: Boolean = true,
) = withDesktopScope {
    DraggableView(
        modifier = modifier,
        contentPadding = contentPadding,
        dragEnabled = dragEnabled,
        backgroundColor = backgroundColor,
        dragBackgroundColor = dragBackgroundColor
    ) {
        val webViewState = remember(url) {
            if (url.startsWith("http")) WebViewState(WebContent.Url(url))
            else WebViewState(WebContent.Url("file://$url"))
        }
        val loadingState = remember(webViewState) { webViewState.loadingState }
        val _jsBridge: WebViewJsBridge = rememberWebViewJsBridge()
        if (api.kcefHelper.restartRequired.value) {
            Text(text = "Restart required.")
        } else {
            if (api.kcefHelper.initialized.value) {
                WebView(
                    modifier = modifier,
                    state = webViewState
                )
                if (loadingState is LoadingState.Loading) {
                    LinearProgressIndicator(
                        progress = loadingState.progress,
                        color = loadingLineColor,
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart)
                    )
                }
            } else {
                Box(
                    modifier = Modifier.matchParentSize()
                        .padding(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White,
                        strokeWidth = 10.dp,
                        backgroundColor = Color.Transparent
                    )
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "${api.kcefHelper.downloading.value}%"
                    )
                }
            }
        }
        LaunchedEffect(Unit) {
            api.kcefHelper.init()
            if (jsBridge != null) _jsBridge.register(jsBridge)
            webViewState.webSettings.apply {
                isJavaScriptEnabled = true
                with(desktopWebSettings) {
                    transparent = true
                    this.disablePopupWindows = disablePopupWindows
                }
            }
        }
    }
}

@Preview
@Composable
fun ComposeWebViewPreview() = ComposeWebView()
