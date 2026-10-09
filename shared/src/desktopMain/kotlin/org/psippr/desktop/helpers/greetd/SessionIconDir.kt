package org.psippr.desktop.helpers.greetd

/**
 * Directories searched for a session logo, in order. Session files rarely carry an `Icon=` key,
 * so the logo is looked up by name in the standard system icon locations.
 *
 * @property path absolute directory path
 */
enum class SessionIconDir(
    val path: String,
) {
    /** Distribution and desktop logos (e.g. `ubuntu-logo.svg`). */
    Pixmaps("/usr/share/pixmaps"),

    /** Top level of the icon themes (e.g. `gnome-logo-text.svg`). */
    Icons("/usr/share/icons"),

    /** Scalable application icons of the fallback theme. */
    HicolorScalable("/usr/share/icons/hicolor/scalable/apps"),

    /** Large bitmap application icons of the fallback theme. */
    Hicolor256("/usr/share/icons/hicolor/256x256/apps"),

    /** Medium bitmap application icons of the fallback theme. */
    Hicolor128("/usr/share/icons/hicolor/128x128/apps"),

    /** Install directory of the psippr desktop package, holding its own icon. */
    PsipprLib("/opt/psippr-desktop/lib"),
}
