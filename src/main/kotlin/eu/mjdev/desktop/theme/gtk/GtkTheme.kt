package eu.mjdev.desktop.theme.gtk

import eu.mjdev.desktop.data.DesktopFile
import eu.mjdev.desktop.provider.DesktopProvider
import java.io.File

@Suppress("unused")
class GtkTheme(
    val themeName: String = "mjdev",
    val api: DesktopProvider,
    val themesDir: File = File(api.homeDir, ".themes"),
    val themeDir: File = File(themesDir, themeName),
    val themeDesktopFile: File = File(themeDir, "index.theme")
) {
    val desktopFile: DesktopFile = DesktopFile.create(themeDesktopFile) // todo
    /*
    [Desktop Entry]
    Type=X-GNOME-Metatheme
    Name=Ambiance
    Comment=Ubuntu Ambiance theme
    Encoding=UTF-8

    [X-GNOME-Metatheme]
    GtkTheme=Ambiance
    MetacityTheme=Ambiance
    IconTheme=ubuntu-mono-dark
    CursorTheme=DMZ-White
    ButtonLayout=close,minimize,maximize:
    X-Ubuntu-UseOverlayScrollbars=true
     */
}