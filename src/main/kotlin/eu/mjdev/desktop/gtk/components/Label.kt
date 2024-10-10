package eu.mjdev.desktop.gtk.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import eu.mjdev.desktop.gtk.GtkApplier
import eu.mjdev.desktop.gtk.GtkComposeNode
import eu.mjdev.desktop.gtk.LeafComposeNode
import eu.mjdev.desktop.gtk.modifier.Modifier
import org.gnome.gtk.Label

// TODO: all other properties
@Suppress("unused")
@Composable
fun Label(
    text: String,
    modifier: Modifier = Modifier,
) {
    ComposeNode<GtkComposeNode<Label>, GtkApplier>({
        LeafComposeNode(Label.builder().build())
    }) {
        set(modifier) { applyModifier(it) }
        set(text) { this.gObject.text = it }
    }
}