package eu.mjdev.desktop.provider.data

import androidx.compose.runtime.mutableStateOf

@Suppress("MemberVisibilityCanBePrivate")
class DesktopConfig(
    autoHidePanel: Boolean = true,
    desktopBackgroundUrls: List<Any> = emptyList()
) {
    val autoHidePanelState = mutableStateOf(autoHidePanel)
    val desktopBackgroundUrlsState = mutableStateOf(desktopBackgroundUrls.toMutableList())

    val autoHidePanel get() = autoHidePanelState.value
    val desktopBackgroundUrls get() = desktopBackgroundUrlsState.value

    fun addBackground(path: String) {
        desktopBackgroundUrls.add(path)
    }

    companion object {
        val Default = DesktopConfig(
            autoHidePanel = false,
        ).apply {
            addBackground("http://erzvo.com/wp-content/uploads/2022/11/22-11-05-10429-Kubo-kvety-2013-1.jpg")
        }
    }
}
