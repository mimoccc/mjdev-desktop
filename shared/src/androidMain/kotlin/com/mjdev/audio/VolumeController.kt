package com.mjdev.audio

import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Android implementace VolumeController pomocí AudioManager.
 * Ovládá STREAM_MUSIC (media volume) jako výchozí.
 */
actual class VolumeController private constructor(
    private val context: Context,
    private val audioManager: AudioManager,
    private val streamType: Int = AudioManager.STREAM_MUSIC
) {

    private var volumeListener: ((Float) -> Unit)? = null
    private var muteListener: ((Boolean) -> Unit)? = null

    init {
        // Registrace listeneru pro změny hlasitosti
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.registerAudioPolicyCallback(object : AudioManager.AudioPolicyCallback() {
                override fun onAudioPolicyChanged(policy: AudioManager.AudioPolicy) {
                    notifyVolumeChange()
                    notifyMuteChange()
                }
            }, null)
        } else {
            // Starší API - polling nebo ContentObserver
            startLegacyListener()
        }
    }

    private fun startLegacyListener() {
        // Pro starší API používáme ContentObserver na Settings.System.VOLUME_SETTINGS
        // Zde zjednodušeně - v reálné aplikaci by se použil ContentObserver
        Thread({
            var lastVolume = volume
            var lastMuted = isMuted
            while (true) {
                Thread.sleep(500)
                if (volume != lastVolume) {
                    lastVolume = volume
                    notifyVolumeChange()
                }
                if (isMuted != lastMuted) {
                    lastMuted = isMuted
                    notifyMuteChange()
                }
            }
        }).apply {
            isDaemon = true
            start()
        }
    }

    private fun notifyVolumeChange() {
        volumeListener?.invoke(volume)
    }

    private fun notifyMuteChange() {
        muteListener?.invoke(isMuted)
    }

    actual protected override fun getVolume(): Float {
        val maxVolume = audioManager.getStreamMaxVolume(streamType)
        val currentVolume = audioManager.getStreamVolume(streamType)
        return if (maxVolume > 0) currentVolume.toFloat() / maxVolume else 0f
    }

    actual protected override fun getIsMuted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return audioManager.isStreamMute(streamType)
        }
        // Fallback pro starší API
        return audioManager.getStreamVolume(streamType) == 0
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
        val maxVolume = audioManager.getStreamMaxVolume(streamType)
        val targetVolume = (clamped * maxVolume).roundToInt().coerceIn(0, maxVolume)
        
        audioManager.setStreamVolume(
            streamType,
            targetVolume,
            AudioManager.FLAG_SHOW_UI or AudioManager.FLAG_PLAY_SOUND
        )
        volumeListener?.invoke(clamped)
    }

    actual override fun toggleMute() {
        val newMute = !isMuted
        setMuted(newMute)
    }

    actual override fun setMuted(muted: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.adjustStreamVolume(
                streamType,
                if (muted) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE,
                AudioManager.FLAG_SHOW_UI
            )
        } else {
            // Starší API - nastavíme hlasitost na 0 pro mute
            if (muted) {
                audioManager.setStreamVolume(streamType, 0, AudioManager.FLAG_SHOW_UI)
            } else {
                // Obnovíme předchozí hlasitost - v reálné aplikaci by se ukládala
                audioManager.setStreamVolume(
                    streamType,
                    audioManager.getStreamMaxVolume(streamType) / 2,
                    AudioManager.FLAG_SHOW_UI
                )
            }
        }
        muteListener?.invoke(muted)
    }

    actual companion object {
        private var INSTANCE: VolumeController? = null

        actual fun getInstance(): VolumeController {
            // Pro Android potřebujeme Context - předpokládáme, že je dostupný přes Application
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: createInstance().also { INSTANCE = it }
            }
        }

        private fun createInstance(): VolumeController {
            // Context musí být poskytnut zvenčí - v reálné aplikaci přes Application singleton
            val context = getApplicationContext()
                ?: throw IllegalStateException("Application context not available. Call VolumeController.init(context) first.")
            
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return VolumeController(context, audioManager)
        }
    }

    /**
     * Inicializace s Application contextem - musí být voláno při startu aplikace.
     */
    fun init(context: Context) {
        val appContext = context.applicationContext
        val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        INSTANCE = VolumeController(appContext, audioManager)
    }

    private fun getApplicationContext(): Context? {
        // V reálné aplikaci by to byl Application singleton
        // Zde placeholder - musí být implementován v androidApp modulu
        return try {
            Class.forName("com.mjdev.android.MjdevApplication")
                .getField("instance")
                .get(null) as Context
        } catch (e: Exception) {
            null
        }
    }
}
