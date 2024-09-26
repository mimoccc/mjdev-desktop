package eu.mjdev.desktop.provider

import androidx.compose.runtime.Composable
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

// todo theme user support
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class DesktopScope(
    val api: DesktopProvider
) {
    val isDebug: Boolean
        get() = api.isDebug

    val scope
        get() = api.scope

    val currentUser
        get() = api.currentUser

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
