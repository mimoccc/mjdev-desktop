package org.mjdev.desktop.components.video

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import java.nio.ByteBuffer

class RenderCallback(
    private val onImageRender: (imageBitmap: ImageBitmap?) -> Unit
) : RenderCallback {
    private var pos: Float = -1f
    private var imageBitmap: ImageBitmap? = null
    private var byteArray: ByteArray? = null

    override fun lock(mediaPlayer: MediaPlayer?) {
    }

    override fun display(
        mediaPlayer: MediaPlayer?,
        nativeBuffers: Array<out ByteBuffer>?,
        bufferFormat: BufferFormat?,
        displayWidth: Int,
        displayHeight: Int
    ) {
        val isPlaying = mediaPlayer?.status()?.isPlaying == true
        if (isPlaying) {
            pos = mediaPlayer?.status()?.position() ?: 0f
            nativeBuffers?.get(0)?.apply {
                get(byteArray)
                rewind()
            }
            imageBitmap = Bitmap().apply {
                allocPixels(
                    ImageInfo.makeN32(
                        displayWidth,
                        displayHeight,
                        ColorAlphaType.OPAQUE
                    )
                )
                installPixels(byteArray)
            }.asComposeImageBitmap().apply {
                onImageRender(this)
            }
        }
    }

    override fun unlock(mediaPlayer: MediaPlayer?) {
    }

    fun allocateBuffers(byteArray: ByteArray) {
        this.byteArray = byteArray
    }
}
