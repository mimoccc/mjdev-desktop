package eu.mjdev.desktop.helpers.internal

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import eu.mjdev.desktop.extensions.BitmapUtils.cut
import eu.mjdev.desktop.extensions.BitmapUtils.topMostColor
import eu.mjdev.desktop.extensions.ColorUtils.darker
import eu.mjdev.desktop.extensions.ColorUtils.isLightColor
import eu.mjdev.desktop.extensions.ColorUtils.lighter
import eu.mjdev.desktop.extensions.ColorUtils.nonAlphaValue
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import eu.mjdev.desktop.extensions.Image.loadPicture
import eu.mjdev.desktop.provider.DesktopProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
class Palette(
    val scope: CoroutineScope,
    val baseColor: Color = Color.SuperDarkGray,
    val borderFactor: Float = 0.1f,
    val textFactor: Float = 0.6f,
) {
    val backgroundColorState = mutableStateOf(baseColor)
    var backgroundColor
        get() = backgroundColorState.value
        set(value) {
            backgroundColorState.value = value
        }

    val textColorState = mutableStateOf(if (isLight) Color.Black else Color.White)
    var textColor
        get() = textColorState.value
        set(value) {
            textColorState.value = value
        }

    private val isLight
        get() = backgroundColor.isLightColor

    val borderColor
        get() = if (isLight) backgroundColor.darker(borderFactor) else backgroundColor.lighter(borderFactor)

    val iconsTintColor
        get() = if (textColor.isLightColor) textColor else backgroundColor.lighter(textFactor)

    fun update(src: Any?) = scope.launch {
        loadPicture(src).getOrNull()?.let { image ->
            val width = image.width
            val height = image.height
            val imagePart1 = image.cut(0, 0, 64, 64)
            val imagePart2 = image.cut(width - 64, 0, 64, 64)
            val imagePart3 = image.cut(0, height - 64, 64, 64)
            val imagePart4 = image.cut(width - 64, height - 64, 64, 64)
            val colors = listOf(
                imagePart1.topMostColor,
                imagePart2.topMostColor,
                imagePart3.topMostColor,
                imagePart4.topMostColor,
            )
            val background = colors.toList().minBy { it.nonAlphaValue }
            val text = if (isLight) backgroundColor.darker(textFactor) else backgroundColor.lighter(textFactor)
            backgroundColorState.value = background
            textColorState.value = text
        }
    }

    companion object {
        @Composable
        inline fun <T> rememberPalette(
            api: DesktopProvider,
            crossinline calculation: @DisallowComposableCalls () -> T
        ) = remember(api.palette.backgroundColor) { calculation() }

        @Composable
        fun rememberBackgroundColor(
            api: DesktopProvider,
        ) = rememberPalette(api) { api.currentUser.theme.backgroundColorState }

        @Composable
        fun rememberTextColor(
            api: DesktopProvider,
        ) = rememberPalette(api) { derivedStateOf { api.palette.textColor } }

        @Composable
        fun rememberBorderColor(
            api: DesktopProvider,
        ) = rememberPalette(api) { derivedStateOf { api.palette.borderColor } }

        @Composable
        fun rememberIconTintColor(
            api: DesktopProvider,
        ) = rememberPalette(api) { derivedStateOf { api.palette.iconsTintColor } }
    }
}
