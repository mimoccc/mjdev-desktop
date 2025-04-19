package org.mjdev.desktop.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.NativePaint
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.mjdev.desktop.helpers.fuzzywuzzy.FuzzySearch
import okio.Path
import org.jetbrains.skia.FilterBlurMode.NORMAL
import org.jetbrains.skia.MaskFilter.Companion.makeBlur
import org.mjdev.desktop.data.DesktopFile
import org.mjdev.desktop.extensions.Locale.toLocale
import org.mjdev.desktop.extensions.PathExt.exists
import org.mjdev.desktop.extensions.PathExt.listFiles
import org.mjdev.desktop.extensions.PathExt.text
import org.mjdev.desktop.helpers.streams.ResourceStream
import java.io.BufferedReader
import java.lang.System.lineSeparator
import java.text.DateFormat
import java.util.Date
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

    fun Path.listDesktopFiles(
        ext: String = DesktopFile.EXTENSION
    ): List<DesktopFile> = if (this.exists) listFiles(ext) { f ->
        DesktopFile(f)
    } else emptyList()

    fun Path.readTextAsLocale(): Locale =
        if (this.exists) text.toLocale() else Locale.ENGLISH

    val Path.textAsLocale: Locale
        get() = if (this.exists) readTextAsLocale() else Locale.ENGLISH

    val Path.desktopFiles: List<DesktopFile>
        get() = if (this.exists) listDesktopFiles() else emptyList()

//    inline fun <T, K> distinctList(
//        vararg lists: List<T>,
//        selector: (T) -> K
//    ): List<T> = mutableListOf<T>().apply {
//        lists.forEach { l -> addAll(l) }
//    }.distinctBy(selector)

//    fun String.notStartsWith(
//        prefix: String,
//        ignoreCase: Boolean = false
//    ) = !startsWith(prefix, ignoreCase)

//    fun String.trimIsEmpty() =
//        trim().isEmpty()

//    fun String.trimIsNotEmpty() =
//        trim().isNotEmpty()

//    fun State<String>.trimIsEmpty() =
//        value.trim().isEmpty()

//    fun String.trimStartsWith(
//        prefix: String,
//        ignoreCase: Boolean = false
//    ) = trim().startsWith(prefix, ignoreCase)

//    fun String.trimNotStartsWith(
//        prefix: String,
//        ignoreCase: Boolean = false
//    ) = !trim().startsWith(prefix, ignoreCase)

    fun NativePaint.setMaskFilter(
        blurRadius: Float
    ) {
        maskFilter = makeBlur(NORMAL, blurRadius / 2, true)
    }

    fun loadKey(key: String): String = runCatching {
        ResourceStream("keys/$key.key").string
    }.getOrNull().orEmpty()

    inline fun <reified T> String.jsonToList(): List<T> = runCatching {
        Gson().fromJson(this, List::class.java).mapNotNull { item -> item as? T }
    }.getOrNull() ?: emptyList()

    inline fun <reified T> String.to(): T? = runCatching {
        Gson().fromJson(this, T::class.java)
    }.getOrNull()

    val Process.command: String?
        get() = info().command().getOrNull()

}
