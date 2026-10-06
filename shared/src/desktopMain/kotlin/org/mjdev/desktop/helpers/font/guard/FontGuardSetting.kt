package org.mjdev.desktop.helpers.font.guard

/**
 * Fixed names and paths used by the font guard (see [FontGuard]).
 *
 * @property value the name / path / command part
 */
enum class FontGuardSetting(
    val value: String,
) {
    /** Environment variable fontconfig reads its configuration file from. */
    FontconfigFileEnv("FONTCONFIG_FILE"),

    /** Marker environment variable set on the relaunched process, so it does not relaunch again. */
    GuardMarkerEnv("MJDEV_FONT_GUARD"),

    /** Value of [GuardMarkerEnv] on the relaunched process. */
    GuardMarkerValue("1"),

    /** Environment variable of the user cache directory (XDG). */
    XdgCacheHomeEnv("XDG_CACHE_HOME"),

    /** System property of the user home directory. */
    UserHomeProperty("user.home"),

    /** System property of the operating system name. */
    OsNameProperty("os.name"),

    /** Lower case part of [OsNameProperty] identifying Linux, the only system using fontconfig here. */
    LinuxOsName("linux"),

    /** Default cache directory inside the user home. */
    CacheDirName(".cache"),

    /** Application directory inside the cache directory (also holds the session log). */
    AppCacheDirName("mjdev-desktop"),

    /** Name of the generated fontconfig file. */
    GuardConfigFileName("fonts-guard.conf"),

    /** System fontconfig file used as the base when [FontconfigFileEnv] is not set. */
    SystemFontconfigFile("/etc/fonts/fonts.conf"),

    /** Command listing the installed fonts. */
    FcListCommand("fc-list"),

    /** Option selecting the output format of [FcListCommand]. */
    FcListFormatOption("--format"),

    /** Output format: family, tab, file path - one font per line. */
    FcListFormat("%{family}\t%{file}\n"),

    /** Separator between family and file in the [FcListFormat] output. */
    FcListSeparator("\t"),
}
