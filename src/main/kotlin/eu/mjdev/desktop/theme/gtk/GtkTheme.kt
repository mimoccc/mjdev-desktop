package eu.mjdev.desktop.theme.gtk

import eu.mjdev.desktop.data.DesktopFile
import eu.mjdev.desktop.provider.DesktopProvider
import java.io.File

@Suppress("unused")
class GtkTheme(
    val themeName: String = "mjdev",
    val api: DesktopProvider,
    val themesDir: File = File(api.homeDir, ".themes"),
    val themeDir: File = File(themesDir, themeName)
) {
    val desktopFile: DesktopFile = DesktopFile.create("") // todo
}