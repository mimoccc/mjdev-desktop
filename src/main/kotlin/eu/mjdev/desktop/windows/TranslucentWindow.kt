package eu.mjdev.desktop.windows

import java.awt.Color
import java.awt.EventQueue
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.*

class TranslucentWindow {
    init {
        EventQueue.invokeLater(object : Runnable {
            override fun run() {
                try {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                    val frame = JWindow()
                    frame.isAlwaysOnTop = true
                    frame.addMouseListener(object : MouseAdapter() {
                        override fun mouseClicked(e: MouseEvent) {
                            if (e.clickCount == 2) {
                                SwingUtilities.getWindowAncestor(e.component).dispose()
                            }
                        }
                    })
                    frame.background = Color(0, 0, 0, 0)
                    frame.contentPane = TranslucentPane()
                    frame.add(JLabel(ImageIcon(ImageIO.read(javaClass.getResource("/Puppy.png")))))
                    frame.pack()
                    frame.setLocationRelativeTo(null)
                    frame.isVisible = true
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        })
    }
}