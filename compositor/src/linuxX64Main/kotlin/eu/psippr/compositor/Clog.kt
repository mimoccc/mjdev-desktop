/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

@file:OptIn(ExperimentalForeignApi::class)

package eu.psippr.compositor

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

/**
 * Tiny tagged logger for the compositor. Verbose by default so a session/login can be
 * diagnosed from the journal; silence with PSIPPRC_VERBOSE=0. High-frequency pointer events
 * are only sampled (medium verbose) so they never drown the window/ipc lifecycle output.
 */
object Clog {
    private val verbose: Boolean =
        (getenv("PSIPPRC_VERBOSE")?.toKString() ?: "1") != "0"
    private var pointerCount = 0L

    /** always printed (lifecycle milestones, errors) */
    fun log(message: String) = println("psipprc: $message")

    /** printed only in verbose mode (per window / per ipc request detail) */
    fun v(message: String) {
        if (verbose) println("psipprc: $message")
    }

    /** medium-verbose: pointer is sampled, the rest is dropped to keep logs readable */
    fun pointer(
        x: Int,
        y: Int,
    ) {
        if (!verbose) return
        if (pointerCount++ % POINTER_SAMPLE == 0L) {
            println("psipprc: pointer ($x, $y) [#$pointerCount, 1/$POINTER_SAMPLE sampled]")
        }
    }

    private const val POINTER_SAMPLE = 60L
}
