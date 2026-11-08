package org.psippr.desktop.components.desktop.widgets.music

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kdroidfilter.composemediaplayer.audio.AudioPlayerState
import io.github.kdroidfilter.composemediaplayer.audio.rememberAudioPlayerLiveState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.plugins.music.MusicDefaults
import org.psippr.desktop.plugins.music.MusicLibrary
import org.psippr.desktop.plugins.music.MusicTrackFolder
import org.psippr.desktop.plugins.music.MusicTrackLines

/**
 * Music player card for the desktop (design: screenshots/widgets/music.jpg): header with the
 * playing time tab, raised card with the previous / current / next track, title with its folder,
 * seek bar with times and previous / play-pause / next buttons. Colors follow the wallpaper palette.
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

    val progress = if (live.duration > 0L) (live.position.toFloat() / live.duration).coerceIn(0f, 1f) else 0f
    val isPlaying = live.state == AudioPlayerState.PLAYING
    val elapsed = formatMusicTime(live.position)
    // colors come from the desktop palette, so the card follows the wallpaper like the rest
    val accentColor = iconsTintColor
    val onAccentColor = backgroundColor
    val secondaryColor = textColor.copy(alpha = MusicDefaults.SECONDARY_ALPHA)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(MusicDefaults.CORNER_DP.dp))
                .background(backgroundColor.copy(alpha = MusicDefaults.CARD_BG_ALPHA)),
        verticalArrangement = Arrangement.spacedBy(MusicDefaults.SPACING_DP.dp),
    ) {
        // the tab shows the playing time of the track (no clock)
        MusicHeader(
            title = MusicDefaults.HEADER_TITLE,
            time = elapsed,
            accentColor = accentColor,
            onAccentColor = onAccentColor,
            textColor = textColor,
        )
        MusicNowPlayingCard(
            lines = MusicTrackLines.of(controller.queue),
            cardColor = lerp(backgroundColor, textColor, MusicDefaults.INNER_CARD_BLEND),
            accentColor = accentColor,
            textColor = textColor,
            modifier = Modifier.padding(horizontal = MusicDefaults.SPACING_DP.dp),
        )
        Column(
            modifier = Modifier.padding(horizontal = MusicDefaults.PADDING_DP.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = controller.queue.current?.title ?: MusicDefaults.EMPTY_TITLE,
                color = textColor,
                fontSize = MusicDefaults.TITLE_SP.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = MusicTrackFolder.of(controller.queue.current),
                color = secondaryColor,
                fontSize = MusicDefaults.SUBTITLE_SP.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            MusicProgressBar(
                progress = progress,
                enabled = live.duration > 0L,
                accentColor = accentColor,
                onSeek = { fraction -> controller.seekTo((fraction * live.duration).toLong()) },
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = elapsed, color = secondaryColor, fontSize = MusicDefaults.TIME_SP.sp)
                Text(text = formatMusicTime(live.duration), color = secondaryColor, fontSize = MusicDefaults.TIME_SP.sp)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(MusicDefaults.TRANSPORT_GAP_DP.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MusicControlButton(Icons.Filled.SkipPrevious, MusicDefaults.PREVIOUS_LABEL, textColor) {
                    controller.previous()
                }
                MusicPlayButton(isPlaying, accentColor, onAccentColor) { controller.toggle(live.state) }
                MusicControlButton(Icons.Filled.SkipNext, MusicDefaults.NEXT_LABEL, textColor) {
                    controller.next()
                }
            }
        }
    }
}
