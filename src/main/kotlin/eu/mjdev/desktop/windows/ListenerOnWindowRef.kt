package eu.mjdev.desktop.windows

import java.awt.Component
import java.awt.Window
import java.awt.event.ComponentListener
import java.awt.event.WindowListener
import java.awt.event.WindowStateListener

class ListenerOnWindowRef<T>(
    private val register: Window.(T) -> Unit,
    private val unregister: Window.(T) -> Unit
) {
    private var value: T? = null

    fun registerWithAndSet(window: Window, listener: T) {
        window.register(listener)
        value = listener
    }

    fun unregisterFromAndClear(window: Window) {
        value?.let {
            window.unregister(it)
            value = null
        }
    }

    companion object {

        fun windowStateListenerRef() = ListenerOnWindowRef<WindowStateListener>(
            register = java.awt.Window::addWindowStateListener,
            unregister = java.awt.Window::removeWindowStateListener
        )

        fun windowListenerRef() = ListenerOnWindowRef<WindowListener>(
            register = java.awt.Window::addWindowListener,
            unregister = java.awt.Window::removeWindowListener
        )

        fun componentListenerRef() = ListenerOnWindowRef<ComponentListener>(
            register = Component::addComponentListener,
            unregister = Component::removeComponentListener
        )

    }
}