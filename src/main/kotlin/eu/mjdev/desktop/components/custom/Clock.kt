package eu.mjdev.desktop.components.custom

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.components.text.TextWithShadow
import eu.mjdev.desktop.extensions.Custom.dateFlow
import eu.mjdev.desktop.extensions.Custom.timeFlow
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

// todo remove params
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
//    talkEveryHour: Boolean = true,
) = withDesktopScope {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val time = timeFlow.value
        Column {
            if (showTime) TextWithShadow(
                modifier = Modifier.fillMaxWidth(),
                text = time,
                textAlign = TextAlign.Center,
                fontWeight = timeTextWeight,
                fontSize = timeTextSize,
                color = timeTextColor
            )
            if (showDate) TextWithShadow(
                modifier = Modifier.fillMaxWidth(),
                text = dateFlow.value,
                textAlign = TextAlign.Center,
                fontWeight = dateTextWeight,
                fontSize = dateTextSize,
                color = dateTextColor
            )
        }
//        LaunchedEffect(time) {
//            if (time.isNotEmpty()) {
//                val parsed = time.split(":")
//                val isZeroMinutes = (parsed.getOrNull(1) ?: "-") == "00"
//                if (talkEveryHour && isZeroMinutes) {
//                    api.ai.talk("It is ${parsed.firstOrNull()} hour")
//                }
//            }
//        }
    }
}

@Preview
@Composable
fun ClockPreview() = Clock()
