package org.psippr.desktop.components.adb

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
import androidx.compose.ui.tooling.preview.Preview
import com.sun.jna.Platform.isAndroid
import dadb.Dadb
import dadb.adbserver.AdbServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.psippr.desktop.components.adb.DeviceState.Companion.rememberDeviceState
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.context.LocalDesktopContext
import org.psippr.desktop.extensions.decodeImage

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
            allowedIds = LocalDesktopContext.current.devicesManager::allowedIds,
        ),
    // the device screen appeared (true) or went away
    onShownChange: (Boolean) -> Unit = {},
) = withDesktopContext {
    if (isAndroid()) return@withDesktopContext
    val image = deviceState.imageBitmap
    val shown = image != null
    LaunchedEffect(shown) { onShownChange(shown) }
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

/**
 * Mirrors the screen of an allowed adb device.
 *
 * @param allowedIds ids of the devices the user allowed (Connect on the question, or remembered
 * and not forgotten); only their adb serials are mirrored. Empty by default: nothing is mirrored
 * unless the caller says which devices are allowed.
 */
class DeviceState(
    private val scope: CoroutineScope,
    private val refreshInterval: Long,
    private val reconnectInterval: Long = 1000L,
    private val allowedIds: () -> List<String> = { emptyList() },
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
                // round robin over the allowed serials, so an unplugged remembered device does not
                // block the next one
                var attempt = 0
                while (isActive) {
                    // old: connected to any device, even a dismissed or forgotten one
//                    val dadb =
//                        try {
//                            Dadb.discover()
//                        } catch (e: Exception) {
//                            error = e.message
//                            null
//                        }
                    val serials = allowedSerials()
                    val serial = serials.getOrNull(attempt++ % serials.size.coerceAtLeast(1))
                    val dadb =
                        try {
                            serial?.let(::connect)
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
                        // stops at once when the device is forgotten or no longer allowed
                        while (isActive && serial in allowedSerials()) {
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

    /** Adb serials of the allowed devices (ids of other providers are skipped). */
    private fun allowedSerials(): List<String> = allowedIds()
        .filter { it.startsWith(ADB_ID_PREFIX) }
        .map { it.removePrefix(ADB_ID_PREFIX) }

    /** Connects through the adb server to the device with [serial] only. */
    private fun connect(serial: String): Dadb = AdbServer.createDadb(deviceQuery = TRANSPORT_QUERY + serial)

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
        /** Prefix of the ids the adb devices provider gives its devices, followed by the serial. */
        private const val ADB_ID_PREFIX = "adb:"

        /** Adb server query selecting the device whose serial follows. */
        private const val TRANSPORT_QUERY = "host:transport:"

        // old: without the allowed devices
//        @Composable
//        fun rememberDeviceState(
//            refreshInterval: Long = 200L,
//            scope: CoroutineScope = rememberCoroutineScope(),
//        ) = remember {
//            DeviceState(scope, refreshInterval)
//        }

        /** Remembers a mirror state that mirrors only the devices of [allowedIds]. */
        @Composable
        fun rememberDeviceState(
            refreshInterval: Long = 200L,
            scope: CoroutineScope = rememberCoroutineScope(),
            allowedIds: () -> List<String> = { emptyList() },
        ) = remember {
            DeviceState(scope, refreshInterval, allowedIds = allowedIds)
        }
    }
}
