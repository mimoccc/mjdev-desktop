package eu.mjdev.desktop.components.controlcenter.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.controlcenter.ControlCenterPage
import eu.mjdev.desktop.extensions.Compose.rememberState
import kotlinx.coroutines.launch

@Suppress("FunctionName")
fun AIPage() = ControlCenterPage(
    icon = Icons.Filled.Campaign,
    name = "AI",
    condition = { ai.isAvailable() }
) {
    val questionsList = remember { mutableStateListOf<Pair<String, String>>() }
    val request = rememberState("")
    val onDone: (String) -> Unit = { what ->
        if (what.isNotEmpty()) {
            questionsList.add(Pair(what, ""))
            api.ai.ask(what) { _, res ->
                res.replace("* **", "").replace("**", "").let { resx ->
                    questionsList.removeIf { it.first == what }
                    questionsList.add(Pair(what, resx))
                    talk(resx)
                }
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
            .background(backgroundColor)
    ) {
        Row {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                ) {
                    itemsIndexed(questionsList) { idx, textData ->
                        Column(
                            modifier = Modifier.padding(4.dp)
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(4.dp),
                        ) {
                            Text(
                                modifier = Modifier.padding(4.dp)
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                text = "${idx + 1}. ${textData.first}",
                                color = Color.White
                            )
                            Text(
                                modifier = Modifier.padding(4.dp)
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                text = textData.second,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                    value = request.value,
                    onValueChange = { t: String ->
                        request.value = t
                        if (t.contains("\n")) {
                            scope.launch {
                                onDone(t.replace("\n", ""))
                            }
                            request.value = ""
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
                    )
                )
            }
        }
    }
}
