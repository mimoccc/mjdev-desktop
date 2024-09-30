package eu.mjdev.desktop.data

import eu.mjdev.desktop.extensions.Custom.ParsedBoolean
import eu.mjdev.desktop.extensions.Custom.ParsedList
import eu.mjdev.desktop.extensions.Custom.ParsedString
import org.ini4j.Ini
import org.ini4j.Profile.Section
import org.ini4j.Wini
import java.io.File

@Suppress("unused", "MemberVisibilityCanBePrivate", "ConstPropertyName", "PropertyName")
class DesktopFile(
    val file: File,
    val content: Ini = Wini(file),
) {
    var fileName: String = file.name
    val absolutePath: String = file.absolutePath

    val sections: Map<String, Section>
        get() = content.map { if (it is Section) it else null }.filterNotNull().associateBy { it.value.toString() }

    val desktopSection
        get() = content[DesktopFileType.DesktopEntry.text]?.let { DesktopSectionScope(it) }

    val themeSection
        get() = content[DesktopFileType.Theme.text]?.let { ThemeSectionScope(it) }

    fun section(
        type: DesktopFileType,
        block: Section.() -> Unit
    ) = (content[type.text] ?: content.add(type.text))?.apply(block)

    fun desktopSection(
        block: DesktopSectionScope.() -> Unit
    ) = section(DesktopFile.DesktopFileType.DesktopEntry) {
        DesktopSectionScope(this).apply(block)
    }

    fun themeSection(
        block: ThemeSectionScope.() -> Unit
    ) = section(DesktopFile.DesktopFileType.Theme) {
        ThemeSectionScope(this).apply(block)
    }

    fun write() {
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        content.store(file)
    }

    interface ISectionScope {
        val section: Section
    }

    class DesktopSectionScope(
        override val section: Section
    ) : ISectionScope {
        var Type: DesktopFileType
            get() = DesktopFileType(section[Prop_Type])
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
    }

    class ThemeSectionScope(
        override val section: Section
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

    fun mkDirs(): DesktopFile {
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        return this
    }

    fun deleteFile(): DesktopFile {
        if (file.exists()) file.delete()
        return this
    }

    fun createNewFile(): DesktopFile {
        file.createNewFile()
        return this
    }

    fun newFile(): DesktopFile {
        mkDirs()
        deleteFile()
        createNewFile()
        return this
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

        fun desktopFile(
            file: File,
            block: DesktopFile.() -> Unit
        ) = DesktopFile(file).apply(block)
    }
}
