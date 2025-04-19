package org.mjdev.desktop.context

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.mjdev.desktop.data.Category
import org.mjdev.desktop.data.User
import org.mjdev.desktop.extensions.Compose.isDesign
import org.mjdev.desktop.helpers.image.ImageLoader.asyncImageLoader
import org.mjdev.desktop.interfaces.IApp
import org.mjdev.desktop.interfaces.IDesktopContext
import org.mjdev.desktop.interfaces.ILocale
import org.mjdev.desktop.interfaces.ITheme
import org.mjdev.desktop.interfaces.IUser
import org.mjdev.desktop.log.Log
import org.mjdev.desktop.managers.base.IDelegate
import kotlin.reflect.KClass

@Suppress("unused")
class DesktopContext(
    override val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    override val imageLoader: ImageLoader? = null,
    val isInDesign: Boolean = false,
    override val platformContext: PlatformContext? = null
) : IDesktopContext() {

    override var isFirstStart: Boolean = false
    override var isInstalled: Boolean = false
    override val machineName: String = ""
    override val appArgs: List<String> = emptyList()
    override val currentUser: IUser = allUsers.firstOrNull { u -> u.isLoggedIn } ?: User.DEFAULT
    override val currentLocale: ILocale = ILocale.DEFAULT
    override val theme: ITheme = ITheme.DEFAULT
    override val appCategories: List<Category> = emptyList()
    override val allApps: List<IApp> = emptyList()
    override val favoriteApps: List<IApp> = emptyList()
    override val isLandscapeMode: Boolean = true

    override val containerSize: DpSize = DpSize(0.dp, 0.dp) // todo

    val allUsers
        get() = runCatching {
            User.allUsers(this)
        }.getOrNull() ?: emptyList<User>()

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

    override suspend fun login(uName: String, uPasswd: String): Boolean {
        return false
    }

    override suspend fun logout(): Boolean {
        return false
    }

    override fun dispose() {
    }

    override fun createManager(cls: KClass<*>): IDelegate?  = null

//    override fun createManager(cls: KClass<*>): IDelegate = when (cls) {
//        IOSManager::class -> OsManager(this)
//        IPalette::class -> Palette(this)
//        ITranslator::class -> Translator(this)
//        IConnectivityManager::class -> ConnectivityManager(this)
//        IAiManager::class -> AiManager(this).apply {
//             todo user can configure
//            pluginAI = AiPluginGemini(this@DesktopContext)
//            pluginTTS = TTSPluginMain(this@DesktopContext)
//            pluginSTT = STTPluginEmpty(this@DesktopContext)
//        }

//        IAppsManager::class -> AppsManager(this)
//        IThemeManager::class -> ThemeManager(this)
//        IProcessManager::class -> ProcessManager(this)
//        else -> cls.companionObject?.members?.first {
//            it.name == "EMPTY"
//        }?.call() as IDelegate
//    }

    companion object {
        @Composable
        fun rememberDesktopContext(
            imageLoader: ImageLoader = asyncImageLoader(),
            platformContext: PlatformContext = LocalPlatformContext.current,
            scope: CoroutineScope = rememberCoroutineScope(),
            isInDesign: Boolean = isDesign,
        ) = remember {
            Log.i("default initialized desktop provider created")
            DesktopContext(
                scope = scope,
                imageLoader = imageLoader,
                isInDesign = isInDesign,
                platformContext = platformContext
            )
        }
    }
}