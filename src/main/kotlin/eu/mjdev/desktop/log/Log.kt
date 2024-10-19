/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.log

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.io.PrintWriter

// todo colors
object Log {
    private val creationDate = System.currentTimeMillis().toString()
    private val logFile = File("/var/tmp/mjdev-desktop/log/$creationDate.log")
    private val logWriter = PrintWriter(logFile.apply {
        runCatching {
            parentFile.mkdirs()
            createNewFile()
        }
    })

    fun i(message: String) {
        println(message)
        runCatching {
            logWriter.println(message)
        }
    }

    fun i(e: Throwable) {
        e.completeText.also { message ->
            println(message)
            runCatching {
                logWriter.println(message)
            }
        }
    }

    fun w(message: String) {
        println(message)
        runCatching {
            logWriter.println(message)
        }
    }

    fun w(e: Throwable) {
        e.completeText.also { message ->
            println(message)
            runCatching {
                logWriter.println(message)
            }
        }
    }

    fun e(message: String) {
        RuntimeException(message).also { e -> e(e) }
    }

    fun e(e: Throwable) {
        e.completeText.also { message ->
            println(message)
            runCatching {
                logWriter.println(message)
            }
        }
    }

    private val Throwable.completeText: String
        get() {
            val out = ByteArrayOutputStream()
            val stream = PrintStream(out)
            printStackTrace(stream)
            return String(out.toByteArray())
        }
}
