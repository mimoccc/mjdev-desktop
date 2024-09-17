package eu.mjdev.desktop.helpers.managers

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.Pointer
import com.sun.jna.platform.unix.X11
import com.sun.jna.platform.unix.X11.*
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.NativeLongByReference
import com.sun.jna.ptr.PointerByReference

@Suppress(
    "unused", "MemberVisibilityCanBePrivate", "FunctionName", "LocalVariableName", "SameParameterValue",
    "DEPRECATION", "UNUSED_PARAMETER"
)
class WindowsManager {
    private val x11: X11 by lazy {
        INSTANCE
    }
    private val x11Ext by lazy {
        Native.loadLibrary("X11", X11Ext::class.java)
    }
    private val xmu by lazy {
        Native.loadLibrary("Xmu", Xmu::class.java)
    }
    private val display: Display by lazy { x11.XOpenDisplay(null) }
    private val rootWindow: Window
        get() = x11.XDefaultRootWindow(display)

    val allSystemWindows: List<SystemWindow>
        get() = getAllWindows(rootWindow, 0)

    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    private fun getAllWindows(root: Window, depth: Int): List<SystemWindow> {
        val windowList = mutableListOf<SystemWindow>()
        val windowRef = WindowByReference()
        val parentRef = WindowByReference()
        val childrenRef = PointerByReference()
        val childCountRef = IntByReference()
        x11.XQueryTree(display, root, windowRef, parentRef, childrenRef, childCountRef)
        if (childrenRef.value == null) return emptyList()
        val ids: LongArray
        if (Native.LONG_SIZE === java.lang.Long.BYTES) {
            ids = childrenRef.value.getLongArray(0, childCountRef.value)
        } else if (Native.LONG_SIZE === Integer.BYTES) {
            val intIds: IntArray = childrenRef.value.getIntArray(0, childCountRef.value)
            ids = LongArray(intIds.size)
            for (i in intIds.indices) {
                ids[i] = intIds[i].toLong()
            }
        } else return emptyList()
        for (id in ids) {
            if (id == 0L) continue
            Window(id).also { window ->
                windowList.add(
                    SystemWindow(
                        this,
                        id,
                        getWindowName(window) ?: "",
                        getWindowPid(window) ?: 0L,
                        getWindowClass(window) ?: "",
                        getWindowDesktop(window) ?: 0L
                    )
                )
                getAllWindows(window, depth + 1)
            }
        }
        return windowList
    }

    fun getWindowsByPids(
        pids: List<Long>
    ): List<IWindow> = allSystemWindows.filter { pids.contains(it.pid) }

    fun getWindowPid(
        window: Window
    ) = get_property_as_long(
        window,
        XA_CARDINAL,
        "_NET_WM_PID"
    )

    fun getWindowName(
        window: Window
    ) = get_property_as_utf8_string(
        window,
        x11.XInternAtom(display, "UTF8_STRING", false),
        "_NET_WM_NAME"
    )

    fun getWindowClass(
        window: Window
    ) = get_property_as_utf8_string(
        window,
        x11.XInternAtom(display, "UTF8_STRING", false),
        "WM_CLASS"
    )

    fun getWindowDesktop(
        window: Window
    ) = get_property_as_long(
        window,
        XA_CARDINAL,
        "_NET_SHOWING_DESKTOP"
    )

    fun minimizeWindow(window: Window): Boolean =
        x11Ext.XIconifyWindow(display, window, x11.XDefaultScreen(display)) == TRUE

    fun maximizeWindow(window: Window) {
        // todo
    }

    fun activateWindow(
        window: Window,
        switchDesktop: Boolean = true
    ) = activate_window(window, switchDesktop)

    fun closeWindow(
        window: Window
    ) = client_msg(window, "_NET_CLOSE_WINDOW", 0, 0, 0, 0, 0)

    fun isWindowActive(
        pids: List<Long>
    ): Boolean = get_active_window()?.let { aw ->
        getWindowsByPids(pids).any {
            it.window.toLong() == aw.toLong()
        }
    } ?: false

