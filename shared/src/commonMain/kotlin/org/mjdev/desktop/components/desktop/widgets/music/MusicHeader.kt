package org.mjdev.desktop.components.desktop.widgets.music

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

/**
 * Header of the music card: round note icon with the [title] on the left and a tab in the
 * top-right corner showing the playing [time] of the current track.
 */
@Suppress("FunctionName")
@Composable
fun MusicHeader(
    title: String,
    time: String,
    accentColor: Color,
    onAccentColor: Color,
    textColor: Color,
) = Row(
    modifier =
        Modifier
            .fillMaxWidth()
            .height(MusicDefaults.HEADER_DP.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
) {
    Row(
        modifier = Modifier.padding(start = MusicDefaults.PADDING_DP.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MusicDefaults.SPACING_DP.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = title,
            tint = onAccentColor,
            modifier =
                Modifier
                    .size(MusicDefaults.HEADER_ICON_DP.dp)
                    .background(accentColor, CircleShape)
                    .padding(MusicDefaults.HEADER_ICON_PADDING_DP.dp),
        )
        Text(
            text = title,
            color = textColor,
            fontSize = MusicDefaults.HEADER_SP.sp,
            fontWeight = FontWeight.Bold,
        )
    }
    Box(
        modifier =
            Modifier
                .fillMaxHeight()
                .background(
                    color = accentColor,
                    shape =
                        RoundedCornerShape(
                            topEnd = MusicDefaults.CORNER_DP.dp,
                            bottomStart = MusicDefaults.TIME_TAB_CORNER_DP.dp,
                        ),
                ).padding(horizontal = MusicDefaults.TIME_TAB_PADDING_DP.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = time,
            color = onAccentColor,
            fontSize = MusicDefaults.TIME_TAB_SP.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
