package eu.mjdev.desktop.theme.gtk

import androidx.compose.ui.graphics.Color
import eu.mjdev.desktop.data.DesktopFile
import eu.mjdev.desktop.data.DesktopFile.Companion.Comment
import eu.mjdev.desktop.data.DesktopFile.Companion.Encoding
import eu.mjdev.desktop.data.DesktopFile.Companion.Name
import eu.mjdev.desktop.data.DesktopFile.Companion.Type
import eu.mjdev.desktop.data.DesktopFile.Companion.MetacityTheme
import eu.mjdev.desktop.data.DesktopFile.Companion.GtkTheme
import eu.mjdev.desktop.data.DesktopFile.Companion.IconTheme
import eu.mjdev.desktop.data.DesktopFile.Companion.CursorTheme
import eu.mjdev.desktop.data.DesktopFile.Companion.ButtonLayout
import eu.mjdev.desktop.data.DesktopFile.Companion.UseOverlayScrollbars
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import eu.mjdev.desktop.extensions.Compose.rgbToHex
import eu.mjdev.desktop.helpers.internal.Palette
import eu.mjdev.desktop.helpers.managers.GnomeManager.Companion.THEME_ADWAITA
import eu.mjdev.desktop.helpers.managers.GnomeManager.Companion.THEME_ADWAITA_DARK
import eu.mjdev.desktop.helpers.managers.GnomeManager.Companion.THEME_MJDEV
import eu.mjdev.desktop.helpers.managers.GnomeManager.Companion.THEME_YARU
import eu.mjdev.desktop.provider.DesktopProvider
import java.io.File

