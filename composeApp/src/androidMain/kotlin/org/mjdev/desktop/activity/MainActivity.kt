package org.mjdev.desktop.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.mjdev.desktop.main.MainView
import org.mjdev.desktop.context.DesktopContext.Companion.rememberDesktopContext
import org.mjdev.desktop.context.LocalDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.helpers.permission.rememberPermissionManager
import org.mjdev.desktop.helpers.theme.DesktopTheme
import org.mjdev.desktop.helpers.WakeLockHelper

@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {
    val wakeLockHelper by lazy { WakeLockHelper(this) }

//    @Suppress("OVERRIDE_DEPRECATION")
//    override fun onBackPressed() {
//        // Disable back button
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        wakeLockHelper.acquireWakeLock()
        installSplashScreen()
        setKeepScreenOn()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            BackHandler(true) {
                Log.d(TAG, "OnBackPressed")
            }
            rememberPermissionManager()
            CompositionLocalProvider(
                LocalDesktopContext provides rememberDesktopContext(baseContext)
            ) {
                ActivityMain()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        resetKeepScreenOn()
        wakeLockHelper.releaseWakeLock()
    }

    private fun ComponentActivity.setKeepScreenOn() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun ComponentActivity.resetKeepScreenOn() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    companion object {
        private val TAG = MainActivity::class.simpleName
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ActivityMain() {
    DesktopTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                    )
            ) {
                MainView()
            }
        }
    }
}

@Preview
@Composable
fun MainActivityPreview() = preview {
    ActivityMain()
}