package eu.mjdev.desktop.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import java.util.*

@Suppress("unused", "UnusedReceiverParameter")
object ColorUtils {

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

    fun Color.randomColor(alpha: Float): Color = Random().let { rnd ->
        Color(
            rnd.nextInt(256),
            rnd.nextInt(256),
            rnd.nextInt(256),
        ).alpha(alpha)
    }

}