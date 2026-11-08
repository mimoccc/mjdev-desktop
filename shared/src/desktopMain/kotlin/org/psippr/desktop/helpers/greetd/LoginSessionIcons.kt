package org.psippr.desktop.helpers.greetd

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Terminal
import java.io.File

/**
 * Finds the picture of a login session. Session files rarely carry an `Icon=` key, so after it
 * the logo is looked up by the session id, its base name (`gnome-wayland` -> `gnome`) and its
 * `DesktopNames=` entries in [SessionIconDir] with the [SessionIconName] patterns; without any
 * logo file a vector icon is used, so every session shows a picture.
 */
object LoginSessionIcons {
    /** Separator between a session's base name and its variant (`ubuntu-wayland`). */
    private const val VARIANT_SEPARATOR = "-"

    /** Session id of this desktop. */
    private const val PSIPPR_ID = "psippr"

    /** Icon file base name of this desktop's package. */
    private const val PSIPPR_ICON_NAME = "psippr-desktop"

    /** Session ids containing this get the terminal icon when no logo is found. */
    private const val TERMINAL_HINT = "terminal"

    /** File extension of vector logos. */
    private const val SVG_EXTENSION = "svg"

    /**
     * Marker of an SVG that colors its shapes through a CSS style sheet (e.g. Ubuntu's
     * `ubuntu-logo.svg`): the SVG renderer ignores CSS classes and draws such a logo as a black
     * square, so these files are skipped.
     */
    private const val SVG_CSS_MARKER = "<style"

    /**
     * Picture of the session [id]: the [iconKey] (`Icon=` value, path or name) when it resolves to
     * a file, else a logo found by name, else a vector icon.
     */
    fun resolve(
        id: String,
        iconKey: String? = null,
        desktopNames: List<String> = emptyList(),
    ): Any = iconKey?.let(::iconFile)
        ?: candidates(id, desktopNames).firstNotNullOfOrNull(::iconFile)
        ?: fallback(id)

    /** Names a logo of session [id] may be stored under, most specific first, without duplicates. */
    fun candidates(
        id: String,
        desktopNames: List<String> = emptyList(),
    ): List<String> = buildList {
        if (id == PSIPPR_ID) add(PSIPPR_ICON_NAME)
        add(id)
        add(id.substringBefore(VARIANT_SEPARATOR))
        desktopNames.forEach { name -> add(name.lowercase()) }
    }.filter { it.isNotBlank() }.distinct()

    // an absolute path that exists, or a name found in the icon dirs
    // old: also returned SVGs styled by CSS, drawn as black squares
//    private fun iconFile(name: String): File? = File(name)
//        .takeIf { it.isAbsolute }
//        ?.takeIf { it.isFile }
//        ?: SessionIconDir.entries.firstNotNullOfOrNull { dir ->
//            SessionIconName.entries
//                .map { pattern -> File(dir.path, name + pattern.suffix) }
//                .firstOrNull { it.isFile }
//        }
    private fun iconFile(name: String): File? = File(name)
        .takeIf { it.isAbsolute }
        ?.takeIf(::isDrawable)
        ?: SessionIconDir.entries.firstNotNullOfOrNull { dir ->
            SessionIconName.entries
                .map { pattern -> File(dir.path, name + pattern.suffix) }
                .firstOrNull(::isDrawable)
        }

    /** True when [file] exists and the image loader can draw it (no CSS styled SVG). */
    fun isDrawable(file: File): Boolean = file.isFile &&
        (
            !file.extension.equals(SVG_EXTENSION, ignoreCase = true) ||
                runCatching { !file.readText().contains(SVG_CSS_MARKER) }.getOrDefault(false)
        )

    // vector icon when the system has no logo for the session
    private fun fallback(id: String) =
        if (id.contains(TERMINAL_HINT, ignoreCase = true)) Icons.Filled.Terminal else Icons.Filled.DesktopWindows
}
