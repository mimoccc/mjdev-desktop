package org.psippr.desktop.managers.theme.linux.gtk

/**
 * Recolor rules layered over the stock base theme (Yaru / Adwaita compile their colors in,
 * so redefining named colors alone does not recolor GTK3 / plain GTK4 widgets).
 *
 * Only colors are touched - sizes, radii, paddings and icons stay stock Ubuntu.
 * Selectors and properties are valid in both GTK3 and GTK4 (no `:root`, `var()`, `:focus-within`,
 * no deprecated color functions), so one text serves both toolkits.
 */
object GtkWidgetRules {
    /** Rules, referencing only names defined by [GtkNamedColor]. */
    val CSS: String =
        """
        window, window.background, .background, dialog, messagedialog, assistant {
            background-color: @window_bg_color;
            color: @window_fg_color;
        }

        headerbar, .titlebar, actionbar, toolbar, .toolbar, menubar, searchbar, .inline-toolbar {
            background-image: none;
            background-color: @headerbar_bg_color;
            color: @headerbar_fg_color;
            border-color: @borders;
        }

        headerbar:backdrop, .titlebar:backdrop {
            background-color: @headerbar_backdrop_color;
        }

        .view, textview text, treeview.view, iconview, columnview, listview, gridview, list, .content-view, .frame {
            background-color: @view_bg_color;
            color: @view_fg_color;
            border-color: @borders;
        }

        treeview.view header button {
            background-color: @view_bg_color;
            color: @insensitive_fg_color;
            border-color: @borders;
        }

        .sidebar, placessidebar, stacksidebar, .navigation-sidebar {
            background-color: @sidebar_bg_color;
            color: @sidebar_fg_color;
            border-color: @sidebar_border_color;
        }

        entry, spinbutton, searchentry, combobox entry {
            background-image: none;
            background-color: @view_bg_color;
            color: @view_fg_color;
            border-color: @borders;
        }

        entry:focus, spinbutton:focus, searchentry:focus {
            border-color: @accent_bg_color;
        }

        button {
            background-image: none;
            color: @window_fg_color;
            text-shadow: none;
            border-color: @borders;
        }

        button:not(.flat) {
            background-color: @button_bg_color;
        }

        button:hover {
            background-color: @button_hover_bg_color;
        }

        button:active, button:checked {
            background-color: @button_active_bg_color;
        }

        button.suggested-action, button.default:not(.flat) {
            background-color: @accent_bg_color;
            color: @accent_fg_color;
            border-color: @accent_bg_color;
        }

        button.destructive-action {
            background-color: @destructive_bg_color;
            color: @destructive_fg_color;
            border-color: @destructive_bg_color;
        }

        button:disabled {
            color: @insensitive_fg_color;
        }

        row:selected, .view:selected, treeview.view:selected, iconview:selected, flowboxchild:selected,
        selection, entry selection, textview text selection, label selection {
            background-color: @theme_selected_bg_color;
            color: @theme_selected_fg_color;
        }

        menu, .menu, .context-menu, popover.background, popover > contents, popover > arrow {
            background-color: @popover_bg_color;
            color: @popover_fg_color;
            border-color: @borders;
        }

        menuitem:hover, menu menuitem:hover, popover modelbutton:hover, popover row:hover {
            background-color: @button_hover_bg_color;
            color: @popover_fg_color;
        }

        tooltip, tooltip.background {
            background-color: @tooltip_bg_color;
            color: @tooltip_fg_color;
        }

        notebook > header {
            background-color: @headerbar_bg_color;
            border-color: @borders;
        }

        notebook > header > tabs > tab:checked {
            color: @window_fg_color;
            box-shadow: inset 0 -3px @accent_bg_color;
        }

        notebook > stack {
            background-color: @view_bg_color;
        }

        scrollbar slider {
            background-color: @scrollbar_slider_color;
        }

        switch:checked, check:checked, radio:checked, checkbutton check:checked, checkbutton radio:checked {
            background-image: none;
            background-color: @accent_bg_color;
            color: @accent_fg_color;
            border-color: @accent_bg_color;
        }

        scale highlight, scale trough highlight, progressbar progress, levelbar block.filled {
            background-image: none;
            background-color: @accent_bg_color;
            border-color: @accent_bg_color;
        }

        separator {
            background-color: @borders;
        }

        *:link, link {
            color: @link_color;
        }
        """.trimIndent()
}
