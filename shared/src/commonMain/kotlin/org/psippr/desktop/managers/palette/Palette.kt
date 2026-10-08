/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.psippr.desktop.managers.palette

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.psippr.desktop.context.IDesktopContext
import org.psippr.desktop.extensions.Colors.SuperDarkGray
import org.psippr.desktop.extensions.Colors.darker
import org.psippr.desktop.extensions.Colors.isLightColor
import org.psippr.desktop.extensions.Colors.lighter
import org.psippr.desktop.extensions.Colors.nonAlphaValue
import org.psippr.desktop.extensions.ImageBitmapExt.loadPicture
import org.psippr.desktop.extensions.pixels
import org.psippr.desktop.helpers.image.ImageUtils.dominantArgbColor

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate", "unused")
class Palette(
    val context: IDesktopContext,
    val scope: CoroutineScope = context.scope,
    val initialColor: Color = Color.SuperDarkGray,
    val borderFactor: Float = 0.1f,
    val borderBlend: Float = 0.5f,
    val textFactor: Float = 0.6f,
    val cutPercent: Int = 6,
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

    private val isLight
        get() = backgroundColor.isLightColor

    // accent (icons, widget rings, selection): from the most colorful part of the wallpaper,
    // moved together with background / text during a transition
    val accentColorState: MutableState<Color> = mutableStateOf(tintFor(initialColor))

    override val borderColor
        // Blend the background toward the (already-contrasting) icon tint: the border stays part
        // of the wallpaper's palette but is reliably visible on both light and dark backgrounds.
        // The old darker/lighter(borderFactor=0.1) was too subtle to see on light wallpapers.
        get() = lerp(backgroundColor, iconsTintColor, borderBlend)

    // old derivation (always a light grey on dark wallpapers) is now the fallback in tintFor()
    override val iconsTintColor
        get() = accentColorState.value

    // tint derived from the background only; used when the wallpaper has no colorful part
    // light background -> darken so the tint stays visible (the old branch lightened an
    // already-light background to ~white, making widgets like MemoryChart invisible)
    private fun tintFor(background: Color): Color = if (background.isLightColor) {
        background.darker(textFactor)
    } else {
        background.lighter(textFactor)
    }

    override val baseColor
        get() = backgroundColor

    override val selectedBgColor
        get() = iconsTintColor

    // todo
    override val selectedFgColor
        get() =
            if (selectedBgColor.isLightColor) {
                textColor
            } else {
                backgroundColor
            }

    override val tooltipBgColor
        get() = backgroundColor

    override val tooltipFgColor
        get() = textColor

    // todo
    override val disabledColor = Color.SuperDarkGray

    override val focusBorderColor: Color
        get() = selectedFgColor

    // running color transition; a newer wallpaper cancels it and continues from the current colors
    private var transitionJob: Job? = null

    // immediate change (old behaviour)
    override fun update(src: Any?) = update(src, NO_TRANSITION_MS)

    // todo cutPartPercent to user prefs as precision
    override fun update(
        src: Any?,
        transitionMs: Long,
    ) {
        transitionJob?.cancel()
        transitionJob =
            scope.launch(Dispatchers.Default) {
                context.loadPicture(src)?.let { image ->
                    val width = image.width
                    val height = image.height
                    val cutWidth = (width / 100) * cutPercent
                    val cutHeight = (height / 100) * cutPercent
                    // One bulk readPixels, then sample the four corners directly from the IntArray —
                    // no four intermediate cut() bitmaps, no per-pixel Color boxing.
                    val pixels = image.pixels
                    val colors =
                        PaletteColors(
                            dominantArgbColor(pixels, width, 0, 0, cutWidth, cutHeight),
                            dominantArgbColor(pixels, width, width - cutWidth, 0, cutWidth, cutHeight),
                            dominantArgbColor(pixels, width, 0, height - cutHeight, cutWidth, cutHeight),
                            dominantArgbColor(pixels, width, width - cutWidth, height - cutHeight, cutWidth, cutHeight),
                        )
                    val background = colors.darkestColor
                    // Derive text from the NEW background (assigned below), not the stale state value,
                    // which made the derived colors lag one wallpaper behind.
                    val text =
                        if (background.isLightColor) {
                            background.darker(textFactor)
                        } else {
                            background.lighter(textFactor)
                        }
                    val accent =
                        PaletteAccent.pick(gridSamples(pixels, width, height), background.isLightColor)
                            ?: tintFor(background)
                    transitionTo(background, text, accent, transitionMs)
                }
                createFromPalette()
            }
    }

    // Moves background + text step by step to the targets; every other color derives from these
    // two, so the whole desktop follows. Each step is one atomic snapshot: writing the two states
    // separately let the UI recompose between them (new background + stale text).
    private suspend fun transitionTo(
        background: Color,
        text: Color,
        accent: Color,
        transitionMs: Long,
    ) {
        val startBackground = backgroundColorState.value
        val startText = textColorState.value
        val startAccent = accentColorState.value
        val steps = PaletteTransition.steps(transitionMs)
        for (step in 1..steps) {
            val fraction = step.toFloat() / steps
            Snapshot.withMutableSnapshot {
                backgroundColorState.value = PaletteTransition.colorAt(startBackground, background, fraction)
                textColorState.value = PaletteTransition.colorAt(startText, text, fraction)
                accentColorState.value = PaletteTransition.colorAt(startAccent, accent, fraction)
            }
            if (step < steps) delay(PaletteTransition.FRAME_MS)
        }
    }

    override fun dispose() = clearSystemTheme()

    fun createFromPalette() = context.themeManager.createFromPalette()

    fun clearSystemTheme() = context.themeManager.clearSystemTheme()

    // accent candidates: dominant color of a small patch (cutPercent of the picture, like the
    // corners) in the middle of every cell of a GRID_CELLS x GRID_CELLS grid - small patches keep
    // the per-pixel counting cheap even for 4K wallpapers
    private fun gridSamples(
        pixels: IntArray,
        width: Int,
        height: Int,
    ): List<Color> {
        val cellWidth = width / GRID_CELLS
        val cellHeight = height / GRID_CELLS
        val patchWidth = (width / PERCENT) * cutPercent
        val patchHeight = (height / PERCENT) * cutPercent
        if (patchWidth <= 0 || patchHeight <= 0) return emptyList()
        return (0 until GRID_CELLS).flatMap { row ->
            (0 until GRID_CELLS).map { column ->
                val x = column * cellWidth + (cellWidth - patchWidth) / 2
                val y = row * cellHeight + (cellHeight - patchHeight) / 2
                dominantArgbColor(pixels, width, x, y, patchWidth, patchHeight)
            }
        }
    }

    companion object {
        /** Transition time of an immediate palette change. */
        const val NO_TRANSITION_MS = 0L

        /** Rows / columns of the grid sampled for the accent color. */
        const val GRID_CELLS = 3

        /** Divisor turning [cutPercent] into a fraction of the picture size. */
        const val PERCENT = 100
    }

    data class PaletteColors(
        val leftTopDominantColor: Color,
        val rightTopDominantColor: Color,
        val leftBottomDominantColor: Color,
        val rightBottomDominantColor: Color,
    ) {
        fun toList(): List<Color> = listOf(
            leftTopDominantColor,
            rightTopDominantColor,
            leftBottomDominantColor,
            rightBottomDominantColor,
        )

        val darkestColor: Color get() = toList().minBy { c -> c.nonAlphaValue }
        val lightestColor: Color get() = toList().maxBy { c -> c.nonAlphaValue }
    }
}
