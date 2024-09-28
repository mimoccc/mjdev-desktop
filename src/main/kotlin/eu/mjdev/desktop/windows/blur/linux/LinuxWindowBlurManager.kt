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
import javax.swing.*

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
                if (window is ComposeWindow) window.setComposeLayerTransparency(true)
                window.hackContentPane()
            updateBlur()
        }
    }

    private fun updateBlur() {
        BlurBackground.applyBlurBackground(window)
    }

    val componentAdapter = object : ComponentAdapter() {
        override fun componentMoved(e: ComponentEvent?) = resetTransparent(e)
        override fun componentResized(e: ComponentEvent?) = resetTransparent(e)
        override fun componentShown(e: ComponentEvent?) = resetTransparent(e)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun resetTransparent(e: AWTEvent?) {
        if (blurEnabled) updateBlur()
    }

    init {
        window.addComponentListener(componentAdapter)
    }

    companion object {
//        val Window.isTransparent
//            get() = when (this) {
//                is ComposeWindow -> skiaLayer?.transparency
//                else -> background.alpha != 255
//            }

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
            println("hacking window: ${this.name}")
            val oldContentPane = contentPane ?: return
            val newContentPane: JComponent = HackedContentPane()
            newContentPane.name = "$name.contentPane"
            newContentPane.layout = object : BorderLayout() {
                override fun addLayoutComponent(comp: Component, constraints: Any?) {
                    super.addLayoutComponent(comp, constraints ?: CENTER)
                }
            }
            newContentPane.background = java.awt.Color(0, 0, 0, 0)
            oldContentPane.components.forEach { newContentPane.add(it) }
            contentPane = newContentPane
        }
    }
}
