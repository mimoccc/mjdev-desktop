package eu.mjdev.desktop.extensions

import androidx.compose.ui.graphics.Color
import java.util.*
import kotlin.math.cos

@Suppress("unused", "UnusedReceiverParameter")
object ColorUtils {
    val Color.randomAlpha: Float
        get() {
            val rnd = Random()
            return (1f / 255f) * (128f + (rnd.nextInt(128)))
        }

    fun Color.darker(factor: Float): Color {
        val a: Int = this.alpha.toInt()
        val r = Math.round(this.red - factor)
        val g = Math.round(this.green - factor)
        val b = Math.round(this.blue - factor)
        return Color(r, g, b, a)
    }

    fun Color.lighter(factor: Float): Color {
        val a: Int = this.alpha.toInt()
        val r = Math.round(this.red + factor)
        val g = Math.round(this.green + factor)
        val b = Math.round(this.blue + factor)
        return Color(
            if (r < 256) r else 255,
            if (g < 256) g else 255,
            if (b < 256) b else 255,
            a
        )
    }

    fun linearInterpolation(
        start: Int,
        end: Int,
        normalizedValue: Float
    ): Int {
        return (start + ((end - start) * normalizedValue)).toInt()
    }

    fun sinInterpolation(
        start: Int,
        end: Int,
        normalizedValue: Float
    ): Int {
        return ((start + (end - start) * (1 - cos(normalizedValue * Math.PI)) / 2)).toInt()
    }

    fun getAlpha(color: Int): Int {
        return (color shr 24) and 0xff
    }

    fun Color.alpha(a: Float) = alpha((255 * a).toInt())

    fun Color.alpha(alpha: Int): Color {
        return Color(
            this.red.toInt(),
            this.green.toInt(),
            this.blue.toInt(),
            (this.alpha.toInt() and 0x00ffffff) or ((alpha and 0xff) shl 24)
        )
    }

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

    fun Color.isLightColor(): Boolean {
        val r = this.red
        val g = this.green
        val b = this.blue
        return ((r > 127) && (g > 127) && (b > 127))
    }

    fun Color.randomColor(alpha: Float): Color = Random().let { rnd ->
        Color(
            rnd.nextInt(256),
            rnd.nextInt(256),
            rnd.nextInt(256),
            alpha.toInt(),
        )
    }


}