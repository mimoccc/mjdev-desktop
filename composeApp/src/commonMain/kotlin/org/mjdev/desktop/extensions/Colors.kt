package org.mjdev.desktop.extensions

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.luminance
import org.mjdev.desktop.helpers.compose.Gravity
import kotlin.math.roundToInt

object Colors {
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

    val Color.isLightColor
        get(): Boolean = luminance() > 0.5

    val Color.isDarkColor
        get() = !isLightColor

    val Color.nonAlphaValue
        get() = alpha(1f).value

    fun Color.alpha(a: Float) = copy(alpha = a)

    fun Color.red(red: Float) = copy(red = red)

    fun Color.green(red: Float) = copy(green = red)

    fun Color.blue(red: Float) = copy(blue = red)

    fun Color.r(r: Int): Color {
        return Color(
            r,
            this.green.toInt(),
            this.blue.toInt(),
            this.alpha.toInt()
        )
    }

    fun Color.g(g: Int): Color {
        return Color(
            this.red.toInt(),
            g,
            this.blue.toInt(),
            this.alpha.toInt()
        )
    }

    fun Color.b(b: Int): Color {
        return Color(
            this.red.toInt(),
            this.green.toInt(),
            b,
            this.alpha.toInt()
        )
    }

    fun Color.invert(): Color {
        val a :Int = alpha.roundToInt()
        val r :Int = 255 - red.roundToInt()
        val g :Int = 255 - green.roundToInt()
        val b :Int = 255 - blue.roundToInt()
        val color = ((a and 0xFF) shl 24) or
                ((r and 0xFF) shl 16) or
                ((g and 0xFF) shl 8) or
                (b and 0xFF)
        return Color(color)
    }

    fun Color.lighter(factor: Float) = copy(
        red = red + factor,
        green = green + factor,
        blue = blue + factor
    )

    fun Color.darker(factor: Float) = copy(
        red = red - factor,
        green = green - factor,
        blue = blue - factor
    )

    @Suppress("FunctionName")
    fun ContrastColorFilter(
        contrast: Float
    ) = ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, 0f,
                0f, contrast, 0f, 0f, 0f,
                0f, 0f, contrast, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

//    fun Color.rgbToHex(): String = copy(alpha = 1f).rgbaToHex()

//    val Color.hexRgb get() = rgbToHex()

//    @OptIn(ExperimentalStdlibApi::class)
//    fun Color.rgbaToHex(): String = value.toHexString("#%02x%02x%02x%02x")

//    val Color.hexRgba get() = rgbaToHex()

//    fun Color.randomColor(alpha: Float): Color = Random().let { rnd ->
//        Color(
//            rnd.nextInt(256),
//            rnd.nextInt(256),
//            rnd.nextInt(256),
//        ).alpha(alpha)
//    }

    fun createVerticalColorBrush(
        color: Color,
        gravity: Gravity
    ): Brush = Brush.verticalGradient(
        when (gravity) {
            Gravity.TOP -> listOf(
                color,
                color,
                color,
                color.copy(alpha = 0.8f),
                color.copy(alpha = 0.5f),
                Color.Transparent
            )

            Gravity.BOTTOM -> listOf(
                Color.Transparent,
                color.copy(alpha = 0.5f),
                color.copy(alpha = 0.8f),
                color,
                color,
                color,
            )

            else -> listOf(color)
        }
    )
}