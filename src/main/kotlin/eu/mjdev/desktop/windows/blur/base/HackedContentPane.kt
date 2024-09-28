package eu.mjdev.desktop.windows.blur.base

import java.awt.AlphaComposite
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.swing.JPanel

@Suppress("MemberVisibilityCanBePrivate", "unused")
class HackedContentPane : JPanel() {
    private var image: BufferedImage? = null

//    var jlayer: JLayer<JComponent> = JLayer(JPanel(), BlurLayerUI())

    init {
        isOpaque = false
//        add(jlayer)
    }

    fun setImage(image: BufferedImage?) {
        this.image = image
    }

    fun clear() = setImage(null)

    // todo not painting nothing somehow
    override fun paint(g: Graphics) {
        if ((width <= 0) || (height <= 0)) {
            return
        }
        val gg = g.create()
        try {
            if (background.alpha != 255) {
                gg.color = background
                if (gg is Graphics2D) {
                    gg.composite = AlphaComposite.getInstance(AlphaComposite.SRC)
                }
                gg.fillRect(0, 0, width, height)
            }
            if (image != null) {
                g.drawImage(image, 0, 0, width, height, null)
            }
        } finally {
            gg.dispose()
        }
        super.paint(g)
    }

//    class BlurLayerUI : LayerUI<JComponent>() {
//        private var mOffscreenImage: BufferedImage? = null
//        private val mOperation: BufferedImageOp
//
//        init {
//            val ninth = 1.0f / 9.0f
//            val blurKernel = floatArrayOf(
//                ninth, ninth, ninth, ninth, ninth, ninth,
//                ninth, ninth, ninth
//            )
//            mOperation = ConvolveOp(
//                Kernel(3, 3, blurKernel),
//                ConvolveOp.EDGE_NO_OP, null
//            )
//        }
//
//        override fun paint(g: Graphics, c: JComponent) {
//            val w = c.width
//            val h = c.height
//            if (w == 0 || h == 0) {
//                return
//            }
//            if (mOffscreenImage == null || mOffscreenImage!!.width != w || mOffscreenImage!!.height != h) {
//                mOffscreenImage = BufferedImage(
//                    w, h,
//                    BufferedImage.TYPE_INT_RGB
//                )
//            }
//            val ig2 = mOffscreenImage!!.createGraphics()
//            ig2.clip = g.clip
//            super.paint(ig2, c)
//            ig2.dispose()
//            val g2 = g as Graphics2D
//            g2.drawImage(mOffscreenImage, mOperation, 0, 0)
//        }
//    }
}
