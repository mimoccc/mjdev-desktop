@file:Suppress("DEPRECATION")

package eu.mjdev.desktop.data

import eu.mjdev.desktop.extensions.Custom.ParsedBoolean
import eu.mjdev.desktop.extensions.Custom.ParsedList
import eu.mjdev.desktop.extensions.Custom.ParsedString
import org.ini4j.Ini
import org.ini4j.Profile
import org.ini4j.Wini
import java.io.File
import java.io.InputStream
import java.io.StringBufferInputStream

@Suppress("ConstPropertyName", "unused", "CanBeParameter", "MemberVisibilityCanBePrivate", "KotlinConstantConditions")
class DesktopFile(
    val file: File,
    private val content: Ini? = runCatching { if (file.exists()) Wini(file) else null }.getOrNull(),
    private val props: Profile.Section? = content?.get(DesktopFileType.DesktopEntry.text)
) {
    var fileName: String = file.name
    val absolutePath: String = file.absolutePath

    val actions: MutableList<Action> = parseActions()

    var type: DesktopFileType = DesktopFileType(props?.get(Prop_Type))
    var version: String = ParsedString(props?.get(Prop_Version))
    var name: String = ParsedString(props?.get(Prop_Name))
    var comment: String = ParsedString(props?.get(Prop_Comment))
    var path: String = ParsedString(props?.get(Prop_Path))
    var exec: String = ParsedString(props?.get(Prop_Exec))
    var icon: String = ParsedString(props?.get(Prop_Icon))
    var categories: MutableList<String> = ParsedList(props?.get(Prop_Categories))
    var notifyOnStart: Boolean = ParsedBoolean(props?.get(Prop_StartupNotify))
    var runInTerminal: Boolean = ParsedBoolean(props?.get(Prop_Terminal))
    var encoding: String = ParsedString(props?.get(Prop_Encoding))

    // todo
    private fun parseActions(): MutableList<Action> = mutableListOf()

    fun action(
        header: DesktopFileType,
        block: Action.() -> Unit
    ) = actions.add(Action("[${header.text}]").apply(block))

    fun write() {
        Wini().also { ini ->
            ini.add(DesktopFileType.DesktopEntry.text).also { section ->
                section.add(Prop_Type, type.text)
                if (name.isNotEmpty()) section.add(Prop_Name, name)
                if (comment.isNotEmpty()) section.add(Prop_Comment, comment)
                if (version.isNotEmpty()) section.add(Prop_Version, version)
                if (encoding.isNotEmpty()) section.add(Prop_Encoding, encoding)
                if (type == DesktopFileType.Application) {
                    if (path.isNotEmpty()) section.add(Prop_Path, path)
                    if (exec.isNotEmpty()) section.add(Prop_Exec, exec)
                    if (icon.isNotEmpty()) section.add(Prop_Icon, icon)
                    if (categories.isNotEmpty()) section.add(Prop_Categories, categories.joinToString { "$it;" })
                    if (notifyOnStart) section.add(Prop_StartupNotify, notifyOnStart)
                    if (runInTerminal) section.add(Prop_Terminal, runInTerminal)
                }
                actions.forEach { action ->
                    section.addChild(action.type.text).also { subsection ->
                        if (type == DesktopFileType.Application) {
                            subsection.add(Prop_Type, type.text)
                            if (version.isNotEmpty()) subsection.add(Prop_Type, version)
                            if (name.isNotEmpty()) subsection.add(Prop_Name, name)
                            if (comment.isNotEmpty()) subsection.add(Prop_Comment, comment)
                            if (path.isNotEmpty()) subsection.add(Prop_Path, path)
                            if (exec.isNotEmpty()) subsection.add(Prop_Exec, exec)
                            if (icon.isNotEmpty()) subsection.add(Prop_Icon, icon)
                            if (categories.isNotEmpty()) subsection.add(
                                Prop_Categories,
                                categories.joinToString { "$it;" })
                            if (notifyOnStart) subsection.add(Prop_StartupNotify, notifyOnStart)
                            if (runInTerminal) subsection.add(Prop_Terminal, runInTerminal)
                        }
                        if (type == DesktopFileType.Theme) {
                            if (action.gtkTheme.isNotEmpty()) subsection.add(Prop_GtkTheme, action.gtkTheme)
                            if (action.metacityTheme.isNotEmpty()) subsection.add(
                                Prop_MetacityTheme,
                                action.metacityTheme
                            )
                            if (action.iconTheme.isNotEmpty()) subsection.add(Prop_IconTheme, action.iconTheme)
                            if (action.cursorTheme.isNotEmpty()) subsection.add(Prop_CursorTheme, action.cursorTheme)
                            if (action.buttonLayout.isNotEmpty()) subsection.add(Prop_ButtonLayout, action.buttonLayout)
//                            subsection.add(Prop_X_Ubuntu_UseOverlayScrollbars, action.useOverlayScrollbars)
                        }
                        if (encoding.isNotEmpty()) section.add(Prop_Encoding, encoding)
                    }
                }
            }
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            ini.store(file)
        }
    }

    class Action(
        val text: String = "",
        private val data: InputStream = StringBufferInputStream(text),
        private val content: Ini? = runCatching { Ini(data) }.getOrNull(),
        private val props: Profile.Section? = content?.get("Desktop Entry")
    ) {
        var type: DesktopFileType = DesktopFileType(props?.get(Prop_Type))
        var version: String = ParsedString(props?.get(Prop_Version))
        var name: String = ParsedString(props?.get(Prop_Name))
        var comment: String = ParsedString(props?.get(Prop_Comment))
        var path: String = ParsedString(props?.get(Prop_Path))
        var exec: String = ParsedString(props?.get(Prop_Exec))
        var icon: String = ParsedString(props?.get(Prop_Icon))
        var categories: MutableList<String> = ParsedList(props?.get(Prop_Categories))
        var notifyOnStart: Boolean = ParsedBoolean(props?.get(Prop_StartupNotify))
        var runInTerminal: Boolean = ParsedBoolean(props?.get(Prop_Terminal))
        var encoding: String = ParsedString(props?.get(Prop_Encoding))
        var gtkTheme: String = ParsedString(props?.get(Prop_GtkTheme))
        var metacityTheme: String = ParsedString(props?.get(Prop_MetacityTheme))
        var iconTheme: String = ParsedString(props?.get(Prop_IconTheme))
        var cursorTheme: String = ParsedString(props?.get(Prop_CursorTheme))
        var buttonLayout: String = ParsedString(props?.get(Prop_ButtonLayout))
//        var useOverlayScrollbars: Boolean = ParsedBoolean(props?.get(Prop_X_Ubuntu_UseOverlayScrollbars))
    }

    enum class DesktopFileType(
        val text: String
    ) {
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
        const val Prop_Type = "Type" // string "Application"
        const val Prop_Version = "Version" // string
        const val Prop_Path = "Path" // string
        const val Prop_Name = "Name" // string
        const val Prop_Comment = "Comment" // string
        const val Prop_Exec = "Exec" // string
        const val Prop_StartupNotify = "StartupNotify" // boolean
        const val Prop_Terminal = "Terminal" // boolean
        const val Prop_Icon = "Icon" // string
        const val Prop_Categories = "Categories" // string delimited ;
        const val Prop_GenericName = "GenericName"
        const val Prop_MimeType = "MimeType" // string delimited ;
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

        fun create(path: String): DesktopFile = create(File(path).apply {
            if (!exists()) createNewFile()
        })

        fun create(file: File): DesktopFile = DesktopFile(file)
    }
}
