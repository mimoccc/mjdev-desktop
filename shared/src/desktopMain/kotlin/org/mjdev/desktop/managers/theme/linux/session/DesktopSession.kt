package org.mjdev.desktop.managers.theme.linux.session

/**
 * Desktop session the shell runs in, read from `XDG_CURRENT_DESKTOP`.
 *
 * Decides which theme parts make sense: window chrome rules only under the mjdev compositor
 * (it draws the frame), GNOME Shell theme only under a running GNOME Shell.
 *
 * @property marker lowercase token searched in `XDG_CURRENT_DESKTOP`, null for the fallback
 * @property hasGnomeShell true when GNOME Shell draws the panel / overview / menus
 * @property usesUbuntuShellMode true when the Ubuntu shell mode (Yaru resources) is loaded
 */
enum class DesktopSession(
    val marker: String?,
    val hasGnomeShell: Boolean,
    val usesUbuntuShellMode: Boolean,
) {
    /** mjdev Wayland compositor session (session/mjdev-session). */
    MJDEV("mjdev", false, false),

    /** Ubuntu GNOME session (`ubuntu:GNOME`). */
    UBUNTU_GNOME("ubuntu", true, true),

    /** Plain GNOME session. */
    GNOME("gnome", true, false),

    /** Anything else (KDE, X11 WM, plain JVM run without a desktop). */
    OTHER(null, false, false),
    ;

    /** True when client-side titlebuttons must be hidden because the compositor draws them. */
    val compositorDrawsFrame: Boolean
        get() = this == MJDEV

    companion object {
        /** Environment variable holding the colon separated desktop names. */
        const val ENV_CURRENT_DESKTOP = "XDG_CURRENT_DESKTOP"

        /** Separator of names in [ENV_CURRENT_DESKTOP]. */
        private const val SEPARATOR = ":"

        /** Session of this process. */
        fun current(): DesktopSession = detect(System.getenv(ENV_CURRENT_DESKTOP))

        /** Session for the given `XDG_CURRENT_DESKTOP` value; first matching entry in declaration order wins. */
        fun detect(currentDesktop: String?): DesktopSession {
            val names =
                currentDesktop
                    .orEmpty()
                    .lowercase()
                    .split(SEPARATOR)
                    .map { name -> name.trim() }
            return entries.firstOrNull { session -> session.marker != null && session.marker in names } ?: OTHER
        }
    }
}
