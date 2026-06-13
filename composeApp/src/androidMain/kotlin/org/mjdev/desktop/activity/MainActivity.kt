package org.mjdev.desktop.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.mjdev.desktop.context.DesktopContext.Companion.rememberDesktopContext
import org.mjdev.desktop.context.LocalDesktopContext
import org.mjdev.desktop.extensions.ComponentActivityExt.OnBackPress
import org.mjdev.desktop.extensions.ComponentActivityExt.TRANSPARENT
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.helpers.permission.rememberPermissionManager
import org.mjdev.desktop.helpers.theme.DesktopTheme
import org.mjdev.desktop.main.MainView

@Suppress("unused", "DEPRECATION")
@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {
//    val sensors = listOf(
//        SensorType.ACCELEROMETER,
//        SensorType.GYROSCOPE,
//        SensorType.MAGNETOMETER,
//        SensorType.BAROMETER,
//        SensorType.STEP_COUNTER,
//        SensorType.LOCATION
//    )

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        runCatching {
            window.statusBarColor = Black.toArgb()
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            window.navigationBarColor = Black.toArgb()
        }.onFailure { e ->
            e.printStackTrace()
        }
        window.decorView.setBackgroundColor(Black.toArgb())
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(TRANSPARENT),
        )
        setContent {
            CompositionLocalProvider(
                LocalDesktopContext provides rememberDesktopContext(this@MainActivity),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .systemBarsPadding(),
                ) {
                    OnBackPress()
                    rememberPermissionManager()
                    ActivityMain()
                }
            }
        }
    }

//    fun enableSensors() {
//        runBlocking {
//            KSensor.registerSensors(
//                types = sensors,
//                locationIntervalMillis = 1000L
//            ).collect { sensorUpdate ->
//                when (sensorUpdate) {
//                    is SensorUpdate.Data -> Log.d(TAG, sensorUpdate.data.toString())
//                    is SensorUpdate.Error -> Log.d(TAG, sensorUpdate.exception.toString())
//                }
//            }
//        }
//    }

//    fun disableSensors() {
//        KSensor.unregisterSensors(sensors)
//    }

    companion object {
        val TAG = MainActivity::class.simpleName
    }
}

@Composable
private fun ActivityMain() {
    DesktopTheme {
        Scaffold(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black),
        ) { paddingValues ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .consumeWindowInsets(paddingValues)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .background(Color.Black),
            ) {
                MainView()
            }
        }
    }
}

@Preview
@Composable
fun PreviewMainActivity() =
    preview {
        ActivityMain()
    }
