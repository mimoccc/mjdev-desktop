package org.mjdev.desktop.plugins.music

import androidx.compose.runtime.Composable
import org.mjdev.desktop.components.desktop.widgets.music.MusicPlayerWidget
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.plugins.IDesktopPlugin
import org.mjdev.desktop.plugins.PluginMetadata
import org.mjdev.desktop.plugins.remote.RemoteAlign
import org.mjdev.desktop.plugins.remote.RemoteDocument
import org.mjdev.desktop.plugins.remote.RemoteVariables

/**
 * Built-in music player widget: plays the files of the user's music folder. A document cannot
 * hold buttons, so the plugin draws its own [content]; the empty [document] only carries the size
 * and the place on the desktop.
 */
class MusicWidgetPlugin : IDesktopPlugin {
    override val document: RemoteDocument =
        RemoteDocument(
            width = MusicDefaults.WIDTH_DP,
            height = MusicDefaults.HEIGHT_DP,
            anchor = RemoteAlign.BOTTOM_START.name,
        )

    override val content: (@Composable (IDesktopContext) -> Unit) = { MusicPlayerWidget() }

    override fun variables(context: IDesktopContext): RemoteVariables = RemoteVariables()

    companion object {
        /** Stable id used to persist whether the widget is enabled. */
        const val ID = "builtin.music"

        /** Label of the plugin in the control center. */
        const val TITLE = "Music player"

        /** Description shown when the plugin row is expanded. */
        const val DESCRIPTION = "Plays the music in your Music folder directly on the desktop."

        /** Author shown in the plugin details. */
        const val AUTHOR = "mjdev"

        /** Version shown in the plugin details. */
        const val VERSION = "1.0"

        /** Metadata of the built-in plugin. */
        val METADATA =
            PluginMetadata(
                id = ID,
                name = TITLE,
                description = DESCRIPTION,
                version = VERSION,
                author = AUTHOR,
            )
    }
}
