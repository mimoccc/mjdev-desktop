package org.mjdev.desktop.components.video

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class MediaPlayer {
    fun play()
    fun pause()
    val isPlaying: Boolean

    fun setRate(rate: Float)
    fun setTime(millis: Long)
    fun setTimeAccurate(millis: Long)

    fun getTimeMillis(): Long
    fun getLengthMillis(): Long
    fun addOnTimeChangedListener(listener: OnTimeChangedListener)

    fun dispose()
}

interface OnTimeChangedListener {
    fun onTimeChanged(timeMillis: Long)
}