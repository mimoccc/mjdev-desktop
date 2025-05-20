package org.mjdev.desktop.extensions

import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.mjdev.desktop.BuildConfig
import org.mjdev.desktop.activity.MainActivity.Companion.TAG
import org.mjdev.desktop.helpers.WakeLockHelper
import androidx.core.graphics.drawable.toDrawable

@Suppress("unused", "MemberVisibilityCanBePrivate")
object ComponentActivityExt {

    const val WAKE_LOCK_TAG = 1

    val TRANSPARENT : Int = Color.Transparent.toArgb()

    val ComponentActivity.decorView
        get() = window.decorView

    val ComponentActivity.wakeLockHelper: WakeLockHelper
        get() = decorView.getTag(WAKE_LOCK_TAG) as? WakeLockHelper ?: WakeLockHelper(this)

    fun ComponentActivity.acquireWakeLock() {
        wakeLockHelper.acquireWakeLock()
    }

    fun ComponentActivity.releaseWakeLock() {
        wakeLockHelper.releaseWakeLock()
    }

    fun ComponentActivity.hideBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun ComponentActivity.showBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            show(WindowInsetsCompat.Type.statusBars())
            show(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
    }

    fun ComponentActivity.setKeepScreenOn() = with(window) {
        addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun ComponentActivity.resetKeepScreenOn() = with(window) {
        clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // todo : black is white ?
    fun ComponentActivity.setBackgroundColor(
        color: Color = Color.Black
    ) {
        decorView.background = color.toArgb().toDrawable()
    }

    @Composable
    fun OnBackPress(
        enabled: Boolean = !BuildConfig.DEBUG,
        action: () -> Unit = {
            Log.d(TAG, "OnBackPressed")
        }
    ) = BackHandler(enabled, action)
}