    private fun g_free(
        pointer: Pointer?
    ) {
        if (pointer != null) x11.XFree(pointer)
    }

    private fun get_property_as_long(
        window: Window,
        xa_prop_type: Atom,
        prop_name: String
    ): Long? = get_property_as_long(window, xa_prop_type, prop_name, null)

    private fun get_property_as_long(
        window: Window,
        xa_prop_type: Atom,
        prop_name: String,
        size: NativeLongByReference?
    ): Long? {
        var longProp: Long? = null
        val prop = get_property(window, xa_prop_type, prop_name, size)
        if (prop != null) {
            longProp = prop.getLong(0)
//            g_free(prop)
        }
        return longProp
    }

    private fun get_property_as_int(
        display: Display,
        win: Window,
        xa_prop_type: Atom,
        prop_name: String
    ): Int? {
        var intProp: Int? = null
        val prop = get_property(win, xa_prop_type, prop_name, null)
        if (prop != null) {
            intProp = prop.getInt(0)
//            g_free(prop)
        }
        return intProp
    }

    private fun g_strdup(
        pointer: Pointer
    ): String {
        val value = pointer.getString(0)
        // g_free(pointer);
        return value
    }

    private fun g_locale_to_utf8(
        pointer: Pointer
    ): String = g_strdup(pointer)

    private fun get_property_as_utf8_string(
        window: Window,
        xa_prop_type: Atom,
        prop_name: String
    ): String? {
        var strProp: String? = null
        val prop = get_property(window, xa_prop_type, prop_name, null)
        if (prop != null) {
            strProp = g_locale_to_utf8(prop)
//            g_free(prop)
        }
        return strProp
    }

    private fun get_property_as_window(
        window: Window,
        xa_prop_type: Atom,
        prop_name: String
    ): Window? {
        var ret: Window? = null
        val prop = get_property(window, xa_prop_type, prop_name, null)
        if (prop != null) {
            ret = Window(prop.getLong(0))
//            g_free(prop)
        }
        return ret
    }

    private fun get_property(
        window: Window?,
        xa_prop_type: Atom,
        prop_name: String?,
        size: NativeLongByReference?
    ): Pointer? {
        val xa_ret_type = AtomByReference()
        val ret_format = IntByReference()
        val ret_n_items = NativeLongByReference()
        val ret_bytes_after = NativeLongByReference()
        val ret_prop = PointerByReference()
        val xa_prop_name = x11.XInternAtom(display, prop_name, false)
        if (x11.XGetWindowProperty(
                display,
                window,
                xa_prop_name,
                NativeLong(0),
                NativeLong((MAX_PROPERTY_VALUE_LEN / 4).toLong()),
                false,
                xa_prop_type,
                xa_ret_type,
                ret_format,
                ret_n_items,
                ret_bytes_after,
                ret_prop
            ) != Success
        ) {
            return null
        }
        if ((xa_ret_type.value == null) || (xa_ret_type.value.toLong() != xa_prop_type.toLong())
        ) {
//            g_free(ret_prop.pointer)
            return null
        }
        if (size != null) {
            var tmp_size = ((ret_format.value / 8) * ret_n_items.value.toLong())
            if (ret_format.value == 32) {
                tmp_size *= (NativeLong.SIZE / 4).toLong()
            }
            size.value = NativeLong(tmp_size)
        }
        return ret_prop.value
    }

    private fun client_msg(
        window: Window,
        msg: String,
        data0: Long,
        data1: Long,
        data2: Long,
        data3: Long,
        data4: Long
    ): Boolean {
        val mask = NativeLong((SubstructureRedirectMask or SubstructureNotifyMask).toLong())
        val xclient = XClientMessageEvent()
        xclient.type = ClientMessage
        xclient.serial = NativeLong(0)
        xclient.send_event = TRUE
        xclient.message_type = x11.XInternAtom(display, msg, false)
        xclient.window = window
        xclient.format = 32
        xclient.data.setType(Array<NativeLong>::class.java)
        xclient.data.l[0] = NativeLong(data0)
        xclient.data.l[1] = NativeLong(data1)
        xclient.data.l[2] = NativeLong(data2)
        xclient.data.l[3] = NativeLong(data3)
        xclient.data.l[4] = NativeLong(data4)
        val event = XEvent()
        event.setTypedValue(xclient)
        return if (x11.XSendEvent(
                display,
                x11.XDefaultRootWindow(display),
                FALSE,
                mask,
                event
            ) != FALSE
        ) EXIT_SUCCESS else EXIT_FAILURE
    }

