package eu.mjdev.desktop.windows.blur.linux

import androidx.compose.ui.awt.ComposeWindow
import eu.mjdev.desktop.windows.blur.WindowBlurManager
import eu.mjdev.desktop.windows.blur.base.HackedContentPane
import org.jetbrains.skiko.SkiaLayer
import java.awt.AWTEvent
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Window
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.lang.reflect.Field
import java.lang.reflect.Method
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JWindow
import javax.swing.SwingUtilities

@Suppress("MemberVisibilityCanBePrivate")
class LinuxWindowBlurManager(
    val window: Window,
    blurEnabled: Boolean = false,
) : WindowBlurManager {
    override var blurEnabled: Boolean = blurEnabled
        set(value) {
            if (field != value) {
                field = value
                updateBlur()
            }
        }

    init {
        SwingUtilities.invokeLater {
            if (window is ComposeWindow) {
                window.setComposeLayerTransparency(true)
            }
            window.hackContentPane()
            updateBlur()
        }
    }

    private fun updateBlur() {
        if (blurEnabled) {
            (window.contentPane as? HackedContentPane)?.repaintBlur()
        }
    }

    val componentAdapter = object : ComponentAdapter() {
        override fun componentMoved(e: ComponentEvent?) = updateBlur(e)
        override fun componentResized(e: ComponentEvent?) = updateBlur(e)
        override fun componentShown(e: ComponentEvent?) = updateBlur(e)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun updateBlur(e: AWTEvent?) {
        if (blurEnabled) updateBlur()
    }

    init {
        window.addComponentListener(componentAdapter)
    }

    companion object {
        var Window.contentPane
            get() = when (this) {
                is JFrame -> contentPane
                is JDialog -> contentPane
                is JWindow -> contentPane
                else -> null
            }
            set(value) = when (this) {
                is JFrame -> contentPane = value
                is JDialog -> contentPane = value
                is JWindow -> contentPane = value
                else -> throw IllegalStateException()
            }

        val delegateField: Field?
            get() = runCatching {
                ComposeWindow::class.java.getDeclaredField("delegate").apply { isAccessible = true }
            }.getOrNull()

        val getLayerMethod: Method?
            get() = runCatching {
                delegateField?.type?.getDeclaredMethod("getLayer")?.apply { isAccessible = true }
            }.getOrNull()

        val getComponentMethod: Method?
            get() = runCatching {
                getLayerMethod?.returnType?.getDeclaredMethod("getComponent")
            }.getOrNull()

        val ComposeWindow.skiaLayer: SkiaLayer?
            get() {
                val delegate = delegateField?.get(this)
                val layer = getLayerMethod?.invoke(delegate)
                return getComponentMethod?.invoke(layer) as? SkiaLayer
            }

        fun ComposeWindow.setComposeLayerTransparency(isTransparent: Boolean) {
            skiaLayer?.transparency = isTransparent
        }

        fun Window.hackContentPane() {
            val oldContentPane = contentPane ?: return
            val newContentPane = HackedContentPane(this as? JFrame)
            newContentPane.name = "$name.contentPane"
            newContentPane.layout = object : BorderLayout() {
                override fun addLayoutComponent(comp: Component, constraints: Any?) {
                    super.addLayoutComponent(comp, constraints ?: CENTER)
                }
            }
            newContentPane.background = java.awt.Color(0, 0, 0, 0)
            oldContentPane.components.forEach { c ->
                newContentPane.add(c)
            }
            contentPane = newContentPane
        }
    }
}
