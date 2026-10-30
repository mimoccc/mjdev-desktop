package org.psippr.desktop.managers.display

import org.psippr.desktop.managers.base.IDelegate

/**
 * Screen resolution of the main display: what it is, what it can be, and switching it. Wired via
 * [org.psippr.desktop.managers.base.ManagerCache] like the other managers.
 */
interface IDisplayManager : IDelegate {
    /** Mode the main display runs in now, null when it cannot be read. */
    val currentMode: DisplayMode?

    /** Modes the main display offers; empty where the resolution cannot be switched. */
    val modes: List<DisplayMode>

    /** Switches the main display to [mode] (one of [modes]); false when refused or unsupported. */
    fun setMode(mode: DisplayMode): Result<Boolean>

    companion object {
        /** No-op fallback used when no platform manager is available. */
        val EMPTY =
            object : IDisplayManager {
                override val currentMode: DisplayMode? = null
                override val modes: List<DisplayMode> = emptyList()

                override fun setMode(mode: DisplayMode): Result<Boolean> = Result.success(false)
            }
    }
}
