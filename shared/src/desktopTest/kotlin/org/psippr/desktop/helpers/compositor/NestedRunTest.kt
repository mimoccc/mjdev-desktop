package org.psippr.desktop.helpers.compositor

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression: a nested test run must be recognised, so it leaves the host's GNOME settings alone. */
class NestedRunTest {
    @Test
    fun nestedMarkIsRecognised() {
        assertTrue(NestedRun.isNested(mapOf(NestedRun.ENV_NESTED to NestedRun.NESTED)))
    }

    @Test
    fun realSessionAndPlainRunAreNotNested() {
        assertFalse(NestedRun.isNested(emptyMap()))
        assertFalse(NestedRun.isNested(mapOf("XDG_CURRENT_DESKTOP" to "psippr")))
        assertFalse(NestedRun.isNested(mapOf(NestedRun.ENV_NESTED to "0")))
    }

    /** Regression: nested apps were never themed; with its own config folder a nested run may theme. */
    @Test
    fun nestedRunWithItsOwnConfigMayTheme() {
        val env =
            mapOf(
                NestedRun.ENV_NESTED to NestedRun.NESTED,
                NestedRun.ENV_CONFIG_HOME to "/h/.cache/psippr-desktop/nested-config",
            )
        assertTrue(NestedRun.hasOwnConfig(env, home = "/h"))
    }

    @Test
    fun nestedRunOnTheHostConfigMayNotTheme() {
        assertFalse(NestedRun.hasOwnConfig(mapOf(NestedRun.ENV_NESTED to NestedRun.NESTED), home = "/h"))
        val hostConfig = mapOf(NestedRun.ENV_NESTED to NestedRun.NESTED, NestedRun.ENV_CONFIG_HOME to "/h/.config")
        assertFalse(NestedRun.hasOwnConfig(hostConfig, home = "/h"))
    }

    @Test
    fun ownConfigOutsideANestedRunChangesNothing() {
        assertFalse(NestedRun.hasOwnConfig(mapOf(NestedRun.ENV_CONFIG_HOME to "/elsewhere"), home = "/h"))
    }

    /** The compositor sets the variable under this name (NestedSessionBus.ENV_NESTED). */
    @Test
    fun nameMatchesTheCompositor() {
        assertTrue(NestedRun.ENV_NESTED == "PSIPPR_NESTED")
    }
}
