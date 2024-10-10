package eu.mjdev.desktop.gtk.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import eu.mjdev.desktop.gtk.GtkApplier
import eu.mjdev.desktop.gtk.GtkComposeNode
import eu.mjdev.desktop.gtk.modifier.Modifier
import eu.mjdev.desktop.gtk.SingleChildComposeNode
import org.gnome.adw.Clamp
import org.gnome.gtk.Orientation

@Suppress("unused")
@Composable
fun VerticalClamp(
    modifier: Modifier = Modifier,
    maximumSize: Int = 600,
    tighteningThreshold: Int = 400,
    content: @Composable () -> Unit,
) {
    Clamp(modifier, Orientation.VERTICAL, maximumSize, tighteningThreshold, content)
}

@Suppress("unused")
@Composable
fun HorizontalClamp(
    modifier: Modifier = Modifier,
    maximumSize: Int = 600,
    tighteningThreshold: Int = 400,
    content: @Composable () -> Unit,
) {
    Clamp(modifier, Orientation.HORIZONTAL, maximumSize, tighteningThreshold, content)
}

@Composable
fun Clamp(
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.HORIZONTAL,
    maximumSize: Int = 600,
    tighteningThreshold: Int = 400,
    content: @Composable () -> Unit,
) {
    ComposeNode<GtkComposeNode<Clamp>, GtkApplier>(
        factory = {
            SingleChildComposeNode(
                Clamp.builder().build(),
                set = { child = it },
            )
        },
        update = {
            set(modifier) { applyModifier(it) }
            set(orientation) { this.gObject.orientation = it }
            set(maximumSize) { this.gObject.maximumSize = it }
            set(tighteningThreshold) { this.gObject.tighteningThreshold = it }
        },
        content = content,
    )
}