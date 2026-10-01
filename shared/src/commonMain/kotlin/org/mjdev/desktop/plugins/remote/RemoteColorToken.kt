package org.mjdev.desktop.plugins.remote

import androidx.compose.ui.graphics.Color
import org.mjdev.desktop.managers.palette.IPalette

/**
 * Named colors a document may use instead of a hardcoded value. They resolve against the live
 * wallpaper [IPalette], so plugin widgets always follow the background image.
 */
enum class RemoteColorToken {
    BACKGROUND,
    TEXT,
    ICONS,
    BORDER,
    SELECTED,
    ;

    /** Resolves this token against [palette]. */
    fun resolve(palette: IPalette): Color = when (this) {
        BACKGROUND -> palette.backgroundColor
        TEXT -> palette.textColor
        ICONS -> palette.iconsTintColor
        BORDER -> palette.borderColor
        SELECTED -> palette.selectedBgColor
    }

    companion object {
        /** Length of an `#RRGGBB` color string including the hash. */
        private const val RGB_LENGTH = 7

        /** Length of an `#AARRGGBB` color string including the hash. */
        private const val ARGB_LENGTH = 9

        /** Radix of hex color digits. */
        private const val HEX_RADIX = 16

        /** Alpha channel added to opaque `#RRGGBB` colors. */
        private const val OPAQUE = 0xFF000000L

        /**
         * Resolves a [spec]: a token name (`ICONS`), `#RRGGBB` or `#AARRGGBB`.
         * Unknown specs resolve to the palette text color so a widget stays readable.
         */
        fun resolve(
            spec: String,
            palette: IPalette,
        ): Color {
            val token = entries.firstOrNull { it.name.equals(spec, true) }
            return when {
                token != null -> token.resolve(palette)
                spec.startsWith("#") -> parseHex(spec) ?: palette.textColor
                else -> palette.textColor
            }
        }

        private fun parseHex(spec: String): Color? {
            val value = spec.drop(1).toLongOrNull(HEX_RADIX) ?: return null
            return when (spec.length) {
                RGB_LENGTH -> Color((OPAQUE or value).toInt())
                ARGB_LENGTH -> Color(value.toInt())
                else -> null
            }
        }
    }
}
