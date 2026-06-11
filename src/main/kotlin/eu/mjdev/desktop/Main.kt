package eu.mjdev.desktop

import eu.mjdev.desktop.components.main.MainWindow
import eu.mjdev.desktop.helpers.application.application

fun main(
    args: Array<String>
) {
    setAwtWindowClass()
    application(
        args = args.toList(),
        exitProcessOnExit = true
    ) {
        MainWindow()
    }
}

/**
 * Sets the X11 WM_CLASS of all AWT windows so the mjdev compositor
 * can recognize shell windows. Must run before AWT initializes.
 * Needs --add-opens java.desktop/sun.awt.X11=ALL-UNNAMED, fails silently
 * otherwise (the mjdev:: window title prefix still applies then).
 */
private fun setAwtWindowClass() = runCatching {
    val toolkit = Class.forName("sun.awt.X11.XToolkit")
    val field = toolkit.getDeclaredField("awtAppClassName")
    field.isAccessible = true
    field.set(null, "mjdev-desktop")
}
