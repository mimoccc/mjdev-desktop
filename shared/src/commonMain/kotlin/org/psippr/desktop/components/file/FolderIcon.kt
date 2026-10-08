package org.psippr.desktop.components.file

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import okio.Path
import org.psippr.desktop.components.draggable.DraggableView
import org.psippr.desktop.components.fonticon.FontIcon
import org.psippr.desktop.components.text.TextWithShadow
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.data.widgets.WidgetKey
import org.psippr.desktop.extensions.Colors.alpha
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.extensions.DpSizeExt.plus
import org.psippr.desktop.extensions.Modifier.circleBorder
import org.psippr.desktop.extensions.PathExt.absolutePath
import org.psippr.desktop.extensions.PathExt.cwd
import org.psippr.desktop.extensions.PathExt.extension

@Suppress("FunctionName", "UNNECESSARY_SAFE_CALL")
@Composable
fun FolderIcon(
    path: Path = cwd,
    customName: String? = null,
    dragEnabled: Boolean = true,
    iconSize: DpSize = DpSize(128.dp, 128.dp),
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(4.dp),
    outerPadding: PaddingValues = PaddingValues(4.dp),
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onContextMenuClick: () -> Unit = {},
    onClick: () -> Unit = {},
) = withDesktopContext {
    val iconName =
        remember(path) {
            when {
                path.absolutePath?.contentEquals("/") == true -> "root"
                path.absolutePath?.contentEquals("~") == true -> "home"
                else -> path.extension.ifEmpty { "folder" }
            }
        } // todo from mime type
    val iconId: Int = remember(path) { iconSet?.iconForName(iconName) ?: 0 }
    val computedSize = remember(path) { iconSize + 2.dp }
    DraggableView(
        modifier = modifier,
        dragEnabled = dragEnabled,
        onClick = onClick,
        onContextMenuClick = onContextMenuClick,
        onDragStart = onDragStart,
        onDragEnd = onDragEnd,
        initialPosition = widgetPositions.position(WidgetKey.Folder.of(path.toString())),
        onPositionChange = { offset -> widgetPositions.save(WidgetKey.Folder.of(path.toString()), offset) },
    ) {
        Column(
            modifier = Modifier.wrapContentSize().aspectRatio(1f),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(computedSize)
                        .circleBorder(2.dp, textColor),
            ) {
                FontIcon(
                    iconId = iconId,
                    iconSize = iconSize,
                    iconColor = textColor,
                    iconBackgroundColor = iconsTintColor.alpha(0.4f),
                    outerPadding = outerPadding,
                    innerPadding = innerPadding,
                    onClick = onClick,
                    onRightClick = onContextMenuClick,
                )
            }
            TextWithShadow(
                modifier =
                    Modifier
                        .width(computedSize.width)
                        .padding(start = 4.dp),
                text = (customName ?: path.name).ifEmpty { iconName },
                color = textColor,
                fontWeight = FontWeight.Bold,
                overflow = Ellipsis,
                minLines = 2,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
fun PreviewFolderIcon() = preview {
    FolderIcon()
}
