package org.psippr.desktop.managers.volume.pactl

/** Parses `LC_ALL=C pactl list <type>` text output into [PactlBlock]s. */
object PactlParser {
    /** Indentation of a field line. */
    private const val FIELD_DEPTH = 1

    /** Separator between a field name and its value. */
    private const val FIELD_SEPARATOR = ":"

    /** Separator between a property key and its value. */
    private const val PROPERTY_SEPARATOR = " = "

    /** Quote around property values. */
    private const val QUOTE = "\""

    /** Indentation character. */
    private const val TAB = '\t'

    /** Blocks of the given output. */
    fun parse(output: String): List<PactlBlock> = output
        .lines()
        .fold(mutableListOf<MutableList<String>>()) { blocks, line ->
            when {
                line.isBlank() -> Unit
                !line.first().isWhitespace() -> blocks.add(mutableListOf(line))
                else -> blocks.lastOrNull()?.add(line)
            }
            blocks
        }.map { lines -> block(lines) }

    private fun block(lines: List<String>): PactlBlock {
        val fields = mutableMapOf<String, String>()
        val sections = mutableMapOf<String, MutableList<String>>()
        var section: MutableList<String>? = null
        lines.drop(1).forEach { line ->
            val depth = line.takeWhile { c -> c == TAB }.length
            val text = line.trim()
            if (depth == FIELD_DEPTH && text.contains(FIELD_SEPARATOR)) {
                val key = text.substringBefore(FIELD_SEPARATOR).trim()
                val value = text.substringAfter(FIELD_SEPARATOR).trim()
                fields[key] = value
                section = if (value.isEmpty()) mutableListOf<String>().also { list -> sections[key] = list } else null
            } else {
                section?.add(text)
            }
        }
        val properties =
            sections[PactlField.PROPERTIES.key]
                .orEmpty()
                .filter { line -> line.contains(PROPERTY_SEPARATOR) }
                .associate { line ->
                    line.substringBefore(PROPERTY_SEPARATOR).trim() to
                        line.substringAfter(PROPERTY_SEPARATOR).trim().removeSurrounding(QUOTE)
                }
        return PactlBlock(lines.first().trim(), fields, sections, properties)
    }
}
