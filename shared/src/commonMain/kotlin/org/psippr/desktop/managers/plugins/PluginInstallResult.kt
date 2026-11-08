package org.psippr.desktop.managers.plugins

/**
 * Outcome of installing plugins from an archive.
 *
 * @property installed names of the plugins that were installed (switched off until the user enables them)
 * @property problems one human readable line per jar or archive that could not be installed
 * @property installedIds plugin ids of [installed], to reload a plugin that was replaced
 */
data class PluginInstallResult(
    val installed: List<String> = emptyList(),
    val problems: List<String> = emptyList(),
    val installedIds: List<String> = emptyList(),
) {
    /** True when at least one plugin was installed. */
    val succeeded: Boolean
        get() = installed.isNotEmpty()

    companion object {
        /** Result of a platform that cannot install plugins. */
        val UNSUPPORTED = PluginInstallResult(problems = listOf("Installing plugins is not supported here."))
    }
}
