package eu.mjdev.desktop.provider.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector

@Suppress("MemberVisibilityCanBePrivate")
class User(
    name: String?,
    picture: ImageVector,
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
        val Empty = User(
            name = null,
            picture = Icons.Filled.AccountCircle,
            config = DesktopConfig.Default,
            theme = Theme.Default
        )
    }
}