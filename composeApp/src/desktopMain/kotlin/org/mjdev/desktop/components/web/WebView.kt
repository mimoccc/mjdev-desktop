/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.web
//
//import androidx.compose.runtime.*
//import androidx.compose.runtime.snapshots.SnapshotStateList
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.awt.SwingPanel
//import javafx.application.Platform
//import javafx.concurrent.Worker.State.*
//import javafx.embed.swing.JFXPanel
//import javafx.scene.Scene
//import javafx.scene.layout.StackPane
//import javafx.scene.web.WebView
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.flow.MutableSharedFlow
//import kotlinx.coroutines.launch
//
//// todo key handler
//@Composable
//internal fun WebView(
//    state: WebViewState,
//    modifier: Modifier = Modifier,
//    navigator: WebViewNavigator = rememberWebViewNavigator(),
//    onCreated: (WebView) -> Unit = {},
//    onDispose: (WebView) -> Unit = {},
//) {
//    var webView by remember { mutableStateOf<WebView?>(null) }
//    LaunchedEffect(webView, navigator) {
//        with(navigator) {
//            webView?.handleNavigationEvents()
//        }
//    }
//    val currentOnDispose by rememberUpdatedState(onDispose)
//    webView?.let { wv ->
//        DisposableEffect(wv) {
//            onDispose { currentOnDispose(wv) }
//        }
//    }
//    SwingPanel(factory = {
//        JFXPanel().also { jfxP ->
//            Platform.runLater {
//                val rootVewView = WebView()
//                webView = rootVewView
//                rootVewView.isVisible = true
//                val root = StackPane()
//                root.children.add(webView)
//                val scene = Scene(root)
//                onCreated.invoke(rootVewView)
//                addEngineListener(rootVewView, state, navigator)
//                when (val content = state.content) {
//                    is WebContent.Url -> {
//                        val url = content.url
//                        if (url.isNotEmpty() && url != rootVewView.getCurrentUrl()) {
//                            rootVewView.load(url)
//                        }
//                    }
//
//                    is WebContent.Data -> {
//                        rootVewView.loadContent(content.data)
//                    }
//                }
//                jfxP.scene = scene
//            }
//        }
//    }, modifier = modifier) { _ ->
//    }
//}
//
//fun addEngineListener(
//    webView: WebView,
//    state: WebViewState,
//    navigator: WebViewNavigator
//) {
//    val engine = webView.engine
//    engine.loadWorker.exceptionProperty().addListener { _, _, newError ->
//        println("page load error : $newError")
//        state.errorsForCurrentRequest.add(
//            WebViewError(
//                engine.getCurrentUrl(),
//                newError.message.toString()
//            )
//        )
//    }
//    engine.setOnError { error -> println("onError : $error") }
//    engine.titleProperty().addListener { observable, oldValue, newValue ->
//        println("page load titleProperty : $newValue")
//        state.pageTitle = newValue
//    }
//    engine.loadWorker.progressProperty().addListener { observable, oldValue, newValue ->
//        println("page load progressProperty : $newValue")
//        if (newValue.toFloat() >= 0f) {
//            state.loadingState = LoadingState.Loading(newValue.toFloat())
//        }
//    }
//    engine.history.currentIndexProperty().addListener { observable, oldValue, newValue ->
//        val url = engine.getCurrentUrl()
//        if (url != null &&
//            !url.startsWith("data:text/html") &&
//            state.content.getCurrentUrl() != url
//        ) {
//            state.content = state.content.withUrl(url)
//        }
//    }
//    engine.loadWorker.stateProperty().addListener { _, _, newState ->
//        when (newState) {
//            SUCCEEDED -> {
//                state.loadingState = LoadingState.Finished
//                navigator.canGoBack = engine.canGoBack()
//                navigator.canGoForward = engine.canGoForward()
//            }
//
//            FAILED -> {}
//            RUNNING -> {
//                state.loadingState = LoadingState.Loading(0f)
//            }
//
//            CANCELLED -> {}
//            READY, SCHEDULED -> {
//                state.loadingState = LoadingState.Initializing
//                state.errorsForCurrentRequest.clear()
//            }
//        }
//    }
//}
//
//@Composable
//fun rememberWebViewNavigator(
//    coroutineScope: CoroutineScope = rememberCoroutineScope()
//): WebViewNavigator = remember(coroutineScope) { WebViewNavigator(coroutineScope) }
//
//sealed class WebContent {
//    data class Url(
//        val url: String,
//        val additionalHttpHeaders: Map<String, String> = emptyMap(),
//    ) : WebContent()
//
//    data class Data(val data: String) : WebContent()
//
//    fun getCurrentUrl(): String? {
//        return when (this) {
//            is Url -> url
//            is Data -> null
//        }
//    }
//}
//
//fun WebContent.withUrl(url: String) = when (this) {
//    is WebContent.Url -> copy(url = url)
//    else -> WebContent.Url(url)
//}
//
//@Stable
//class WebViewState(webContent: WebContent) {
//    var content: WebContent by mutableStateOf(webContent)
//    var loadingState: LoadingState by mutableStateOf(LoadingState.Finished)
//        internal set
//    val isLoading: Boolean
//        get() = loadingState !is LoadingState.Finished
//    var pageTitle: String? by mutableStateOf(null)
//        internal set
//    val errorsForCurrentRequest: SnapshotStateList<WebViewError> = mutableStateListOf()
//}
//
//@Stable
//class WebViewNavigator(private val coroutineScope: CoroutineScope) {
//    private enum class NavigationEvent { BACK, FORWARD, RELOAD, STOP_LOADING }
//
//    private val navigationEvents: MutableSharedFlow<NavigationEvent> = MutableSharedFlow()
//    suspend fun WebView.handleNavigationEvents() {
//        navigationEvents.collect { event ->
//            when (event) {
//                NavigationEvent.BACK -> {
//                    if (canGoBack) {
//                        engine.goBack()
//                    }
//                }
//
//                NavigationEvent.FORWARD -> {
//                    if (canGoForward) {
//                        engine.goForward()
//                    }
//                }
//
//                NavigationEvent.RELOAD -> engine.reload()
//                NavigationEvent.STOP_LOADING -> engine.stopLoading()
//            }
//        }
//    }
//
//    var canGoBack: Boolean by mutableStateOf(false)
//        internal set
//
//    var canGoForward: Boolean by mutableStateOf(false)
//        internal set
//
//    fun navigateBack() {
//        coroutineScope.launch { navigationEvents.emit(NavigationEvent.BACK) }
//    }
//
//    fun navigateForward() {
//        coroutineScope.launch { navigationEvents.emit(NavigationEvent.FORWARD) }
//    }
//
//    fun reload() {
//        coroutineScope.launch { navigationEvents.emit(NavigationEvent.RELOAD) }
//    }
//
//    fun stopLoading() {
//        coroutineScope.launch { navigationEvents.emit(NavigationEvent.STOP_LOADING) }
//    }
//}
//
//@Immutable
//data class WebViewError(
//    val request: String?,
//    val error: String
//)
//
//@Composable
//fun rememberWebViewState(
//    url: String,
//    additionalHttpHeaders: Map<String, String> = mapOf()
//): WebViewState = remember(url, additionalHttpHeaders) {
//    WebViewState(
//        WebContent.Url(
//            url = url,
//            additionalHttpHeaders = additionalHttpHeaders
//        )
//    )
//}
//
//@Composable
//fun rememberWebViewStateWithHTMLData(
//    data: String,
//    baseUrl: String?
//): WebViewState = remember(data) {
//    WebViewState(WebContent.Data(data))
//}
