package org.psippr.desktop.data.widgets

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okio.Path
import org.psippr.desktop.data.DesktopConfigStore
import org.psippr.desktop.extensions.PathExt.exists
import org.psippr.desktop.extensions.PathExt.mkdirs
import org.psippr.desktop.extensions.PathExt.parentFile
import org.psippr.desktop.extensions.PathExt.text
import org.psippr.desktop.extensions.PathExt.writeText
import org.psippr.desktop.helpers.generic.JsonHelper.fromJson
import org.psippr.desktop.helpers.generic.JsonHelper.toJson
import org.psippr.desktop.interfaces.IUser
import org.psippr.desktop.log.Log

/**
 * Remembers where the user dragged the desktop widgets, in its own file
 * (`~/.psippr-desktop/desktop/widgets.json`), so the control-center Save of the desktop config can never
 * overwrite it. Reads once, keeps the positions in memory, writes in the background.
 *
 * @param file the JSON file
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WidgetPositionStore(
    val file: Path,
) {
    // one writer at a time, in call order, so an older snapshot never overwrites a newer one
    private val writer = CoroutineScope(Dispatchers.Default.limitedParallelism(1))

    private val positions: MutableMap<String, WidgetPosition> = read().positions

    /** Saved position of the widget [key], or [Offset.Zero] (its default place) when none. */
    fun position(key: String): Offset = positions[key]?.toOffset() ?: Offset.Zero

    /** Remembers [offset] for the widget [key] and writes all positions to the file. */
    fun save(
        key: String,
        offset: Offset,
    ): Job {
        positions[key] = WidgetPosition.of(offset)
        val snapshot = WidgetPositionsData(positions.toMutableMap())
        return writer.launch { write(snapshot) }
    }

    // a missing, corrupt or null-containing file (Gson ignores Kotlin nullability) gives defaults;
    // copying the map throws inside runCatching when Gson left it null
    private fun read(): WidgetPositionsData = runCatching {
        if (file.exists) {
            // nullable: Gson returns null for an empty file
            val data: WidgetPositionsData? = fromJson(file.text)
            data?.let { WidgetPositionsData(it.positions.toMutableMap()) }
        } else {
            null
        }
    }.onFailure { e ->
        Log.e(e)
    }.getOrNull() ?: WidgetPositionsData()

    private fun write(data: WidgetPositionsData) = runCatching {
        file.parentFile.mkdirs()
        file.writeText(data.toJson())
    }.onFailure { e ->
        Log.e(e)
    }

    companion object {
        /** Name of the widget positions file, next to the desktop config file. */
        const val FILE_NAME = "widgets.json"

        private val cache = mutableMapOf<String, WidgetPositionStore>()

        /** Store of the given [user] (one instance per user). */
        fun forUser(user: IUser): WidgetPositionStore = cache.getOrPut(user.userName) {
            WidgetPositionStore(
                user.homeDir
                    .resolve(DesktopConfigStore.CONFIG_DIR_NAME)
                    .resolve(DesktopConfigStore.CONFIG_SUBDIR_NAME)
                    .resolve(FILE_NAME),
            )
        }
    }
}
