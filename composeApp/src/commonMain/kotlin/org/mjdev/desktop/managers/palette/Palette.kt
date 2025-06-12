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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.desktop.extensions.Colors.SuperDarkGray
import org.mjdev.desktop.extensions.Colors.darker
import org.mjdev.desktop.extensions.Colors.isLightColor
import org.mjdev.desktop.extensions.Colors.lighter
import org.mjdev.desktop.extensions.Colors.nonAlphaValue
import org.mjdev.desktop.extensions.ImageBitmapExt.loadPicture
import org.mjdev.desktop.helpers.image.ImageUtils.topMostColor
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.extensions.cut

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate", "unused")
class Palette(
    val context: IDesktopContext,
    val scope: CoroutineScope = context.scope,
    val initialColor: Color = Color.SuperDarkGray,
    val borderFactor: Float = 0.1f,
    val textFactor: Float = 0.6f,
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

    override val borderColor
        get() = if (isLight) backgroundColor.darker(borderFactor)
        else backgroundColor.lighter(borderFactor)

    override val iconsTintColor
        get() = if (textColor.isLightColor) textColor
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

    // todo cutPartPercent to user prefs as precision
    override fun update(
        src: Any?
    ) {
        scope.launch(Dispatchers.Default) {
            context.loadPicture(src)?.let { image ->
                val width = image.width
                val height = image.height
                val cutPercent = 5
                val cutWidth = (width / 100) * cutPercent
                val cutHeight = (height / 100) * cutPercent
                val leftTopPart = image.cut(0, 0, cutWidth, cutHeight)
                val rightTopPart = image.cut(width - cutWidth, 0, cutWidth, cutHeight)
                val leftBottomPart = image.cut(0, height - cutHeight, cutWidth, cutHeight)
                val rightBottomPart = image.cut(width - cutWidth, height - cutHeight, cutWidth, cutHeight)
                val colors = PaletteColors(
                    leftTopPart.topMostColor,
                    rightTopPart.topMostColor,
                    leftBottomPart.topMostColor,
                    rightBottomPart.topMostColor,
                )
                val background = colors.darkestColor
                val text = if (isLight) backgroundColor.darker(textFactor)
                else backgroundColor.lighter(textFactor)
                backgroundColorState.value = background
                textColorState.value = text
            }
            createFromPalette()
        }
    }

    override fun dispose() = clearSystemTheme()

    fun createFromPalette() =
        context.themeManager.createFromPalette()

    fun clearSystemTheme() =
        context.themeManager.clearSystemTheme()

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
            rightBottomDominantColor
        )

        val darkestColor: Color get() = toList().minBy { c -> c.nonAlphaValue }
        val lightestColor: Color get() = toList().maxBy { c -> c.nonAlphaValue }
    }

}
