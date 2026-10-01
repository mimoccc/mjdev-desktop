package org.mjdev.desktop.managers.plugins

import androidx.compose.runtime.mutableStateListOf
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.data.DesktopConfigStore
import org.mjdev.desktop.extensions.PathExt.exists
import org.mjdev.desktop.extensions.PathExt.mkdirs
import org.mjdev.desktop.extensions.PathExt.parentFile
import org.mjdev.desktop.extensions.PathExt.text
import org.mjdev.desktop.extensions.PathExt.writeText
import org.mjdev.desktop.helpers.generic.JsonHelper
import org.mjdev.desktop.helpers.generic.JsonHelper.toJson
import org.mjdev.desktop.log.Log
import org.mjdev.desktop.plugins.IDesktopPlugin
import org.mjdev.desktop.plugins.MemoryWidgetPlugin
import org.mjdev.desktop.plugins.PluginDefaults
import org.mjdev.desktop.plugins.PluginInfo
import java.io.File
import java.net.URLClassLoader
import java.util.ServiceLoader

/**
 * Desktop (JVM) [IPluginManager]. External plugins are jars in `~/.mjdev/plugins/`; a jar is only
 * opened as a class loader once the user enables its plugin, and the loader is closed again on
 * disable. Plugins run with the desktop's own permissions, so only enable jars you trust.
 */
class PluginManager(
    private val context: IDesktopContext,
) : IPluginManager {
    private val homeDir: String
        get() = context.currentUser.homeDir.toString()

    private val stateFile =
        context.currentUser.homeDir
            .resolve(DesktopConfigStore.CONFIG_DIR_NAME)
            .resolve(DesktopConfigStore.CONFIG_SUBDIR_NAME)
            .resolve(PluginDefaults.STATE_FILE_NAME)

    override val pluginsDir: String
        get() = File(homeDir, "${DesktopConfigStore.CONFIG_DIR_NAME}/${PluginDefaults.PLUGINS_DIR_NAME}").path

    private val state: PluginState = loadState()

    private val builtIn = MemoryWidgetPlugin()

    private val jars = mutableMapOf<String, File>()
    private val loaders = mutableMapOf<String, URLClassLoader>()
    private val instances = mutableMapOf<String, IDesktopPlugin>()

    private val pluginList = mutableStateListOf<PluginInfo>()

    override val plugins: List<PluginInfo>
        get() = pluginList

    init {
        refresh()
    }

    override fun refresh() {
        jars.clear()
        val found = mutableListOf(builtInInfo())
        scanJars().forEach { jar ->
            val metadata = PluginJarReader.readMetadata(jar)
            when {
                metadata == null -> Log.d("Skipping ${jar.name}: no valid ${PluginDefaults.METADATA_ENTRY}")
                found.any { it.id == metadata.id } -> Log.d("Skipping ${jar.name}: duplicate id ${metadata.id}")
                else -> {
                    jars[metadata.id] = jar
                    found.add(
                        PluginInfo(
                            metadata = metadata,
                            enabled = metadata.id in state.enabled,
                            icon = PluginJarReader.readIcon(jar, metadata),
                            source = jar.path,
                            error = pluginList.firstOrNull { it.id == metadata.id }?.error,
                        ),
                    )
                }
            }
        }
        pluginList.clear()
        pluginList.addAll(found)
    }

    override fun setEnabled(
        id: String,
        enabled: Boolean,
    ) {
        val index = pluginList.indexOfFirst { it.id == id }
        if (index < 0) return
        if (!enabled) {
            state.enabled.remove(id)
        } else if (id !in state.enabled) {
            state.enabled.add(id)
        }
        saveState()
        if (!enabled) unload(id)
        pluginList[index] = pluginList[index].copy(enabled = enabled, error = null)
        if (enabled) instance(id)
    }

    override fun instance(id: String): IDesktopPlugin? {
        val info = pluginList.firstOrNull { it.id == id }
        return when {
            info == null || !info.enabled -> null
            instances.containsKey(id) -> instances[id]
            info.builtIn -> builtIn.also { instances[id] = it; it.onEnabled() }
            else -> load(id)
        }
    }

    private fun load(id: String): IDesktopPlugin? = runCatching {
        val jar = jars[id] ?: return null
        val loader = URLClassLoader(arrayOf(jar.toURI().toURL()), IDesktopPlugin::class.java.classLoader)
        loaders[id] = loader
        ServiceLoader
            .load(IDesktopPlugin::class.java, loader)
            .first()
            .also { plugin ->
                instances[id] = plugin
                plugin.onEnabled()
                Log.i("Plugin $id loaded from ${jar.name}")
            }
    }.onFailure { e ->
        // LinkageError and friends are Errors, but a broken plugin must never take the desktop down.
        Log.e(e)
        unload(id)
        markError(id, e.message ?: e.javaClass.simpleName)
    }.getOrNull()

    private fun unload(id: String) {
        instances.remove(id)?.let { runCatching { it.onDisabled() }.onFailure { e -> Log.e(e) } }
        loaders.remove(id)?.let { runCatching { it.close() } }
    }

    private fun markError(
        id: String,
        message: String,
    ) {
        val index = pluginList.indexOfFirst { it.id == id }
        if (index >= 0) pluginList[index] = pluginList[index].copy(error = message)
    }

    private fun builtInInfo() = PluginInfo(
        metadata = MemoryWidgetPlugin.METADATA,
        enabled = MemoryWidgetPlugin.ID in state.enabled,
        builtIn = true,
        source = BUILT_IN_SOURCE,
    )

    private fun scanJars(): List<File> {
        val dir = File(pluginsDir)
        if (!dir.exists()) dir.mkdirs()
        return dir
            .listFiles { file -> file.isFile && file.extension.equals(PluginDefaults.JAR_EXTENSION, true) }
            ?.sortedBy { it.name }
            .orEmpty()
    }

    private fun loadState(): PluginState = runCatching {
        if (stateFile.exists) JsonHelper.fromJson<PluginState>(stateFile.text) else null
    }.onFailure { e ->
        Log.e(e)
    }.getOrNull() ?: PluginState()

    private fun saveState() {
        runCatching {
            stateFile.parentFile.mkdirs()
            stateFile.writeText(state.toJson())
        }.onFailure { e ->
            Log.e(e)
        }
    }

    companion object {
        /** Source label shown for plugins that ship inside the desktop. */
        private const val BUILT_IN_SOURCE = "built-in"
    }
}
