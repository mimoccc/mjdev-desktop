package eu.mjdev.desktop.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.NativePaint
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import com.google.gson.Gson
import eu.mjdev.desktop.data.DesktopFile
import eu.mjdev.desktop.extensions.Locale.toLocale
import eu.mjdev.desktop.helpers.streams.ResourceStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.text.DateFormat
import java.util.*
import java.util.Locale
import kotlin.jvm.optionals.getOrNull

@Suppress("MemberVisibilityCanBePrivate", "FunctionName")
object Custom {
    val ProcessHandle.command: String
        get() = info().command().getOrNull().orEmpty()

//    val ProcessHandle.commandLine: String
//        get() = info().commandLine().getOrNull().orEmpty()

    val Process.consoleOutput: String
        get() {
            val sj = StringJoiner(System.lineSeparator())
            val bfr = BufferedReader(inputReader())
            bfr.lines().iterator().forEachRemaining { s: String? -> sj.add(s) }
            return sj.toString()
        }

    inline fun <reified T> Any?.orElse(
        block: () -> T
    ) = if (this == null) block() else this as T

    val Char.isPrintable: Boolean
        get() {
            val block = Character.UnicodeBlock.of(this)
            return (!Character.isISOControl(this)) &&
                    this != java.awt.event.KeyEvent.CHAR_UNDEFINED &&
                    block != null &&
                    block != Character.UnicodeBlock.SPECIALS
        }

    fun File.listFiles(
        ext: String? = null
    ): List<File> = if (exists()) listFiles().let { list ->
        when {
            list == null -> emptyList()
            ext != null -> list.filter { f -> f.extension == ext }
            else -> list.toList()
        }
    } else emptyList()

    fun File.listFilesFiltered(
        ext: String? = null,
        predicate: (File) -> Boolean
    ) = listFiles(ext).filter(predicate)

    fun File.listFilesOnly(
        ext: String? = null,
    ) = listFilesFiltered(ext) { f -> !f.isDirectory }

    fun List<File>.sortedByName() =
        sortedBy { f -> f.name }

//    fun List<File>.sortedByNameDescending() =
//        sortedByDescending { f -> f.name }

    inline fun <R> File.listFiles(
        ext: String? = null,
        mapper: (File) -> R
    ): List<R> = listFiles(ext).map(mapper)

//    inline fun <R : Comparable<R>> File.listFilesSortedBy(
//        ext: String? = null,
//        crossinline selector: (File) -> R?
//    ): List<File> = listFiles(ext).sortedBy(selector)

    fun File.listDesktopFiles(
        ext: String = DesktopFile.EXTENSION
    ): List<DesktopFile> = listFiles(ext) { f -> DesktopFile(f) }

//    inline fun <reified T : Any> MutableList<T>.addIfNotExists(
//        element: T,
//        equal: (e1: T, e2: T) -> Boolean = { e1, e2 -> e1 == e2 }
//    ) = any { e -> equal(e, element) }.also { contains -> if (!contains) add(element) }

    operator fun File.get(name: String) =
        resolve(name)

    fun File.readTextAsLocale(): Locale =
        if (exists()) text.toLocale() else Locale.ENGLISH

    val File.all
        get() = listFiles()?.toList() ?: emptyList<File>()

    val File.filesOnly
        get() = listFiles()?.filter { !it.isDirectory }?.toList() ?: emptyList<File>()

    val File.dirsOnly
        get() = listFiles()?.filter { it.isDirectory }?.toList() ?: emptyList<File>()

    val File.lines
        get() = runCatching { if (exists()) readLines() else null }.getOrNull() ?: emptyList()

    val File.text
        get() = runCatching { if (exists()) readText() else null }.getOrNull() ?: ""

    val File.textAsLocale: Locale
        get() = if (exists()) readTextAsLocale() else Locale.ENGLISH

    val File.desktopFiles
        get() = listDesktopFiles()

//    inline fun <T, K> distinctList(
//        vararg lists: List<T>,
//        selector: (T) -> K
//    ): List<T> = mutableListOf<T>().apply {
//        lists.forEach { l -> addAll(l) }
//    }.distinctBy(selector)

    fun String.notStartsWith(
        prefix: String,
        ignoreCase: Boolean = false
    ) = !startsWith(prefix, ignoreCase)

    fun String.trimIsEmpty() =
        trim().isEmpty()

//    fun String.trimIsNotEmpty() =
//        trim().isNotEmpty()

//    fun State<String>.trimIsEmpty() =
//        value.trim().isEmpty()

    fun State<String>.trimIsNotEmpty() =
        value.trim().isNotEmpty()

    fun String.trimStartsWith(
        prefix: String,
        ignoreCase: Boolean = false
    ) = trim().startsWith(prefix, ignoreCase)

//    fun String.trimNotStartsWith(
//        prefix: String,
//        ignoreCase: Boolean = false
//    ) = !trim().startsWith(prefix, ignoreCase)

    fun ParsedList(
        value: String?,
        delimiter: String = ";"
    ): MutableList<String> = value?.split(delimiter)?.toMutableList() ?: mutableListOf()

    fun ParsedBoolean(
        value: String?,
        defaultValue: Boolean = false
    ) = value.orEmpty().trim().lowercase().let { b ->
        when (b) {
            "true" -> true
            "1" -> true
            else -> defaultValue
        }
    }

    fun ParsedString(
        value: String?,
        defaultValue: String = ""
    ) = value ?: defaultValue

    fun <T> flowBlock(
        block: suspend () -> T
    ): Flow<T> = flow {
        emit(block())
    }

    fun NativePaint.setMaskFilter(
        blurRadius: Float
    ) {
        this.maskFilter =
            org.jetbrains.skia.MaskFilter.makeBlur(org.jetbrains.skia.FilterBlurMode.NORMAL, blurRadius / 2, true)
    }

    val dateFlow
        @Composable
        get() = channelFlow {
            launch {
                var date = "1.1.1970"
                do {
                    DateFormat.getDateInstance().format(Date()).also { t ->
                        if (date != t) {
                            date = t
                            send(date)
                        }
                    }
                    delay(5000L)
                } while (true)
            }
        }.collectAsState(initial = "")

    val timeFlow
        @Composable
        get() = channelFlow {
            launch {
                var time = "00:00:00"
                do {
                    DateFormat.getTimeInstance().format(Date()).also { t ->
                        if (time != t) {
                            time = t
                            send(time)
                        }
                    }
                    delay(200L)
                } while (true)
            }
        }.collectAsState(initial = "")

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

    operator fun DpSize.plus(dp: Dp) =
        copy(width = width + dp, height = height + dp)

    operator fun DpSize.minus(dp: Dp) =
        copy(width = width - dp, height = height - dp)

//    operator fun PaddingValues.plus(dp: Dp) =
//        copy(left = width + dp, height = height + dp)

//    operator fun PaddingValues.minus(dp: Dp) =
//        copy(width = width - dp, height = height - dp)

    fun MutableState<Boolean>.toggle() {
        value = !value
    }

}