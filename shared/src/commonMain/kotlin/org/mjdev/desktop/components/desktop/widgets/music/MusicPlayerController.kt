package org.mjdev.desktop.components.desktop.widgets.music

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.kdroidfilter.composemediaplayer.audio.AudioPlayer
import io.github.kdroidfilter.composemediaplayer.audio.AudioPlayerState
import io.github.kdroidfilter.composemediaplayer.audio.ErrorListener
import org.mjdev.desktop.log.Log
import org.mjdev.desktop.plugins.music.MusicQueue
import org.mjdev.desktop.plugins.music.MusicTrack

/**
 * Drives one [AudioPlayer] over a [MusicQueue]. The UI reads [queue] and the live player state and
 * calls the transport functions; a track that ends (or fails) never makes the player skip in a
 * loop: only [wantsPlayback] together with a finished track moves to the next one.
 */
class MusicPlayerController(
    private val player: AudioPlayer,
) {
    /** Tracks and the cursor; replaced when the music folder was scanned. */
    var queue by mutableStateOf(MusicQueue())
        private set

    /** True while the user wants music, set by play and cleared by pause or a playback error. */
    var wantsPlayback by mutableStateOf(false)
        private set

    init {
        player.setOnErrorListener(
            object : ErrorListener {
                override fun onError(message: String?) {
                    Log.d("Music player error: $message")
                    wantsPlayback = false
                }
            },
        )
    }

    /** Replaces the queue with [tracks], starting at the first one. Playback is left untouched. */
    fun setTracks(tracks: List<MusicTrack>) {
        queue = MusicQueue(tracks)
    }

    /** Pauses a playing track, resumes a paused one, or starts the current track when idle. */
    fun toggle(state: AudioPlayerState) {
        when (state) {
            AudioPlayerState.PLAYING -> pause()
            AudioPlayerState.PAUSED -> resume()
            else -> startCurrent()
        }
    }

    /** Moves to the next track and plays it when music was playing. */
    fun next() = moveTo(queue.next())

    /** Moves to the previous track and plays it when music was playing. */
    fun previous() = moveTo(queue.previous())

    /** Called when the current track reached its end: continues with the next track. */
    fun onTrackEnded() {
        if (wantsPlayback) next()
    }

    /** Seeks the current track to [positionMs]. */
    fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    /** Stops playback and frees the native player. */
    fun release() {
        wantsPlayback = false
        player.release()
    }

    private fun pause() {
        wantsPlayback = false
        player.pause()
    }

    private fun resume() {
        wantsPlayback = true
        player.play()
    }

    private fun startCurrent() {
        val track = queue.current ?: return
        wantsPlayback = true
        player.play(track.path)
    }

    private fun moveTo(target: MusicQueue) {
        queue = target
        if (wantsPlayback) startCurrent()
    }
}
