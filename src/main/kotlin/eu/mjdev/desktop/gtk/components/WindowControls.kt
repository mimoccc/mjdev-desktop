package eu.mjdev.desktop.gtk.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import eu.mjdev.desktop.gtk.GtkApplier
import eu.mjdev.desktop.gtk.GtkComposeNode
import eu.mjdev.desktop.gtk.LeafComposeNode
import eu.mjdev.desktop.gtk.modifier.Modifier
import org.gnome.gtk.PackType
import org.gnome.gtk.WindowControls

@Suppress("unused")
@Composable
fun WindowControls(
    modifier: Modifier = Modifier,
    side: PackType = PackType.START,
) {
    ComposeNode<GtkComposeNode<WindowControls>, GtkApplier>({
        LeafComposeNode(WindowControls.builder().build())
    }) {
        set(modifier) { applyModifier(it) }
        set(side) { gObject.side = it }
    }
}