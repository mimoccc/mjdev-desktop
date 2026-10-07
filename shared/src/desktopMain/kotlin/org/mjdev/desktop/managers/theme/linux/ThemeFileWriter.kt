package org.mjdev.desktop.managers.theme.linux

import okio.Path
import org.mjdev.desktop.extensions.PathExt.absolutePath
import org.mjdev.desktop.extensions.PathExt.exists
import org.mjdev.desktop.extensions.PathExt.mkdirs
import org.mjdev.desktop.extensions.PathExt.parentFile
import org.mjdev.desktop.extensions.PathExt.text
import org.mjdev.desktop.extensions.PathExt.writeText
import org.mjdev.desktop.log.Log

/** Writes generated theme files, skipping identical content so unchanged themes are not reloaded. */
object ThemeFileWriter {
    /** Writes [content] to [file]; true when the file content changed. */
    fun writeIfChanged(
        file: Path,
        content: String,
    ): Boolean {
        if (file.exists && runCatching { file.text }.getOrNull() == content) return false
        if (!file.parentFile.exists) {
            file.parentFile.mkdirs()
        }
        Log.i("Writing theme file: file://${file.absolutePath}")
        file.writeText(content)
        return true
    }
}
