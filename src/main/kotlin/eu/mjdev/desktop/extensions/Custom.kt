package eu.mjdev.desktop.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.NativePaint
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import com.google.gson.Gson
import eu.mjdev.desktop.helpers.streams.ResourceStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.text.DateFormat
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Suppress("FunctionName", "MemberVisibilityCanBePrivate", "unused")
object Custom {
    val Process.handle: ProcessHandle
        get() = toHandle()

    val ProcessHandle.command: String
        get() = info().command().getOrNull().orEmpty()

    val ProcessHandle.commandLine: String
        get() = info().commandLine().getOrNull().orEmpty()

    val Process.consoleOutput: String
        get() {
            val sj = StringJoiner(System.lineSeparator())
            val bfr = BufferedReader(inputReader())
            bfr.lines().iterator().forEachRemaining { s: String? -> sj.add(s) }
            return sj.toString()
        }

    fun BufferedReader.readAvailable(): String = StringBuilder().apply {
        var ch = read()
        while (ch != -1) {
            append(Char(ch))
            ch = read()
        }
    }.toString()

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

    // todo better solution
    fun <T> SnapshotStateList<T>.invalidate() = apply {
        val old = toList()
        clear()
        addAll(old)
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