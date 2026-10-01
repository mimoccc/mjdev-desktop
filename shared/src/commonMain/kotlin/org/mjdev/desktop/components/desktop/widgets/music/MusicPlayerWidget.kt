package org.mjdev.desktop.components.desktop.widgets.music

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kdroidfilter.composemediaplayer.audio.AudioPlayerState
import io.github.kdroidfilter.composemediaplayer.audio.rememberAudioPlayerLiveState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.plugins.music.MusicDefaults
import org.mjdev.desktop.plugins.music.MusicLibrary

/**
 * Music player card for the desktop: shows the current track of the user's music folder with a
 * seek bar and previous / play-pause / next buttons. Colors follow the wallpaper palette.
 */
@Suppress("FunctionName")
@Composable
fun MusicPlayerWidget(modifier: Modifier = Modifier) = withDesktopContext {
    val live = rememberAudioPlayerLiveState()
    val controller = remember(live.player) { MusicPlayerController(live.player) }
    val musicDir = currentUser.userDirs.musicDirectory
    val tracks by produceState(emptyList(), musicDir) {
        value = withContext(Dispatchers.Default) { MusicLibrary.scan(musicDir) }
    }
    LaunchedEffect(tracks) { controller.setTracks(tracks) }
    DisposableEffect(controller) { onDispose { controller.release() } }

    // The last PLAYING sample tells whether a following IDLE is a finished track, not a manual stop.
    var nearEnd by remember { mutableStateOf(false) }
    LaunchedEffect(live.position, live.duration, live.state) {
        if (live.state == AudioPlayerState.PLAYING) {
            nearEnd = live.duration > 0L && live.position + MusicDefaults.END_TOLERANCE_MS >= live.duration
        }
    }
    LaunchedEffect(live.state) {
        if (live.state == AudioPlayerState.IDLE && nearEnd) {
            nearEnd = false
            controller.onTrackEnded()
        }
    }

    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val progress = if (live.duration > 0L) (live.position.toFloat() / live.duration).coerceIn(0f, 1f) else 0f
    val isPlaying = live.state == AudioPlayerState.PLAYING

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(MusicDefaults.CORNER_DP.dp))
                .background(backgroundColor.copy(alpha = MusicDefaults.CARD_ALPHA))
                .padding(MusicDefaults.PADDING_DP.dp),
        verticalArrangement = Arrangement.spacedBy(MusicDefaults.SPACING_DP.dp),
    ) {
        Text(
            text = controller.queue.current?.title ?: MusicDefaults.EMPTY_TITLE,
            color = textColor,
            fontSize = MusicDefaults.TITLE_SP.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "${formatMusicTime(live.position)} / ${formatMusicTime(live.duration)}",
            color = textColor.copy(alpha = MusicDefaults.CARD_ALPHA),
            fontSize = MusicDefaults.SUBTITLE_SP.sp,
        )
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = dragFraction ?: progress,
            enabled = live.duration > 0L,
            onValueChange = { fraction -> dragFraction = fraction },
            onValueChangeFinished = {
                dragFraction?.let { fraction -> controller.seekTo((fraction * live.duration).toLong()) }
                dragFraction = null
            },
            colors =
                SliderDefaults.colors(
                    thumbColor = iconsTintColor,
                    activeTrackColor = iconsTintColor,
                ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MusicControlButton(Icons.Filled.SkipPrevious, MusicDefaults.PREVIOUS_LABEL, iconsTintColor) {
                controller.previous()
            }
            MusicControlButton(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                description = if (isPlaying) MusicDefaults.PAUSE_LABEL else MusicDefaults.PLAY_LABEL,
                tint = iconsTintColor,
            ) { controller.toggle(live.state) }
            MusicControlButton(Icons.Filled.SkipNext, MusicDefaults.NEXT_LABEL, iconsTintColor) {
                controller.next()
            }
        }
    }
}
