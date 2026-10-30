package org.psippr.desktop.managers.plugins

import org.psippr.desktop.plugins.PluginDefaults
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipFile

/**
 * Installs plugin jars from a zip archive, or a single plugin jar, into the plugins folder.
 *
 * Nothing of a plugin runs here: a jar is only accepted when its `plugin.json` can be read (as
 * the plugin list does, without loading classes) and it is installed switched off. Only `.jar`
 * entries are taken, by their base name only, so an archive can never write outside the folder;
 * each entry is size limited. A plugin already installed under the same id is moved to
 * [REPLACED_DIR_NAME], never deleted.
 */
object PluginArchiveInstaller {
    /** Largest plugin jar taken from an archive, in bytes (guards against zip bombs). */
    const val MAX_JAR_BYTES: Long = 50L * 1024 * 1024

    /** Sub folder of the plugins folder keeping jars replaced by a newer install. */
    const val REPLACED_DIR_NAME = "replaced"

    /** Suffix of jar file names. */
    private const val JAR_SUFFIX = ".${PluginDefaults.JAR_EXTENSION}"

    /** Characters allowed in an installed jar's file name; others become [NAME_FILLER]. */
    private val SAFE_NAME = Regex("[^A-Za-z0-9._-]")
    private const val NAME_FILLER = "_"

    /** Separators of directories inside a zip entry name. */
    private val ENTRY_SEPARATORS = charArrayOf('/', '\\')

    /** Prefix of the temporary copy of a jar while it is checked. */
    private const val TEMP_PREFIX = "psippr-plugin-"

    /**
     * Installs the plugins of [archive] into [pluginsDir]. [reservedIds] (the built-in plugins)
     * cannot be replaced.
     */
    fun install(
        archive: File,
        pluginsDir: File,
        reservedIds: Set<String>,
    ): PluginInstallResult {
        if (!archive.isFile) return PluginInstallResult(problems = listOf("${archive.name}: not found."))
        // a single plugin jar is installed as it is
        if (PluginJarReader.readMetadata(archive) != null) {
            return installJar(archive, archive.name, pluginsDir, reservedIds)
        }
        val results =
            runCatching {
                ZipFile(archive).use { zip ->
                    zip
                        .entries()
                        .asSequence()
                        .filter { entry -> !entry.isDirectory && entry.name.lowercase().endsWith(JAR_SUFFIX) }
                        .map { entry -> installEntry(zip.getInputStream(entry), baseName(entry.name), pluginsDir, reservedIds) }
                        .toList()
                }
            }.getOrElse { return PluginInstallResult(problems = listOf("${archive.name}: not a zip archive.")) }
        if (results.isEmpty()) return PluginInstallResult(problems = listOf("${archive.name}: contains no plugin .jar."))
        return PluginInstallResult(
            installed = results.flatMap { it.installed },
            problems = results.flatMap { it.problems },
            installedIds = results.flatMap { it.installedIds },
        )
    }

    // copies one archive entry (size limited) to a temp file, checks and installs it
    private fun installEntry(
        input: InputStream,
        name: String,
        pluginsDir: File,
        reservedIds: Set<String>,
    ): PluginInstallResult {
        val temp = Files.createTempFile(TEMP_PREFIX, JAR_SUFFIX).toFile()
        return try {
            if (!copyLimited(input, temp)) {
                PluginInstallResult(problems = listOf("$name: larger than ${MAX_JAR_BYTES / (1024 * 1024)} MB."))
            } else {
                installJar(temp, name, pluginsDir, reservedIds)
            }
        } finally {
            temp.delete()
        }
    }

    private fun installJar(
        jar: File,
        name: String,
        pluginsDir: File,
        reservedIds: Set<String>,
    ): PluginInstallResult {
        val metadata =
            PluginJarReader.readMetadata(jar)
                ?: return PluginInstallResult(problems = listOf("$name: no valid ${PluginDefaults.METADATA_ENTRY}."))
        if (metadata.id in reservedIds) {
            return PluginInstallResult(problems = listOf("$name: ${metadata.id} is a built-in plugin."))
        }
        pluginsDir.mkdirs()
        replaceInstalled(metadata.id, pluginsDir)
        val target = File(pluginsDir, SAFE_NAME.replace(metadata.id, NAME_FILLER) + JAR_SUFFIX)
        Files.copy(jar.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        return PluginInstallResult(installed = listOf(metadata.name.ifBlank { metadata.id }), installedIds = listOf(metadata.id))
    }

    // jars already installed with the same id go to the replaced folder (kept, not deleted)
    private fun replaceInstalled(
        id: String,
        pluginsDir: File,
    ) {
        pluginsDir
            .listFiles { file -> file.isFile && file.name.lowercase().endsWith(JAR_SUFFIX) }
            ?.filter { jar -> PluginJarReader.readMetadata(jar)?.id == id }
            ?.forEach { jar ->
                val replacedDir = File(pluginsDir, REPLACED_DIR_NAME).apply { mkdirs() }
                val kept = File(replacedDir, "${System.currentTimeMillis()}-${jar.name}")
                Files.move(jar.toPath(), kept.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
    }

    // false (and a partial file) when the entry is larger than MAX_JAR_BYTES
    private fun copyLimited(
        input: InputStream,
        target: File,
    ): Boolean = input.use { source ->
        target.outputStream().use { out ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0L
            var read = source.read(buffer)
            while (read >= 0 && total + read <= MAX_JAR_BYTES) {
                out.write(buffer, 0, read)
                total += read
                read = source.read(buffer)
            }
            read < 0
        }
    }

    private fun baseName(entryName: String): String = entryName.substringAfterLast(ENTRY_SEPARATORS.first()).let { name ->
        name.substringAfterLast(ENTRY_SEPARATORS.last())
    }
}
