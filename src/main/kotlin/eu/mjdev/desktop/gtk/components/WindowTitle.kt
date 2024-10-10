package eu.mjdev.desktop.gtk.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import eu.mjdev.desktop.gtk.GtkApplier
import eu.mjdev.desktop.gtk.GtkComposeNode
import eu.mjdev.desktop.gtk.LeafComposeNode
import eu.mjdev.desktop.gtk.modifier.Modifier
import org.gnome.adw.WindowTitle

@Suppress("unused")
@Composable
fun WindowTitle(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    ComposeNode<GtkComposeNode<WindowTitle>, GtkApplier>({
        LeafComposeNode(WindowTitle.builder().build())
    }) {
        set(modifier) { applyModifier(it) }
        set(title) { this.gObject.title = it }
        set(subtitle) { this.gObject.subtitle = it }
    }
}