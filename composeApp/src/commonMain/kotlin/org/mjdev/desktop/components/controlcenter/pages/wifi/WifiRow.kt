package org.mjdev.desktop.components.controlcenter.pages.wifi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.mjdev.desktop.extensions.Modifier.onMousePress
import org.mjdev.desktop.extensions.MutableStateExt.toggle
import androidx.compose.material.Divider
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.alpha
import org.mjdev.desktop.components.icon.WifiLevelIcon
import org.mjdev.desktop.components.text.KeyValueText
import org.mjdev.desktop.components.text.TextAny
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.data.WifiNetwork
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.extensions.Compose.preview

@Suppress("UNUSED_PARAMETER")
@Composable
fun WifiRow(
    modifier: Modifier = Modifier,
    idx: Int = 0,
    item: WifiNetwork,
    expandedState: MutableState<Boolean> = mutableStateOf(false),
    isConnecting: Boolean = false,
    connect: suspend () -> Unit = {}
) = withDesktopContext {
    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextAny(
                modifier = Modifier.weight(1f).onMousePress {
                    expandedState.toggle()
                },
                text = item.ssid,
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                singleLine = true,
                textSelectionEnabled = false
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextAny(
                    modifier = Modifier.padding(start = 8.dp).wrapContentWidth(),
                    text = item.encryption,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Start,
                    singleLine = true,
                    textSelectionEnabled = false
                )
                WifiLevelIcon(
                    modifier = Modifier.padding(start = 8.dp).size(28.dp),
                    color = textColor,
                    level = item.signalLevel.toInt()
                )
                Box(
                    modifier = Modifier.padding(start = 8.dp).size(24.dp)
                ) {
                    RadioButton(
                        modifier = Modifier.size(24.dp)
                            .alpha(if (isConnecting) 0f else 1f),
                        selected = item.isActive,
                        enabled = !item.isActive && !isConnecting,
                        onClick = {
                            scope.launch {
                                connect()
                            }
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = textColor,
                            unselectedColor = textColor,
                            disabledColor = textColor
                        )
                    )
                }
            }
        }
        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth()
                .wrapContentHeight(),
            visible = expandedState.value
        ) {
            Column {
                Divider(
                    modifier = Modifier.fillMaxWidth()
                        .padding(vertical = 4.dp),
                    color = textColor,
                    thickness = 1.dp,
                )
                KeyValueText(
                    modifier = Modifier.fillMaxWidth(),
                    "ssid",
                    item.ssid,
                )
                KeyValueText(
                    modifier = Modifier.fillMaxWidth(),
                    "encryption",
                    item.encryption,
                )
                KeyValueText(
                    modifier = Modifier.fillMaxWidth(),
                    "channel",
                    item.channel,
                )
                KeyValueText(
                    modifier = Modifier.fillMaxWidth(),
                    "bandwidth",
                    item.bandwidth,
                )
                KeyValueText(
                    modifier = Modifier.fillMaxWidth(),
                    "frequency",
                    item.frequency,
                )
                KeyValueText(
                    modifier = Modifier.fillMaxWidth(),
                    "mode",
                    item.mode,
                )
                KeyValueText(
                    modifier = Modifier.fillMaxWidth(),
                    "signal level",
                    item.signalLevel,
                )
                KeyValueText(
                    modifier = Modifier.fillMaxWidth(),
                    key = "password",
                    value = item.password,
                    editable = true,
                    keyboardType = KeyboardType.Password
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewWifiRow() = preview {
    WifiRow(
        item = WifiNetwork(),
        expandedState = mutableStateOf(false)
    )
}

@Preview
@Composable
fun WifiRowPreview2() = preview {
    WifiRow(
        item = WifiNetwork(),
        expandedState = mutableStateOf(true)
    )
}