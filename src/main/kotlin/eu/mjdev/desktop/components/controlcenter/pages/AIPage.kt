package eu.mjdev.desktop.components.controlcenter.pages

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.button.TransparentButton
import eu.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import eu.mjdev.desktop.components.controlcenter.base.ControlCenterPageScope.Companion.remember
import eu.mjdev.desktop.components.input.SelectableOutlineEditText
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.extensions.Compose.scrollWithAnimToLast
import eu.mjdev.desktop.extensions.Compose.verticalTouchScrollable
import eu.mjdev.desktop.extensions.Custom.replaceLast
import eu.mjdev.desktop.extensions.Modifier.clipCircle
import eu.mjdev.desktop.helpers.compose.rememberForeverLazyListState
import eu.mjdev.desktop.icons.Icons

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName")
fun AIPage() = ControlCenterPage(
    icon = Icons.Chat,
    name = "Assistant",
    condition = { ai.isAvailable() }
) {
    val scrollState = rememberForeverLazyListState("Assistant")
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val questionsList = remember { mutableStateListOf<Pair<String, String>>() }
    val request = rememberState("")
    val iconSend = androidx.compose.runtime.remember {
        derivedStateOf {
            if (request.value.isNotEmpty()) Icons.SendIcon else null
        }
    }
    val onDone: () -> Unit = {
        val what = request.value.replace("\n", "")
        if (what.isNotEmpty()) {
            questionsList.add(Pair(what, ""))
            request.value = ""
            scrollState.scrollWithAnimToLast(scope)
            api.ai.ask(what) { _, res ->
                res.replace("* **", "")
                    .replace("**", "")
                    .trim()
                    .let { resx ->
                        questionsList.replaceLast(Pair(what, resx))
                        scrollState.scrollWithAnimToLast(scope)
                        talk(resx)
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
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                        .weight(1f)
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        ).verticalTouchScrollable(scrollState),
                    state = scrollState
                ) {
                    itemsIndexed(questionsList) { idx, textData ->
                        Column(
                            modifier = Modifier.padding(4.dp)
                                .fillMaxWidth()
                                .background(
                                    Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(4.dp),
                        ) {
                            TextBlock(
                                idx = idx,
                                text = textData.first,
                                clipboardManager = clipboardManager
                            )
                            TextBlock(
                                text = textData.second,
                                clipboardManager = clipboardManager
                            )
                        }
                    }
                }
                SelectableOutlineEditText(
                    modifier = Modifier.padding(top = 8.dp)
                        .fillMaxWidth(),
                    value = request.value,
                    onValueChange = { t: String ->
                        request.value = t
                        if (t.contains("\n")) {
                            onDone()
                        }
                    },
                    maxLines = 4,
                    minLines = 4,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color.White.copy(alpha = 0.1f),
                        textColor = Color.White.copy(alpha = 0.9f),
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Black
                    ),
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (request.value.isNotEmpty()) {
                                Icon(
                                    modifier = Modifier.padding(4.dp)
                                        .size(24.dp)
                                        .onClick {
                                            clipboardManager.setText(AnnotatedString(request.value))
                                        },
                                    imageVector = Icons.CopyToClipboard,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    contentDescription = ""
                                )
                            }
                            if (iconSend.value != null) {
                                Icon(
                                    modifier = Modifier.padding(4.dp)
                                        .size(24.dp)
                                        .onClick { onDone() },
                                    imageVector = iconSend.value!!,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    contentDescription = ""
                                )
                            }
                        }
                    }
                )
            }
        }
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
                imageVector = Icons.CopyToClipboard,
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
