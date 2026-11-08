package org.psippr.desktop.context

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.app.ComponentActivity
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.psippr.desktop.data.Category
import org.psippr.desktop.data.User
import org.psippr.desktop.extensions.Compose.isDesign
import org.psippr.desktop.helpers.image.ImageLoader.asyncImageLoader
import org.psippr.desktop.interfaces.IApp
import org.psippr.desktop.interfaces.ILocale
import org.psippr.desktop.interfaces.ITheme
import org.psippr.desktop.interfaces.IUser
import org.psippr.desktop.log.Log
import org.psippr.desktop.managers.ai.AiManager
import org.psippr.desktop.managers.ai.IAiManager
import org.psippr.desktop.managers.ai.plugins.AiPluginGemini
import org.psippr.desktop.managers.ai.plugins.AiPluginOpenAi
import org.psippr.desktop.managers.ai.stt.STTPluginEmpty
import org.psippr.desktop.managers.ai.tts.TTSPluginAndroid
import org.psippr.desktop.managers.apps.AppsManager
import org.psippr.desktop.managers.apps.IAppsManager
import org.psippr.desktop.managers.base.IDelegate
import org.psippr.desktop.managers.connectivity.ConnectivityManager
import org.psippr.desktop.managers.connectivity.IConnectivityManager
import org.psippr.desktop.managers.keys.IKeyManager
import org.psippr.desktop.managers.keys.KeysManager
import org.psippr.desktop.managers.language.Translator
import org.psippr.desktop.managers.os.IOSManager
import org.psippr.desktop.managers.os.OsManager
import org.psippr.desktop.managers.palette.IPalette
import org.psippr.desktop.managers.palette.Palette
import org.psippr.desktop.managers.plugins.IPluginManager
import org.psippr.desktop.managers.process.IProcessManager
import org.psippr.desktop.managers.processes.ProcessManager
import org.psippr.desktop.managers.theme.IThemeManager
import org.psippr.desktop.managers.theme.ThemeManager
import org.psippr.desktop.managers.translations.ITranslator
import org.psippr.desktop.managers.volume.IVolumeManager
import org.psippr.desktop.managers.volume.VolumeManager
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject

@Suppress("unused", "MemberVisibilityCanBePrivate")
class DesktopContext(
    @param:SuppressLint("RestrictedApi")
    val context: ComponentActivity? = null,
    override val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    override val imageLoader: ImageLoader? = null,
    val isInDesign: Boolean = false,
    override val platformContext: PlatformContext? = null,
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

    override val containerSize: DpSize =
        androidContext
            ?.resources
            ?.displayMetrics
            ?.let { dm ->
                DpSize(dm.widthPixels.dp, dm.heightPixels.dp)
            } ?: DpSize(1024.dp, 640.dp)

    val allUsers
        get() =
            runCatching {
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

    override suspend fun login(
        uName: String,
        uPasswd: String,
    ): Boolean = false

    override suspend fun logout(): Boolean = false

    override fun dispose() {
    }

    override fun createManager(cls: KClass<*>): IDelegate = when (cls) {
        IOSManager::class -> OsManager(this)
        IPalette::class -> Palette(this)
        ITranslator::class -> Translator(this)
        IConnectivityManager::class -> ConnectivityManager(this)
        IAiManager::class ->
            AiManager(
                context = this,
                // todo user can configure
                pluginAI = AiPluginOpenAi(this@DesktopContext),
                pluginTTS = TTSPluginAndroid(this@DesktopContext),
                pluginSTT = STTPluginEmpty(this@DesktopContext),
            )

        IAppsManager::class -> AppsManager(this)
        IThemeManager::class -> ThemeManager(this)
        IProcessManager::class -> ProcessManager(this)
        IKeyManager::class -> KeysManager(this)
        IVolumeManager::class -> VolumeManager(this)
        // Plugins are loaded from jars on the JVM desktop only.
        IPluginManager::class -> IPluginManager.EMPTY
        else ->
            cls.companionObject
                ?.members
                ?.first {
                    it.name == "EMPTY"
                }?.call() as IDelegate
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
                platformContext = platformContext,
            )
        }
    }
}
