package com.mjdev.audio

import java.lang.ProcessBuilder

/**
 * Linux implementace VolumeController pomocí pactl (PulseAudio) nebo wpctl (PipeWire).
 * Automaticky detekuje dostupný audio server.
 */
actual class VolumeController private constructor() {

    private var volumeListener: ((Float) -> Unit)? = null
    private var muteListener: ((Boolean) -> Unit)? = null
    private val isPipeWire: Boolean
    private val sinkName: String

    init {
        // Detekce PipeWire vs PulseAudio
        isPipeWire = checkPipeWire()
        sinkName = getDefaultSink()
        startListener()
    }

    private fun checkPipeWire(): Boolean {
        return try {
            ProcessBuilder("wpctl", "status").start().waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun getDefaultSink(): String {
        return if (isPipeWire) {
            // PipeWire - získá výchozí sink
            try {
                ProcessBuilder("wpctl", "status", "-n")
                    .redirectErrorStream(true)
                    .start()
                    .inputStream.bufferedReader()
                    .readText()
                    .lines()
                    .firstOrNull { it.contains("*") && it.contains("Sink") }
                    ?.split("\\s+".toRegex())
                    ?.getOrNull(1)
                    ?.let { "@DEFAULT_AUDIO_SINK@" } // PipeWire používá speciální název
                    ?: "@DEFAULT_AUDIO_SINK@"
            } catch (e: Exception) {
                "@DEFAULT_AUDIO_SINK@"
            }
        } else {
            // PulseAudio - získá výchozí sink
            try {
                ProcessBuilder("pactl", "get-default-sink")
                    .redirectErrorStream(true)
                    .start()
                    .inputStream.bufferedReader()
                    .readText()
                    .trim()
                    .takeIf { it.isNotEmpty() }
                    ?: "@DEFAULT_SINK@"
            } catch (e: Exception) {
                "@DEFAULT_SINK@"
            }
        }
    }

    private fun startListener() {
        // Spustí listener pro změny hlasitosti v pozadí
        Thread({
            if (isPipeWire) {
                listenPipeWire()
            } else {
                listenPulseAudio()
            }
        }).apply {
            isDaemon = true
            start()
        }
    }

    private fun listenPulseAudio() {
        ProcessBuilder("pactl", "subscribe")
            .redirectErrorStream(true)
            .start()
            .inputStream.bufferedReader()
            .use { reader ->
                reader.forEachLine { line ->
                    if (line.contains("sink") && line.contains("change")) {
                        notifyVolumeChange()
                        notifyMuteChange()
                    }
                }
            }
    }

    private fun listenPipeWire() {
        // PipeWire events přes pw-cli nebo wpctl
        ProcessBuilder("pw-cli", "subscribe", "params", "all")
            .redirectErrorStream(true)
            .start()
            .inputStream.bufferedReader()
            .use { reader ->
                reader.forEachLine { line ->
                    if (line.contains("volume") || line.contains("mute")) {
                        notifyVolumeChange()
                        notifyMuteChange()
                    }
                }
            }
    }

    private fun notifyVolumeChange() {
        val vol = getVolume()
        volumeListener?.invoke(vol)
    }

    private fun notifyMuteChange() {
        val muted = getIsMuted()
        muteListener?.invoke(muted)
    }

    actual protected override fun getVolume(): Float {
        return try {
            if (isPipeWire) {
                // wpctl get-volume @DEFAULT_AUDIO_SINK@
                val output = ProcessBuilder("wpctl", "get-volume", sinkName)
                    .redirectErrorStream(true)
                    .start()
                    .inputStream.bufferedReader()
                    .readText()
                    .trim()
                // Formát: "Volume: 0.75" nebo "Volume: 0.75 [MUTED]"
                val volumeStr = output
                    .removePrefix("Volume: ")
                    .split(" ")
                    .first()
                volumeStr.toFloat()
            } else {
                // pactl get-sink-volume @DEFAULT_SINK@
                val output = ProcessBuilder("pactl", "get-sink-volume", sinkName)
                    .redirectErrorStream(true)
                    .start()
                    .inputStream.bufferedReader()
                    .readText()
                    .trim()
                // Formát: "Volume: front-left: 65536 / 100% / 0.00 dB, front-right: 65536 / 100% / 0.00 dB"
                val percentStr = output
                    .split("/")
                    .getOrNull(1)
                    ?.trim()
                    ?.removeSuffix("%")
                    ?: "100"
                (percentStr.toFloat() / 100f).coerceIn(0f, 1f)
            }
        } catch (e: Exception) {
            1.0f // default
        }
    }

    actual protected override fun getIsMuted(): Boolean {
        return try {
            if (isPipeWire) {
                val output = ProcessBuilder("wpctl", "get-volume", sinkName)
                    .redirectErrorStream(true)
                    .start()
                    .inputStream.bufferedReader()
                    .readText()
                output.contains("[MUTED]")
            } else {
                val output = ProcessBuilder("pactl", "get-sink-mute", sinkName)
                    .redirectErrorStream(true)
                    .start()
                    .inputStream.bufferedReader()
                    .readText()
                    .trim()
                output.contains("yes")
            }
        } catch (e: Exception) {
            false
        }
    }

    actual protected override fun getOnVolumeChanged(): ((Float) -> Unit)? = volumeListener
    actual protected override fun setOnVolumeChanged(listener: ((Float) -> Unit)?) {
        volumeListener = listener
    }
    actual protected override fun getOnMuteChanged(): ((Boolean) -> Unit)? = muteListener
    actual protected override fun setOnMuteChanged(listener: ((Boolean) -> Unit)?) {
        muteListener = listener
    }

    actual override fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        try {
            if (isPipeWire) {
                // wpctl set-volume @DEFAULT_AUDIO_SINK@ 0.75
                ProcessBuilder("wpctl", "set-volume", sinkName, clamped.toString())
                    .start()
                    .waitFor()
            } else {
                // pactl set-sink-volume @DEFAULT_SINK@ 75%
                val percent = (clamped * 100).toInt()
                ProcessBuilder("pactl", "set-sink-volume", sinkName, "${percent}%")
                    .start()
                    .waitFor()
            }
            volumeListener?.invoke(clamped)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual override fun toggleMute() {
        try {
            if (isPipeWire) {
                ProcessBuilder("wpctl", "set-mute", sinkName, "toggle")
                    .start()
                    .waitFor()
            } else {
                ProcessBuilder("pactl", "set-sink-mute", sinkName, "toggle")
                    .start()
                    .waitFor()
            }
            muteListener?.invoke(getIsMuted())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual override fun setMuted(muted: Boolean) {
        try {
            if (isPipeWire) {
                ProcessBuilder("wpctl", "set-mute", sinkName, if (muted) "1" else "0")
                    .start()
                    .waitFor()
            } else {
                ProcessBuilder("pactl", "set-sink-mute", sinkName, if (muted) "1" else "0")
                    .start()
                    .waitFor()
            }
            muteListener?.invoke(muted)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual companion object {
        private var INSTANCE: VolumeController? = null

        actual fun getInstance(): VolumeController {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VolumeController().also { INSTANCE = it }
            }
        }
    }
}
