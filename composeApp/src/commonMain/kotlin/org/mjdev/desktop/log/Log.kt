/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.log

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import okio.Path.Companion.toPath
import org.mjdev.desktop.extensions.System.currentTimeMillis

object Log {
    private var isInitialized = false
    private val creationDate = currentTimeMillis.toString()
    // todo
    private val logFile = "/var/tmp/mjdev-desktop/log/$creationDate.log".toPath()
//    private val logWriter = PrintWriter(logFile.apply {
//        runCatching {
//            parentFile.mkdirs()
//            createNewFile()
//        }
//    })

    fun init() {
        if (!isInitialized) {
            isInitialized = true
            Napier.base(DebugAntilog())
        }
    }

    fun i(message: String) {
        init()
        Napier.i(message)
//        println(message)
//        runCatching {
//            logWriter.println(message)
//        }
    }

    fun i(e: Throwable) {
        init()
        Napier.i(e.message ?: "", e)
//        e.stackTraceToString().also { message ->
//            println(message)
//            runCatching {
//                logWriter.println(message)
//            }
//        }
    }

    fun d(message: String) {
        init()
        Napier.d(message)
//        println(message)
//        runCatching {
//            logWriter.println(message)
//        }
    }

    fun d(e: Throwable) {
        init()
        Napier.d(e.message ?: "", e)
//        e.stackTraceToString().also { message ->
//            println(message)
//            runCatching {
//                logWriter.println(message)
//            }
//        }
    }

    fun w(message: String) {
        init()
        Napier.w(message)
//        println(message)
//        runCatching {
//            logWriter.println(message)
//        }
    }

    fun w(e: Throwable) {
        init()
        Napier.w(e.message ?: "", e)
//        e.stackTraceToString().also { message ->
//            println(message)
//            runCatching {
//                logWriter.println(message)
//            }
//        }
    }

    fun e(message: String) {
        init()
        Napier.e(message)
//        RuntimeException(message).also { e -> e(e) }
    }

    fun e(e: Throwable) {
        init()
        Napier.e(e.message ?: "", e)
//        e.stackTraceToString().also { message ->
//            println(message)
//            runCatching {
//                logWriter.println(message)
//            }
//        }
    }

    fun e(message: String, e: Throwable) {
        init()
        Napier.e(message, e)
//        e(message)
//        e(e)
    }

}
