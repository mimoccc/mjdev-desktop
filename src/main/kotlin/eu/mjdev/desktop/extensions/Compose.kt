@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package eu.mjdev.desktop.extensions

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.relocation.BringIntoViewResponder
import androidx.compose.foundation.relocation.bringIntoViewResponder
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.*
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.svg.SvgDecoder
import eu.mjdev.desktop.components.guide.GuideLines
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.helpers.compose.FocusHelper
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.DesktopScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.reflect.Modifier.isStatic
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

object Compose {

    val Color.Companion.SuperDarkGray
        get() = Color(0xff202020)

    val Color.Companion.DarkDarkGray
        get() = Color(0xff404040)

    val Color.Companion.MediumDarkGray
        get() = Color(0xff808080)

    val Color.Companion.LiteDarkGray
        get() = Color(0xffc0c0c0)

    val Color.Companion.DarkBlue
        get() = Color(0xff202060)

    fun Color.rgbToHex(): String {
        val red = (red * 255).toInt()
        val green = (green * 255).toInt()
        val blue = (blue * 255).toInt()
        return String.format("#%02x%02x%02x", red, green, blue)
    }

    val Color.hexRgb get() = rgbToHex()

    fun Color.rgbaToHex(): String {
        val alpha: Int = (alpha * 255).toInt()
        val red = (red * 255).toInt()
        val green = (green * 255).toInt()
        val blue = (blue * 255).toInt()
        return String.format("#%02x%02x%02x%02x", alpha, red, green, blue)
    }

    val Color.hexRgba get() = rgbaToHex()

    val PaddingValues.width: Dp
        get() = calculateLeftPadding(LayoutDirection.Ltr) + calculateRightPadding(LayoutDirection.Ltr)

    val Dp.sp: TextUnit
        get() = value.sp

    val PaddingValues.height: Dp
        get() = calculateTopPadding() + calculateBottomPadding()

    val PaddingValues.size: DpSize
        get() = DpSize(width, height)

    @Composable
    fun launchedEffect(
        block: suspend CoroutineScope.() -> Unit
    ) = LaunchedEffect(Unit, block)

    @Composable
    fun <T> launchedEffect(
        key: T,
        block: suspend CoroutineScope.(key: T) -> Unit
    ) = LaunchedEffect(key) { block(key) }

    @Composable
    fun <T, E> launchedEffect(
        key1: T,
        key2: E,
        block: suspend CoroutineScope.(key1: T, key2: E) -> Unit
    ) = LaunchedEffect(key1, key2) { block(key1, key2) }

    @Composable
    fun <T> textFrom(text: T?): String = when (text) {
        is MutableState<*> -> textFrom(text.value)
        else -> text.toString()
    }.toString()

