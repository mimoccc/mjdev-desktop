package org.mjdev.desktop.managers.theme.linux.shell

/**
 * Stock GNOME Shell stylesheet the generated one imports, so the shell keeps its stock look
 * and only the colors are overridden.
 *
 * @property light resource of the light variant
 * @property dark resource of the dark variant
 */
enum class GnomeShellBase(
    val light: String,
    val dark: String,
) {
    /** Ubuntu Yaru (resources registered by the `ubuntu` shell mode). */
    YARU(
        "resource:///org/gnome/shell/theme/Yaru/gnome-shell.css",
        "resource:///org/gnome/shell/theme/Yaru/gnome-shell-dark.css",
    ),

    /** Upstream GNOME (Adwaita). */
    ADWAITA(
        "resource:///org/gnome/shell/theme/gnome-shell-light.css",
        "resource:///org/gnome/shell/theme/gnome-shell-dark.css",
    ),
    ;

    /** Resource for the variant. */
    fun stylesheet(dark: Boolean): String = if (dark) this.dark else light

    companion object {
        /** Base for the running shell: Yaru in the Ubuntu shell mode, Adwaita otherwise. */
        fun forUbuntuMode(ubuntuMode: Boolean): GnomeShellBase = if (ubuntuMode) YARU else ADWAITA
    }
}
