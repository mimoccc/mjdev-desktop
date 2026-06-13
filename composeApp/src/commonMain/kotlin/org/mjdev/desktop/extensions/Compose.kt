package org.mjdev.desktop.extensions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.desktop.components.guide.GuideLines
import org.mjdev.desktop.context.DesktopContextScope
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.SuperDarkGray
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Modifier.size
import org.mjdev.desktop.helpers.fuzzywuzzy.FuzzySearch
import kotlin.coroutines.CoroutineContext

@Suppress("unused", "MemberVisibilityCanBePrivate")
object Compose {
    inline fun <reified T> Any?.orElse(block: () -> T) = if (this == null) block() else this as T

    fun <T> SnapshotStateList<T>.replaceLast(data: T) {
        this[this.size - 1] = data
    }

    inline fun <reified T : Any> MutableList<T>.addIfNotExists(
        element: T,
        equal: (e1: T, e2: T) -> Boolean = { e1, e2 -> e1 == e2 },
    ) = any { e -> equal(e, element) }.also { contains -> if (!contains) add(element) }

    fun State<String>.trimIsNotEmpty() = value.trim().isNotEmpty()

    fun <E> List<E>.sortByRelevance(
        value: String,
        block: E.() -> String = { toString() },
    ): List<E> =
        map { e ->
            Pair(FuzzySearch.ratio(block(e), value), e)
        }.sortedByDescending { p ->
            p.first
        }.map { p ->
            p.second
        }

//    operator fun PaddingValues.plus(dp: Dp) =
//        copy(left = width + dp, height = height + dp)

//    operator fun PaddingValues.minus(dp: Dp) =
//        copy(width = width - dp, height = height - dp)

    val isDesign
        @Composable
        get() = LocalInspectionMode.current

    @Composable
    fun <T> rememberDerivedStateOf(
        function: () -> T,
        key: Any? = function,
    ): State<T> = remember(key) { derivedStateOf(function) }

    @Composable
    fun rememberDpSize(initial: DpSize = DpSize.Zero) = remember { mutableStateOf(initial) }

    fun LazyListState.scrollWithAnimToLast(scope: CoroutineScope) = scrollWithAnimTo(layoutInfo.totalItemsCount, scope)

    fun LazyListState.scrollWithAnimTo(
        idx: Int,
        scope: CoroutineScope,
    ) = scope.launch {
        animateScrollToItem(idx)
    }

    // todo
    fun Modifier.verticalTouchScrollable(state: LazyListState) =
        this then
            pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
//            Log.i("drag ammount : $dragAmount")
                    state.dispatchRawDelta(-dragAmount)
                }
            }

    @Composable
    fun runAsync(
        context: CoroutineContext = Dispatchers.Main,
        scope: CoroutineScope = rememberCoroutineScope(),
        block: suspend () -> Unit,
    ) = scope.launch(context) { block() }

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
        content: @Composable AnimatedContentScope.(targetState: S) -> Unit,
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
        content = content,
    )

//    @Composable
//    @UiComposable
//    fun preview(
//        isDark: Boolean = true,
//        showGuide: Boolean = true,
//        guideStepY: Dp = 8.dp,
//        guideStepX: Dp = 8.dp,
//        guideAlpha: Float = 0.3f,
//        backgroundColor: Color = if (isDark) Color.SuperDarkGray else Color.White,
//        guideLinesColor: Color = if (isDark) Color.White else Color.SuperDarkGray,
//        gravity: Alignment = Alignment.Center,
//        padding: PaddingValues = PaddingValues(16.dp),
//        content: @Composable BoxScope.() -> Unit = {}
//    ) = BoxWithConstraints(
//        modifier = Modifier.fillMaxSize()
//            .background(backgroundColor),
//        contentAlignment = gravity,
//    ) {
//        GuideLines(
//            modifier = Modifier.fillMaxSize(),
//            color = guideLinesColor.alpha(guideAlpha),
//            cellSize = DpSize(guideStepX, guideStepY),
//            visible = showGuide
//        )
//        Box(
//            modifier = Modifier.padding(padding)
//        ) {
//            content()
//        }
//    }

    @Suppress("ComposableNaming")
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
        content: @Composable DesktopContextScope.() -> Unit = {},
    ) = withDesktopContext {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(backgroundColor),
            contentAlignment = gravity,
        ) {
            GuideLines(
                modifier = Modifier.fillMaxSize(),
                color = guideLinesColor.alpha(guideAlpha),
                cellSize = DpSize(guideStepX, guideStepY),
                visible = showGuide,
            )
            Box(
                modifier = Modifier.padding(padding),
            ) {
                content()
            }
        }
    }

    @Suppress("ComposableNaming")
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
        content: @Composable BoxScope.() -> Unit = {},
    ) = preview(
        isDark = isDark,
        showGuide = showGuide,
        guideStepY = guideStepY,
        guideStepX = guideStepX,
        guideAlpha = guideAlpha,
        backgroundColor = backgroundColor,
        guideLinesColor = guideLinesColor,
        gravity = gravity,
        padding = padding,
    ) {
        Box(
            modifier = Modifier.size(width, height),
            content = content,
        )
    }

    @Suppress("ComposableNaming")
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
        content: @Composable BoxScope.() -> Unit = {},
    ) = preview(
        isDark = isDark,
        showGuide = showGuide,
        guideStepY = guideStepY,
        guideStepX = guideStepX,
        guideAlpha = guideAlpha,
        backgroundColor = backgroundColor,
        guideLinesColor = guideLinesColor,
        gravity = gravity,
        padding = padding,
    ) {
        Box(
            modifier = Modifier.size(size, size),
            content = content,
        )
    }

