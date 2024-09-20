package eu.mjdev.desktop.windows

import androidx.compose.ui.awt.ComposeWindow
import eu.mjdev.desktop.helpers.image.SmartBlurFilter
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.image.BufferedImage

fun ComposeWindow.applyBlurBackground(
    blurAmount: Float
) {
    println("apply blur called")
    if (blurAmount > 0) {
        addComponentListener(BlurComponentHandler(this, blurAmount))
    }
}

// todo
class BlurComponentHandler(
    val container: ComposeWindow,
    val blurAmount: Float
) : ComponentListener {
    override fun componentResized(e: ComponentEvent?) {
        apply()
    }

    override fun componentMoved(e: ComponentEvent?) {
        apply()
    }

    override fun componentShown(e: ComponentEvent?) {
        apply()
    }

    override fun componentHidden(e: ComponentEvent?) {
    }

    fun apply() {
        container.removeComponentListener(this)
        try {
            BufferedImage(
                container.getWidth(),
                container.getHeight(),
                BufferedImage.TYPE_INT_ARGB
            ).apply {
                container.paint(graphics)
            }.let { image ->
                SmartBlurFilter().filter(image, null)
            }.also { blurredImage ->
                container.graphics.drawImage(blurredImage, 0, 0, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
