package org.psippr.desktop.managers.theme.linux.icons

/**
 * Sizes an icon is rendered in, the ones GTK and file managers ask for.
 *
 * @property px edge length in pixels
 */
enum class IconSize(
    val px: Int,
) {
    /** Menus, lists. */
    PX16(16),

    /** Panels. */
    PX22(22),

    /** Toolbars. */
    PX24(24),

    /** Small icons. */
    PX32(32),

    /** Default file manager grid. */
    PX48(48),

    /** Large grid. */
    PX64(64),

    /** Larger grid. */
    PX96(96),

    /** Huge grid. */
    PX128(128),

    /** Largest zoom. */
    PX256(256),
    ;

    /** Folder of this size in a theme (`48x48`). */
    val dirName: String
        get() = "${px}x$px"
}
