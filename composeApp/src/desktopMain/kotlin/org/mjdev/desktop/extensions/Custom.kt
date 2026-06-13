package org.mjdev.desktop.extensions

import androidx.compose.ui.graphics.NativePaint
import okio.Path
import org.jetbrains.skia.FilterBlurMode.NORMAL
import org.jetbrains.skia.MaskFilter.Companion.makeBlur
import org.mjdev.desktop.data.DesktopFile
import org.mjdev.desktop.extensions.Locale.toLocale
import org.mjdev.desktop.extensions.PathExt.exists
import org.mjdev.desktop.extensions.PathExt.listFiles
import org.mjdev.desktop.extensions.PathExt.text
import java.io.BufferedReader
import java.lang.System.lineSeparator
import java.util.Locale
import java.util.StringJoiner
import kotlin.jvm.optionals.getOrNull

@Suppress("MemberVisibilityCanBePrivate", "unused")
object Custom {
    val ProcessHandle.command: String
        get() = info().command().getOrNull().orEmpty()

    val ProcessHandle.commandLine: String
        get() = info().commandLine().getOrNull().orEmpty()

    val Process.consoleOutput: String
        get() {
            val sj = StringJoiner(lineSeparator())
            val bfr = BufferedReader(inputReader())
            bfr.lines().iterator().forEachRemaining { s: String? -> sj.add(s) }
            return sj.toString()
        }

    val Char.isPrintable: Boolean
        get() {
            val block = Character.UnicodeBlock.of(this)
            return (!Character.isISOControl(this)) &&
                this != java.awt.event.KeyEvent.CHAR_UNDEFINED &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS
        }

    fun Path.listDesktopFiles(ext: String = DesktopFile.EXTENSION): List<DesktopFile> =
        if (this.exists) {
            listFiles(ext) { f ->
                DesktopFile(f)
            }
        } else {
            emptyList()
        }

    fun Path.readTextAsLocale(): Locale = if (this.exists) text.toLocale() else Locale.ENGLISH

    val Path.textAsLocale: Locale
        get() = if (this.exists) readTextAsLocale() else Locale.ENGLISH

    val Path.desktopFiles: List<DesktopFile>
        get() = if (this.exists) listDesktopFiles() else emptyList()

    fun NativePaint.setMaskFilter(blurRadius: Float) {
        maskFilter = makeBlur(NORMAL, blurRadius / 2, true)
    }

    val Process.command: String?
        get() = info().command().getOrNull()
}
