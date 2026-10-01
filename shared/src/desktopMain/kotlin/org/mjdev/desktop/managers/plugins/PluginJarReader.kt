package org.mjdev.desktop.managers.plugins

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import org.mjdev.desktop.helpers.generic.JsonHelper
import org.mjdev.desktop.log.Log
import org.mjdev.desktop.plugins.PluginDefaults
import org.mjdev.desktop.plugins.PluginMetadata
import java.io.File
import java.util.zip.ZipFile

/**
 * Reads plugin descriptions straight out of a jar archive. Only plain data entries are read;
 * no class from the jar is loaded, so listing plugins never runs third-party code.
 */
object PluginJarReader {
    /** Returns the metadata of [jar], or null when `plugin.json` is missing or has no id. */
    fun readMetadata(jar: File): PluginMetadata? = runCatching {
        readEntry(jar, PluginDefaults.METADATA_ENTRY)
            ?.toString(Charsets.UTF_8)
            ?.let { JsonHelper.fromJson<PluginMetadata>(it) }
            ?.takeIf { it.id.isNotBlank() }
    }.onFailure { e ->
        Log.e(e)
    }.getOrNull()

    /** Returns the plugin image of [jar] named by [metadata], or null when there is none. */
    fun readIcon(
        jar: File,
        metadata: PluginMetadata,
    ): ImageBitmap? = runCatching {
        readEntry(jar, metadata.icon.ifBlank { PluginDefaults.DEFAULT_ICON_ENTRY })
            ?.let { Image.makeFromEncoded(it).toComposeImageBitmap() }
    }.onFailure { e ->
        Log.e(e)
    }.getOrNull()

    private fun readEntry(
        jar: File,
        name: String,
    ): ByteArray? = ZipFile(jar).use { zip ->
        zip
            .getEntry(name)
            ?.takeIf { it.size in 0..PluginDefaults.MAX_ENTRY_BYTES.toLong() }
            ?.let { entry -> zip.getInputStream(entry).use { it.readBytes() } }
    }
}
