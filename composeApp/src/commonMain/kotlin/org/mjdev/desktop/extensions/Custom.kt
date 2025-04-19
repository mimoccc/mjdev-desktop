package org.mjdev.desktop.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toLocalTime

object Custom {

    val dateFlow
        @Composable
        get() = channelFlow {
            launch {
                var date = "1.1.1970"
                do {
                    // todo
                    Clock.System.now().toString().also { t ->
                        if (date != t) {
                            date = t
                            send(date)
                        }
                    }
                    delay(5000L)
                } while (true)
            }
        }.collectAsState(initial = "")

    val timeFlow
        @Composable
        get() = channelFlow {
            launch {
                var time = "00:00:00"
                do {
                    // todo
                    Clock.System.now().toString().also { t ->
                        if (time != t) {
                            time = t
                            send(time)
                        }
                    }
                    delay(200L)
                } while (true)
            }
        }.collectAsState(initial = "")
}