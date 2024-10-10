package eu.mjdev.desktop.gtk.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import eu.mjdev.desktop.gtk.GtkApplier
import eu.mjdev.desktop.gtk.GtkComposeNode
import eu.mjdev.desktop.gtk.modifier.Modifier
import eu.mjdev.desktop.gtk.SingleChildComposeNode
import org.gnome.gtk.CornerType
import org.gnome.gtk.PolicyType
import org.gnome.gtk.ScrolledWindow

@Suppress("unused")
@Composable
fun ScrolledWindow(
    modifier: Modifier = Modifier,
    horizontalScrollbarPolicy: PolicyType = PolicyType.AUTOMATIC,
    verticalScrollbarPolicy: PolicyType = PolicyType.AUTOMATIC,
    kineticScrolling: Boolean = true,
    minContentHeight: Int? = null,
    maxContentHeight: Int? = null,
    minContentWidth: Int? = null,
    maxContentWidth: Int? = null,
    overlayScrolling: Boolean = true,
    placement: CornerType = CornerType.TOP_LEFT,
    propagateNaturalHeight: Boolean = false,
    propagateNaturalWidth: Boolean = false,
    content: @Composable () -> Unit,
) {
    ComposeNode<GtkComposeNode<ScrolledWindow>, GtkApplier>(
        factory = {
            SingleChildComposeNode(
                ScrolledWindow.builder().build(),
                set = { child = it },
            )
        },
        update = {
            set(modifier) { applyModifier(it) }
            set(horizontalScrollbarPolicy to verticalScrollbarPolicy) { (h, v) -> this.gObject.setPolicy(h, v) }
            set(kineticScrolling) { this.gObject.kineticScrolling = it }
            set(minContentHeight) { this.gObject.minContentHeight = it ?: -1 }
            set(maxContentHeight) { this.gObject.maxContentHeight = it ?: -1 }
            set(minContentWidth) { this.gObject.minContentWidth = it ?: -1 }
            set(maxContentWidth) { this.gObject.maxContentWidth = it ?: -1 }
            set(overlayScrolling) { this.gObject.overlayScrolling = it }
            set(placement) { this.gObject.placement = it }
            set(propagateNaturalHeight) { this.gObject.propagateNaturalHeight = it }
            set(propagateNaturalWidth) { this.gObject.propagateNaturalWidth = it }
        },
        content = content,
    )
}