package org.mjdev.desktop.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import okio.Path
import org.mjdev.desktop.extensions.PathExt.exists
import org.mjdev.desktop.extensions.PathExt.text
import org.mjdev.desktop.helpers.streams.ResourceStream
import org.mjdev.desktop.interfaces.ILocale

object CustomExt {

    // todo
    fun Instant.formatDate(): String {
        val localDateTime = this.toLocalDateTime(currentSystemDefault())
        val day = localDateTime.date.dayOfMonth.toString().padStart(2, '0')
        val month = localDateTime.date.monthNumber.toString().padStart(2, '0')
        val year = localDateTime.date.year.toString()
        return "$day.$month.$year"
    }

    // todo
    fun Instant.formatTime(): String {
        val localDateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
        val hours = localDateTime.hour.toString().padStart(2, '0')
        val minutes = localDateTime.minute.toString().padStart(2, '0')
        val seconds = localDateTime.second.toString().padStart(2, '0')
        return "$hours:$minutes:$seconds"
    }

    val dateFlow
        @Composable
        get() = channelFlow {
            launch {
                var date = "1.1.1970"
                do {
                    // todo
                    Clock.System.now().formatDate().also { t ->
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
                    Clock.System.now().formatTime().also { t ->
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
        ResourceStream("keys/$key.key").text
    }.getOrNull().orEmpty()

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

}