package org.psippr.desktop.managers.plugins

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression tests: plugins are installed from a zip (or jar) instead of copying jars by hand. */
class PluginArchiveInstallerTest {
    private val root = Files.createTempDirectory("plugin-install-test").toFile()
    private val pluginsDir = File(root, "home/.psippr-desktop/plugins")

    @AfterTest
    fun cleanUp() {
        root.deleteRecursively()
    }

    @Test
    fun pluginJarInsideAZipIsInstalledUnderItsId() {
        val archive = zip("clock.zip", "some/dir/clock-1.0.jar" to pluginJar(CLOCK_ID, "Clock"))
        val result = PluginArchiveInstaller.install(archive, pluginsDir, BUILT_IN)
        assertEquals(listOf("Clock"), result.installed)
        assertEquals(listOf(CLOCK_ID), result.installedIds)
        assertTrue(File(pluginsDir, "$CLOCK_ID.jar").isFile)
    }

    @Test
    fun singlePluginJarIsInstalledDirectly() {
        val jar = File(root, "clock.jar").apply { writeBytes(pluginJar(CLOCK_ID, "Clock")) }
        assertEquals(listOf(CLOCK_ID), PluginArchiveInstaller.install(jar, pluginsDir, BUILT_IN).installedIds)
    }

    /** Regression: an entry path climbing out of the folder must not write outside it (zip slip). */
    @Test
    fun entryPathCannotLeaveThePluginsFolder() {
        val archive = zip("evil.zip", "../../../../escaped.jar" to pluginJar(CLOCK_ID, "Clock"))
        PluginArchiveInstaller.install(archive, pluginsDir, BUILT_IN)
        assertTrue(root.walk().none { it.name == "escaped.jar" })
        assertTrue(File(pluginsDir, "$CLOCK_ID.jar").isFile)
    }

    @Test
    fun jarWithoutMetadataIsRefused() {
        val archive = zip("bad.zip", "lib.jar" to jarOf("README" to "no plugin here"))
        val result = PluginArchiveInstaller.install(archive, pluginsDir, BUILT_IN)
        assertFalse(result.succeeded)
        assertTrue(result.problems.single().startsWith("lib.jar"))
    }

    @Test
    fun builtInPluginCannotBeReplaced() {
        val archive = zip("memory.zip", "memory.jar" to pluginJar(BUILT_IN_ID, "Fake memory"))
        val result = PluginArchiveInstaller.install(archive, pluginsDir, BUILT_IN)
        assertFalse(result.succeeded)
        assertFalse(File(pluginsDir, "$BUILT_IN_ID.jar").exists())
    }

    /** Reinstalling keeps the old jar in the replaced folder instead of deleting it. */
    @Test
    fun reinstallMovesTheOldJarAside() {
        pluginsDir.mkdirs()
        File(pluginsDir, "old-clock.jar").writeBytes(pluginJar(CLOCK_ID, "Clock 1"))
        PluginArchiveInstaller.install(
            zip("clock.zip", "clock.jar" to pluginJar(CLOCK_ID, "Clock 2")),
            pluginsDir,
            BUILT_IN,
        )
        assertFalse(File(pluginsDir, "old-clock.jar").exists())
        assertTrue(File(pluginsDir, "$CLOCK_ID.jar").isFile)
        val kept = File(pluginsDir, PluginArchiveInstaller.REPLACED_DIR_NAME).listFiles().orEmpty()
        assertTrue(kept.single().name.endsWith("old-clock.jar"))
    }

    @Test
    fun otherFilesAreRefused() {
        val text = File(root, "notes.txt").apply { writeText("hello") }
        assertFalse(PluginArchiveInstaller.install(text, pluginsDir, BUILT_IN).succeeded)
        val empty = zip("empty.zip", "readme.txt" to "nothing".toByteArray())
        assertTrue(
            PluginArchiveInstaller
                .install(empty, pluginsDir, BUILT_IN)
                .problems
                .single()
                .contains("no plugin"),
        )
    }

    private fun pluginJar(
        id: String,
        name: String,
    ): ByteArray = jarOf("plugin.json" to """{"id":"$id","name":"$name"}""")

    private fun jarOf(vararg entries: Pair<String, String>): ByteArray =
        zipBytes(*entries.map { (path, text) -> path to text.toByteArray() }.toTypedArray())

    private fun zip(
        name: String,
        vararg entries: Pair<String, ByteArray>,
    ): File = File(root, name).apply { writeBytes(zipBytes(*entries)) }

    private fun zipBytes(vararg entries: Pair<String, ByteArray>): ByteArray {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { out ->
            entries.forEach { (path, content) ->
                out.putNextEntry(ZipEntry(path))
                out.write(content)
                out.closeEntry()
            }
        }
        return bytes.toByteArray()
    }

    private companion object {
        const val CLOCK_ID = "com.example.clock"
        const val BUILT_IN_ID = "builtin.memory"
        val BUILT_IN = setOf(BUILT_IN_ID)
    }
}
