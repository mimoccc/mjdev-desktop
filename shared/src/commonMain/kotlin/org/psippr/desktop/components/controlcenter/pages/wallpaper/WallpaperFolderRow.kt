package org.psippr.desktop.components.controlcenter.pages.wallpaper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.psippr.desktop.components.image.ImageAny
import org.psippr.desktop.components.text.TextAny
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Colors.alpha
import org.psippr.desktop.providers.background.WallpaperFolder

/**
 * One line per wallpaper [folder]: name plus an include switch. The whole line (label area)
 * is clickable and expands a 16:9 preview that cycles through the folder images every second,
 * with the current file name (not the full path) below it.
 */
@Suppress("FunctionName")
@Composable
fun WallpaperFolderRow(
    folder: WallpaperFolder,
    included: Boolean,
    modifier: Modifier = Modifier,
    onIncludedChange: (Boolean) -> Unit,
) = withDesktopContext {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextAny(
                modifier = Modifier.weight(1f),
                text = "${folder.name} (${folder.images.size})",
                color = if (included) textColor else textColor.alpha(0.4f),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Switch(
                checked = included,
                onCheckedChange = onIncludedChange,
                colors =
                    SwitchDefaults.colors(
                        checkedThumbColor = textColor,
                        checkedTrackColor = textColor.alpha(0.4f),
                        uncheckedThumbColor = textColor.alpha(0.6f),
                        uncheckedTrackColor = backgroundColor.alpha(0.6f),
                    ),
            )
        }
        if (expanded) {
            WallpaperFolderPreview(folder)
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun WallpaperFolderPreview(folder: WallpaperFolder) = withDesktopContext {
    var index by remember(folder.key) { mutableStateOf(0) }
    LaunchedEffect(folder.key) {
        while (folder.images.size > 1) {
            delay(WallpaperPageDefaults.PREVIEW_DELAY_MS)
            index = (index + 1) % folder.images.size
        }
    }
    val current = folder.images.getOrNull(index)
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = WallpaperPageDefaults.previewGap),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(WallpaperPageDefaults.PREVIEW_ASPECT_RATIO)
                    .clip(RoundedCornerShape(WallpaperPageDefaults.previewCorner))
                    .padding(0.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (current != null) {
                ImageAny(
                    modifier = Modifier.fillMaxWidth().aspectRatio(WallpaperPageDefaults.PREVIEW_ASPECT_RATIO),
                    src = current,
                    contentScale = ContentScale.Crop,
                )
            } else {
                TextAny(
                    text = WallpaperPageDefaults.NO_IMAGES,
                    color = textColor.alpha(0.6f),
                    fontSize = 12.sp,
                )
            }
        }
        TextAny(
            modifier = Modifier.padding(top = WallpaperPageDefaults.previewGap),
            text = current?.name.orEmpty(),
            color = textColor.alpha(0.7f),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
