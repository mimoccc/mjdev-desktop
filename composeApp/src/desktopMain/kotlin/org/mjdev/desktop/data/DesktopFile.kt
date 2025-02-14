package org.mjdev.desktop.data

import okio.Path
import okio.Path.Companion.toPath
import org.mjdev.desktop.helpers.desktopparser.DesktopFileParser
import org.mjdev.desktop.log.Log
import org.ini4j.Ini
import org.ini4j.Profile.Section
import org.mjdev.desktop.extensions.PathExt.absolutePath
import org.mjdev.desktop.extensions.PathExt.createNewFile
import org.mjdev.desktop.extensions.PathExt.cwd
import org.mjdev.desktop.extensions.PathExt.exists
import org.mjdev.desktop.extensions.PathExt.text
import org.mjdev.desktop.extensions.PathExt.delete
import org.mjdev.desktop.extensions.PathExt.mkdirs
import org.mjdev.desktop.extensions.PathExt.nameWithoutExtension
import org.mjdev.desktop.extensions.PathExt.parentFile

@Suppress("unused", "MemberVisibilityCanBePrivate")
class DesktopFile(
    var file: Path = cwd,
    val correctDir: Path = "/var/tmp/mjdev-desktop/corrected-desktop-files/".toPath(),
    val correctedFile: Path = correctDir.absolutePath.toPath().resolve(file.name),
    var fileData: String = (if (correctedFile.exists) correctedFile else file).text,
    private var content: DesktopFileParser = (runCatching {
        DesktopFileParser(fileData)
    }.onFailure { e ->
        Log.e(e)
    }.getOrNull() ?: DesktopFileParser())
) {
    val fileName: String
        get() = if (correctedFile.exists) correctedFile.name else file.name

    val absolutePath: String
        get() = if (correctedFile.exists) correctedFile.absolutePath else file.absolutePath

    val fullAppName
        get() = if (correctedFile.exists) correctedFile.nameWithoutExtension
        else file.nameWithoutExtension

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
        get() = correctedFile.exists

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
    ) = section(DesktopEntryType.DesktopEntry) {
        DesktopSectionScope(this).apply(block)
    }

    fun themeSection(
        block: ThemeSectionScope.() -> Unit
    ) = section(DesktopEntryType.Theme) {
        ThemeSectionScope(this).apply(block)
    }

    fun mkDirs(): DesktopFile {
        if (file.parent?.exists == false) file.parent?.mkdirs()
        return this
    }

    fun deleteFile(): DesktopFile {
        if (file.exists) file.delete()
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

    fun writeTo(f: Path) {
        Log.i("Writing: file:///${f.absolutePath}")
        if (!f.parentFile.exists) {
            f.parentFile.mkdirs()
        }
        if (f.exists) {
            f.delete()
        }
        if (f.parentFile.exists) {
            f.createNewFile()
        } else {
            Log.i("Cant store file: ${f.absolutePath}")
        }
        content.store(f.toFile())
    }

    fun write() = writeTo(file)

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
            file: Path,
            block: DesktopFile.() -> Unit
        ) = DesktopFile(file).apply(block)
    }
}
