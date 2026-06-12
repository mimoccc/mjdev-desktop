package org.mjdev.desktop.context

import android.annotation.SuppressLint
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.app.ComponentActivity
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import org.mjdev.desktop.R
import org.mjdev.desktop.data.Category
import org.mjdev.desktop.data.User
import org.mjdev.desktop.extensions.Compose.isDesign
import org.mjdev.desktop.helpers.image.ImageLoader.asyncImageLoader
import org.mjdev.desktop.interfaces.IApp
import org.mjdev.desktop.managers.apps.IAppsManager
import org.mjdev.desktop.managers.connectivity.IConnectivityManager
import org.mjdev.desktop.interfaces.ILocale
import org.mjdev.desktop.managers.palette.IPalette
import org.mjdev.desktop.managers.process.IProcessManager
import org.mjdev.desktop.interfaces.ITheme
import org.mjdev.desktop.interfaces.IUser
import org.mjdev.desktop.managers.language.Translator
import org.mjdev.desktop.log.Log
import org.mjdev.desktop.managers.ai.AiManager
import org.mjdev.desktop.managers.ai.IAiManager
import org.mjdev.desktop.managers.os.IOSManager
import org.mjdev.desktop.managers.theme.IThemeManager
import org.mjdev.desktop.managers.translations.ITranslator
import org.mjdev.desktop.managers.ai.plugins.AiPluginGemini
import org.mjdev.desktop.managers.ai.plugins.AiPluginOpenAi
import org.mjdev.desktop.managers.ai.stt.STTPluginEmpty
import org.mjdev.desktop.managers.ai.tts.TTSPluginAndroid
import org.mjdev.desktop.managers.base.IDelegate
import org.mjdev.desktop.managers.connectivity.ConnectivityManager
import org.mjdev.desktop.managers.os.OsManager
import org.mjdev.desktop.managers.processes.ProcessManager
import kotlin.reflect.KClass
import org.mjdev.desktop.managers.apps.AppsManager
import org.mjdev.desktop.managers.keys.IKeyManager
import org.mjdev.desktop.managers.keys.KeysManager
import org.mjdev.desktop.managers.palette.Palette
import org.mjdev.desktop.managers.theme.ThemeManager
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance

@Suppress("unused", "MemberVisibilityCanBePrivate")
class DesktopContext(
    @SuppressLint("RestrictedApi")
    val context: ComponentActivity? = null,
    override val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    override val imageLoader: ImageLoader? = null,
    val isInDesign: Boolean = false,
    override val platformContext: PlatformContext? = null
) : IDesktopContext() {

    val androidContext
        get() = context

    // todo
    override var isFirstStart: Boolean = false

    override var isInstalled: Boolean = true

    // todo
    override val machineName: String = ""

    // todo
    override val appArgs: List<String> = emptyList()

    // todo
    override val currentUser: IUser = allUsers.firstOrNull { u -> u.isLoggedIn } ?: User.DEFAULT

    // todo
    override val currentLocale: ILocale = ILocale.DEFAULT

    override val theme: ITheme = ITheme.DEFAULT

    override val appCategories: List<Category>
        get() = appsManager.categories

    override val allApps: List<IApp>
        get() = appsManager.allApps

    override val favoriteApps: List<IApp>
        get() = appsManager.favoriteApps

    override val containerSize: DpSize = androidContext?.resources
        ?.displayMetrics
        ?.let { dm ->
            DpSize(dm.widthPixels.dp, dm.heightPixels.dp)
        } ?: DpSize(1024.dp, 640.dp)

    val allUsers
        get() = runCatching {
            User.allUsers(this)
        }.getOrNull() ?: emptyList()

    override suspend fun logOut() {
    }

    override suspend fun restart() {
    }

    override suspend fun suspend() {
    }

    override suspend fun shutdown() {
    }

    override suspend fun open(what: Any?) {
    }

    /**
     * fingerprint or device credential (pin/pattern/password) via the
     * framework biometric prompt - the password argument is unused,
     * the system dialog collects the credential itself
     */
    override suspend fun login(uName: String, uPasswd: String): Boolean {
        val activity = context ?: return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            // no biometric prompt api below 28 - nothing to verify against
            return true
        }
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { cont ->
                val executor = ContextCompat.getMainExecutor(activity)
                val builder = BiometricPrompt.Builder(activity)
                    .setTitle(activity.getString(R.string.app_name))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    builder.setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_WEAK or
                                BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                } else {
                    @Suppress("DEPRECATION")
                    builder.setDeviceCredentialAllowed(true)
                }
                val signal = CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                builder.build().authenticate(
                    signal,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult?
                        ) {
                            if (cont.isActive) {
                                cont.resume(true)
                            }
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence?
                        ) {
                            if (cont.isActive) {
                                cont.resume(false)
                            }
                        }
                    }
                )
            }
        }
    }

    override suspend fun logout(): Boolean {
        authenticatedState.value = false
        return true
    }

    override fun dispose() {
    }

    override fun createManager(cls: KClass<*>): IDelegate = when (cls) {
        IOSManager::class -> OsManager(this)
        IPalette::class -> Palette(this)
        ITranslator::class -> Translator(this)
        IConnectivityManager::class -> ConnectivityManager(this)
        IAiManager::class -> AiManager(
            context = this,
            // todo user can configure
            pluginAI = AiPluginOpenAi(this@DesktopContext),
            pluginTTS = TTSPluginAndroid(this@DesktopContext),
            pluginSTT = STTPluginEmpty(this@DesktopContext)
        )

        IAppsManager::class -> AppsManager(this)
        IThemeManager::class -> ThemeManager(this)
        IProcessManager::class -> ProcessManager(this)
        IKeyManager::class -> KeysManager(this)
        else -> cls.companionObject?.members?.first {
            it.name == "EMPTY"
        }?.call(cls.companionObjectInstance) as IDelegate
    }

    companion object {
        @Composable
        fun rememberDesktopContext(
            @SuppressLint("RestrictedApi")
            context: ComponentActivity,
            imageLoader: ImageLoader = asyncImageLoader(),
            platformContext: PlatformContext = LocalPlatformContext.current,
            scope: CoroutineScope = rememberCoroutineScope(),
            isInDesign: Boolean = isDesign,
        ) = remember {
            Log.i("default initialized desktop provider created")
            DesktopContext(
                context = context,
                scope = scope,
                imageLoader = imageLoader,
                isInDesign = isInDesign,
                platformContext = platformContext
            )
        }
    }
}