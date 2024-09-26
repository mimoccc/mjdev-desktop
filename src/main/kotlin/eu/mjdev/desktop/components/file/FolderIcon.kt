package eu.mjdev.desktop.components.file

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.draggable.DraggableView
import eu.mjdev.desktop.components.fonticon.FontIcon
import eu.mjdev.desktop.components.text.TextWithShadow
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import java.io.File
import eu.mjdev.desktop.extensions.Custom.plus
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.extensions.Modifier.circleBorder

@Suppress("FunctionName")
@Composable
fun FolderIcon(
    path: File = File("/"),
    customName: String? = null,
    dragEnabled: Boolean = true,
    iconSize: DpSize = DpSize(128.dp, 128.dp),
    zIndex: () -> Int = { 0 },
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onClick: () -> Unit = {},
    onContextMenuClick: () -> Unit = {},
) = withDesktopScope {
    val iconName = remember(path) {
        path.let {
            it.extension.ifEmpty {
                // todo
//                if (it.list() != null)
                "folder"
//                else ""
            }
        }
    }
    val iconId: Int = remember(path) { api.currentUser.theme.iconSet.iconForName(iconName) ?: "?".toInt() }
    val computedSize = remember(path) { iconSize + 2.dp }
    DraggableView(
        modifier = Modifier.wrapContentSize(),
        dragEnabled = dragEnabled,
        zIndex = zIndex().toFloat(),
        onClick = onClick,
        onContextMenuClick = onContextMenuClick,
        onDragStart = onDragStart,
        onDragEnd = onDragEnd
    ) {
        Column(
            modifier = Modifier.wrapContentSize()
        ) {
            Box(
                modifier = Modifier.size(computedSize)
                    .circleBorder(2.dp, textColor)
            ) {
                FontIcon(
                    iconId = iconId,
                    iconSize = iconSize,
                    iconColor = textColor,
                    iconBackgroundColor = iconsTintColor.alpha(0.4f),
                    outerPadding = PaddingValues(8.dp),
                    innerPadding = PaddingValues(8.dp),
                    onClick = onClick,
                    onRightClick = onContextMenuClick
                )
            }
            TextWithShadow(
                modifier = Modifier.width(computedSize.width)
                    .padding(start = 4.dp),
                text = customName ?: path.name ?: "",
                color = textColor,
                fontWeight = FontWeight.Bold,
                overflow = Ellipsis,
                minLines = 2,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
fun FolderIconPreview() = FolderIcon()
