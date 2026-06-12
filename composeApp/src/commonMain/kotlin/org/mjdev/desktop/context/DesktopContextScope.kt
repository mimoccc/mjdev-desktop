package org.mjdev.desktop.context

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import org.mjdev.desktop.data.Category
import org.mjdev.desktop.interfaces.IApp
import org.mjdev.desktop.managers.apps.IAppsManager
import org.mjdev.desktop.interfaces.ILocale
import org.mjdev.desktop.managers.palette.IPalette
import org.mjdev.desktop.managers.process.IProcessManager
import org.mjdev.desktop.managers.window.IWindowsManager
import org.mjdev.desktop.interfaces.ITheme
import org.mjdev.desktop.interfaces.IUser
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// todo theme user support
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class DesktopContextScope(
    val context: IDesktopContext
) {
    val isDebug: Boolean
        get() = context.isDebug

    val isFirstStart: Boolean
        get() = context.isFirstStart

    val appArgs: List<String>
        get() = context.appArgs

    val scope: CoroutineScope
        get() = context.scope

    val containerSize: DpSize
        get() = context.containerSize

//    val osManager
//        get() = context.osManager

//    val machineName
//        get() = context.machineName

    val currentUser: IUser
        get() = context.currentUser

    val isUserLoggedIn: Boolean
        get() = currentUser.isLoggedIn

//    val homeDir
//        get() = context.homeDir

    val theme: ITheme
        get() = currentUser.theme

    val iconSet
        get() = currentUser.theme.iconSet

    val ai
        get() = context.ai

//    val adb
//        get() = api.adbHandler

    val imageLoader: ImageLoader?
        get() = context.imageLoader

//    val scriptManager
//        get() = api.scriptManager

//    val scriptEngine
//        get() = api.scriptEngine

//    val connectionManager
//        get() = context.connectionManager

    val windowsManager: IWindowsManager
        get() = context.windowsManager

//    val dbus
//        get() = api.dbus

    val currentLocale: ILocale
        get() = context.currentLocale

    val appsManager: IAppsManager
        get() = context.appsManager

    val appCategories: List<Category>
        get() = appsManager.categories

    val allApps: List<IApp>
        get() = appsManager.allApps

    val favoriteApps: List<IApp>
        get() = appsManager.favoriteApps

    val palette: IPalette
        get() = context.palette

//    val desktopUtils
//        get() = context.desktopUtils

    val controlCenterPages
        get() = context.controlCenterPages

    val backgroundColor: Color
        get() = context.palette.backgroundColor
    val menuColor: Color
        get() = context.palette.menuColor
    val controlCenterColor: Color
        get() = context.palette.controlCenterColor
    val iconsTintColor: Color
        get() = context.palette.iconsTintColor
    val textColor: Color
        get() = context.palette.textColor
    val focusedTextBackgroundColor: Color
        get() = context.palette.focusedTextBackgroundColor
    val borderColor: Color
        get() = context.palette.borderColor
    val disabledColor: Color
        get() = context.palette.disabledColor
    val focusBorderColor: Color
        get() = context.palette.textColor

    val menuPadding: Dp = context.theme.appMenuOuterPadding

    val panelHideDelay: Long
        get() = context.theme.panelHideDelay
    val windowFocusGraceDelay: Long
        get() = context.theme.windowFocusGraceDelay
    val panelAutoHideEnabled
        get() = context.theme.panelHideDelay > 0L
    val panelDividerWidth: Dp
        get() = context.theme.panelDividerWidth

    val controlPanelHideDelay: Long
        get() = context.theme.controlPanelHideDelay
    val controlCenterBackgroundAlpha: Float
        get() = context.theme.controlCenterBackgroundAlpha
    val controlCenterDividerWidth: Dp
        get() = context.theme.controlCenterDividerWidth
    val controlCenterIconSize: DpSize
        get() = context.theme.controlCenterIconSize

    val backgrounds: MutableList<Any>
        get() = context.currentUser.backgrounds

    val processManager: IProcessManager
        get() = context.processManager

    val appMenuMinWidthRatio get() = theme.appMenuMinWidthRatio
    val appMenuMinHeightRatio get() = theme.appMenuMinHeightRatio

    val appMenuMinWidth get() = (containerSize.width.value * appMenuMinWidthRatio).dp
    val appMenuMinHeight get() = (containerSize.height.value * appMenuMinHeightRatio).dp

    fun dispose() = context.dispose()

    fun runAsync(
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) = context.runAsync(coroutineContext, start, block)

    companion object {
        @Suppress("ComposableNaming")
        @Composable
        fun withDesktopContext(
            context: IDesktopContext = LocalDesktopContext.current,
            block: @Composable DesktopContextScope.() -> Unit
        ) {
            block(DesktopContextScope(context))
        }
    }
}
