package eu.mjdev.desktop.windows.blur.base

import javax.swing.JFrame
import javax.swing.JPanel

@Suppress("MemberVisibilityCanBePrivate", "unused")
class HackedContentPane(
    val window: JFrame?
) : JPanel() {
//    val glassPanel = GlassPanel(window)
//    var blurImage: BufferedImage? = null

    init {
        isOpaque = false
//        add(glassPanel)
    }

    fun repaintBlur() {
//        if (window != null && window.width > 0 && window.height > 0) {
//            Log.i("creating blur image")
//            BufferedImage(
//                window.width,
//                window.height,
//                BufferedImage.TYPE_INT_ARGB
//            ).also { image ->
//                window.paint(image.graphics)
//                GaussianFilter(5f).filter(image, null)
//            }.also { blurBuffer ->
//                blurImage = blurBuffer
//            }
//            glassPanel.setSize(window.width, window.height)
//        } else {
//            glassPanel.setSize(0, 0)
//        }
    }

//    inner class GlassPanel(
//        private var window: JFrame?
//    ) : JLabel() {
//        init {
//            isOpaque = false
//            setSize(window?.width ?: 0, window?.height ?: 0)
//        }
//
//        override fun paintComponent(g: Graphics?) {
//            Log.i("paint called")
//            if (window != null && blurImage != null && width > 1 && height > 1) {
//                Log.i("painting blur")
//                g?.color = Color.WHITE
//                g?.fillRect(0,0,window!!.width, window!!.height)
//                g?.drawImage(blurImage, 0, 0, window!!.width, window!!.height, null)
//            }
//        }
//    }
}