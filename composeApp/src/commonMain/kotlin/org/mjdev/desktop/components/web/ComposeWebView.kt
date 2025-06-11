package org.mjdev.desktop.components.web

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext

@Suppress( "FunctionName", "unused")
@Composable
fun ComposeWebView(
    modifier: Modifier = Modifier,
    url: String = "https://m.youtube.com/watch?v=BwY6LD7KAQk",
) = withDesktopContext {
//    val state = rememberWebViewState(url)
//    val navigator: WebViewNavigator = rememberWebViewNavigator()
//    WebView(
//        modifier = modifier,
//        state = state,
//        navigator = navigator
//    )
}

@Preview
@Composable
fun PreviewComposeWebView() = preview {
    ComposeWebView()
}
