package org.psippr.desktop.data.widgets

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression tests of "widgets do not remember their positions on relaunch". */
class WidgetPositionStoreTest {
    private fun tempFile() = Files.createTempDirectory(TEMP_PREFIX).resolve(WidgetPositionStore.FILE_NAME)

    @Test
    fun savedPositionIsRestoredAfterRelaunch() {
        val file = tempFile().toString().toPath()
        runBlocking { WidgetPositionStore(file).save(KEY, SAVED).join() }
        assertEquals(SAVED, WidgetPositionStore(file).position(KEY))
    }

    @Test
    fun laterSaveWins() {
        val file = tempFile().toString().toPath()
        val store = WidgetPositionStore(file)
        runBlocking {
            store.save(KEY, Offset.Zero)
            store.save(KEY, SAVED).join()
        }
        assertEquals(SAVED, WidgetPositionStore(file).position(KEY))
    }

    @Test
    fun unknownWidgetStaysAtDefaultPlace() {
        assertEquals(Offset.Zero, WidgetPositionStore(tempFile().toString().toPath()).position(KEY))
    }

    @Test
    fun corruptOrNullFileGivesDefaults() {
        listOf(CORRUPT_JSON, NULL_POSITIONS_JSON).forEach { content ->
            val file = tempFile()
            Files.writeString(file, content)
            assertEquals(Offset.Zero, WidgetPositionStore(file.toString().toPath()).position(KEY))
        }
    }

    private companion object {
        /** Prefix of the temporary test directories. */
        const val TEMP_PREFIX = "psippr-widgets"

        /** Sample widget key and position. */
        val KEY = WidgetKey.Plugin.of("clock")
        val SAVED = Offset(-120f, 48.5f)

        /** Broken file contents. */
        const val CORRUPT_JSON = "{ not json"
        const val NULL_POSITIONS_JSON = "{ \"positions\": null }"
    }
}
