package org.mjdev.desktop.data

import org.ini4j.Profile
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_ButtonLayout
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_CursorTheme
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_GtkTheme
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_IconTheme
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_MetacityTheme
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_X_Ubuntu_UseOverlayScrollbars
import org.mjdev.desktop.helpers.parsers.Parsers.ParsedBoolean
import org.mjdev.desktop.helpers.parsers.Parsers.ParsedString

class ThemeSectionScope(
        override val section: Profile.Section
    ) : ISectionScope {
        var GtkTheme: String
            get() = ParsedString(section[Prop_GtkTheme])
            set(value) {
                section[Prop_GtkTheme] = value
            }

        var MetacityTheme: String
            get() = ParsedString(section[Prop_MetacityTheme])
            set(value) {
                section[Prop_MetacityTheme] = value
            }

        var IconTheme: String
            get() = ParsedString(section[Prop_IconTheme])
            set(value) {
                section[Prop_IconTheme] = value
            }

        var CursorTheme: String
            get() = ParsedString(section[Prop_CursorTheme])
            set(value) {
                section[Prop_CursorTheme] = value
            }

        var ButtonLayout: String
            get() = ParsedString(section[Prop_ButtonLayout])
            set(value) {
                section[Prop_ButtonLayout] = value
            }

        var UseOverlayScrollbars: Boolean
            get() = ParsedBoolean(section[Prop_X_Ubuntu_UseOverlayScrollbars])
            set(value) {
                section[Prop_X_Ubuntu_UseOverlayScrollbars] = value.toString()
            }
    }