package org.psippr.desktop.managers.display

/** One way of reading and switching the screen mode (the psippr compositor, GNOME's mutter). */
interface IDisplayBackend {
    /** Mode in use, null when it cannot be read. */
    fun currentMode(): DisplayMode?

    /** Modes offered; empty when they cannot be switched. */
    fun modes(): List<DisplayMode>

    /** Switches to [mode]; false when refused. */
    fun setMode(mode: DisplayMode): Result<Boolean>
}