//    class GreyScaleModifier : DrawModifier {
//        override fun ContentDrawScope.draw() {
//            val saturationFilter = ColorMatrix().apply {
//                setToSaturation(0f)
//            }.let { cm ->
//                ColorFilter.colorMatrix(cm)
//            }
//            val paint = Paint().apply {
//                colorFilter = saturationFilter
//            }
//            drawIntoCanvas { canvas ->
//                val bounds = Rect(0f, 0f, size.width, size.height)
//                canvas.saveLayer(bounds, paint)
//                drawContent()
//                canvas.restore()
//            }
//        }
//    }

//    fun Modifier.greyScale(): Modifier = this then GreyScaleModifier()

    fun Modifier.grayScale(): Modifier =
        this then
            drawWithContent {
                val saturationFilter =
                    ColorMatrix()
                        .apply {
                            setToSaturation(0f)
                        }.let { cm ->
                            ColorFilter.colorMatrix(cm)
                        }
                val paint =
                    Paint().apply {
                        colorFilter = saturationFilter
                    }
                drawIntoCanvas { canvas ->
                    val bounds = Rect(0f, 0f, size.width, size.height)
                    canvas.saveLayer(bounds, paint)
                    drawContent()
                    canvas.restore()
                }
            }

    // todo
    fun Modifier.applyTransform(transform: (bitmap: ImageBitmap) -> ImageBitmap = { bitmap -> bitmap }): Modifier =
        this then
            drawWithContent {
                val paint = Paint()
                val size = IntSize(size.width.toInt(), size.height.toInt())
                var bitmap = ImageBitmap(size.width, size.height)
                drawIntoCanvas { canvas ->
                    canvas.drawImage(bitmap, Offset.Zero, paint)
                }
                bitmap = transform(bitmap)
                drawIntoCanvas { canvas ->
                    canvas.drawImage(bitmap, Offset.Zero, paint)
                }
            }

    // todo
    fun Modifier.dither(): Modifier =
        this then
            drawWithContent {
                val paint = Paint()
                val size = IntSize(size.width.toInt(), size.height.toInt())
                val bitmap = ImageBitmap(size.width, size.height)
                val pixels = IntArray(size.width * size.height)
                val bayerMatrix =
                    arrayOf(
                        arrayOf(0, 128, 32, 160),
                        arrayOf(192, 64, 224, 96),
                        arrayOf(48, 176, 16, 144),
                        arrayOf(240, 112, 208, 80),
                    )
                drawIntoCanvas { canvas ->
                    canvas.drawImage(bitmap, Offset.Zero, paint)
                }
                bitmap.readPixels(pixels)
                val ditheredPixels =
                    pixels
                        .mapIndexed { index, pixel ->
                            val x = index % size.width
                            val y = index / size.width
                            val color = Color(pixel)
                            val intensity = (color.red * 255).toInt()
                            val threshold = bayerMatrix[y % 4][x % 4]
                            if (intensity > threshold) Color.White else Color.Black
                        }.toTypedArray()
                drawIntoCanvas { canvas ->
                    for (y in 0 until size.height) {
                        for (x in 0 until size.width) {
                            paint.color = ditheredPixels[y * size.width + x]
                            canvas.drawRect(
                                Rect(
                                    x.toFloat(),
                                    y.toFloat(),
                                    (x + 1).toFloat(),
                                    (y + 1).toFloat(),
                                ),
                                paint,
                            )
                        }
                    }
                }
            }
}
