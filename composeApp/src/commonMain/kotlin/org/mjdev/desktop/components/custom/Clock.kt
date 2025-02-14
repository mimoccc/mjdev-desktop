package org.mjdev.desktop.components.custom

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.mjdev.desktop.components.text.TextWithShadow
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.Modifier.size
//import org.mjdev.desktop.extensions.Custom.dateFlow
//import org.mjdev.desktop.extensions.Custom.timeFlow
import org.mjdev.desktop.extensions.MutableStateExt.rememberState
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.LaunchedEffect.runAsync
import org.mjdev.desktop.extensions.Modifier.onMousePress
import org.mjdev.desktop.helpers.parsers.Parsers.ParsedList

// todo remove params
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Clock(
    modifier: Modifier = Modifier,
    timeTextSize: TextUnit = 32.sp,
    timeTextWeight: FontWeight = FontWeight.Bold,
    timeTextColor: Color = Color.Black,
    dateTextSize: TextUnit = 16.sp,
    dateTextWeight: FontWeight = FontWeight.Bold,
    dateTextColor: Color = Color.Black,
    showTime: Boolean = true,
    showDate: Boolean = true,
    talkEveryHour: Boolean = true,
    talkOnClick: Boolean = true
) = withDesktopContext {
    val time = "time" //timeFlow.value // todo
    var lastTalk by rememberState("")
    val talk: () -> Unit = {
        runAsync {
            lastTalk = time
            val parsed = ParsedList(time, ":")
//            ai.talk(
//                "It is ${parsed.firstOrNull()} hour, ${
//                    parsed.getOrNull(1)?.let { "$it minutes" }.orEmpty()
//                }, ${
//                    parsed.getOrNull(2)?.let { "$it seconds" }.orEmpty()
//                }"
//            )
        }
    }
    Box(
        modifier = modifier.onMousePress { if (talkOnClick) talk() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showTime) TextWithShadow(
                text = time,
                textAlign = TextAlign.Center,
                fontWeight = timeTextWeight,
                fontSize = timeTextSize,
                color = timeTextColor,
                singleLine = true
            )
            if (showDate) TextWithShadow(
                text = "date", // dateFlow.value, // todo
                textAlign = TextAlign.Center,
                fontWeight = dateTextWeight,
                fontSize = dateTextSize,
                color = dateTextColor,
                singleLine = true
            )
        }
        LaunchedEffect(time) {
            if (time.isNotEmpty() && lastTalk != time) {
                val parsed = ParsedList(time, ":")
                val isZeroSeconds = (parsed.getOrNull(2) ?: "-") == "00"
                val isZeroMinutes = (parsed.getOrNull(1) ?: "-") == "00"
                if (talkEveryHour && isZeroMinutes && isZeroSeconds) {
                    talk()
                }
            }
        }
    }
}

// todo
@Suppress("unused")
//@Preview
@Composable
fun ClockPreview() = preview {
    Clock(
        modifier = Modifier.size(320, 200)
    )
}
