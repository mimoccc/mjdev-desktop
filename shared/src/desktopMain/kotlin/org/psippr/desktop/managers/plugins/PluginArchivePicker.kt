package org.psippr.desktop.managers.plugins

import org.psippr.desktop.plugins.PluginDefaults
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/** System file dialog for picking a plugin archive (`.zip`) or a single plugin `.jar`. */
object PluginArchivePicker {
    /** Title of the dialog. */
    private const val TITLE = "Install plugin"

    /** Extensions the dialog offers. */
    private val EXTENSIONS = setOf(ZIP_EXTENSION, PluginDefaults.JAR_EXTENSION)

    /** Extension of a plugin archive. */
    private const val ZIP_EXTENSION = "zip"

    /** Path of the picked file, null when the dialog was cancelled. Blocks until it closes. */
    fun choose(): String? {
        val dialog =
            FileDialog(null as Frame?, TITLE, FileDialog.LOAD).apply {
                isMultipleMode = false
                setFilenameFilter { _, name -> File(name).extension.lowercase() in EXTENSIONS }
            }
        dialog.isVisible = true
        val directory = dialog.directory ?: return null
        val file = dialog.file ?: return null
        return File(directory, file).path
    }
}
