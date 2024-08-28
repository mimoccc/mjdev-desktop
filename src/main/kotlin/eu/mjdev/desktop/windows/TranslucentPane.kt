package eu.mjdev.desktop.windows

import java.awt.AlphaComposite
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JPanel

class TranslucentPane : JPanel() {
    init {
        isOpaque = false
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g.create() as Graphics2D
        g2d.composite = AlphaComposite.SrcOver.derive(0.85f)
        g2d.color = background
        g2d.fillRect(0, 0, width, height)
    }
}