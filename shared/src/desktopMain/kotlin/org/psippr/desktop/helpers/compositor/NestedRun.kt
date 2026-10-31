package org.psippr.desktop.helpers.compositor

import java.io.File

/**
 * Whether the shell runs in a nested psippr compositor (a test window on another desktop). Such
 * a run shares the user's home and settings with the host desktop, so it must not change them:
 * psipprc marks it with [ENV_NESTED] (see the compositor's NestedSessionBus).
 */
object NestedRun {
    /** Environment variable psipprc sets for a nested shell; same name as in the compositor. */
    const val ENV_NESTED = "PSIPPR_NESTED"

    /** Value of [ENV_NESTED] in a nested run. */
    const val NESTED = "1"

    /** True when [environment] (the process environment by default) marks a nested run. */
    fun isNested(environment: Map<String, String> = System.getenv()): Boolean = environment[ENV_NESTED] == NESTED

    /** Variable naming the config folder; a nested run with its own one may theme freely. */
    const val ENV_CONFIG_HOME = "XDG_CONFIG_HOME"

    /** The user's config folder under the home folder. */
    private const val USER_CONFIG = ".config"

    /**
     * True when a nested run has its own config folder (not the host's `~/.config`, see the
     * runNestedDesktop task), so theming it cannot change the host desktop.
     */
    fun hasOwnConfig(
        environment: Map<String, String> = System.getenv(),
        home: String = System.getProperty("user.home").orEmpty(),
    ): Boolean {
        val configHome = environment[ENV_CONFIG_HOME]?.takeIf { value -> value.isNotBlank() } ?: return false
        return isNested(environment) && File(configHome).absoluteFile != File(home, USER_CONFIG).absoluteFile
    }
}
