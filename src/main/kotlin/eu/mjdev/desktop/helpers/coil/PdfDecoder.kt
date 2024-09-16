@file:Suppress("unused")

package eu.mjdev.desktop.helpers.coil

import coil3.DrawableImage
import coil3.Image
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.decode.DecodeResult
import coil3.decode.DecodeUtils
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.size.Dimension
import coil3.size.Scale
import coil3.size.isOriginal
import coil3.size.pxOrElse
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Data
import org.jetbrains.skia.Rect
import org.jetbrains.skia.svg.SVGDOM
import org.jetbrains.skia.svg.SVGLength
import org.jetbrains.skia.svg.SVGLengthUnit
import kotlin.math.roundToInt

// todo from svg
class PdfDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val useViewBoundsAsIntrinsicSize: Boolean,
) : Decoder {
    @OptIn(ExperimentalCoilApi::class)
    override suspend fun decode(): DecodeResult {
//        val context = options.context
        val bytes = source.source().readByteArray()
        val pdf = SVGDOM(Data.makeFromBytes(bytes))
        val pdfWidth: Float
        val pdfHeight: Float
        val viewBox: Rect? = pdf.root?.viewBox
        if (useViewBoundsAsIntrinsicSize && viewBox != null) {
            pdfWidth = viewBox.width
            pdfHeight = viewBox.height
        } else {
            pdfWidth = pdf.root?.width?.value ?: 0f
            pdfHeight = pdf.root?.height?.value ?: 0f
        }
        val bitmapWidth: Int
        val bitmapHeight: Int
        val (dstWidth, dstHeight) = getDstSize(pdfWidth, pdfHeight, options.scale)
        if (pdfWidth > 0f && pdfHeight > 0f) {
            val multiplier = DecodeUtils.computeSizeMultiplier(
                srcWidth = pdfWidth,
                srcHeight = pdfHeight,
                dstWidth = dstWidth.toFloat(),
                dstHeight = dstHeight.toFloat(),
                scale = options.scale,
            )
            bitmapWidth = (multiplier * pdfWidth).toInt()
            bitmapHeight = (multiplier * pdfHeight).toInt()
        } else {
            bitmapWidth = dstWidth
            bitmapHeight = dstHeight
        }
        if (viewBox == null && pdfWidth > 0f && pdfHeight > 0f) {
            pdf.root?.viewBox = Rect.makeWH(pdfWidth, pdfHeight)
        }
        pdf.root?.width = SVGLength(
            value = 100f,
            unit = SVGLengthUnit.PERCENTAGE,
        )
        pdf.root?.height = SVGLength(
            value = 100f,
            unit = SVGLengthUnit.PERCENTAGE,
        )
        pdf.setContainerSize(bitmapWidth.toFloat(), bitmapHeight.toFloat())
        return DecodeResult(
            image = pdf.asCoilImage(bitmapWidth, bitmapHeight),
            isSampled = false,
        )
    }

    private fun getDstSize(srcWidth: Float, srcHeight: Float, scale: Scale): Pair<Int, Int> {
        if (options.size.isOriginal) {
            val dstWidth = if (srcWidth > 0) srcWidth.roundToInt() else PDF_DEFAULT_SIZE
            val dstHeight = if (srcHeight > 0) srcHeight.roundToInt() else PDF_DEFAULT_SIZE
            return dstWidth to dstHeight
        } else {
            val (dstWidth, dstHeight) = options.size
            return dstWidth.toPx(scale) to dstHeight.toPx(scale)
        }
    }

    class Factory(
        private val useViewBoundsAsIntrinsicSize: Boolean = true,
    ) : Decoder.Factory {
        override fun create(result: SourceFetchResult, options: Options, imageLoader: ImageLoader): Decoder? {
            if (!isApplicable(result)) return null
            return PdfDecoder(result.source, options, useViewBoundsAsIntrinsicSize)
        }

        private fun isApplicable(result: SourceFetchResult): Boolean =
            result.mimeType == MIME_TYPE_PDF
    }

    @OptIn(ExperimentalCoilApi::class)
    private class PdfImage(
        val pdf: SVGDOM,
        override val width: Int,
        override val height: Int,
    ) : DrawableImage() {
        override val size: Long
            get() = 4L * width * height

        override val shareable: Boolean = true

        @ExperimentalCoilApi
        override fun Canvas.onDraw() {
            pdf.render(this)
        }
    }

    companion object {
        private const val PDF_DEFAULT_SIZE = 512
        private const val MIME_TYPE_PDF = "application/pdf"

        private fun Dimension.toPx(scale: Scale): Int = pxOrElse {
            when (scale) {
                Scale.FILL -> Int.MIN_VALUE
                Scale.FIT -> Int.MAX_VALUE
            }
        }

        @OptIn(ExperimentalCoilApi::class)
        internal fun SVGDOM.asCoilImage(
            width: Int,
            height: Int,
        ): Image = PdfImage(
            pdf = this,
            width = width,
            height = height,
        )
    }
}