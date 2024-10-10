/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */
package eu.mjdev.desktop.windows.blur.linux

import eu.mjdev.desktop.helpers.image.filters.GaussianFilter
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities

@Suppress("UNUSED_VARIABLE")
class BlurApp : ActionListener {
    var jf: JFrame = JFrame("Example")
    var background: JPanel = object : JPanel() {
        override fun paintComponent(g: Graphics) {
            val g2d = g as Graphics2D
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            val w = width
            val h = height
            val c1 = Color(41, 59, 102)
            val c2 = Color(2, 2, 2)
            val gp = GradientPaint(0f, 0f, c1, 0f, (h - 20).toFloat(), c2)
            g2d.paint = gp
            g2d.fillRect(0, 0, w, h)
        }
    }
    private var glassPanel: JPanel
    var button: JButton = JButton("Click me!")
    private var another: JButton = JButton("Close")
    var blurBuffer: BufferedImage? = null
    var backBuffer: BufferedImage? = null
    var currentGraphics: BufferedImage? = null
    var alpha: Float = 0.0f

    init {
        button.addActionListener(this)
        another.addActionListener(this)
        jf.setSize(900, 400)
        jf.setLocationRelativeTo(null)
        jf.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        jf.add(background)
        jf.add(button, BorderLayout.NORTH)
        glassPanel = BlurPanel()
        jf.glassPane = glassPanel
        jf.isVisible = true
    }

    override fun actionPerformed(e: ActionEvent) {
        if (e.source === button) {
            createBlur()
            jf.glassPane.isVisible = true
            val root = SwingUtilities.getRootPane(jf)
            currentGraphics = BufferedImage(root.width, root.height, BufferedImage.TYPE_3BYTE_BGR)
            jf.glassPane.isVisible = false
            glassPanel = GlassPanel()
            jf.glassPane.isVisible = true
        }
        if (e.source === another) {
            jf.glassPane.isVisible = false
        }
    }

    inner class BlurPanel : JPanel() {
        override fun paintComponent(g: Graphics) {
            if (isVisible && blurBuffer != null) {
                val g2d = g.create() as Graphics2D
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                g2d.drawImage(backBuffer, 0, 0, null)
                g2d.composite = AlphaComposite.SrcOver.derive(alpha)
                println(alpha)
                println("Function called 1")
                g2d.drawImage(blurBuffer, 0, 0, jf.width, jf.height, null)
                g2d.dispose()
            }
        }
    }

    private fun createBlur() {
        alpha = 1.0f
        val root = SwingUtilities.getRootPane(jf)
        blurBuffer = BufferedImage(jf.width, jf.height, BufferedImage.TYPE_INT_ARGB)
        val g2d = blurBuffer!!.createGraphics()
        root.paint(g2d)
        g2d.dispose()
        backBuffer = blurBuffer
        blurBuffer = GaussianFilter(5f).filter(blurBuffer, null)
    }

    inner class GlassPanel : JPanel() {
        init {
            isOpaque = false
        }

        override fun paintComponent(g: Graphics) {
            val x = 34
            val y = 34
            val w = width - 68
            val h = height - 68
            val arc = 30
            var g2 = currentGraphics!!.createGraphics()
            g2.drawImage(currentGraphics, 0, 0, null)
            g2 = g.create() as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.color = Color(0, 0, 0, 220)
            g2.fillRoundRect(x, y, w, h, arc, arc)
            g2.stroke = BasicStroke(1f)
            g2.color = Color.WHITE
            g2.drawRoundRect(x, y, w, h, arc, arc)
            g2.dispose()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val n = BlurApp()
        }
    }
}