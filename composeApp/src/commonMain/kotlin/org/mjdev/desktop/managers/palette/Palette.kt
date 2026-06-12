/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.palette

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mjdev.desktop.extensions.Colors.SuperDarkGray
import org.mjdev.desktop.extensions.Colors.darker
import org.mjdev.desktop.extensions.Colors.isLightColor
import org.mjdev.desktop.extensions.Colors.lighter
import org.mjdev.desktop.extensions.Colors.nonAlphaValue
import org.mjdev.desktop.extensions.ImageBitmapExt.loadPicture
import org.mjdev.desktop.context.IDesktopContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate", "unused")
class Palette(
    val context: IDesktopContext,
    val scope: CoroutineScope = context.scope,
    val initialColor: Color = Color.SuperDarkGray,
    val borderFactor: Float = 0.1f,
    val textFactor: Float = 0.6f,
    // size of the wallpaper cut-outs the component colors are taken from
    val regionSize: Int = 128,
    // sampling grid per region axis - caps the analysis cost on any image
    val sampleGrid: Int = 64,
    // minimal required difference between a color and its background (128/255)
    val minContrast: Float = 128f / 255f,
) : IPalette {
    val backgroundColorState: MutableState<Color> = mutableStateOf(initialColor)

    override var backgroundColor
        get() = backgroundColorState.value
        set(value) {
            backgroundColorState.value = value
        }

    val textColorState: MutableState<Color> =
        mutableStateOf(if (isLight) Color.Black else Color.White)

    override var textColor
        get() = textColorState.value
        set(value) {
            textColorState.value = value
        }

    val focusedTextBackgroundColorState: MutableState<Color> =
        mutableStateOf(if (isLight) Color.Black else Color.White)

    override var focusedTextBackgroundColor
        get() = textColorState.value
        set(value) {
            textColorState.value = value
        }

    // menu opens above the bottom left corner of the wallpaper
    val menuColorState: MutableState<Color> = mutableStateOf(initialColor)

    override val menuColor
        get() = menuColorState.value

    // control center slides in over the right edge of the wallpaper
    val controlCenterColorState: MutableState<Color> = mutableStateOf(initialColor)

    override val controlCenterColor
        get() = controlCenterColorState.value

    // icons follow the wallpaper center; null = no wallpaper analyzed yet
    val iconsTintColorState: MutableState<Color?> = mutableStateOf(null)

    private val isLight
        get() = backgroundColor.isLightColor

    override val borderColor
        get() = if (isLight) backgroundColor.darker(borderFactor)
        else backgroundColor.lighter(borderFactor)

    override val iconsTintColor
        get() = iconsTintColorState.value
            ?: if (textColor.isLightColor) textColor
            else backgroundColor.lighter(textFactor)

    override val baseColor
        get() = backgroundColor

    override val selectedBgColor
        get() = iconsTintColor

    // todo
    override val selectedFgColor
        get() = if (selectedBgColor.isLightColor) textColor
        else backgroundColor

    override val tooltipBgColor
        get() = backgroundColor

    override val tooltipFgColor
        get() = textColor

    // todo
    override val disabledColor = Color.SuperDarkGray

    override val focusBorderColor: Color
        get() = selectedFgColor

    override fun update(
        src: Any?
    ) {
        scope.launch(Dispatchers.Default) {
            // wallpaper analysis stays on the background dispatcher
            val computed = context.loadPicture(src)?.let { image ->
                analyze(image)
            }
            // state writes and theme regeneration happen on the ui thread,
            // writes from a background dispatcher do not reach the window
            // compositions (and the gtk theme must read the same values)
            withContext(Dispatchers.Main) {
                if (computed != null) {
                    backgroundColorState.value = computed.background
                    textColorState.value = computed.text
                    menuColorState.value = computed.menu
                    controlCenterColorState.value = computed.controlCenter
                    iconsTintColorState.value = computed.icons
                }
                createFromPalette()
            }
        }
    }

    override fun dispose() = clearSystemTheme()

    fun createFromPalette() =
        context.themeManager.createFromPalette()

    fun clearSystemTheme() =
        context.themeManager.clearSystemTheme()

    // wallpaper analysis ------------------------------------------------

    private class ComputedPalette(
        val background: Color,
        val text: Color,
        val menu: Color,
        val controlCenter: Color,
        val icons: Color,
    )

    /**
     * Extracts all component colors from the wallpaper in one go:
     * - background: the darkest corner cut-out (legacy behavior)
     * - menu: dominant color of the bottom left corner (where it opens)
     * - control center: majority color of the right edge strip
     * - icons: wallpaper center decides - light center picks the darkest
     *   wallpaper color, dark center the lightest one
     * - every on-background color is pushed to at least [minContrast]
     *   luminance distance so texts and icons stay readable
     */
    private fun analyze(image: ImageBitmap): ComputedPalette {
        val width = image.width
        val height = image.height
        val region = min(regionSize, min(width, height))
        val leftTop = image.dominantColor(0, 0, region, region)
        val rightTop = image.dominantColor(width - region, 0, region, region)
        val leftBottom = image.dominantColor(0, height - region, region, region)
        val rightBottom = image.dominantColor(width - region, height - region, region, region)
        val center = image.dominantColor((width - region) / 2, (height - region) / 2, region, region)
        val rightStrip = image.dominantColor(width - region, 0, region, height)

        val corners = listOf(leftTop, rightTop, leftBottom, rightBottom)
        val sampled = corners + center + rightStrip
        val darkest = sampled.minBy { color -> color.luminance() }
        val lightest = sampled.maxBy { color -> color.luminance() }

        val background = corners.minBy { color -> color.nonAlphaValue }
        // foreground rule: texts and icons (icons are texts) must differ
        // from the background by at least minContrast (128/255)
        val text = ensureContrast(
            if (background.isLightColor) background.darker(textFactor)
            else background.lighter(textFactor),
            against = background,
        )
        // icon base by background type: light background picks the darkest
        // wallpaper color, dark background the lightest one
        val icons = ensureContrast(
            if (background.isLightColor) darkest else lightest,
            against = background,
        )
        return ComputedPalette(
            background = background,
            text = text,
            menu = leftBottom,
            controlCenter = rightStrip,
            icons = icons,
        )
    }

    /**
     * Dominant color of a wallpaper region: pixels are sampled on a
     * [sampleGrid] grid, quantized to 4 bits per channel and the winning
     * bucket is averaged back to a smooth representative color.
     * One pixel-map read per region, no bitmap copies.
     */
    private fun ImageBitmap.dominantColor(x: Int, y: Int, w: Int, h: Int): Color {
        val map = toPixelMap(x, y, w, h)
        val stepX = max(1, w / sampleGrid)
        val stepY = max(1, h / sampleGrid)
        val counts = IntArray(BUCKET_COUNT)
        val sumR = FloatArray(BUCKET_COUNT)
        val sumG = FloatArray(BUCKET_COUNT)
        val sumB = FloatArray(BUCKET_COUNT)
        var best = -1
        var py = 0
        while (py < h) {
            var px = 0
            while (px < w) {
                val color = map[px, py]
                if (color.alpha > 0f) {
                    val key = (quantize(color.red) shl (2 * BUCKET_BITS)) or
                            (quantize(color.green) shl BUCKET_BITS) or
                            quantize(color.blue)
                    counts[key] += 1
                    sumR[key] += color.red
                    sumG[key] += color.green
                    sumB[key] += color.blue
                    if (best < 0 || counts[key] > counts[best]) {
                        best = key
                    }
                }
                px += stepX
            }
            py += stepY
        }
        if (best < 0) {
            return initialColor
        }
        val n = counts[best].toFloat()
        return Color(sumR[best] / n, sumG[best] / n, sumB[best] / n)
    }

    private fun quantize(channel: Float): Int =
        (channel * BUCKET_MAX).toInt().coerceIn(0, BUCKET_MAX)

    /**
     * Guarantees at least [minContrast] luminance difference between the
     * color and its background, pushing the color toward white on dark
     * backgrounds and toward black on light ones (closed form, no loops).
     */
    private fun ensureContrast(
        color: Color,
        against: Color,
    ): Color {
        val colorLum = color.luminance()
        val againstLum = against.luminance()
        if (abs(colorLum - againstLum) >= minContrast) {
            return color
        }
        return if (againstLum < 0.5f) {
            val target = (againstLum + minContrast).coerceAtMost(1f)
            if (colorLum >= 1f) Color.White
            else lerp(color, Color.White, ((target - colorLum) / (1f - colorLum)).coerceIn(0f, 1f))
        } else {
            val target = (againstLum - minContrast).coerceAtLeast(0f)
            if (colorLum <= 0f) Color.Black
            else lerp(color, Color.Black, ((colorLum - target) / colorLum).coerceIn(0f, 1f))
        }
    }

    companion object {
        // 4 bits per channel keeps the histogram at 4096 buckets
        private const val BUCKET_BITS = 4
        private const val BUCKET_MAX = (1 shl BUCKET_BITS) - 1
        private const val BUCKET_COUNT = 1 shl (3 * BUCKET_BITS)
    }
}
