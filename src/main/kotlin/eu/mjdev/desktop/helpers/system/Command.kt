package eu.mjdev.desktop.helpers.system

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

@Suppress("MemberVisibilityCanBePrivate", "unused")
class Command(
    val cmd: String,
    vararg val args: String
) {
    private val _cmd
        get() = mutableListOf<String>().apply {
            add(cmd)
            addAll(args)
        }

    var error: Throwable? = null
    var result: String = ""

    fun execute(): String? = runCatching {
        StringJoiner(System.lineSeparator()).let { sj ->
            val p = ProcessBuilder(_cmd).start()
            BufferedReader(InputStreamReader(p.inputStream))
                .lines()
                .iterator()
                .forEachRemaining { newElement: String? ->
                    sj.add(newElement)
                }
            result = sj.toString()
            p.waitFor()
            p.destroy()
            result
        }
    }.onFailure { e ->
        error = e
    }.getOrNull()

    companion object {
        inline fun <reified T> String.to(): T? = runCatching {
            Gson().fromJson(this, T::class.java)
        }.getOrNull()

        inline fun <reified T> String.toList(): List<T>? = runCatching {
            Gson().fromJson(this, List::class.java).mapNotNull { item -> item as? T }
        }.getOrNull()
    }
}