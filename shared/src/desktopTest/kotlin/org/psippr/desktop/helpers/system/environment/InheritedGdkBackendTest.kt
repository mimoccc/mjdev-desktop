package org.psippr.desktop.helpers.system.environment

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Regression: Nautilus opened from a desktop icon got `GDK_BACKEND=x11` from Java's AWT, ran
 * through XWayland and ignored the generated icon theme and the palette colour changes.
 */
class InheritedGdkBackendTest {
    @Test
    fun backendAddedInsideTheProcessIsRemoved() {
        val env = mutableMapOf(InheritedGdkBackend.GDK_BACKEND to "x11", "HOME" to "/h")
        InheritedGdkBackend.restore(env, mapOf("HOME" to "/h"))
        assertEquals(mapOf("HOME" to "/h"), env)
    }

    @Test
    fun backendTheSessionChoseIsKept() {
        val env = mutableMapOf(InheritedGdkBackend.GDK_BACKEND to "x11")
        InheritedGdkBackend.restore(env, mapOf(InheritedGdkBackend.GDK_BACKEND to "wayland"))
        assertEquals("wayland", env[InheritedGdkBackend.GDK_BACKEND])
    }

    @Test
    fun environTextIsParsed() {
        assertEquals(
            mapOf("A" to "1", "B" to "x=y"),
            InheritedGdkBackend.parse("A=1\u0000B=x=y\u0000\u0000"),
        )
    }

    @Test
    fun unreadableStartEnvironmentIsUnknown() {
        assertNull(InheritedGdkBackend.startEnvironment(File("/nonexistent/environ")))
    }
}