    @Composable
    fun ButtonDefaults.noElevation() =
        elevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)

    @Composable
    fun ButtonDefaults.color(color: Color) = buttonColors(
        backgroundColor = color,
        contentColor = color,
        disabledContentColor = color,
        disabledBackgroundColor = color
    )

    @Composable
    fun ButtonDefaults.transparent() = buttonColors(
        backgroundColor = Color.Transparent,
        contentColor = Color.Transparent,
        disabledContentColor = Color.Transparent,
        disabledBackgroundColor = Color.Transparent
    )

    @Composable
    fun imageLoaderContext() = LocalPlatformContext.current

    @Composable
    fun imageLoaderMemoryCache() = MemoryCache.Builder()
        .maxSizePercent(imageLoaderContext(), 0.3)
        .strongReferencesEnabled(true)
        .build()

    @Composable
    fun asyncImageLoader(
        imageLoaderContext: PlatformContext = imageLoaderContext(),
        imageLoaderMemoryCache: MemoryCache = imageLoaderMemoryCache()
    ) = ImageLoader.Builder(imageLoaderContext)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .components {
            add(SvgDecoder.Factory())
//            add(GifDecoder.Factory())
        }
        .memoryCache { imageLoaderMemoryCache }
        .build()

    @Composable
    fun <T> rememberState(
        value: T,
        policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy()
    ) = remember { mutableStateOf(value, policy) }

    @Composable
    fun <T> rememberCalculated(
        vararg key: Any?,
        calculation: () -> T
    ) = remember(*key) { derivedStateOf { calculation() } }

    @Composable
    fun <T> rememberComputed(
        vararg key: Any?,
        calculation: () -> T
    ) = remember(*key) { derivedStateOf { calculation() } }

    @OptIn(ExperimentalComposeUiApi::class)
    fun Modifier.onMouseEnter(
        pass: PointerEventPass = PointerEventPass.Main,
        onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
    ): Modifier = onPointerEvent(
        eventType = PointerEventType.Enter,
        pass = pass,
        onEvent = onEvent
    )

    @OptIn(ExperimentalComposeUiApi::class)
    fun Modifier.onMouseLeave(
        pass: PointerEventPass = PointerEventPass.Main,
        onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
    ): Modifier = onPointerEvent(
        eventType = PointerEventType.Exit,
        pass = pass,
        onEvent = onEvent
    )

    @OptIn(ExperimentalComposeUiApi::class)
    fun Modifier.onMousePress(
        pass: PointerEventPass = PointerEventPass.Main,
        onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
    ): Modifier = onPointerEvent(
        eventType = PointerEventType.Press,
        pass = pass,
        onEvent = onEvent
    )

    @Suppress("UNUSED_PARAMETER")
    fun Modifier.onMouseLongPress(
        pass: PointerEventPass = PointerEventPass.Main,
        onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
    ): Modifier {
//        return onPointerEvent(
//            eventType = PointerEventType.Press,
//            pass = pass,
//            onEvent = onEvent
//        )
        return this
    }

    @OptIn(ExperimentalComposeUiApi::class)
    val AwaitPointerEventScope.isPrimary
        get() = currentEvent.button.isPrimary

    @OptIn(ExperimentalComposeUiApi::class)
    val AwaitPointerEventScope.isSecondary
        get() = currentEvent.button.isSecondary

    fun AwaitPointerEventScope.onLeftClick(
        block: AwaitPointerEventScope.() -> Unit
    ) {
        if (isPrimary) block()
    }

    fun AwaitPointerEventScope.onRightClick(
        block: AwaitPointerEventScope.() -> Unit
    ) {
        if (isSecondary) block()
    }

    fun Modifier.onKey(
        keyCode: Key,
        action: KeyEventType = KeyEventType.KeyDown,
        block: () -> Unit
    ): Modifier = this then onKeyEvent { ev ->
        if (ev.type == action) {
            if (ev.key == keyCode) {
                block()
                true
            } else false
        } else false
    }

    fun MutableState<String>.clear() {
        value = ""
    }

    fun MutableState<String>.removeLast() {
        if (value.isNotEmpty()) value = value.take(value.length - 1)
    }

    operator fun MutableState<String>.plus(text: String) {
        value += text
    }

    operator fun MutableState<String>.plus(char: Char) {
        value += char
    }

    @Composable
    fun Modifier.requestFocusOnTouch(
        focusRequester: FocusRequester,
        requestFocus: Boolean = true,
        scope: CoroutineScope = rememberCoroutineScope(),
        onTouch: (() -> Unit)? = null
    ): Modifier = this then focusRequester(
        focusRequester
    ).pointerInput(this) {
        detectTapGestures(
            onTap = {
                try {
                    scope.launch {
                        onTouch?.invoke()
                        if (requestFocus) {
                            focusRequester.requestFocus()
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    fun Modifier.bringIntoViewIfChildrenAreFocused(): Modifier = composed(
        inspectorInfo = debugInspectorInfo { name = "bringIntoViewIfChildrenAreFocused" },
        factory = {
            var myRect: Rect = Rect.Zero
            this.onSizeChanged {
                myRect = Rect(Offset.Zero, Offset(it.width.toFloat(), it.height.toFloat()))
            }.bringIntoViewResponder(
                remember {
                    object : BringIntoViewResponder {
                        @ExperimentalFoundationApi
                        override fun calculateRectForParent(localRect: Rect): Rect = myRect

                        @ExperimentalFoundationApi
                        override suspend fun bringChildIntoView(localRect: () -> Rect?) {
                        }
                    }
                }
            )
        }
    )

    @Composable
    fun rememberFocusRequester(
        key: Any? = Unit
    ) = remember(key) { FocusRequester() }

    @Composable
    fun <T> produceStateInCoroutine(
        initialValue: T,
        key: Any? = null,
        block: suspend () -> T
    ) = produceState(
        initialValue = initialValue,
        key1 = key
    ) {
        withContext(Dispatchers.IO) {
            value = block()
        }
    }

    @Composable
    fun <T> rememberDerivedState(
        key: Any? = Unit,
        initialValue: T,
        block: suspend () -> T
    ) = produceStateInCoroutine(initialValue, key, block)

    @Composable
    fun <T> rememberDerivedStateOf(
        function: () -> T,
        key: Any? = function
    ): State<T> = remember(key) { derivedStateOf(function) }

    @Composable
    fun rememberFocusState(
        initial: FocusState = FocusHelper(false)
    ) = remember(initial) {
        mutableStateOf(initial)
    }

    @Composable
    fun rememberFocusState(
        key: Any?,
        initial: FocusState = FocusHelper(false),
    ) = remember(key) {
        mutableStateOf(initial)
    }

    fun Modifier.focusState(
        focusState: MutableState<FocusState>
    ): Modifier = onFocusChanged { state ->
        focusState.value = state
    }

    val MutableState<FocusState>.isFocused
        get() = value.isFocused || value.hasFocus

    val MutableState<FocusState>.isNotFocused
        get() = !isFocused

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    val FocusState.isFocused
        get() = this.isFocused || this.hasFocus

    val FocusState.isNotFocused
        get() = !isFocused

    @Composable
    fun Modifier.dashedBorder(
        strokeWidth: Dp,
        color: Color,
        cornerRadiusDp: Dp
    ) = composed(
        factory = {
            val density = LocalDensity.current
            val strokeWidthPx = density.run { strokeWidth.toPx() }
            val cornerRadiusPx = density.run { cornerRadiusDp.toPx() }
            then(
                Modifier.drawWithCache {
                    onDrawBehind {
                        val stroke = Stroke(
                            width = strokeWidthPx,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawRoundRect(
                            color = color,
                            style = stroke,
                            cornerRadius = CornerRadius(cornerRadiusPx)
                        )
                    }
                }
            )
        }
    )

    @Suppress("FunctionName")
    @Composable
    fun <S> Crossfade(
        targetState: S,
        modifier: Modifier = Modifier,
        fadeInDuration: Long = 3000L,
        fadeOutDuration: Long = 5000L,
        contentAlignment: Alignment = Alignment.TopStart,
        label: String = "FadeInContent",
        contentKey: (targetState: S) -> Any? = { it },
        content: @Composable AnimatedContentScope.(targetState: S) -> Unit
    ) = AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            fadeIn(animationSpec = tween(durationMillis = fadeInDuration.toInt())) togetherWith
                    fadeOut(animationSpec = tween(durationMillis = fadeOutDuration.toInt()))
        },
        contentAlignment = contentAlignment,
        label = label,
        contentKey = contentKey,
        content = content
    )

    @Composable
    fun isLandscapeMode(
        api: DesktopProvider = LocalDesktop.current
    ) = api.isLandscapeMode

    @Composable
    fun rememberDpSize(
        initial: DpSize = DpSize.Zero
    ) = remember { mutableStateOf(initial) }

    fun LazyListState.scrollWithAnimTo(
        idx: Int,
        scope: CoroutineScope
    ) = scope.launch {
        animateScrollToItem(idx)
    }

    fun LazyListState.scrollWithAnimToLast(
        scope: CoroutineScope
    ) = scrollWithAnimTo(layoutInfo.totalItemsCount, scope)

    fun DesktopScope.runAsync(
        context: CoroutineContext = Dispatchers.Main,
        block: suspend () -> Unit
    ) = scope.launch(context) { block() }

    // todo
    fun Modifier.verticalTouchScrollable(
        state: LazyListState
    ) = this then pointerInput(Unit) {
        detectVerticalDragGestures { _, dragAmount ->
            println("drag ammount : $dragAmount")
            state.dispatchRawDelta(-dragAmount)
        }
    }

    fun getFunctionFromFile(
        fileName: String,
        funcName: String
    ): KFunction<*>? {
        val selfRef = ::getFunctionFromFile
        val currentClass = selfRef.javaMethod?.declaringClass
        val classDefiningFunctions = currentClass?.classLoader?.loadClass("${fileName}Kt")
        val javaMethod = classDefiningFunctions?.methods?.find {
            it.name == funcName && isStatic(it.modifiers)
        }
        return javaMethod?.kotlinFunction
    }

    fun Modifier.size(width: Int, height: Int) = size(width.dp, height.dp)

    fun Modifier.size(value: Int) = size(value.dp, value.dp)

    @Composable
    fun preview(
        isDark: Boolean = true,
        showGuide: Boolean = true,
        guideStepY: Dp = 8.dp,
        guideStepX: Dp = 8.dp,
        guideAlpha: Float = 0.3f,
        backgroundColor: Color = if (isDark) Color.SuperDarkGray else Color.White,
        guideLinesColor: Color = if (isDark) Color.White else Color.SuperDarkGray,
        gravity: Alignment = Alignment.Center,
        padding: PaddingValues = PaddingValues(16.dp),
        content: @Composable BoxScope.() -> Unit = {}
    ) = BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
            .background(backgroundColor),
        contentAlignment = gravity,
    ) {
        GuideLines(
            modifier = Modifier.fillMaxSize(),
            color = guideLinesColor.alpha(guideAlpha),
            cellSize = DpSize(guideStepX, guideStepY),
            visible = showGuide
        )
        Box(
            modifier = Modifier.padding(padding)
        ) {
            content()
        }
    }

    @Composable
    fun preview(
        width: Int,
        height: Int,
        isDark: Boolean = true,
        showGuide: Boolean = true,
        guideStepY: Dp = 8.dp,
        guideStepX: Dp = 8.dp,
        guideAlpha: Float = 0.3f,
        backgroundColor: Color = if (isDark) Color.SuperDarkGray else Color.White,
        guideLinesColor: Color = if (isDark) Color.White else Color.SuperDarkGray,
        gravity: Alignment = Alignment.Center,
        padding: PaddingValues = PaddingValues(16.dp),
        content: @Composable BoxScope.() -> Unit = {}
    ) = preview(
        isDark = isDark,
        showGuide = showGuide,
        guideStepY = guideStepY,
        guideStepX = guideStepX,
        guideAlpha = guideAlpha,
        backgroundColor = backgroundColor,
        guideLinesColor = guideLinesColor,
        gravity = gravity,
        padding = padding
    ) {
        Box(
            modifier = Modifier.size(width, height),
            content = content
        )
    }

    @Composable
    fun preview(
        size: Int,
        isDark: Boolean = true,
        showGuide: Boolean = true,
        guideStepY: Dp = 8.dp,
        guideStepX: Dp = 8.dp,
        guideAlpha: Float = 0.3f,
        backgroundColor: Color = if (isDark) Color.SuperDarkGray else Color.White,
        guideLinesColor: Color = if (isDark) Color.White else Color.SuperDarkGray,
        gravity: Alignment = Alignment.Center,
        padding: PaddingValues = PaddingValues(16.dp),
        content: @Composable BoxScope.() -> Unit = {}
    ) = preview(
        isDark = isDark,
        showGuide = showGuide,
        guideStepY = guideStepY,
        guideStepX = guideStepX,
        guideAlpha = guideAlpha,
        backgroundColor = backgroundColor,
        guideLinesColor = guideLinesColor,
        gravity = gravity,
        padding = padding
    ) {
        Box(
            modifier = Modifier.size(size, size),
            content = content
        )
    }

//    @Composable
//    fun runAsync(
//        context: CoroutineContext = Dispatchers.Main,
//        scope: CoroutineScope = rememberCoroutineScope(),
//        block: suspend () -> Unit
//    ) = scope.launch(context) { block() }

//    Toolkit.getDefaultToolkit().addAWTEventListener({ event ->
//        }, AWTEvent.MOUSE_EVENT_MASK or AWTEvent.FOCUS_EVENT_MASK
//    )

//    Toolkit.getDefaultToolkit().addAWTEventListener(object:AWTEventListener {
//        override fun eventDispatched(event: AWTEvent?) {
//            println(event)
//        }
//    }, AWTEvent.WINDOW_EVENT_MASK)

}
