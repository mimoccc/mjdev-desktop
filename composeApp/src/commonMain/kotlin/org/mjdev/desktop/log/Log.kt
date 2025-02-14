/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.log

import okio.Path.Companion.toPath
import org.mjdev.desktop.extensions.System.currentTimeMillis

// todo colors
object Log {
    private val creationDate = currentTimeMillis().toString()
    private val logFile = "/var/tmp/mjdev-desktop/log/$creationDate.log".toPath()
//    private val logWriter = PrintWriter(logFile.apply {
//        runCatching {
//            parentFile.mkdirs()
//            createNewFile()
//        }
//    })

    fun i(message: String) {
        println(message)
//        runCatching {
//            logWriter.println(message)
//        }
    }

    fun i(e: Throwable) {
        e.stackTraceToString().also { message ->
            println(message)
//            runCatching {
//                logWriter.println(message)
//            }
        }
    }

    fun d(message: String) {
        println(message)
//        runCatching {
//            logWriter.println(message)
//        }
    }

    fun d(e: Throwable) {
        e.stackTraceToString().also { message ->
            println(message)
//            runCatching {
//                logWriter.println(message)
//            }
        }
    }

    fun w(message: String) {
        println(message)
//        runCatching {
//            logWriter.println(message)
//        }
    }

    fun w(e: Throwable) {
        e.stackTraceToString().also { message ->
            println(message)
//            runCatching {
//                logWriter.println(message)
//            }
        }
    }

    fun e(message: String) {
        RuntimeException(message).also { e -> e(e) }
    }

    fun e(e: Throwable) {
        e.stackTraceToString().also { message ->
            println(message)
//            runCatching {
//                logWriter.println(message)
//            }
        }
    }


}
