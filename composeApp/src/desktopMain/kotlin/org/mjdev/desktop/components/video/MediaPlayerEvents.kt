package org.mjdev.desktop.components.video

import uk.co.caprica.vlcj.media.MediaRef
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayer

class MediaPlayerEvents(
    private val mediaPlayer:EmbeddedMediaPlayer?
)  : MediaPlayerEventAdapter() {
    override fun timeChanged(mediaPlayer: MediaPlayer?, newTime: Long) {
        super.timeChanged(mediaPlayer, newTime)
    }

    override fun positionChanged(mp: MediaPlayer?, newPosition: Float) {
        super.positionChanged(mediaPlayer, newPosition)
    }

    override fun mediaChanged(mp: MediaPlayer?, media: MediaRef?) {
        super.mediaChanged(mediaPlayer, media)
    }
}