    private fun activate_window(
        window: Window,
        switch_desktop: Boolean
    ): Boolean {
        var desktop: Int? = get_property_as_int(display, window, XA_CARDINAL, "_NET_WM_DESKTOP")
        if (desktop == null) {
            desktop = get_property_as_int(display, window, XA_CARDINAL, "_WIN_WORKSPACE")
        }
        if (switch_desktop && (desktop != null)) {
            client_msg(
                x11.XDefaultRootWindow(display),
                "_NET_CURRENT_DESKTOP",
                desktop.toLong(),
                0,
                0,
                0,
                0
            )
        }
        client_msg(window, "_NET_ACTIVE_WINDOW", 0, 0, 0, 0, 0)
        x11.XMapRaised(display, window)
        return EXIT_SUCCESS
    }

    private fun get_active_window(): Window? = get_property_as_window(
        x11.XDefaultRootWindow(display),
        XA_WINDOW,
        "_NET_ACTIVE_WINDOW"
    )

    fun dispose() {
        x11.XCloseDisplay(display)
    }

    companion object {
        const val MAX_PROPERTY_VALUE_LEN = 4096
        const val TRUE: Int = 1
        const val FALSE: Int = 0
        const val EXIT_SUCCESS: Boolean = true
        const val EXIT_FAILURE: Boolean = false
    }

    class SystemWindow(
        val windowTracker: WindowsManager,
        val id: Long,
        val name: String,
        val pid: Long,
        val windowClass: String,
        val desktop: Long,
        override val window: Window = Window(id)
    ) : IWindow {
        override fun toBack() {
            // todo
        }

        override fun toFront() {
            windowTracker.activateWindow(window)
        }

        override fun minimize() {
            windowTracker.minimizeWindow(window)
        }

        override fun maximize() {
            windowTracker.maximizeWindow(window)
        }

        override fun close() {
            windowTracker.closeWindow(window)
        }

        override fun toString(): String {
            return "WID : $id  PID: $pid NAME: $name"
        }
    }

    interface IWindow {
        val window: Window

        fun toBack() {}
        fun toFront() {}
        fun minimize() {}
        fun maximize() {}
        fun close() {}
    }

    private interface Xmu : Library {
        fun XmuClientWindow(display: Display?, win: Window?): Window?
    }

    private interface X11Ext : Library {
        fun XMoveWindow(
            display: Display?,
            window: Window?,
            x: Long,
            y: Long
        )

        fun XResizeWindow(
            display: Display?,
            window: Window?,
            width: Long,
            height: Long
        )

        fun XMoveResizeWindow(
            display: Display?,
            window: Window?,
            x: Long,
            y: Long,
            width: Long,
            height: Long
        )

        fun XCreateFontCursor(
            display: Display?,
            shape: Long
        ): Cursor?

        fun XGrabPointer(
            display: Display?,
            grab_window: Window?,
            owner_events: Int,
            event_mask: NativeLong?,
            pointer_mode: Int,
            keyboard_mode: Int,
            confine_to: Window?,
            cursor: Cursor?,
            time: Int
        ): Int

        fun XAllowEvents(
            display: Display?,
            event_mode: Int,
            time: Int
        ): Int

        fun XUngrabPointer(
            display: Display?,
            time: Int
        ): Int

        fun XmuClientWindow(
            display: Display?,
            window: Window?
        ): Window?

        fun XGetIconName(
            display: Display?,
            window: Window?,
            icon_name_return: PointerByReference?
        ): Int

        fun XIconifyWindow(
            display: Display?,
            window: Window?,
            screen: Int
        ): Int

        companion object {
            const val XC_CROSS_HAIR: Int = 34
        }
    }
}