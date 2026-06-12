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
    // how much the text color is calmed from the accent toward the
    // neutral inverse (0 = full accent, 1 = plain black/white)
    val textNeutralBlend: Float = 0.4f,
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
     * - accent: the most vivid color of the wallpaper (saturation
     *   weighted by occurrence) - the photorealistic touch, foregrounds
     *   carry the hue of the picture instead of plain gray
     * - icons: the accent pushed to full readability (icons are texts)
     * - text: the same hue calmed toward the neutral inverse
     * - every foreground keeps at least [minContrast] (128/255) luminance
     *   distance from the background
     */
    private fun analyze(image: ImageBitmap): ComputedPalette {
        val width = image.width
        val height = image.height
        val region = min(regionSize, min(width, height))
        val leftTop = image.analyzeRegion(0, 0, region, region)
        val rightTop = image.analyzeRegion(width - region, 0, region, region)
        val leftBottom = image.analyzeRegion(0, height - region, region, region)
        val rightBottom = image.analyzeRegion(width - region, height - region, region, region)
        val center = image.analyzeRegion((width - region) / 2, (height - region) / 2, region, region)
        val rightStrip = image.analyzeRegion(width - region, 0, region, height)

        val corners = listOf(leftTop, rightTop, leftBottom, rightBottom)
        val regions = corners + center + rightStrip
        val darkest = regions.minBy { stats -> stats.dominant.luminance() }.dominant
        val lightest = regions.maxBy { stats -> stats.dominant.luminance() }.dominant

        val background = corners.minBy { stats -> stats.dominant.nonAlphaValue }.dominant
        // the most vivid color of the whole wallpaper; dull images fall
        // back to the readable extreme so gray photos degrade gracefully
        val accent = regions.maxBy { stats -> stats.accentScore }
            .takeIf { stats -> stats.accentScore > 0f }?.accent
            ?: if (background.isLightColor) darkest else lightest
        // foreground rule: texts and icons (icons are texts) must differ
        // from the background by at least minContrast (128/255)
        val icons = ensureContrast(accent, against = background)
        val text = ensureContrast(
            lerp(
                icons,
                if (background.isLightColor) Color.Black else Color.White,
                textNeutralBlend,
            ),
            against = background,
        )
        return ComputedPalette(
            background = background,
            text = text,
            menu = leftBottom.dominant,
            controlCenter = rightStrip.dominant,
            icons = icons,
        )
    }

    private class RegionStats(
        // most frequent color of the region (surface color)
        val dominant: Color,
        // most vivid color of the region (saturation weighted by count)
        val accent: Color,
        val accentScore: Float,
    )

    /**
     * Region statistics in one pass: pixels are sampled on a [sampleGrid]
     * grid, quantized to 4 bits per channel; the most frequent bucket is
     * averaged back to a smooth dominant color, the bucket with the best
     * saturation x occurrence score becomes the accent color.
     * One pixel-map read per region, no bitmap copies.
     */
    private fun ImageBitmap.analyzeRegion(x: Int, y: Int, w: Int, h: Int): RegionStats {
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
            return RegionStats(initialColor, initialColor, 0f)
        }
        var accent = -1
        var accentScore = 0f
        for (key in 0 until BUCKET_COUNT) {
            val n = counts[key]
            if (n == 0) {
                continue
            }
            val r = sumR[key] / n
            val g = sumG[key] / n
            val b = sumB[key] / n
            val maxChannel = max(r, max(g, b))
            val minChannel = min(r, min(g, b))
            val saturation = if (maxChannel > 0f) (maxChannel - minChannel) / maxChannel else 0f
            // vivid and present beats vivid and rare; saturation squared
            // prefers truly colorful buckets over washed out ones
            val score = saturation * saturation * n
            if (score > accentScore) {
                accentScore = score
                accent = key
            }
        }
        val n = counts[best].toFloat()
        val dominant = Color(sumR[best] / n, sumG[best] / n, sumB[best] / n)
        val accentColor = if (accent < 0) dominant else {
            val an = counts[accent].toFloat()
            Color(sumR[accent] / an, sumG[accent] / an, sumB[accent] / an)
        }
        return RegionStats(dominant, accentColor, accentScore)
    }

    private fun quantize(channel: Float): Int =
        (channel * BUCKET_MAX).toInt().coerceIn(0, BUCKET_MAX)

    /**
     * Guarantees at least [minContrast] (128/255) difference against the
     * background in every rgb channel: on a dark channel the foreground
     * channel is lifted, on a light one lowered - the hue of the color
     * survives, readability is guaranteed.
     */
    private fun ensureContrast(
        color: Color,
        against: Color,
    ): Color {
        fun channel(fg: Float, bg: Float): Float =
            if (bg < 0.5f) max(fg, (bg + minContrast).coerceAtMost(1f))
            else min(fg, (bg - minContrast).coerceAtLeast(0f))
        return Color(
            red = channel(color.red, against.red),
            green = channel(color.green, against.green),
            blue = channel(color.blue, against.blue),
            alpha = color.alpha,
        )
    }

    companion object {
        // 4 bits per channel keeps the histogram at 4096 buckets
        private const val BUCKET_BITS = 4
        private const val BUCKET_MAX = (1 shl BUCKET_BITS) - 1
        private const val BUCKET_COUNT = 1 shl (3 * BUCKET_BITS)
    }
}
