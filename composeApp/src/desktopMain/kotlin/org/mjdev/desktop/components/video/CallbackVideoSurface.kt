package org.mjdev.desktop.components.video

import org.mjdev.desktop.log.Log
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat
import java.nio.ByteBuffer

class CallbackVideoSurface(
    private val renderCallback: RenderCallback,
    private val onNewSize: (
        bufferWidth: Int,
        bufferHeight: Int,
        displayWidth: Int,
        displayHeight: Int
    ) -> Unit = { _, _, _, _ -> }
) : BufferFormatCallback {
    override fun getBufferFormat(sourceWidth: Int, sourceHeight: Int): BufferFormat {
        Log.d("Got buffer format for source width: $sourceWidth, height: $sourceHeight")
        return RV32BufferFormat(sourceWidth, sourceHeight)
    }

    override fun newFormatSize(
        bufferWidth: Int,
        bufferHeight: Int,
        displayWidth: Int,
        displayHeight: Int
    ) {
        Log.d("New Size : bufferWidth: $bufferWidth, bufferHeight: $bufferHeight, displayWidth: $displayWidth, displayHeight: $displayHeight")
        onNewSize(bufferWidth, bufferHeight, displayWidth, displayHeight)
    }

    override fun allocatedBuffers(buffers: Array<out ByteBuffer>?) {
        val buffSize = buffers?.get(0)?.limit() ?: 0
        Log.d("Allocated buffers with size: $buffSize")
        renderCallback.allocateBuffers(ByteArray(buffSize))
    }
}
