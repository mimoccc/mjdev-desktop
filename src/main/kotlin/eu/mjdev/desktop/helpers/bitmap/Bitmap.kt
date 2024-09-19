package eu.mjdev.desktop.helpers.bitmap

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import eu.mjdev.desktop.helpers.image.ImegeScaler
import okio.Sink
import okio.buffer
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.stream.ImageOutputStream
import kotlin.math.abs

@Suppress("unused", "UNUSED_PARAMETER")
class Bitmap(
    val image: BufferedImage
) {
    val width: Int = image.width
    val height: Int = image.height
    val config: Config = Config.ARGB_8888
    val colorSpace: ColorSpace = ColorSpaces.Srgb

    enum class CompressFormat {
        JPEG,
        PNG,
        WEBP_LOSSY,
        WEBP_LOSSLESS,
    }

    enum class Config {
        ALPHA_8,
        RGB_565,
        ARGB_4444,
        ARGB_8888,
        RGBA_F16,
        HARDWARE,
        RGBA_1010102,
    }

    val allPixels: List<Color>
        get() = image.data.getPixels(0, 0, width, height, null as IntArray?).map { Color(it) }

    fun compress(format: CompressFormat, quality: Int, sink: Sink): Boolean {
        require(quality in 0..100) { "quality must be 0..100" }
        val qualityFloat = quality.toFloat() / 100
        val formatString = when (format) {
            CompressFormat.PNG -> "png"
            CompressFormat.JPEG -> "jpg"
            else -> throw IllegalArgumentException("unsupported compression format!")
        }
        val writers = ImageIO.getImageWritersByFormatName(formatString)
        check(writers.hasNext()) { "no image writers found for this format!" }
        val writer = writers.next()
        val ios: ImageOutputStream = try {
            ImageIO.createImageOutputStream(sink.buffer().outputStream())
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
        writer.output = ios
        val param = writer.defaultWriteParam
        if ("jpg" == formatString) {
            param.compressionMode = ImageWriteParam.MODE_EXPLICIT
            param.compressionQuality = qualityFloat
        }
        try {
            writer.write(null, IIOImage(image, null, null), param)
            ios.close()
            writer.dispose()
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
        return true
    }

    private fun checkPixelsAccess(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        offset: Int,
        stride: Int,
        pixels: IntArray,
    ) {
        checkXYSign(x, y)
        require(width >= 0) { "width must be >= 0" }
        if (height < 0) {
            throw IllegalArgumentException("height must be >= 0")
        }
        if (x + width > this.width) {
            throw IllegalArgumentException("x + width must be <= bitmap.width()")
        }
        if (y + height > this.height) {
            throw IllegalArgumentException("y + height must be <= bitmap.height()")
        }
        if (abs(stride) < width) {
            throw IllegalArgumentException("abs(stride) must be >= width")
        }
        val lastScanline = offset + (height - 1) * stride
        val length = pixels.size
        if (offset < 0 || offset + width > length || lastScanline < 0 || lastScanline + width > length) {
            throw ArrayIndexOutOfBoundsException()
        }
    }

    fun createScaledBitmap(
        src: Bitmap,
        dstWidth: Int,
        dstHeight: Int,
    ): Bitmap = Bitmap(ImegeScaler.resize(src.image, dstWidth, dstHeight))

    fun getPixel(x: Int, y: Int): Color = IntArray(4).let { pixelData ->
        image.data.getPixel(x, y, pixelData)
    }.let {
        Color(it[0], it[1], it[2], it[3])
    }

    fun getPixels(
        pixels: IntArray,
        offset: Int,
        stride: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        checkPixelsAccess(x, y, width, height, offset, stride, pixels)
        val raster = image.data
        val rasterPixels = raster.getPixels(x, y, width, height, null as IntArray?)
        for (ht in 0 until height) {
            val rowOffset = offset + stride * ht
            System.arraycopy(rasterPixels, ht * width, pixels, rowOffset, width)
        }
    }

    fun cut(x: Int, y: Int, w: Int, h: Int): Bitmap =
        Bitmap(ImegeScaler.crop(image, w, h))

    companion object {
        fun Bitmap.applyCanvas(block: Canvas.() -> Unit): Bitmap {
            Canvas(this).apply(block)
            return this
        }

        private fun checkXYSign(x: Int, y: Int) {
            if (x < 0) {
                throw IllegalArgumentException("x must be >= 0")
            }
            if (y < 0) {
                throw IllegalArgumentException("y must be >= 0")
            }
        }

        private fun checkWidthHeight(width: Int, height: Int) {
            if (width <= 0) {
                throw IllegalArgumentException("width must be > 0")
            }
            if (height <= 0) {
                throw IllegalArgumentException("height must be > 0")
            }
        }

        fun createBitmap(width: Int, height: Int, config: Config): Bitmap {
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            return Bitmap(image)
        }

        fun createBitmap(source: Bitmap, x: Int, y: Int, width: Int, height: Int): Bitmap {
            checkXYSign(x, y)
            checkWidthHeight(width, height)
            if (x + width > source.width) {
                throw IllegalArgumentException("x + width must be <= bitmap.width()")
            }
            if (y + height > source.height) {
                throw IllegalArgumentException("y + height must be <= bitmap.height()")
            }
            val subImage = source.image.getSubimage(x, y, width, height)
            val newImage = BufferedImage(subImage.width, subImage.height, subImage.type)
            newImage.data = subImage.data
            return Bitmap(newImage)
        }
    }
}
