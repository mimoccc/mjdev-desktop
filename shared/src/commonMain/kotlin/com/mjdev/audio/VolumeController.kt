package com.mjdev.audio

/**
 * Rozhraní pro jednotné ovládání systémové hlasitosti napříč platformami.
 * Implementace pro Linux desktop (PulseAudio/PipeWire) a Android (AudioManager).
 */
expect class VolumeController private constructor() {

    /**
     * Aktuální hlasitost v rozsahu 0.0 - 1.0
     */
    val volume: Float
        get() = getVolume()

    /**
     * Zda je zvuk ztlumen
     */
    val isMuted: Boolean
        get() = getIsMuted()

    /**
     * Callback při změně hlasitosti
     */
    var onVolumeChanged: ((Float) -> Unit)? = null
        get() = getOnVolumeChanged()
        set(value) = setOnVolumeChanged(value)

    /**
     * Callback při změně mute stavu
     */
    var onMuteChanged: ((Boolean) -> Unit)? = null
        get() = getOnMuteChanged()
        set(value) = setOnMuteChanged(value)

    // Platform-specific implementations
    protected expect fun getVolume(): Float
    protected expect fun getIsMuted(): Boolean
    protected expect fun getOnVolumeChanged(): ((Float) -> Unit)?
    protected expect fun setOnVolumeChanged(listener: ((Float) -> Unit)?)
    protected expect fun getOnMuteChanged(): ((Boolean) -> Unit)?
    protected expect fun setOnMuteChanged(listener: ((Boolean) -> Unit)?)

    /**
     * Nastaví hlasitost (0.0 - 1.0)
     */
    expect fun setVolume(volume: Float)

    /**
     * Přepne mute stav
     */
    expect fun toggleMute()

    /**
     * Nastaví mute stav
     */
    expect fun setMuted(muted: Boolean)

    /**
     * Zvýší hlasitost o krok (default 5%)
     */
    fun volumeUp(step: Float = 0.05f) {
        setVolume((volume + step).coerceIn(0f, 1f))
    }

    /**
     * Sníží hlasitost o krok (default 5%)
     */
    fun volumeDown(step: Float = 0.05f) {
        setVolume((volume - step).coerceIn(0f, 1f))
    }

    companion object {
        /**
         * Singleton instance pro danou platformu
         */
        @Suppress("UNUSED_PARAMETER")
        expect fun getInstance(): VolumeController
    }
}
