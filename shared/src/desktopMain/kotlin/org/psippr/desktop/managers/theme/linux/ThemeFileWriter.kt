package org.psippr.desktop.managers.theme.linux

import okio.Path
import org.psippr.desktop.extensions.PathExt.absolutePath
import org.psippr.desktop.extensions.PathExt.exists
import org.psippr.desktop.extensions.PathExt.mkdirs
import org.psippr.desktop.extensions.PathExt.parentFile
import org.psippr.desktop.extensions.PathExt.text
import org.psippr.desktop.extensions.PathExt.writeText
import org.psippr.desktop.log.Log

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
