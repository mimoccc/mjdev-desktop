package org.psippr.desktop.helpers.compositor

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
}
