package eu.mjdev.desktop.gtk.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import eu.mjdev.desktop.gtk.GtkApplier
import eu.mjdev.desktop.gtk.GtkComposeNode
import eu.mjdev.desktop.gtk.LeafComposeNode
import eu.mjdev.desktop.gtk.modifier.Modifier
import org.gnome.gtk.Orientation
import org.gnome.gtk.Separator

@Suppress("unused")
@Composable
fun VerticalSeparator(
    modifier: Modifier = Modifier,
) {
    Separator(modifier, Orientation.VERTICAL)
}

@Suppress("unused")
@Composable
fun HorizontalSeparator(
    modifier: Modifier = Modifier,
) {
    Separator(modifier, Orientation.HORIZONTAL)
}

@Composable
fun Separator(
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.HORIZONTAL,
) {
    ComposeNode<GtkComposeNode<Separator>, GtkApplier>({
        LeafComposeNode(Separator.builder().build())
    }) {
        set(modifier) { applyModifier(it) }
        set(orientation) { gObject.orientation = it }
    }
}