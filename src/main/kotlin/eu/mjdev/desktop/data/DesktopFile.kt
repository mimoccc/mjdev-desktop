package eu.mjdev.desktop.data

import eu.mjdev.desktop.extensions.Custom.ParsedBoolean
import eu.mjdev.desktop.extensions.Custom.ParsedList
import eu.mjdev.desktop.extensions.Custom.ParsedString
import org.ini4j.Ini
import org.ini4j.Profile.Section
import org.ini4j.Wini
import java.io.File

@Suppress("unused", "MemberVisibilityCanBePrivate", "ConstPropertyName")
class DesktopFile(
    val file: File,
    val content: Ini = Wini(file),
) {
    var fileName: String = file.name
    val absolutePath: String = file.absolutePath

    val sections: Map<String, Section>
        get() = content.map { if (it is Section) it else null }.filterNotNull().associateBy { it.value.toString() }

    val desktopSection
        get() = content[DesktopFileType.DesktopEntry.text]

    val themeSection
        get() = content[DesktopFileType.Theme.text]

    fun section(
        type: DesktopFileType,
        block: Section.() -> Unit
    ) = (content[type.text] ?: content.add(type.text))?.apply(block)

    fun write() {
        if (file.exists()) file.delete()
        file.createNewFile()
        content.store(file)
    }

    enum class DesktopFileType(val text: String) {
        Unknown(""),
        DesktopEntry("Desktop Entry"),
        Application("Application"),
        Theme("X-GNOME-Metatheme");

        companion object {
            operator fun invoke(
                value: String?
            ): DesktopFileType = value.orEmpty().trim().let { v ->
                entries.firstOrNull { e -> e.text.contentEquals(v, true) } ?: Unknown
            }
        }
    }

    companion object {
        const val Prop_Type = "Type"
        const val Prop_Version = "Version"
        const val Prop_Path = "Path"
        const val Prop_Name = "Name"
        const val Prop_Comment = "Comment"
        const val Prop_Exec = "Exec"
        const val Prop_StartupNotify = "StartupNotify"
        const val Prop_Terminal = "Terminal"
        const val Prop_Icon = "Icon"
        const val Prop_Categories = "Categories"
        const val Prop_GenericName = "GenericName"
        const val Prop_MimeType = "MimeType"
        const val Prop_Actions = "Actions"
        const val Prop_DBusActivatable = "DBusActivatable"
        const val Prop_Encoding = "Encoding"

        const val Prop_X_GNOME_UsesNotifications = "X-GNOME-UsesNotifications"
        const val Prop_X_Purism_FormFactor = "X-Purism-FormFactor"
        const val Prop_X_Unity_IconBackgroundColor = "X-Unity-IconBackgroundColor"
        const val Prop_X_Ubuntu_Gettext_Domain = "X-Ubuntu-Gettext-Domain"

        const val Prop_GtkTheme = "GtkTheme"
        const val Prop_MetacityTheme = "MetacityTheme"
        const val Prop_IconTheme = "IconTheme"
        const val Prop_CursorTheme = "CursorTheme"
        const val Prop_ButtonLayout = "ButtonLayout"
        const val Prop_X_Ubuntu_UseOverlayScrollbars = "X-Ubuntu-UseOverlayScrollbars"

        var Section.Type: DesktopFileType
            get() = DesktopFileType(this[Prop_Type])
            set(value) {
                this[Prop_Type] = value.text
            }

        var Section.Version: String
            get() = ParsedString(this[Prop_Version])
            set(value) {
                this[Prop_Version] = value
            }

        var Section.Name: String
            get() = ParsedString(this[Prop_Name])
            set(value) {
                this[Prop_Name] = value
            }

        var Section.Comment: String
            get() = ParsedString(this[Prop_Comment])
            set(value) {
                this[Prop_Comment] = value
            }

        var Section.Path: String
            get() = ParsedString(this[Prop_Path])
            set(value) {
                this[Prop_Path] = value
            }

        var Section.Exec: String
            get() = ParsedString(this[Prop_Exec])
            set(value) {
                this[Prop_Exec] = value
            }

        var Section.Icon: String
            get() = ParsedString(this[Prop_Icon])
            set(value) {
                this[Prop_Icon] = value
            }

        var Section.Encoding: String
            get() = ParsedString(this[Prop_Encoding])
            set(value) {
                this[Prop_Encoding] = value
            }

        var Section.NotifyOnStart: Boolean
            get() = ParsedBoolean(this[Prop_StartupNotify])
            set(value) {
                this[Prop_StartupNotify] = value.toString()
            }

        var Section.RunInTerminal: Boolean
            get() = ParsedBoolean(this[Prop_Terminal])
            set(value) {
                this[Prop_Terminal] = value.toString()
            }

        var Section.Categories: MutableList<String>
            get() = ParsedList(this[Prop_Categories])
            set(value) {
                this[Prop_Categories] = value.joinToString { "$it;" }
            }

        var Section.GtkTheme: String
            get() = ParsedString(this[Prop_GtkTheme])
            set(value) {
                this[Prop_GtkTheme] = value
            }

        var Section.MetacityTheme: String
            get() = ParsedString(this[Prop_MetacityTheme])
            set(value) {
                this[Prop_MetacityTheme] = value
            }

        var Section.IconTheme: String
            get() = ParsedString(this[Prop_IconTheme])
            set(value) {
                this[Prop_IconTheme] = value
            }

        var Section.CursorTheme: String
            get() = ParsedString(this[Prop_CursorTheme])
            set(value) {
                this[Prop_CursorTheme] = value
            }

        var Section.ButtonLayout: String
            get() = ParsedString(this[Prop_ButtonLayout])
            set(value) {
                this[Prop_ButtonLayout] = value
            }

        var Section.UseOverlayScrollbars: Boolean
            get() = ParsedBoolean(this[Prop_X_Ubuntu_UseOverlayScrollbars])
            set(value) {
                this[Prop_X_Ubuntu_UseOverlayScrollbars] = value.toString()
            }

    }
}
