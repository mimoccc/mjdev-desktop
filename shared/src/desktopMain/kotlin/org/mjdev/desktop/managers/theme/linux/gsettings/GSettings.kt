package org.mjdev.desktop.managers.theme.linux.gsettings

import org.mjdev.desktop.helpers.system.shell.Shell
import org.mjdev.desktop.log.Log

/** Thin wrapper over the `gsettings` CLI (part of every GNOME / Ubuntu install, no extra dependency). */
object GSettings {
    /** gsettings binary. */
    private const val CMD = "gsettings"

    /** Option pointing gsettings to a non-default compiled schema directory. */
    private const val OPT_SCHEMA_DIR = "--schemadir"

    /** Sub-command reading a value. */
    private const val ACTION_GET = "get"

    /** Sub-command writing a value. */
    private const val ACTION_SET = "set"

    /** Sub-command restoring the schema default. */
    private const val ACTION_RESET = "reset"

    /** Quote gsettings puts around string values. */
    private const val QUOTE = "'"

    /** Current value of [key] without quotes, null when the key / schema is unavailable. */
    fun get(
        key: GSettingsKey,
        schemaDir: String? = null,
    ): String? = run(ACTION_GET, key, schemaDir)
        ?.trim()
        ?.takeIf { value -> value.isNotEmpty() }
        ?.removeSurrounding(QUOTE)

    /** Writes [value] to [key]. */
    fun set(
        key: GSettingsKey,
        value: String,
        schemaDir: String? = null,
    ) {
        run(ACTION_SET, key, schemaDir, value)
    }

    /** Restores the schema default of [key]. */
    fun reset(
        key: GSettingsKey,
        schemaDir: String? = null,
    ) {
        run(ACTION_RESET, key, schemaDir)
    }

    // stdout of the command, null on failure (missing gsettings or schema)
    private fun run(
        action: String,
        key: GSettingsKey,
        schemaDir: String?,
        vararg values: String,
    ): String? {
        val dirArgs = schemaDir?.let { dir -> listOf(OPT_SCHEMA_DIR, dir) }.orEmpty()
        val args = dirArgs + listOf(action, key.schema, key.key) + values
        var failed = false
        val out =
            Shell.executeAndRead(CMD, *args.toTypedArray()) { e ->
                failed = true
                Log.w("gsettings ${key.schema} ${key.key} failed: ${e.message}")
            }
        return out.takeUnless { failed }
    }
}
