package eu.mjdev.desktop.data

import androidx.compose.runtime.mutableStateOf

@Suppress("MemberVisibilityCanBePrivate")
class DesktopConfig(
    desktopBackgroundUrls: List<Any> = emptyList()
) {
    val desktopBackgroundsState = mutableStateOf(desktopBackgroundUrls.toMutableList())
    val desktopBackgrounds get() = desktopBackgroundsState.value

    fun addBackground(path: String) {
        desktopBackgrounds.add(path)
    }

    companion object {
        val Default = DesktopConfig().apply {
            // todo add from url or file
            addBackground("http://erzvo.com/wp-content/uploads/2022/11/22-11-05-10429-Kubo-kvety-2013-1.jpg")
        }
    }
}
