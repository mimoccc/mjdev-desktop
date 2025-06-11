package org.mjdev.desktop.components.video

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo
import org.mjdev.desktop.log.Log
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import java.nio.ByteBuffer

class RenderCallback(
    val onImageRender: (imageBitmap: ImageBitmap?) -> Unit
) : RenderCallback {
    var pos: Float = -1f
    var imageBitmap: ImageBitmap? = null
    var byteArray: ByteArray? = null

    override fun lock(mediaPlayer: MediaPlayer?) {
        Log.d("Locked media player: ${mediaPlayer?.status()?.isPlaying}")
    }

    override fun display(
        mediaPlayer: MediaPlayer?,
        nativeBuffers: Array<out ByteBuffer>?,
        bufferFormat: BufferFormat?,
        displayWidth: Int,
        displayHeight: Int
    ) {
        Log.d("Display called with width: $displayWidth, height: $displayHeight")
        val isPlaying = mediaPlayer?.status()?.isPlaying == true
        val newPos = mediaPlayer?.status()?.position() ?: -1f
        Log.d("Display position: $newPos, isPlaying: $isPlaying")
        if (isPlaying && pos != newPos) {
            Log.d("Creating image frame at position: $newPos")
            pos = mediaPlayer?.status()?.position() ?: 0f
            Log.d("Getting data from native buffers")
            nativeBuffers?.get(0)?.apply {
                get(byteArray)
                rewind()
            }
            Log.d("Creating image bitmap with size: ${byteArray?.size ?: 0}")
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
                Log.d("Image bitmap created with width: $width, height: $height, $this")
                onImageRender(this)
            }
        }
    }

    override fun unlock(mediaPlayer: MediaPlayer?) {
        Log.d("Unlocked media player: ${mediaPlayer?.status()?.isPlaying}")
    }

    fun allocateBuffers(byteArray: ByteArray) {
        this.byteArray = byteArray
    }
}
