package eu.mjdev.desktop.provider

import androidx.compose.runtime.mutableStateListOf
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.data.DesktopWindow
import java.awt.Desktop
import java.awt.desktop.*

// todo
@Suppress("UNUSED_PARAMETER", "unused")
class WindowsManager(
    val api: DesktopProvider,
    val desktop: Desktop = api.desktopUtils
) : AppForegroundListener, AppHiddenListener, AppReopenedListener {
    private val windows = mutableStateListOf<DesktopWindow>()

    fun init() {
        desktop.addAppEventListener(this)
    }

    fun dispose() {
        desktop.removeAppEventListener(this)
    }

    override fun appRaisedToForeground(
        e: AppForegroundEvent?
    ) {
        println(e)
    }

    override fun appMovedToBackground(
        e: AppForegroundEvent?
    ) {
        println(e)
    }

    override fun appHidden(
        e: AppHiddenEvent?
    ) {
        println(e)
    }

    override fun appUnhidden(
        e: AppHiddenEvent?
    ) {
        println(e)
    }

    override fun appReopened(
        e: AppReopenedEvent?
    ) {
        println(e)
    }

    fun isWindowFocus(
        app: App
    ) = false

    fun minimizeWindow(
        app: App
    ) = false

    fun maximizeWindow(
        app: App
    ) = false

    fun requestWindowFocus(
        app: App
    ) = false
}