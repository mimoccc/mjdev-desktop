package eu.mjdev.desktop.windows

@Suppress("unused")
class WindowsTracker {

    @Suppress("UNUSED_PARAMETER")
    fun getWindowByPid(
        pid: Long?
    ): IWindow? {
        return null // todo
    }

    interface IWindow {
        fun toBack() {}
        fun toFront() {}
        fun minimize() {}
        fun maximize() {}
        fun close() {}
    }
}