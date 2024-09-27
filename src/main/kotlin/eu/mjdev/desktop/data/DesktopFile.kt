package eu.mjdev.desktop.data

import eu.mjdev.desktop.extensions.Custom.ParsedBoolean
import eu.mjdev.desktop.extensions.Custom.ParsedList
import org.ini4j.Ini
import java.io.File

@Suppress("ConstPropertyName", "unused")
class DesktopFile(
    val file: File
) {
    private val content
        get() = runCatching { if (file.exists()) Ini(file) else null }.getOrNull()

    private val props
        get() = content?.get("Desktop Entry")

    val fileName: String
        get() = file.name

    val type
        get() = DesktopFileType(props?.get(Prop_Type))

    val version
        get() = props?.get(Prop_Version).orEmpty()

    val name
        get() = props?.get(Prop_Name).orEmpty()

    val comment
        get() = props?.get(Prop_Comment).orEmpty()

    val path
        get() = props?.get(Prop_Path).orEmpty()

    val exec
        get() = props?.get(Prop_Exec).orEmpty()

    val icon
        get() = props?.get(Prop_Icon).orEmpty()

    val categories
        get() = ParsedList(props?.get(Prop_Categories))

    val notifyOnStart
        get() = ParsedBoolean(props?.get(Prop_StartupNotify))

    val runInTerminal
        get() = ParsedBoolean(props?.get(Prop_Terminal))

    val absolutePath: String
        get() = file.absolutePath

    enum class DesktopFileType(
        val header: String
    ) {
        Unknown(""),
        Application("Application"),
        Theme("X-GNOME-Metatheme");

        companion object {
            operator fun invoke(
                value: String?
            ): DesktopFileType = value.orEmpty().trim().let { v ->
                entries.firstOrNull { e -> e.header.contentEquals(v, true) } ?: Unknown
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

        const val Prop_X_GNOME_UsesNotifications = "X-GNOME-UsesNotifications"
        const val Prop_X_Purism_FormFactor = "X-Purism-FormFactor"
        const val Prop_X_Unity_IconBackgroundColor = "X-Unity-IconBackgroundColor"
        const val Prop_X_Ubuntu_Gettext_Domain = "X-Ubuntu-Gettext-Domain"

        private fun String.asBoolean(): Boolean = when (this.lowercase()) {
            "true" -> true
            "1" -> true
            else -> false
        }

        fun create(path: String): DesktopFile = create(File(path).apply {
            if (!exists()) createNewFile()
        })

        fun create(file: File): DesktopFile = DesktopFile(file)
    }
}
