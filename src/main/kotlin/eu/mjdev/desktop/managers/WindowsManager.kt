/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

@file:Suppress("LocalVariableName")

package eu.mjdev.desktop.managers

import com.sun.jna.platform.unix.X11.*
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.provider.DesktopProvider
import kotlinx.coroutines.*

@Suppress("unused")
class WindowsManager(
    val api: DesktopProvider,
    val scope: CoroutineScope = api.scope,
) {
//    private var loop: Job? = null
//    private val x11: X11 by lazy { Native.load("X11", X11::class.java) }
//    private val x11Ext by lazy { Native.load("X11", X11Ext::class.java) }
//    private val display: Display by lazy { x11.XOpenDisplay(null) }

//    private val rootWindow: Window
//        get() = x11.XDefaultRootWindow(display)

//    val activeWindow
//        get() = get_active_window()

//    val windows = getAllWindows(rootWindow, 0)

//    val environment: Environment = Environment()
//    val currentDisplay
//        get() = environment["WAYLAND_DISPLAY"]
//    val WlRegistryEvents = object : WlRegistryEvents {
//        override fun global(emitter: WlRegistryProxy?, name: Int, interface_: String, version: Int) {
//            println("Got global event: $name")
//        }
//
//        override fun globalRemove(emitter: WlRegistryProxy?, name: Int) {
//            println("Got global remove event: $name")
//        }
//    }
//    val display: WlDisplayProxy by lazy { WlDisplayProxy.connect(currentDisplay) }
//    val registry: WlRegistryProxy by lazy { display.getRegistry(WlRegistryEvents) }

    // todo
    val windows
        get() = findWindows()

    // todo
    private val activeWindow: SystemWindow?
        get() = null

    // todo
    private fun findWindows(app: App? = null): List<SystemWindow> = listOf<SystemWindow>().let { list ->
        if (app == null) list else list.filter { w -> w.windowClass == app.windowClass }
    }

//    init {
//        println("Current display : $currentDisplay")
//        println("Registry : ${registry.id}")
//    }

    fun dispose() {
//        loop?.cancel()
//        x11.XCloseDisplay(display)
    }

//    fun findWindows(app: App): List<SystemWindow> = windows.filter { w ->
//        println("w: (${w.id},${w.windowClass}, ${w.command})")
//        w.windowClass.isNotEmpty()
//    }.filter { w ->
//        w.windowClass.trim().contains(app.windowClass, true) ||
//                w.command.trim().contains(app.windowClass, true) ||
//                w.command.trim().contains(app.fullAppName, true) ||
//                w.command.trim().contentEquals(app.cmd, true)
//    }.apply {
//        println("app[${app.windowClass}] : ${count()}")
//    }

    fun hasWindow(app: App): Boolean =
        findWindows(app).isNotEmpty()

    fun isWindowFocus(app: App): Boolean {
        val appWindow = windows.firstOrNull { it.windowClass.contentEquals(app.windowClass, true) }
        return (appWindow != null && activeWindow != null && appWindow.id == activeWindow?.id)
    }

    @Suppress("UNUSED_PARAMETER", "CanBeVal", "KotlinConstantConditions")
    fun minimizeWindows(app: App): Boolean {
        var isMinimized = false
//        findWindows(app).forEach { sw ->
//            isMinimized = isMinimized or minimizeWindow(sw.window)
//        }
        return isMinimized
    }

    @Suppress("UNUSED_PARAMETER", "CanBeVal", "KotlinConstantConditions")
    fun requestWindowsFocus(app: App): Boolean {
        var isRequested = false
//        findWindows(app).forEach { sw ->
//            isRequested = isRequested or activateWindow(sw.window)
//        }
        return isRequested
    }

    @Suppress("unused", "UNUSED_PARAMETER", "KotlinConstantConditions", "CanBeVal")
    fun closeWindows(app: App): Boolean {
        var isClosed = false
//        findWindows(app).forEach { sw ->
//            isClosed = isClosed or closeWindow(sw.window)
//        }
        return isClosed
    }

//    @Suppress("DEPRECATED_IDENTITY_EQUALS")
//    private fun getAllWindows(root: Window, depth: Int): List<SystemWindow> {
//        val windowList = mutableListOf<SystemWindow>()
//        val windowRef = WindowByReference()
//        val parentRef = WindowByReference()
//        val childrenRef = PointerByReference()
//        val childCountRef = IntByReference()
//        x11.XQueryTree(display, root, windowRef, parentRef, childrenRef, childCountRef)
//        if (childrenRef.value == null) return emptyList()
//        val ids: LongArray
//        if (Native.LONG_SIZE === java.lang.Long.BYTES) {
//            ids = childrenRef.value.getLongArray(0, childCountRef.value)
//        } else if (Native.LONG_SIZE === Integer.BYTES) {
//            val intIds: IntArray = childrenRef.value.getIntArray(0, childCountRef.value)
//            ids = LongArray(intIds.size)
//            for (i in intIds.indices) {
//                ids[i] = intIds[i].toLong()
//            }
//        } else return emptyList()
//        for (id in ids) {
//            if (id == 0L) continue
//            Window(id).also { window ->
//                val pid = getWindowPid(window) ?: 0L
////                val name = getWindowName(window) ?: ""
//                val cls = getWindowClass(window) ?: ""
//                val desk = getWindowDesktop(window) ?: 0L
//                val process = ProcessHandle.allProcesses().toList().firstOrNull { it.pid() == pid }
////                val icon = getWindowIconName(window) ?: ""
//                windowList.add(
//                    SystemWindow(
//                        id,
////                        name,
////                        icon,
//                        pid,
//                        cls,
//                        desk,
//                        process?.info()?.command()?.get().orEmpty(),
//                    )
//                )
//                getAllWindows(window, depth + 1)
//            }
//        }
//        return windowList
//    }

//    private fun getWindowPid(
//        window: Window
//    ) = get_property_as_long(
//        window,
//        XA_CARDINAL,
//        "_NET_WM_PID"
//    )

//    @Suppress("unused")
//    private fun getWindowName(
//        window: Window
//    ) = get_property_as_utf8_string(
//        window,
//        x11.XInternAtom(display, "UTF8_STRING", false),
//        "_NET_WM_NAME"
//    )

//    private fun getWindowClass(
//        window: Window
//    ) = get_property_as_utf8_string(
//        window,
//        x11.XInternAtom(display, "UTF8_STRING", false),
//        "WM_CLASS"
//    ) ?: get_property_as_string(
//        window,
//        XA_STRING,
//        "WM_CLASS"
//    )

//    @Suppress("unused")
//    private fun getWindowCommand(
//        pid: Long
//    ): String = ProcessHandle.allProcesses().toList().firstOrNull {
//        it.pid() == pid
//    }?.info()?.command()?.getOrNull().orEmpty()

//    private fun getWindowDesktop(
//        window: Window
//    ) = get_property_as_long(
//        window,
//        XA_CARDINAL,
//        "_NET_SHOWING_DESKTOP"
//    )

//    @Suppress("unused")
//    private fun getWindowIconName(
//        window: Window
//    ): String? = PointerByReference().let { icon_name_return ->
//        if (x11Ext.XGetIconName(display, window, icon_name_return) == 0) {
//            g_free(icon_name_return.pointer)
//            null
//        } else {
//            g_strdup(icon_name_return.pointer)
//        }
//    }

//    private fun minimizeWindow(
//        window: Window
//    ): Boolean = x11Ext.XIconifyWindow(display, window, x11.XDefaultScreen(display)) == TRUE

//    private fun activateWindow(
//        window: Window,
//        switchDesktop: Boolean = true
//    ) = activate_window(window, switchDesktop)

//    private fun closeWindow(
//        window: Window
//    ) = client_msg(window, "_NET_CLOSE_WINDOW", 0, 0, 0, 0, 0)

//    @Suppress("unused")
//    private fun g_free(
//        pointer: Pointer?
//    ) {
//        if (pointer != null) x11.XFree(pointer)
//    }

//    @Suppress("SameParameterValue")
//    private fun get_property_as_long(
//        window: Window,
//        xa_prop_type: Atom,
//        prop_name: String
//    ): Long? = get_property_as_long(window, xa_prop_type, prop_name, null)

//    @Suppress("SameParameterValue")
//    private fun get_property_as_long(
//        window: Window,
//        xa_prop_type: Atom,
//        prop_name: String,
//        size: NativeLongByReference? = null
//    ): Long? {
//        var longProp: Long? = null
//        val prop = get_property(window, xa_prop_type, prop_name, size)
//        if (prop != null) {
//            longProp = prop.getLong(0)
////            g_free(prop)
//        }
//        return longProp
//    }

//    @Suppress("SameParameterValue")
//    private fun get_property_as_int(
//        win: Window,
//        xa_prop_type: Atom,
//        prop_name: String
//    ): Int? {
//        var intProp: Int? = null
//        val prop = get_property(win, xa_prop_type, prop_name, null)
//        if (prop != null) {
//            intProp = prop.getInt(0)
////            g_free(prop)
//        }
//        return intProp
//    }

//    @Suppress("SameParameterValue")
//    private fun get_property_as_string(
//        win: Window,
//        xa_prop_type: Atom,
//        prop_name: String
//    ): String? {
//        var strProp: String? = null
//        val prop = get_property(
//            win, xa_prop_type, prop_name,
//            null
//        )
//        if (prop != null) {
//            strProp = g_strdup(prop)
////            g_free(prop)
//        }
//        return strProp
//    }

//    private fun g_strdup(
//        pointer: Pointer
//    ): String {
//        val value = pointer.getString(0)
//        // g_free(pointer);
//        return value
//    }

//    private fun g_locale_to_utf8(
//        pointer: Pointer
//    ): String = g_strdup(pointer)

//    @Suppress("SameParameterValue")
//    private fun get_property_as_utf8_string(
//        window: Window,
//        xa_prop_type: Atom,
//        prop_name: String
//    ): String? {
//        var strProp: String? = null
//        val prop = get_property(window, xa_prop_type, prop_name, null)
//        if (prop != null) {
//            strProp = g_locale_to_utf8(prop)
////            g_free(prop)
//        }
//        return strProp
//    }

//    @Suppress("SameParameterValue")
//    private fun get_property_as_window(
//        window: Window,
//        xa_prop_type: Atom,
//        prop_name: String
//    ): Window? {
//        var ret: Window? = null
//        val prop = get_property(window, xa_prop_type, prop_name, null)
//        if (prop != null) {
//            ret = Window(prop.getLong(0))
////            g_free(prop)
//        }
//        return ret
//    }

//    private fun get_property(
//        window: Window?,
//        xa_prop_type: Atom,
//        prop_name: String?,
//        size: NativeLongByReference?
//    ): Pointer? {
//        val xa_ret_type = AtomByReference()
//        val ret_format = IntByReference()
//        val ret_n_items = NativeLongByReference()
//        val ret_bytes_after = NativeLongByReference()
//        val ret_prop = PointerByReference()
//        val xa_prop_name = x11.XInternAtom(display, prop_name, false)
//        if (x11.XGetWindowProperty(
//                display,
//                window,
//                xa_prop_name,
//                NativeLong(0),
//                NativeLong((MAX_PROPERTY_VALUE_LEN / 4).toLong()),
//                false,
//                xa_prop_type,
//                xa_ret_type,
//                ret_format,
//                ret_n_items,
//                ret_bytes_after,
//                ret_prop
//            ) != Success
//        ) {
//            return null
//        }
//        if ((xa_ret_type.value == null) || (xa_ret_type.value.toLong() != xa_prop_type.toLong())
//        ) {
////            g_free(ret_prop.pointer)
//            return null
//        }
//        if (size != null) {
//            var tmp_size = ((ret_format.value / 8) * ret_n_items.value.toLong())
//            if (ret_format.value == 32) {
//                tmp_size *= (NativeLong.SIZE / 4).toLong()
//            }
//            size.value = NativeLong(tmp_size)
//        }
//        return ret_prop.value
//    }

//    @Suppress("SameParameterValue")
//    private fun client_msg(
//        window: Window,
//        msg: String,
//        data0: Long,
//        data1: Long,
//        data2: Long,
//        data3: Long,
//        data4: Long
//    ): Boolean {
//        val mask = NativeLong((SubstructureRedirectMask or SubstructureNotifyMask).toLong())
//        val xclient = XClientMessageEvent()
//        xclient.type = ClientMessage
//        xclient.serial = NativeLong(0)
//        xclient.send_event = TRUE
//        xclient.message_type = x11.XInternAtom(display, msg, false)
//        xclient.window = window
//        xclient.format = 32
//        xclient.data.setType(Array<NativeLong>::class.java)
//        xclient.data.l[0] = NativeLong(data0)
//        xclient.data.l[1] = NativeLong(data1)
//        xclient.data.l[2] = NativeLong(data2)
//        xclient.data.l[3] = NativeLong(data3)
//        xclient.data.l[4] = NativeLong(data4)
//        val event = XEvent()
//        event.setTypedValue(xclient)
//        return if (x11.XSendEvent(
//                display,
//                x11.XDefaultRootWindow(display),
//                FALSE,
//                mask,
//                event
//            ) != FALSE
//        ) EXIT_SUCCESS else EXIT_FAILURE
//    }

//    private fun activate_window(
//        window: Window,
//        switch_desktop: Boolean = true
//    ): Boolean {
//        var desktop: Int? = get_property_as_int(window, XA_CARDINAL, "_NET_WM_DESKTOP")
//        if (desktop == null) {
//            desktop = get_property_as_int(window, XA_CARDINAL, "_WIN_WORKSPACE")
//        }
//        if (switch_desktop && (desktop != null)) {
//            client_msg(
//                x11.XDefaultRootWindow(display),
//                "_NET_CURRENT_DESKTOP",
//                desktop.toLong(),
//                0,
//                0,
//                0,
//                0
//            )
//        }
//        client_msg(window, "_NET_ACTIVE_WINDOW", 0, 0, 0, 0, 0)
//        x11.XMapRaised(display, window)
//        return EXIT_SUCCESS
//    }

//    private fun get_active_window(): Window? = get_property_as_window(
//        x11.XDefaultRootWindow(display),
//        XA_WINDOW,
//        "_NET_ACTIVE_WINDOW"
//    )

//    companion object {
//        const val MAX_PROPERTY_VALUE_LEN = 4096
//        const val TRUE: Int = 1
//        const val FALSE: Int = 0
//        const val EXIT_SUCCESS: Boolean = true
//        const val EXIT_FAILURE: Boolean = false
//    }

    data class SystemWindow(
        val id: Long,
        val pid: Long,
        val windowClass: String,
        val desktop: Long,
//        val name: String,
//        val iconName: String,
        val command: String,
    ) {
        companion object {
            val SystemWindow.window: Window get() = Window(id)
        }
    }

//    private interface Xmu : Library {
//        fun XmuClientWindow(display: Display?, window: Window?): Window?
//    }

//    private interface X11Ext : Library {
//        fun XMoveWindow(
//            display: Display?,
//            window: Window?,
//            x: Long,
//            y: Long
//        )

//        fun XResizeWindow(
//            display: Display?,
//            window: Window?,
//            width: Long,
//            height: Long
//        )

//        fun XMoveResizeWindow(
//            display: Display?,
//            window: Window?,
//            x: Long,
//            y: Long,
//            width: Long,
//            height: Long
//        )

//        fun XCreateFontCursor(
//            display: Display?,
//            shape: Long
//        ): Cursor?

//        fun XGrabPointer(
//            display: Display?,
//            grab_window: Window?,
//            owner_events: Int,
//            event_mask: NativeLong?,
//            pointer_mode: Int,
//            keyboard_mode: Int,
//            confine_to: Window?,
//            cursor: Cursor?,
//            time: Int
//        ): Int

//        fun XAllowEvents(
//            display: Display?,
//            event_mode: Int,
//            time: Int
//        ): Int

//        fun XUngrabPointer(
//            display: Display?,
//            time: Int
//        ): Int

//        fun XmuClientWindow(
//            display: Display?,
//            window: Window?
//        ): Window?

//        fun XGetIconName(
//            display: Display?,
//            window: Window?,
//            icon_name_return: PointerByReference?
//        ): Int

//        fun XIconifyWindow(
//            display: Display?,
//            window: Window?,
//            screen: Int
//        ): Int

//    }
}
