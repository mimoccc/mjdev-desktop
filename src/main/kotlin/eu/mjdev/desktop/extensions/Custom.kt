package eu.mjdev.desktop.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.NativePaint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*

object Custom {

    fun NativePaint.setMaskFilter(
        blurRadius: Float
    ) {
        this.maskFilter =
            org.jetbrains.skia.MaskFilter.makeBlur(org.jetbrains.skia.FilterBlurMode.NORMAL, blurRadius / 2, true)
    }

    val dateFlow
        @Composable
        get() = channelFlow {
            launch {
                var date = "1.1.1970"
                do {
                    DateFormat.getDateInstance().format(Date()).also { t ->
                        if (date != t) {
                            date = t
                            send(date)
                        }
                    }
                    delay(5000L)
                } while (true)
            }
        }.collectAsState(initial = "1.1.1970")

    val timeFlow
        @Composable
        get() = channelFlow {
            launch {
                var time = "00:00:00"
                do {
                    DateFormat.getTimeInstance().format(Date()).also { t ->
                        if (time != t) {
                            time = t
                            send(time)
                        }
                    }
                    delay(200L)
                } while (true)
            }
        }.collectAsState(initial = "00:00:00")

    // todo better solution
    fun <T> SnapshotStateList<T>.invalidate() = toList().also {
        clear()
        addAll(it)
    }

}