package org.psippr.desktop.managers.theme.linux.gtk

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import org.psippr.desktop.extensions.Colors.isLightColor
import org.psippr.desktop.managers.palette.IPalette

/**
 * Immutable set of colors one generated theme is written from, derived from the desktop palette.
 *
 * Every surface is a mix of [windowBg] and [windowFg] (see [GtkSurfaceBlend]), so the theme keeps
 * the stock layering and contrast and only the hue follows the wallpaper.
 */
data class GtkThemeColors(
    /** Window background (palette background). */
    val windowBg: Color,
    /** Window text (palette text). */
    val windowFg: Color,
    /** Accent fill (palette accent). */
    val accentBg: Color,
    /** Tooltip background. */
    val tooltipBg: Color,
    /** Tooltip text. */
    val tooltipFg: Color,
) {
    /** True for a dark palette - picks the dark base theme and `prefer-dark`. */
    val isDark: Boolean = !windowBg.isLightColor

    /** Text on [accentBg]: black or white, whichever reads. */
    val accentFg: Color = if (accentBg.isLightColor) Color.Black else Color.White

    /** Accent used as text / link color. */
    val accent: Color = accentBg

    /** Content view background: darker than the window on dark themes, lighter on light ones (as stock). */
    val viewBg: Color = lerp(windowBg, if (isDark) Color.Black else Color.White, GtkSurfaceBlend.VIEW.fraction)

    /** Header bar background. */
    val headerbarBg: Color = mix(GtkSurfaceBlend.HEADERBAR)

    /** Sidebar background. */
    val sidebarBg: Color = mix(GtkSurfaceBlend.SIDEBAR)

    /** Secondary sidebar background. */
    val secondarySidebarBg: Color = mix(GtkSurfaceBlend.SECONDARY_SIDEBAR)

    /** Card background. */
    val cardBg: Color = mix(GtkSurfaceBlend.CARD)

    /** Popover / menu / dialog background. */
    val popoverBg: Color = mix(GtkSurfaceBlend.POPOVER)

    /** Button background. */
    val buttonBg: Color = mix(GtkSurfaceBlend.BUTTON)

    /** Hovered button background. */
    val buttonHoverBg: Color = mix(GtkSurfaceBlend.BUTTON_HOVER)

    /** Pressed button background. */
    val buttonActiveBg: Color = mix(GtkSurfaceBlend.BUTTON_ACTIVE)

    /** Borders and separators. */
    val border: Color = mix(GtkSurfaceBlend.BORDER)

    /** Disabled text. */
    val insensitiveFg: Color = mix(GtkSurfaceBlend.INSENSITIVE)

    /** Scrollbar slider. */
    val scrollbar: Color = mix(GtkSurfaceBlend.SCROLLBAR)

    /** Drop shadow color (stock libadwaita alpha). */
    val shade: Color = Color.Black.copy(alpha = if (isDark) SHADE_ALPHA_DARK else SHADE_ALPHA_LIGHT)

    private fun mix(blend: GtkSurfaceBlend): Color = lerp(windowBg, windowFg, blend.fraction)

    companion object {
        /** Shadow alpha on dark themes. */
        private const val SHADE_ALPHA_DARK = 0.36f

        /** Shadow alpha on light themes. */
        private const val SHADE_ALPHA_LIGHT = 0.07f

        /** Theme colors for the current desktop palette. */
        fun from(palette: IPalette): GtkThemeColors = GtkThemeColors(
            windowBg = palette.backgroundColor,
            windowFg = palette.textColor,
            accentBg = palette.selectedBgColor,
            tooltipBg = palette.tooltipBgColor,
            tooltipFg = palette.tooltipFgColor,
        )
    }
}
