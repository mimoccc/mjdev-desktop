package com.mjdev.audio

/**
 * Factory pro získání instance VolumeController.
 * Používá expect/actual mechanismus pro platform-specific implementaci.
 */
object VolumeControllerFactory {
    /**
     * Vrátí singleton instanci VolumeController pro aktuální platformu.
     */
    fun getController(): VolumeController = VolumeController.getInstance()
}
