package eu.mjdev.desktop.gtk.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import eu.mjdev.desktop.gtk.GtkApplier
import eu.mjdev.desktop.gtk.GtkComposeNode
import eu.mjdev.desktop.gtk.SingleChildComposeNode
import eu.mjdev.desktop.gtk.VirtualComposeNode
import eu.mjdev.desktop.gtk.VirtualComposeNodeContainer
import eu.mjdev.desktop.gtk.modifier.Modifier
import org.gnome.gtk.*

@Suppress("unused")
@Composable
fun CenterBox(
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.HORIZONTAL,
    start: @Composable () -> Unit = {},
    end: @Composable () -> Unit = {},
    baselinePosition: BaselinePosition = BaselinePosition.CENTER,
    //shrinkCenterLast: Boolean = true, TODO: gtk 4.12
    center: @Composable () -> Unit,
) {
    ComposeNode<GtkComposeNode<CenterBox>, GtkApplier>(
        factory = {
            VirtualComposeNodeContainer(CenterBox.builder().build())
        },
        update = {
            set(modifier) { applyModifier(it) }
            set(orientation) { this.gObject.orientation = it }
            set(baselinePosition) { this.gObject.baselinePosition = it }
            //set(shrinkCenterLast) { this.gObject.shrinkCenterLast = it }
        },
        content = {
            Child({ startWidget = it }, content = start)
            Child({ centerWidget = it }, content = center)
            Child({ endWidget = it }, content = end)
        },
    )
}

@Composable
private fun Child(
    setter: CenterBox.(Widget?) -> Unit,
    content: @Composable () -> Unit,
) {
    ComposeNode<GtkComposeNode<Nothing?>, GtkApplier>(
        factory = {
            VirtualComposeNode<CenterBox> { overlay ->
                SingleChildComposeNode(
                    overlay,
                    set = { setter(it) },
                )
            }
        },
        update = { },
        content = content,
    )
}