package org.mjdev.desktop.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import org.mjdev.desktop.extensions.System.currentTime
import org.mjdev.desktop.log.Log
import kotlin.coroutines.suspendCoroutine
import kotlin.time.ExperimentalTime
import org.mjdev.desktop.helpers.generic.JsonHelper.toJson
import org.mjdev.desktop.helpers.generic.JsonHelper.fromJson

@OptIn(ExperimentalTime::class)
@Suppress("unused")
object CustomExt {

//    fun runSafe(
//        shouldLog: Boolean = true,
//        block: () -> Unit
//    ) = runCatching {
//        block()
//    }.onFailure { err ->
//        if (shouldLog) Log.e(err)
//    }

    // todo

    private fun Instant.formatDate(): String {
        val localDateTime = toLocalDateTime(currentSystemDefault())
        val day = localDateTime.date.dayOfMonth.toString().padStart(2, '0')
        val month = localDateTime.date.monthNumber.toString().padStart(2, '0')
        val year = localDateTime.date.year.toString()
        return "$day.$month.$year"
    }

    // todo
    private fun Instant.formatTime(): String {
        val localDateTime = toLocalDateTime(currentSystemDefault())
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
                    currentTime.formatDate().also { t ->
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
                    currentTime.formatTime().also { t ->
                        if (time != t) {
                            time = t
                            send(time)
                        }
                    }
                    delay(200L)
                } while (true)
            }
        }.collectAsState(initial = "")

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

    suspend fun <T : Any> getAsync(
        block: () -> T
    ): T = suspendCoroutine { continuation ->
        runCatching {
            block()
        }.onSuccess { result ->
            continuation.resumeWith(Result.success(result))
        }.onFailure { e ->
            continuation.resumeWith(Result.failure(e))
        }
    }

    inline fun <reified T> String.jsonToList(): List<T> = runCatching {
        fromJson<List<T>>(this)
    }.onFailure { err ->
        Log.e(err)
    }.getOrNull() ?: emptyList()

    inline fun <reified T> String.to(): T? = runCatching {
        fromJson<T>(this)
    }.onFailure { err ->
        Log.e(err)
    }.getOrNull()

    inline fun <reified T> T.asJson(): String = runCatching {
        toJson()
    }.onFailure { err ->
        Log.e(err)
    }.getOrDefault("")

//    @ExperimentalSerializationApi
//    class DynamicLookupSerializer : KSerializer<Any> {
//        override val descriptor: SerialDescriptor = ContextualSerializer(
//            Any::class,
//            null,
//            emptyArray()
//        ).descriptor
//
//        @Suppress("UNCHECKED_CAST")
//        @OptIn(InternalSerializationApi::class)
//        override fun serialize(
//            encoder: Encoder,
//            value: Any
//        ) {
//            if (value is List<*>) {
//                encoder.encodeSerializableValue(
//                    ListSerializer(
//                        DynamicLookupSerializer()
//                    ),
//                    value as List<Any>
//                )
//                return
//            }
//            encoder.encodeSerializableValue(
//                encoder.serializersModule.getContextual(
//                    value::class
//                ) ?: value::class.serializer(),
//                value
//            )
//        }
//
//        override fun deserialize(decoder: Decoder): Any {
//            error("Unsupported")
//        }
//    }

}