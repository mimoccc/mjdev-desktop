package eu.mjdev.desktop.provider.data

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

    val type
        get() = props?.get(Prop_Type) ?: ""

    val version
        get() = props?.get(Prop_Version) ?: ""

    val name
        get() = props?.get(Prop_Name) ?: ""

    val comment
        get() = props?.get(Prop_Comment) ?: ""

    val path
        get() = props?.get(Prop_Path) ?: ""

    val exec
        get() = props?.get(Prop_Exec) ?: ""

    val icon
        get() = props?.get(Prop_Icon) ?: ""

    val categories
        get() = (props?.get(Prop_Categories) ?: "").split(";").toList()

    val notifyOnStart
        get() = (props?.get(Prop_StartupNotify) ?: "false").asBoolean()

    val runInTerminal
        get() = (props?.get(Prop_Terminal) ?: "false").asBoolean()

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

        private fun String.asBoolean(): Boolean = when (this.lowercase()) {
            "true" -> true
            "1" -> true
            else -> false
        }
    }

}