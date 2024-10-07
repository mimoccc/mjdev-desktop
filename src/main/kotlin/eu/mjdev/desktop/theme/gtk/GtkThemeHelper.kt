package eu.mjdev.desktop.theme.gtk

import androidx.compose.ui.graphics.Color
import eu.mjdev.desktop.data.DesktopFile
import eu.mjdev.desktop.data.DesktopFile.Companion.desktopFile
import eu.mjdev.desktop.extensions.ColorUtils.isLightColor
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import eu.mjdev.desktop.extensions.Compose.hexRgb
import eu.mjdev.desktop.helpers.internal.Palette
import eu.mjdev.desktop.managers.GnomeManager.Companion.THEME_ADWAITA_DARK
import eu.mjdev.desktop.managers.GnomeManager.Companion.THEME_CURSOR_BLOOM
import eu.mjdev.desktop.managers.GnomeManager.Companion.THEME_MJDEV
import eu.mjdev.desktop.provider.DesktopProvider
import java.io.File


// global theme : ~/.config/gtk-2.0 and ~/.config/gtk-3.0 and ~/.config/gtk-4.0
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
        desktopFile(themeDesktopFile) {
            mkDirs()
            deleteFile()
            desktopSection {
                Type = DesktopFile.DesktopFileType.Theme
                Name = THEME_MJDEV
                Comment = "dynamic system theme"
                Encoding = Charsets.UTF_8.name()
            }
            themeSection {
                GtkTheme = THEME_MJDEV
                MetacityTheme = THEME_ADWAITA_DARK
                IconTheme = THEME_ADWAITA_DARK
                CursorTheme = THEME_CURSOR_BLOOM
                ButtonLayout = "close,minimize,maximize:"
                UseOverlayScrollbars = true
            }
            write()
        }
    }

    private fun createCssFile(file: File) = theme(file) {
        bgColor = palette.backgroundColor
        fgColor = palette.textColor

        baseColor = palette.baseColor
        textColor = palette.textColor

        selectedBgColor = palette.selectedBgColor
        selectedFgColor = palette.selectedFgColor

        tooltipBgColor = palette.tooltipBgColor
        tooltipFgColor = palette.tooltipFgColor

        // todo
        buttonBgColor = palette.iconsTintColor
        // if (palette.backgroundColor.isLightColor) palette.textColor else palette.iconsTintColor

        // todo
        buttonFgColor = if (palette.iconsTintColor.isLightColor) Color.Black else Color.White
        // if (palette.backgroundColor.isLightColor) palette.iconsTintColor else palette.textColor

        write()
    }

    /*
    @define-color accent_color #ffb49e;
    @define-color accent_bg_color #ffb49e;
    @define-color accent_fg_color #5e1703;
    @define-color destructive_color #ffb4a9;
    @define-color destructive_bg_color #930006;
    @define-color destructive_fg_color #680003;
    @define-color success_color #d9c58d;
    @define-color success_bg_color #534619;
    @define-color success_fg_color #f6e1a6;
    @define-color warning_color #e7bdb2;
    @define-color warning_bg_color #5d4038;
    @define-color warning_fg_color #ffdacf;
    @define-color error_color #ffb4a9;
    @define-color error_bg_color #930006;
    @define-color error_fg_color #680003;
    @define-color shade_color rgba(0, 0, 0, 0.36);
    @define-color scrollbar_outline_color rgba(160, 140, 135, 0.5);
 */
    // ~/.config/gtk-3.0/gtk.css
    // ~/.config/gtk-4.0/gtk.css
    class Theme(val file: File) {
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
        var osdFgColor: Color = Color.Black
        var osdBgColor: Color = Color.SuperDarkGray
        var buttonBgColor: Color = Color.SuperDarkGray
        var buttonFgColor: Color = Color.White

        override fun toString() = """
            @define-color bg_color ${bgColor.hexRgb};
            @define-color fg_color ${fgColor.hexRgb};
            @define-color base_color ${baseColor.hexRgb};
            @define-color text_color ${textColor.hexRgb};
            @define-color selected_bg_color ${selectedBgColor.hexRgb};
            @define-color selected_fg_color ${selectedFgColor.hexRgb};
            @define-color tooltip_bg_color ${tooltipBgColor.hexRgb};
            @define-color tooltip_fg_color ${tooltipFgColor.hexRgb};
            @define-color info_fg_color ${infoFgColor.hexRgb};
            @define-color info_bg_color ${infoBgColor.hexRgb};
            @define-color warning_fg_color ${warningFgColor.hexRgb};
            @define-color warning_bg_color ${warningBgColor.hexRgb};
            @define-color question_fg_color ${questionFgColor.hexRgb};
            @define-color question_bg_color ${questionBgColor.hexRgb};
            @define-color error_fg_color ${errorFgColor.hexRgb};
            @define-color error_bg_color ${errorBgColor.hexRgb};
            @define-color success_color ${successBgColor.hexRgb};
            @define-color error_color ${errorBgColor.hexRgb};
            @define-color dark_bg_color ${bgColor.hexRgb};
            @define-color dark_fg_color ${fgColor.hexRgb};
            @define-color window_bg_color ${bgColor.hexRgb};
            @define-color window_fg_color ${fgColor.hexRgb};
            @define-color view_bg_color ${bgColor.hexRgb};
            @define-color view_fg_color ${textColor.hexRgb};
            @define-color headerbar_bg_color ${bgColor.hexRgb};
            @define-color headerbar_fg_color ${fgColor.hexRgb};
            @define-color headerbar_border_color ${borderColor.hexRgb};
            @define-color headerbar_backdrop_color ${bgColor.hexRgb};
            @define-color headerbar_shade_color ${borderColor.hexRgb};
            @define-color card_bg_color ${bgColor.hexRgb};
            @define-color card_fg_color ${fgColor.hexRgb};
            @define-color card_shade_color ${borderColor.hexRgb};
            @define-color osd_fg_color ${osdFgColor.hexRgb};
            @define-color osd_bg_color ${osdBgColor.hexRgb};
            @define-color osd_border_color ${borderColor.hexRgb};
            @define-color dialog_bg_color ${bgColor.hexRgb};
            @define-color dialog_fg_color ${fgColor.hexRgb};
            @define-color popover_bg_color ${bgColor.hexRgb};
            @define-color popover_fg_color ${fgColor.hexRgb};
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
            @define-color blue_1 #99c1f1;
            @define-color blue_2 #62a0ea;
            @define-color blue_3 #3584e4;
            @define-color blue_4 #1c71d8;
            @define-color blue_5 #1a5fb4;
            @define-color green_1 #8ff0a4;
            @define-color green_2 #57e389;
            @define-color green_3 #33d17a;
            @define-color green_4 #2ec27e;
            @define-color green_5 #26a269;
            @define-color yellow_1 #f9f06b;
            @define-color yellow_2 #f8e45c;
            @define-color yellow_3 #f6d32d;
            @define-color yellow_4 #f5c211;
            @define-color yellow_5 #e5a50a;
            @define-color orange_1 #ffbe6f;
            @define-color orange_2 #ffa348;
            @define-color orange_3 #ff7800;
            @define-color orange_4 #e66100;
            @define-color orange_5 #c64600;
            @define-color red_1 #f66151;
            @define-color red_2 #ed333b;
            @define-color red_3 #e01b24;
            @define-color red_4 #c01c28;
            @define-color red_5 #a51d2d;
            @define-color purple_1 #dc8add;
            @define-color purple_2 #c061cb;
            @define-color purple_3 #9141ac;
            @define-color purple_4 #813d9c;
            @define-color purple_5 #613583;
            @define-color brown_1 #cdab8f;
            @define-color brown_2 #b5835a;
            @define-color brown_3 #986a44;
            @define-color brown_4 #865e3c;
            @define-color brown_5 #63452c;
            @define-color light_1 #ffffff;
            @define-color light_2 #f6f5f4;
            @define-color light_3 #deddda;
            @define-color light_4 #c0bfbc;
            @define-color light_5 #9a9996;
            @define-color dark_1 #77767b;
            @define-color dark_2 #5e5c64;
            @define-color dark_3 #3d3846;
            @define-color dark_4 #241f31;
            @define-color dark_5 #000000;
            
            window {
                background-image: none;
                background-color: ${bgColor.hexRgb};
            	border-radius: 8px;
            	border-bottom-left-radius: 8px;
            	border-bottom-right-radius: 8px;
                border-top-right-radius: 8px;
                border-top-left-radius: 8px;
            	box-shadow: 0px 0px 0px 1px ${textColor.hexRgb};
            	border: 2px solid ${bgColor.hexRgb};
            	border-top: none;
                box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
            }
            
            decoration {
                background-image: none;
            	background-color: ${bgColor.hexRgb};
            	border-radius: 8px;
            	border-bottom-left-radius: 8px;
            	border-bottom-right-radius: 8px;
            	box-shadow: 0px 0px 0px 1px ${textColor.hexRgb};
            	border-top: none;
            }
            
            .titlebar, headerbar {
                padding-top: 2px;
                padding-bottom: 2px;
                background-image: none;
                background-color: ${bgColor.hexRgb};
            }

            .titlebar:backdrop, headerbar:backdrop  {
                background: ${bgColor.hexRgb};
                color: ${textColor.hexRgb};
            }
            
            /* all buttons */
            button {
                background: ${buttonBgColor.hexRgb};
                opacity: 0.7;
                margin: 2px;
                padding: 4px;
                min-width: 24px;
                min-height: 24px;
                text-shadow: none;
                color:  ${buttonFgColor.hexRgb};
                border-radius: 8px;
            }
            
            /* window buttons */
            button.minimize,
            button.maximize,
            button.close,
            button.maximize:hover,
            button.minimize:hover,
            button.close:hover {
                opacity: 0.7;
            }

            button:hover,
            button.maximize:hover,
            button.minimize:hover,
            button.close:hover {
                opacity: 1;
            }
        """.trimIndent()
    }

    companion object {
        fun theme(
            file: File,
            block: Theme.() -> Unit
        ) = Theme(file).apply(block)

        fun Theme.write() {
            toString().also { data ->
                file.apply {
                    parentFile.mkdirs()
                    delete()
                    createNewFile()
                }.writeText(data)
            }
        }
    }
}
