package org.mjdev.desktop.components.desktop.widgets.music

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.desktop.plugins.music.MusicDefaults
import org.mjdev.desktop.plugins.music.MusicTrackLines

/**
 * Raised inner card of the music widget: cover placeholder on the left, "now playing" label with
 * a badge and the [lines] (previous / current / next track) styled like lyric lines.
 */
@Suppress("FunctionName")
@Composable
fun MusicNowPlayingCard(
    lines: MusicTrackLines,
    cardColor: Color,
    accentColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) = Row(
    modifier =
        modifier
            .fillMaxWidth()
            .height(MusicDefaults.INNER_CARD_DP.dp)
            .background(cardColor, RoundedCornerShape(MusicDefaults.INNER_CORNER_DP.dp))
            .border(
                width = MusicDefaults.INNER_BORDER_DP.dp,
                color = textColor.copy(alpha = MusicDefaults.INNER_BORDER_ALPHA),
                shape = RoundedCornerShape(MusicDefaults.INNER_CORNER_DP.dp),
            ).padding(MusicDefaults.SPACING_DP.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(MusicDefaults.PADDING_DP.dp),
) {
    Box(
        modifier =
            Modifier
                .size(MusicDefaults.COVER_DP.dp)
                .background(
                    accentColor.copy(alpha = MusicDefaults.COVER_ALPHA),
                    RoundedCornerShape(MusicDefaults.COVER_CORNER_DP.dp),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = lines.current,
            tint = accentColor,
        )
    }
    Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = MusicDefaults.NOW_PLAYING_LABEL,
                color = textColor,
                fontSize = MusicDefaults.LABEL_SP.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = MusicDefaults.NOW_PLAYING_LABEL,
                tint = accentColor,
                modifier = Modifier.size(MusicDefaults.BADGE_DP.dp),
            )
        }
        val fadedColor = accentColor.copy(alpha = MusicDefaults.FADED_LINE_ALPHA)
        MusicTrackLine(lines.previous, fadedColor, MusicDefaults.LINE_SP.sp)
        MusicTrackLine(lines.current, accentColor, MusicDefaults.LINE_SP.sp, FontWeight.Bold)
        MusicTrackLine(lines.next, fadedColor, MusicDefaults.LINE_SP.sp)
    }
}
