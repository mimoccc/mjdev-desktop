package eu.mjdev.desktop.gtk.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import eu.mjdev.desktop.gtk.GtkApplier
import eu.mjdev.desktop.gtk.GtkComposeNode
import eu.mjdev.desktop.gtk.LeafComposeNode
import eu.mjdev.desktop.gtk.modifier.Modifier
import org.gnome.gtk.Spinner

@Suppress("unused")
@Composable
fun Spinner(
    spinning: Boolean = false,
    modifier: Modifier = Modifier,
) {
    ComposeNode<GtkComposeNode<Spinner>, GtkApplier>({
        LeafComposeNode(Spinner.builder().build())
    }) {
        set(modifier) { applyModifier(it) }
        set(spinning) { this.gObject.spinning = it }
    }
}