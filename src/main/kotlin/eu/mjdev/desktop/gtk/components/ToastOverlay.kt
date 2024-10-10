package eu.mjdev.desktop.gtk.components

import eu.mjdev.desktop.gtk.GtkApplier
import eu.mjdev.desktop.gtk.GtkComposeNode
import eu.mjdev.desktop.gtk.SingleChildComposeNode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import eu.mjdev.desktop.gtk.modifier.Modifier
import org.gnome.adw.Toast
import org.gnome.adw.ToastOverlay

interface ToastOverlayScope {
    /**
     * Shows a Toast
     * TODO: should the toast be a GTK widget? Probably not
     */
    fun addToast(toast: Toast)
}

private class ToastOverlayScopeImpl : ToastOverlayScope {
    var toastOverlay: ToastOverlay? = null
    override fun addToast(toast: Toast) {
        toastOverlay!!.addToast(toast)
    }
}

@Suppress("unused")
@Composable
fun ToastOverlay(
    modifier: Modifier = Modifier,
    content: @Composable ToastOverlayScope.() -> Unit,
) {
    val overlayScope = ToastOverlayScopeImpl()
    ComposeNode<GtkComposeNode<ToastOverlay>, GtkApplier>(
        factory = {
            val toastOverlay = ToastOverlay.builder().build()
            SingleChildComposeNode(
                toastOverlay,
                set = { toastOverlay.child = it },
            )
        },
        update = {
            set(modifier) { applyModifier(it) }
            set(overlayScope) { it.toastOverlay = this.gObject }
        },
        content = {
            overlayScope.content()
        },
    )
}