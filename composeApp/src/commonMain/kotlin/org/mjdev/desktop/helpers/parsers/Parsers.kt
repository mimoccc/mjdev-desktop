package org.mjdev.desktop.helpers.parsers

object Parsers {
    fun ParsedList(
        value: String?,
        delimiter: String = ";"
    ): MutableList<String> = value?.split(delimiter)?.toMutableList() ?: mutableListOf()

    fun ParsedBoolean(
        value: String?,
        defaultValue: Boolean = false,
    ) = value.orEmpty().trim().lowercase().let { b ->
        when (b) {
            "true" -> true
            "ano" -> true
            "yes" -> true
            "enabled" -> true
            "*" -> true
            "1" -> true
            else -> defaultValue
        }
    }

    fun ParsedString(
        value: Any?,
        defaultValue: String = ""
    ) = value?.toString() ?: defaultValue
}