package org.mjdev.desktop.managers.volume.pactl

/**
 * One object of `pactl list` output (`Sink #58`, `Source #63`, `Card #54`).
 *
 * @property header first line, e.g. `Sink #58`
 * @property fields single line fields (`Name`, `Description`, `Active Port`, …)
 * @property sections indented lists under a field (`Ports`, `Profiles`, …), raw trimmed lines
 * @property properties the `Properties` section as key / value
 */
data class PactlBlock(
    val header: String,
    val fields: Map<String, String>,
    val sections: Map<String, List<String>>,
    val properties: Map<String, String>,
) {
    /** Value of field [key], null when absent. */
    fun field(key: PactlField): String? = fields[key.key]

    /** Lines of section [key], empty when absent. */
    fun section(key: PactlField): List<String> = sections[key.key].orEmpty()

    /** Value of property [key], null when absent. */
    fun property(key: PactlProperty): String? = properties[key.key]
}
