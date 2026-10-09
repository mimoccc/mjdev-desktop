package org.psippr.desktop.helpers.greetd

/**
 * File names tried for a session logo of one candidate name, in order (vector before bitmap).
 *
 * @property suffix appended to the candidate name to form the file name
 */
enum class SessionIconName(
    val suffix: String,
) {
    /** `<name>.svg` */
    Svg(".svg"),

    /** `<name>.png` */
    Png(".png"),

    /** `<name>-logo.svg` (e.g. `ubuntu-logo.svg`) */
    LogoSvg("-logo.svg"),

    /** `<name>-logo.png` */
    LogoPng("-logo.png"),

    /** `<name>-logo-text.svg` (e.g. `gnome-logo-text.svg`) */
    LogoTextSvg("-logo-text.svg"),
}
