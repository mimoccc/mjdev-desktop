package eu.mjdev.desktop.theme.gtk

import eu.mjdev.desktop.data.DesktopFile
import eu.mjdev.desktop.provider.DesktopProvider
import java.io.File

@Suppress("CanBeParameter", "unused")
class GtkTheme(
    val api: DesktopProvider,
    private val themeName: String = "mjdev",
    private val themesDir: File = File(api.homeDir, ".themes"),
    private val themeDir: File = File(themesDir, themeName),
    private val themeDesktopFile: File = File(themeDir, "index.theme"),
    private val themeCssFile: File = File(themeDir, "gtk.css"),
    private val desktopFile: DesktopFile = DesktopFile(themeDesktopFile)
) {
    fun createFromPalette() {
        themeDir.mkdirs()
        with(desktopFile) {
            type = DesktopFile.DesktopFileType.Theme
            name = "mjdev"
            comment = "dynamic mjdev theme"
            encoding = "UTF-8" // todo
            action(DesktopFile.DesktopFileType.Theme) {
                gtkTheme = "mjdev"
                metacityTheme = "mjdev"
                iconTheme = "mjdev"
                cursorTheme = "DMZ-White" // todo
                buttonLayout = "close,minimize,maximize:"
//                useOverlayScrollbars = true
            }
            write()
        }
    }

    /*
    [Desktop Entry]
    Type=X-GNOME-Metatheme
    Name=Ambiance
    Comment=Ubuntu Ambiance theme
    Encoding=UTF-8

    [X-GNOME-Metatheme]
    GtkTheme=Ambiance
    MetacityTheme=Ambiance
    IconTheme=ubuntu-mono-dark
    CursorTheme=DMZ-White
    ButtonLayout=close,minimize,maximize:
    X-Ubuntu-UseOverlayScrollbars=true
     */

    val cssFile = themeCssFile

    /*
    /*default color scheme */
    @define-color bg_color #f2f1f0;
    @define-color fg_color #4c4c4c;
    @define-color base_color #ffffff;
    @define-color text_color #3C3C3C;
    @define-color selected_bg_color #f07746;
    @define-color selected_fg_color #ffffff;
    @define-color tooltip_bg_color #000000;
    @define-color tooltip_fg_color #ffffff;

    /* misc colors used by gtk+
     *
     * Gtk doesn't currently expand color variables for style properties. Thus,
     * gtk-widgets.css uses literal color names, but includes a comment containing
     * the name of the variable. Please remember to change values there as well
     * when changing one of the variables below.
     */
    @define-color info_fg_color rgb (181, 171, 156);
    @define-color info_bg_color rgb (252, 252, 189);
    @define-color warning_fg_color rgb (173, 120, 41);
    @define-color warning_bg_color rgb (250, 173, 61);
    @define-color question_fg_color rgb (97, 122, 214);
    @define-color question_bg_color rgb (138, 173, 212);
    @define-color error_fg_color rgb (235, 235, 235);
    @define-color error_bg_color rgb (223, 56, 44);
    @define-color link_color @selected_bg_color;
    @define-color success_color #4e9a06;
    @define-color error_color #df382c;

    /* theme common colors */
    @define-color button_bg_color shade (@bg_color, 1.02); /*shade (#cdcdcd, 1.08);*/
    @define-color notebook_button_bg_color shade (@bg_color, 1.02);
    @define-color button_insensitive_bg_color mix (@button_bg_color, @bg_color, 0.6);
    @define-color dark_bg_color #3c3b37;
    @define-color dark_fg_color #dfdbd2;

    @define-color backdrop_fg_color mix (@bg_color, @fg_color, 0.8);
    @define-color backdrop_text_color mix (@base_color, @text_color, 0.8);
    @define-color backdrop_dark_fg_color mix (@dark_bg_color, @dark_fg_color, 0.75);
    /*@define-color backdrop_dark_bg_color mix (@dark_bg_color, @dark_fg_color, 0.75);*/
    @define-color backdrop_selected_bg_color shade (@bg_color, 0.92);
    @define-color backdrop_selected_fg_color @fg_color;

    @define-color focus_color alpha (@selected_bg_color, 0.5);
    @define-color focus_bg_color alpha (@selected_bg_color, 0.1);

    @define-color shadow_color alpha(black, 0.5);

    @define-color osd_fg_color #eeeeec;
    @define-color osd_bg_color alpha(#202526, 0.7);
    @define-color osd_border_color alpha(black, 0.7);

    @import url("gtk-widgets-borders.css");
    @import url("gtk-widgets-assets.css");
    @import url("gtk-widgets.css");
    @import url("apps/geary.css");
    @import url("apps/unity.css");
    @import url("apps/baobab.css");
    @import url("apps/gedit.css");
    @import url("apps/nautilus.css");
    @import url("apps/gnome-panel.css");
    @import url("apps/gnome-terminal.css");
    @import url("apps/gnome-system-log.css");
    @import url("apps/unity-greeter.css");
    @import url("apps/glade.css");
    @import url("apps/california.css");
    @import url("apps/software-center.css");
    @import url("public-colors.css");
     */
}