@Suppress("unused", "MemberVisibilityCanBePrivate")
class GtkThemeHelper(
    private val api: DesktopProvider,
    private val palette: Palette = api.palette,
    private val themeName: String = THEME_MJDEV,
    private val themesDir: File = File(api.homeDir, ".themes"),
    private val themeDir: File = File(themesDir, themeName),
    private val themeDesktopFile: File = File(themeDir, "index.theme"),
    private val gtk2ThemeDir: File = File(themeDir, "gtk-2.0"),
    private val gtk3ThemeDir: File = File(themeDir, "gtk-3.0"),
    private val gtk4ThemeDir: File = File(themeDir, "gtk-4.0"),
    private val gtk3CssFile: File = File(gtk3ThemeDir, "gtk.css"),
    private val gtk4CssFile: File = File(gtk4ThemeDir, "gtk.css"),
) {
    fun createFromPalette() {
        createDesktopFile()
        createCssFile(gtk3CssFile)
        createCssFile(gtk4CssFile)
    }

    private fun createDesktopFile() {
        if (!themeDir.exists()) themeDir.mkdirs()
        if (!themeDesktopFile.exists()) themeDesktopFile.delete()
        themeDesktopFile.createNewFile()
        with(DesktopFile(themeDesktopFile)) {
            section(DesktopFile.DesktopFileType.DesktopEntry) {
                Type = DesktopFile.DesktopFileType.Theme
                Name = THEME_MJDEV
                Comment = "dynamic system theme"
                Encoding = "UTF-8" // todo
            }
            section(DesktopFile.DesktopFileType.Theme) {
                GtkTheme = THEME_MJDEV
                MetacityTheme = THEME_ADWAITA_DARK
                IconTheme = THEME_ADWAITA_DARK
                CursorTheme = "DMZ-White" // todo
                ButtonLayout = "close,minimize,maximize:"
                UseOverlayScrollbars = true
            }
            write()
        }
    }

    private fun createCssFile(file: File) {
        theme {
            bgColor = palette.backgroundColor
            fgColor = palette.textColor
            baseColor = palette.baseColor
            textColor = palette.textColor
            selectedBgColor = palette.selectedBgColor
            selectedFgColor = palette.selectedFgColor
            tooltipBgColor = palette.tooltipBgColor
            tooltipFgColor = palette.tooltipFgColor
        }.toString().also { data ->
            file.apply { parentFile.mkdirs() }.writeText(data)
        }
    }

    class Theme(
        val name: String = THEME_ADWAITA,
    ) {
        var bgColor: Color = Color.SuperDarkGray
        var fgColor: Color = Color.SuperDarkGray
        var baseColor: Color = Color.SuperDarkGray
        var textColor: Color = Color.SuperDarkGray
        var borderColor: Color = Color.SuperDarkGray
        var selectedBgColor: Color = Color.SuperDarkGray
        var selectedFgColor: Color = Color.SuperDarkGray
        var tooltipBgColor: Color = Color.SuperDarkGray
        var tooltipFgColor: Color = Color.SuperDarkGray
        var errorFgColor: Color = Color.White
        var errorBgColor: Color = Color.Red
        var successFgColor: Color = Color.White
        var successBgColor: Color = Color.Red
        var questionFgColor: Color = Color.White
        var questionBgColor: Color = Color.Yellow
        var warningFgColor: Color = Color.White
        var warningBgColor: Color = Color.Magenta
        var infoFgColor: Color = Color.Black
        var infoBgColor: Color = Color.White
        var osdFgColor : Color = Color.Black
        var osdBgColor : Color = Color.SuperDarkGray

        override fun toString() = """
            @define-color bg_color ${bgColor.rgbToHex()};
            @define-color fg_color ${fgColor.rgbToHex()};
            @define-color base_color ${baseColor.rgbToHex()};
            @define-color text_color ${textColor.rgbToHex()};
            @define-color selected_bg_color ${selectedBgColor.rgbToHex()};
            @define-color selected_fg_color ${selectedFgColor.rgbToHex()};
            @define-color tooltip_bg_color ${tooltipBgColor.rgbToHex()};
            @define-color tooltip_fg_color ${tooltipFgColor.rgbToHex()};
            @define-color info_fg_color ${infoFgColor.rgbToHex()};
            @define-color info_bg_color ${infoBgColor.rgbToHex()};
            @define-color warning_fg_color ${warningFgColor.rgbToHex()};
            @define-color warning_bg_color ${warningBgColor.rgbToHex()};
            @define-color question_fg_color ${questionFgColor.rgbToHex()};
            @define-color question_bg_color ${questionBgColor.rgbToHex()};
            @define-color error_fg_color ${errorFgColor.rgbToHex()};
            @define-color error_bg_color ${errorBgColor.rgbToHex()};
            @define-color success_color ${successBgColor.rgbToHex()};
            @define-color error_color ${errorBgColor.rgbToHex()};
            @define-color dark_bg_color${bgColor.rgbToHex()};
            @define-color dark_fg_color ${fgColor.rgbToHex()};
            @define-color osd_fg_color ${osdFgColor.rgbToHex()};
            @define-color osd_bg_color ${osdBgColor.rgbToHex()};
            @define-color osd_border_color ${borderColor.rgbToHex()};
            @define-color link_color @selected_bg_color;
            @define-color button_bg_color shade (@bg_color, 1.02);
            @define-color notebook_button_bg_color shade (@bg_color, 1.02);
            @define-color button_insensitive_bg_color mix (@button_bg_color, @bg_color, 0.6);
            @define-color backdrop_fg_color mix (@bg_color, @fg_color, 0.8);
            @define-color backdrop_text_color mix (@base_color, @text_color, 0.8);
            @define-color backdrop_dark_fg_color mix (@dark_bg_color, @dark_fg_color, 0.75);
            @define-color backdrop_dark_bg_color mix (@dark_bg_color, @dark_fg_color, 0.75);
            @define-color backdrop_selected_bg_color shade (@bg_color, 0.92);
            @define-color backdrop_selected_fg_color @fg_color;
            @define-color focus_color alpha (@selected_bg_color, 0.5);
            @define-color focus_bg_color alpha (@selected_bg_color, 0.1);
            @define-color shadow_color alpha(black, 0.5);
            
            window {
                background-color: ${bgColor.rgbToHex()};
            	border-radius: 8px;
            	border-bottom-left-radius: 8px;
            	border-bottom-right-radius: 8px;
            	box-shadow: 0px 0px 0px 1px ${textColor.rgbToHex()};
            	border: 1px solid ${textColor.rgbToHex()};
            	border-top: none;
            }
            
            decoration {
            	background-color: ${bgColor.rgbToHex()};
            	border-radius: 8px;
            	border-bottom-left-radius: 8px;
            	border-bottom-right-radius: 8px;
            	box-shadow: 0px 0px 0px 1px ${textColor.rgbToHex()};
            	border: 1px solid ${textColor.rgbToHex()};
            	border-top: none;
            }
            
            headerbar {
                background-color: ${bgColor.rgbToHex()};
            	border-bottom: 1px solid ${textColor.rgbToHex()};
            	box-shadow: 0px 1px 0px 0px ${textColor.rgbToHex()};
            	border-top: 1px solid ${textColor.rgbToHex()};
            }
            
        """.trimIndent()
    }

    companion object {
        fun theme(
            name: String = THEME_YARU,
            block: Theme.() -> Unit
        ) = Theme(name).apply(block)
    }
}
