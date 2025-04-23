package org.mjdev.desktop.components.controlcenter.pages.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pushpal.jetlime.EventPointType
import com.pushpal.jetlime.ItemsList
import com.pushpal.jetlime.JetLimeColumn
import com.pushpal.jetlime.JetLimeDefaults
import com.pushpal.jetlime.JetLimeEvent
import com.pushpal.jetlime.JetLimeEventDefaults
import org.mjdev.desktop.components.button.TransparentButton
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPageScope.Companion.remember
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.components.text.TextArea
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.Compose.replaceLast
import org.mjdev.desktop.extensions.Compose.scrollWithAnimToLast
import org.mjdev.desktop.extensions.Compose.verticalTouchScrollable
import org.mjdev.desktop.extensions.Modifier.clipCircle
import org.mjdev.desktop.helpers.compose.rememberForeverLazyListState
import org.mjdev.desktop.icons.chat.Chat
import org.mjdev.desktop.icons.system.ContentCopy
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("FunctionName")
fun AIPage() = ControlCenterPage(
    icon = Chat,
    name = "Assistant",
    condition = {
         ai.isAvailable()
    }
) {
    val scrollState = rememberForeverLazyListState("Assistant")
    val clipboardManager = LocalClipboardManager.current
    val questionsList = remember { mutableStateListOf<Pair<String, String>>() }
    val request = remember { mutableStateOf("") }
    val onDone: () -> Unit = {
        val what = request.value.replace("\n", "")
        if (what.isNotEmpty()) {
            questionsList.add(Pair(what, ""))
            request.value = ""
            scrollState.scrollWithAnimToLast(scope)
            context.ai.ask(what) { _, res ->
                res.replace("* **", "")
                    .replace("**", "")
                    .trim()
                    .let { resx ->
                        questionsList.replaceLast(Pair(what, resx))
                        scrollState.scrollWithAnimToLast(scope)
                        context.ai.say(resx)
                    }
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(16.dp)
            ) {
                JetLimeColumn(
                    modifier = Modifier.fillMaxWidth()
                        .weight(1f)
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        ).verticalTouchScrollable(scrollState),
                    itemsList = ItemsList(questionsList),
                    style = JetLimeDefaults.columnStyle(
                        lineThickness = 2.dp,
                        lineBrush = JetLimeDefaults.lineSolidBrush(
                            color = Color.White.alpha(0.6f)
                        )
                    ),
                ) { idx, item, position ->
                    JetLimeEvent(
                        style = JetLimeEventDefaults.eventStyle(
                            pointColor = Color.Black,
                            pointFillColor = Color.White.alpha(0.5f),
                            pointRadius = 8.dp,
                            position = position,
                            pointAnimation = JetLimeEventDefaults.pointAnimation(),
                            pointType = EventPointType.filled(0.8f),
                            pointStrokeWidth = 2.dp,
                            pointStrokeColor = Color.Black,
                        ),
                    ) {
                        TimeLineItem(
                            idx = idx,
                            item = item,
                            clipboardManager = clipboardManager
                        )
                    }
                }
                TextArea(
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                    clipboardManager = clipboardManager,
                    textState = request,
                    onDone = onDone
                )
            }
        }
    }
}

@Composable
fun TimeLineItem(
    idx: Int,
    item: Pair<String, String>,
    clipboardManager: ClipboardManager
) {
    Column(
        modifier = Modifier.padding(4.dp)
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            ),
    ) {
        TextBlock(
            idx = idx,
            text = item.first,
            clipboardManager = clipboardManager
        )
        TextBlock(
            text = item.second,
            clipboardManager = clipboardManager
        )
    }
}

@Composable
fun TextBlock(
    idx: Int = -1,
    text: String = "",
    clipboardManager: ClipboardManager = LocalClipboardManager.current
) {
    Box(
        Modifier.padding(4.dp)
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        TextAny(
            modifier = Modifier.fillMaxWidth()
                .padding(end = 24.dp),
            text = "${if (idx >= 0) "${idx + 1}. " else ""}${text}",
            color = Color.White,
            textAlign = TextAlign.Start,
            textSelectionEnabled = true
        )
        TransparentButton(
            modifier = Modifier.size(32.dp)
                .clipCircle()
                .align(Alignment.TopEnd),
            onClick = {
                clipboardManager.setText(AnnotatedString(text))
            },
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = ContentCopy,
                tint = Color.White.copy(alpha = 0.9f),
                contentDescription = ""
            )
        }
    }
}

@Preview
@Composable
fun AIPagePreview() = preview {
    AIPage().render()
}
