package eu.mjdev.desktop.windows

import com.sun.jna.Native
import com.sun.jna.platform.unix.X11
import com.sun.jna.platform.unix.X11.*
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference

@Suppress("unused")
class WindowsTracker {
    private val x11: X11 = X11.INSTANCE
    private val display: Display = x11.XOpenDisplay(null)
    private val rootWindow: Window
        get() = x11.XDefaultRootWindow(display)
    val allWindows: List<SystemWindow>
        get() = recurse(rootWindow, 0)

    fun getWindowByPid(
        pid: Long?
    ): IWindow? {
        allWindows.forEachIndexed { idx, wn ->
            println("$idx, ${wn.name}")
        }
        return if (pid == null) null else null
    }

    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    private fun recurse(root: Window, depth: Int): List<SystemWindow> {
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
//            throw IllegalStateException("Unexpected size for Native.LONG_SIZE" + Native.LONG_SIZE)
        for (id in ids) {
            if (id == 0L) continue
            val window = Window(id)
            val name = XTextProperty()
            val pids = mutableListOf<Long>()
            // todo
            x11.XGetWMName(display, window, name)
//            x11.XGetWindowAttributes()
//            x11.XGetGeometry()
//            x11.XGetWMHints()
//            x11.XGetVisualInfo()
//            x11.XGetWindowProperty()
//            x11.XGetKeyboardMapping()
//            x11.XGetModifierMapping()
//            x11.XGetKeyboardControl()
            windowList.add(
                SystemWindow(
                    this,
                    id,
                    name.value ?: "",
                    pids
                )
            )
            // todo error free memory
//            try {
//                x11.XFree(name.getPointer())
//            } catch (t: Throwable) {
//                t.printStackTrace()
//            }
            recurse(window, depth + 1)
        }
        return windowList
    }

    class SystemWindow(
        val windowTracker: WindowsTracker,
        val id: Long,
        val name: String,
        val pids: List<Long>
    ) : IWindow {
        override fun toBack() {
            // todo
        }

        override fun toFront() {
            // todo
        }

        override fun minimize() {
            // todo
        }

        override fun maximize() {
            // todo
        }

        override fun close() {
            // todo
        }
    }

    interface IWindow {
        fun toBack() {}
        fun toFront() {}
        fun minimize() {}
        fun maximize() {}
        fun close() {}
    }
}