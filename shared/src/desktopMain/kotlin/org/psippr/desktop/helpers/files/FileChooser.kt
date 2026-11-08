package org.psippr.desktop.helpers.files

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/** The system file dialog for picking files to open. Blocks until it closes. */
object FileChooser {
    /**
     * Files the user picked, empty when cancelled. [extensions] limits the offer (all files when
     * empty); [multiple] allows picking several.
     */
    fun open(
        title: String,
        extensions: Set<String> = emptySet(),
        multiple: Boolean = false,
    ): List<File> {
        val dialog =
            FileDialog(null as Frame?, title, FileDialog.LOAD).apply {
                isMultipleMode = multiple
                if (extensions.isNotEmpty()) {
                    setFilenameFilter { _, name -> File(name).extension.lowercase() in extensions }
                }
            }
        dialog.isVisible = true
        return dialog.files.orEmpty().toList()
    }
}
