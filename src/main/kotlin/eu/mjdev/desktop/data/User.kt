package eu.mjdev.desktop.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.mutableStateOf
import eu.mjdev.desktop.helpers.system.Command

@Suppress("MemberVisibilityCanBePrivate")
class User(
    name: String?,
    picture: Any?,
    config: DesktopConfig,
    theme: Theme
) {
    val nameState = mutableStateOf(name)
    val pictureState = mutableStateOf(picture)
    val configState = mutableStateOf(config)
    val themeState = mutableStateOf(theme)

    val name get() = nameState.value
    val picture get() = pictureState.value
    val config get() = configState.value
    val theme get() = themeState.value

    companion object {
        // todo
        fun load() = User(
            name = Command("whoami").execute(),
            picture = Icons.Filled.AccountCircle,
            config = DesktopConfig.Default,
            theme = Theme.Default
        )
    }
}