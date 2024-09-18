package eu.mjdev.desktop.helpers.bitmap

import okio.Source
import okio.buffer
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.imageio.ImageIO
import javax.imageio.ImageReader

@Suppress("unused")
interface BitmapFactory {
    fun decodeSource(source: Source): Bitmap

    fun decodeByteArray(data: ByteArray, offset: Int, length: Int): Bitmap?

    companion object : BitmapFactory {
        private var instance: BitmapFactory = BitmapFactoryImageIoImpl()

        override fun decodeSource(source: Source): Bitmap {
            return instance.decodeSource(source)
        }

        override fun decodeByteArray(data: ByteArray, offset: Int, length: Int): Bitmap? {
            return instance.decodeByteArray(data, offset, length)
        }

        fun setInstance(bitmapFactory: BitmapFactory) {
            instance = bitmapFactory
        }
    }
}

private class BitmapFactoryImageIoImpl : BitmapFactory {

    override fun decodeSource(source: Source): Bitmap {
        val bitmap: Bitmap
        try {
            val imageInputStream = ImageIO.createImageInputStream(source.buffer().inputStream())
            val imageReaders: Iterator<ImageReader> = ImageIO.getImageReaders(imageInputStream)
            require(imageReaders.hasNext()) { "no reader for image" }
            val imageReader: ImageReader = imageReaders.next()
            imageReader.input = imageInputStream
            val image: BufferedImage = imageReader.read(0, imageReader.defaultReadParam)
            bitmap = Bitmap(image)
            imageReader.dispose()
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
        return bitmap
    }

    override fun decodeByteArray(data: ByteArray, offset: Int, length: Int): Bitmap {
        val byteArrayStream = ByteArrayInputStream(data)
        val bitmap: Bitmap = try {
            val image = ImageIO.read(byteArrayStream)
            Bitmap(image)
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
        return bitmap
    }
}
