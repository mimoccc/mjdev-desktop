package eu.mjdev.desktop.provider

import androidx.compose.runtime.Composable
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

// todo theme user support
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class DesktopScope(
    val api: DesktopProvider
) {
    val isDebug
        get() = api.isDebug

    val scope
        get() = api.scope

    val containerSize
        get() = api.containerSize

    val graphicsDevice
        get() = api.graphicsDevice

    val graphicsEnvironment
        get() = api.graphicsEnvironment

    val isTransparencySupported
        get() = api.isTransparencySupported

    val osDetails
        get() = api.osDetails

    val machineName
        get() = api.machineName

    val currentUser
        get() = api.currentUser

    val homeDir
        get() = api.homeDir

    val theme
        get() = currentUser.theme

    val iconSet
        get() = theme.iconSet

    val ai
        get() = api.ai

//    val adb
//        get() = api.adbHandler

    val imageLoader
        get() = api.imageLoader

//    val scriptManager
//        get() = api.scriptManager

//    val scriptEngine
//        get() = api.scriptEngine

    val connectionManager
        get() = api.connectionManager

//    val windowsManager
//        get() = api.windowsManager

//    val dbus
//        get() = api.dbus

    val gnome
        get() = api.gnome

    val appsProvider
        get() = api.appsProvider

    val appCategories
        get() = appsProvider.appCategories

    val allApps
        get() = appsProvider.allApps

    val favoriteApps
        get() = appsProvider.favoriteApps

    val palette
        get() = api.palette

    val desktopUtils
        get() = api.desktopUtils

    val controlCenterPages
        get() = api.controlCenterPages

    val backgroundColorState
        get() = api.palette.backgroundColorState
    val backgroundColor
        get() = api.palette.backgroundColor
    val iconsTintColorState
        get() = api.palette.iconsTintColor
    val iconsTintColor
        get() = api.palette.iconsTintColor
    val textColorState
        get() = api.palette.textColorState
    val textColor
        get() = textColorState.value
    val borderColor
        get() = api.palette.borderColor
    val disabledColor
        get() = api.palette.disabledColor

    val appMenuMinWidth = theme.appMenuMinWidth
    val appMenuMinHeight = theme.appMenuMinHeight
    val menuPadding = theme.appMenuOuterPadding

    val panelAutoHideEnabled
        get() = theme.panelHideDelay > 0L

    val backgrounds
        get() = currentUser.backgrounds

    companion object {
        @Composable
        fun withDesktopScope(
            api: DesktopProvider = LocalDesktop.current,
            block: @Composable DesktopScope.() -> Unit
        ) = DesktopScope(api).apply {
            block()
        }
    }
}
