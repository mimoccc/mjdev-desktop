package eu.mjdev.desktop.gtk.modifier

@Suppress("unused")
fun Modifier.sizeRequest(
    width: Int = -1,
    height: Int = -1,
) = combine(
    apply = {
        it.setSizeRequest(width, height)
    },
    undo = {
        it.setSizeRequest(-1, -1)
    }
)
