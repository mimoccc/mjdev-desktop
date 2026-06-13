package org.mjdev.desktop.components.adb

import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import dadb.Dadb
import androidx.compose.ui.Modifier
import com.sun.jna.Platform.isAndroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.adb.DeviceState.Companion.rememberDeviceState
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.decodeImage

@Preview
@Composable
fun AdbScreenMirror(
    modifier: Modifier = Modifier,
    visibilityState: MutableState<Boolean> = mutableStateOf(false),
    scope : CoroutineScope = rememberCoroutineScope(),
    refreshInterval : Long  = 100L,
    deviceState : DeviceState = rememberDeviceState(
        refreshInterval = refreshInterval,
        scope = scope
    )
)  = withDesktopContext {
    if(isAndroid()) return@withDesktopContext
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            deviceState.isConnecting -> CircularProgressIndicator()
            deviceState.error != null -> Text("Error: ${deviceState.error}")
            deviceState.imageBitmap != null -> {
                val currentImage = deviceState.imageBitmap!!
                Image(
                    bitmap = currentImage,
                    contentDescription = "Device screen",
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(deviceState.dadbInstance) {
                            detectTapGestures { offset ->
                                deviceState.tapOnDevice(
                                    offset.x.toInt(),
                                    offset.y.toInt(),
                                    size.width,
                                    size.height
                                )
                            }
                        }
                )
            }

            else -> Text("Waiting for device...")
        }
    }
    DisposableEffect(visibilityState.value) {
        deviceState.start()
        onDispose {
            deviceState.stop()
        }
    }
}

class DeviceState(
    private val scope:CoroutineScope,
    private val refreshInterval: Long,
) {
    var imageBitmap by mutableStateOf<ImageBitmap?>(null)
    var error by mutableStateOf<String?>(null)
    var isConnecting by mutableStateOf(true)
    var dadbInstance by mutableStateOf<Dadb?>(null)
    var screenWidth by mutableIntStateOf(0)
    var screenHeight by mutableIntStateOf(0)
    private var job: Job? = null

    fun start() {
        job = scope.launch {
            try {
                val dadb = Dadb.discover()
                if (dadb == null) {
                    error = "No device found"
                    isConnecting = false
                    return@launch
                }
                dadbInstance = dadb
                isConnecting = false
                while (job?.isActive == true) {
                    try {
                        val stream = dadb.open("exec:screencap -p")
                        val bytes = stream.source.readByteArray()
                        stream.close()
                        if (bytes.isNotEmpty()) {
                            val decoded = bytes.decodeImage()
                            if (decoded != null) {
                                imageBitmap = decoded.bitmap
                                screenWidth = decoded.width
                                screenHeight = decoded.height
                            }
                        }
                    } catch (e: Exception) {
                        error = e.message
                    }
                    delay(refreshInterval)
                }
            } catch (e: Exception) {
                error = e.message
                isConnecting = false
            }
        }
    }

    fun stop() {
        job?.cancel()
        dadbInstance?.close()
    }

    fun tapOnDevice(
        offsetX: Int,
        offsetY: Int,
        sizeWidth: Int,
        sizeHeight: Int
    ) {
        dadbInstance?.let { dadb ->
            scope.launch {
                try {
                    val scaleX = screenWidth.toFloat() / sizeWidth
                    val scaleY = screenHeight.toFloat() / sizeHeight
                    val deviceX = (offsetX * scaleX).toInt()
                    val deviceY = (offsetY * scaleY).toInt()
                    dadb.shell("input tap $deviceX $deviceY")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        @Composable
        fun rememberDeviceState(
            refreshInterval : Long  = 100L,
            scope : CoroutineScope = rememberCoroutineScope()
        ) = remember {
            DeviceState(scope,  refreshInterval)
        }
    }
}
