package eu.mjdev.desktop.windows.blur.linux

import eu.mjdev.desktop.helpers.image.filters.GaussianFilter
import eu.mjdev.desktop.windows.blur.base.HackedContentPane
import eu.mjdev.desktop.windows.blur.linux.LinuxWindowBlurManager.Companion.contentPane
import java.awt.Container
import java.awt.Window
import java.awt.image.BufferedImage

object BlurBackground {
    fun applyBlurBackground(
        window: Window,
        blurRadius: Float = 3f,
        contentPane: Container? = window.contentPane
    ) {
//        val toolkit = Toolkit.getDefaultToolkit()
        val hackedPane = contentPane as? HackedContentPane
        val isHacked = hackedPane != null
        val width = window.width
        val height = window.height
        if (isHacked && (width > 0) && (height > 0)) {
            try {
                BufferedImage(
                    width,
                    height,
                    BufferedImage.TYPE_INT_ARGB
                ).let { image ->
                    with(image.graphics) {
                        hackedPane?.paint(this)
                        GaussianFilter(
                            blurRadius
                        ).filter(
                            image,
                            null
                        ).let { blurredImage ->
                            hackedPane?.setImage(blurredImage)
                        }
                        dispose()
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}
