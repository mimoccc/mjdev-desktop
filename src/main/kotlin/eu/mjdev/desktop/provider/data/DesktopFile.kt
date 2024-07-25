package eu.mjdev.desktop.provider.data

import java.io.File

@Suppress("ConstPropertyName", "unused")
class DesktopFile(
    val file: File
) {
    private val content
        get() = file.readText()

    private val lines
        get() = content.split("\n").map { line ->
            line.trim()
        }

    private val props
        get() = lines.mapNotNull { line ->
            if (line.contains("=")) {
                val first = line.substring(0, line.indexOf('='))
                val second = line.substring(line.indexOf('=') + 1)
                Pair(first, second)
            } else null
        }.toMap()

    val type
        get() = props[Prop_Type] ?: ""

    val version
        get() = props[Prop_Version] ?: ""

    val name
        get() = props[Prop_Name] ?: ""

    val comment
        get() = props[Prop_Comment] ?: ""

    val path
        get() = props[Prop_Path] ?: ""

    val exec
        get() = props[Prop_Exec] ?: ""

    val icon
        get() = props[Prop_Icon] ?: ""

    val categories
        get() = (props[Prop_Categories] ?: "").split(";").toList()

    val notifyOnStart
        get() = (props[Prop_StartupNotify] ?: "false").asBoolean()

    val runInTerminal
        get() = (props[Prop_Terminal] ?: "false").asBoolean()

    companion object {
        const val Prop_Type = "Type" // string
        const val Prop_Version = "Version" // string
        const val Prop_Path = "Path" // string
        const val Prop_Name = "Name" // string
        const val Prop_Comment = "Comment" // string
        const val Prop_Exec = "Exec" // string
        const val Prop_StartupNotify = "StartupNotify" // boolean
        const val Prop_Terminal = "Terminal" // boolean
        const val Prop_Icon = "Icon" // string
        const val Prop_Categories = "Categories" // string delimited ;

        private fun String.asBoolean(): Boolean = when (this.lowercase()) {
            "true" -> true
            "1" -> true
            else -> false
        }
    }

}