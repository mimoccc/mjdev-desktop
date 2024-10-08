package eu.mjdev.desktop.components.custom

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.onClick
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.components.text.TextWithShadow
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.extensions.Custom.ParsedList
import eu.mjdev.desktop.extensions.Custom.dateFlow
import eu.mjdev.desktop.extensions.Custom.timeFlow
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import kotlinx.coroutines.launch

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
) = withDesktopScope {
    val time = timeFlow.value
    var lastTalk by rememberState("")
    val talk: () -> Unit = {
        scope.launch {
            lastTalk = time
            val parsed = ParsedList(time, ":")
            ai.talk(
                "It is ${parsed.firstOrNull()} hour, ${
                    parsed.getOrNull(1)?.let { "$it minutes" }.orEmpty()
                }, ${
                    parsed.getOrNull(2)?.let { "$it seconds" }.orEmpty()
                }"
            )
        }
    }
    Box(
        modifier = modifier.onClick { if (talkOnClick) talk() },
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
                text = dateFlow.value,
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

@Preview
@Composable
fun ClockPreview() = Clock()
