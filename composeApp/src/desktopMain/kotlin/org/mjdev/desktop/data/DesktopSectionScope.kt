package org.mjdev.desktop.data

import org.ini4j.Profile
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_Categories
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_Comment
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_Encoding
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_Exec
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_Icon
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_Name
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_OnlyShowIn
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_Path
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_StartupNotify
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_StartupWMClass
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_Terminal
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_Type
import org.mjdev.desktop.data.DesktopFile.Companion.Prop_Version
import org.mjdev.desktop.helpers.parsers.Parsers.ParsedBoolean
import org.mjdev.desktop.helpers.parsers.Parsers.ParsedList
import org.mjdev.desktop.helpers.parsers.Parsers.ParsedString

class DesktopSectionScope(
        override val section: Profile.Section
    ) : ISectionScope {
        var Type: DesktopEntryType
            get() = DesktopEntryType(section[Prop_Type])
            set(value) {
                section[Prop_Type] = value.text
            }

        var Version: String
            get() = ParsedString(section[Prop_Version])
            set(value) {
                section[Prop_Version] = value
            }

        var Name: String
            get() = ParsedString(section[Prop_Name])
            set(value) {
                section[Prop_Name] = value
            }

        var Comment: String
            get() = ParsedString(section[Prop_Comment])
            set(value) {
                section[Prop_Comment] = value
            }

        var Path: String
            get() = ParsedString(section[Prop_Path])
            set(value) {
                section[Prop_Path] = value
            }

        var Exec: String
            get() = ParsedString(section[Prop_Exec])
            set(value) {
                section[Prop_Exec] = value
            }

        var Icon: String
            get() = ParsedString(section[Prop_Icon])
            set(value) {
                section[Prop_Icon] = value
            }

        var Encoding: String
            get() = ParsedString(section[Prop_Encoding])
            set(value) {
                section[Prop_Encoding] = value
            }

        var NotifyOnStart: Boolean
            get() = ParsedBoolean(section[Prop_StartupNotify])
            set(value) {
                section[Prop_StartupNotify] = value.toString()
            }

        var RunInTerminal: Boolean
            get() = ParsedBoolean(section[Prop_Terminal])
            set(value) {
                section[Prop_Terminal] = value.toString()
            }

        var Categories: MutableList<String>
            get() = ParsedList(section[Prop_Categories])
            set(value) {
                section[Prop_Categories] = value.joinToString { "$it;" }
            }

        var OnlyShowIn: MutableList<String>
            get() = ParsedList(section[Prop_OnlyShowIn])
            set(value) {
                section[Prop_OnlyShowIn] = value.joinToString { "$it;" }
            }

        var StartupWMClass: String
            get() = ParsedString(section[Prop_StartupWMClass]).trim()
            set(value) {
                section[Prop_StartupWMClass] = value
            }
    }