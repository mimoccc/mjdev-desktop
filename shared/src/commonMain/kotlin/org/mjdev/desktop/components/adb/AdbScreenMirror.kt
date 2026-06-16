package org.mjdev.desktop.components.adb

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import com.sun.jna.Platform.isAndroid
import dadb.Dadb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
    scope: CoroutineScope = rememberCoroutineScope(),
    refreshInterval: Long = 200L,
    deviceState: DeviceState =
        rememberDeviceState(
            refreshInterval = refreshInterval,
            scope = scope,
        ),
) = withDesktopContext {
    if (isAndroid()) return@withDesktopContext
    val image = deviceState.imageBitmap
    if (image != null) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Image(
                bitmap = image,
                contentDescription = "Device screen",
                contentScale = ContentScale.Fit,
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .pointerInput(deviceState.dadbInstance) {
                            detectTapGestures { offset ->
                                deviceState.tapOnDevice(
                                    offset.x.toInt(),
                                    offset.y.toInt(),
                                    size.width,
                                    size.height,
                                )
                            }
                        },
            )
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
    private val scope: CoroutineScope,
    private val refreshInterval: Long,
    private val reconnectInterval: Long = 1000L,
) {
    var imageBitmap by mutableStateOf<ImageBitmap?>(null)
    var error by mutableStateOf<String?>(null)
    var isConnecting by mutableStateOf(true)
    var dadbInstance by mutableStateOf<Dadb?>(null)
    var screenWidth by mutableIntStateOf(0)
    var screenHeight by mutableIntStateOf(0)
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        // Blocking adb/socket I/O (discover, open, screencap read) MUST run off the UI/Default
        // dispatcher — on the Compose Main dispatcher it freezes the whole desktop.
        job =
            scope.launch(Dispatchers.IO) {
                while (isActive) {
                    val dadb =
                        try {
                            Dadb.discover()
                        } catch (e: Exception) {
                            error = e.message
                            null
                        }
                    if (dadb == null) {
                        dadbInstance = null
                        imageBitmap = null
                        isConnecting = false
                        delay(reconnectInterval)
                        continue
                    }
                    dadbInstance = dadb
                    isConnecting = false
                    error = null
                    try {
                        while (isActive) {
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
                            delay(refreshInterval)
                        }
                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        runCatching { dadb.close() }
                        dadbInstance = null
                        imageBitmap = null
                    }
                    delay(reconnectInterval)
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
        sizeHeight: Int,
    ) {
        dadbInstance?.let { dadb ->
            scope.launch(Dispatchers.IO) {
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
            refreshInterval: Long = 200L,
            scope: CoroutineScope = rememberCoroutineScope(),
        ) = remember {
            DeviceState(scope, refreshInterval)
        }
    }
}
