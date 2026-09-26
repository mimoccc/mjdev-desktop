package com.mjdev.android

import android.app.Application
import com.mjdev.audio.VolumeController

/**
 * Hlavní Application třída pro Android modul.
 * Inicializuje VolumeController s application contextem.
 */
class MjdevApplication : Application() {

    companion object {
        @Suppress("UNUSED_PROPERTY")
        var instance: MjdevApplication? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Inicializace VolumeController
        VolumeController.init(this)
    }
}
