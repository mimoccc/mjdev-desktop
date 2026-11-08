package org.psippr.desktop.helpers.font.guard

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/** Regression test: the relaunched child must not inherit the jpackage launcher marker. */
class FontGuardRelaunchEnvTest {
    @Test
    fun childEnvHasGuardButNoLauncherMarker() {
        val env = FontGuardRelauncher.childProcess(listOf(COMMAND), Path.of(CONFIG)).environment()
        assertFalse(env.containsKey(FontGuardSetting.JpackageLauncherEnv.value))
        assertEquals(CONFIG, env[FontGuardSetting.FontconfigFileEnv.value])
        assertEquals(FontGuardSetting.GuardMarkerValue.value, env[FontGuardSetting.GuardMarkerEnv.value])
    }

    private companion object {
        const val COMMAND = "true"
        const val CONFIG = "/tmp/fonts-guard.conf"
    }
}
