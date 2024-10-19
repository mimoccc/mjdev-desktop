package eu.mjdev.desktop.data

import eu.mjdev.desktop.extensions.Custom.ParsedBoolean
import eu.mjdev.desktop.extensions.Custom.ParsedList
import eu.mjdev.desktop.extensions.Custom.ParsedString
import eu.mjdev.desktop.extensions.Custom.text
import eu.mjdev.desktop.helpers.desktopparser.DesktopFileParser
import eu.mjdev.desktop.log.Log
import org.ini4j.Ini
import org.ini4j.Profile.Section
import java.io.File

@Suppress("unused", "MemberVisibilityCanBePrivate", "ConstPropertyName", "PropertyName")
class DesktopFile(
    var file: File = File(""),
    val correctDir: File = File("/var/tmp/mjdev-desktop/corrected-desktop-files/"),
    val correctedFile: File = File(correctDir, file.name),
    var fileData: String = (if (correctedFile.exists()) correctedFile else file).text,
    private var content: DesktopFileParser = (runCatching {
        DesktopFileParser(fileData)
    }.onFailure { e ->
        Log.e(e)
    }.getOrNull() ?: DesktopFileParser())
) {
    var fileName: String = if (correctedFile.exists()) correctedFile.name else file.name
    val absolutePath: String = if (correctedFile.exists()) correctedFile.absolutePath else file.absolutePath

    val fullAppName
        get() = if (correctedFile.exists()) correctedFile.nameWithoutExtension else file.nameWithoutExtension

    val sections: Map<String, Section>
        get() = content.map {
            if (it is Section) it else null
        }.filterNotNull().associateBy {
            it.value.toString()
        }

    val desktopSection: DesktopSectionScope?
        get() = content[DesktopEntryType.DesktopEntry]?.let { DesktopSectionScope(it) }

    val themeSection
        get() = content[DesktopEntryType.Theme]?.let { ThemeSectionScope(it) }

    val isApp
        get() = desktopSection?.Type == DesktopEntryType.Application

    val isCorrect
        get() = correctedFile.exists()

    init {
        correctDir.mkdirs()
        if (isApp && !isCorrect) {
            runCatching {
                correctedFile.let { f ->
                    desktopSection?.StartupWMClass = fullAppName
                    desktopSection?.OnlyShowIn = mutableListOf()
                    writeTo(f)
                    file = f
                    fileData = file.text
                    content = DesktopFileParser(fileData)
                }
            }.onFailure { e ->
                Log.e(e)
            }
        }
    }

    fun section(
        type: DesktopEntryType,
        block: Section.() -> Unit
    ) = (content[type.text] ?: content.add(type.text))?.apply(block)

    fun desktopSection(
        block: DesktopSectionScope.() -> Unit
    ) = section(DesktopFile.DesktopEntryType.DesktopEntry) {
        DesktopSectionScope(this).apply(block)
    }

    fun themeSection(
        block: ThemeSectionScope.() -> Unit
    ) = section(DesktopFile.DesktopEntryType.Theme) {
        ThemeSectionScope(this).apply(block)
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

    fun writeTo(f: File) {
        Log.i("Writing: file:///${f.absolutePath}")
        if (!f.parentFile.exists()) {
            f.parentFile.mkdirs()
        }
        if (f.exists()) {
            f.delete()
        }
        if (f.parentFile.exists()) {
            f.createNewFile()
        } else {
            Log.i("Cant store file: ${f.absolutePath}")
        }
        content.store(f)
    }

    fun write() =
        writeTo(file)

    interface ISectionScope {
        val section: Section
    }

    class DesktopSectionScope(
        override val section: Section
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

    enum class DesktopEntryType(val text: String) {
        Unknown(""),
        DesktopEntry("Desktop Entry"),
        Application("Application"),
        Theme("X-GNOME-Metatheme");

        companion object {
            operator fun invoke(
                value: String?
            ): DesktopEntryType = value.orEmpty().trim().let { v ->
                entries.firstOrNull { e -> e.text.contentEquals(v, true) } ?: Unknown
            }
        }
    }

    companion object {
        const val EXTENSION: String = "desktop"

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
        const val Prop_StartupWMClass = "StartupWMClass"
        const val Prop_OnlyShowIn = "OnlyShowIn"

        // todo
        const val Prop_GenericName = "GenericName"
        const val Prop_MimeType = "MimeType"
        const val Prop_Actions = "Actions"
        const val Prop_DBusActivatable = "DBusActivatable"
        const val Prop_Encoding = "Encoding"
        const val Prop_Keywords = "Keywords"
        const val Prop_X_ExecArg = "X-ExecArg"
        const val Prop_X_Ubuntu_Gettext_Domain = "X-Ubuntu-Gettext-Domain"
        const val Prop_NoDisplay = "NoDisplay" // boolean
        const val Prop_X_GNOME_UsesNotifications = "X-GNOME-UsesNotifications"
        const val Prop_X_Purism_FormFactor = "X-Purism-FormFactor"
        const val Prop_X_Unity_IconBackgroundColor = "X-Unity-IconBackgroundColor"
        const val Prop_SingleMainWindow = "SingleMainWindow"

        // theme
        const val Prop_GtkTheme = "GtkTheme"
        const val Prop_MetacityTheme = "MetacityTheme"
        const val Prop_IconTheme = "IconTheme"
        const val Prop_CursorTheme = "CursorTheme"
        const val Prop_ButtonLayout = "ButtonLayout"
        const val Prop_X_Ubuntu_UseOverlayScrollbars = "X-Ubuntu-UseOverlayScrollbars"

        val Empty = DesktopFile()
        val Test = DesktopFile(
            fileData = """
                [Desktop Entry]
                Name=mjdev-desktop
                Comment=MJDev desktop environment
                Exec=/opt/mjdev-desktop/bin/mjdev-desktop
                Icon=/opt/mjdev-desktop/lib/mjdev-desktop.png
                Terminal=false
                Type=Application
                Categories=Desktop
                MimeType=
                """.trimMargin()
        )

        operator fun Ini.get(
            type: DesktopEntryType
        ): Section? = get(type.text)

        fun desktopFile(
            file: File,
            block: DesktopFile.() -> Unit
        ) = DesktopFile(file).apply(block)
    }
}
