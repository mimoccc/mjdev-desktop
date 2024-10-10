package eu.mjdev.desktop.gtk.modifier

@Suppress("unused")
fun Modifier.cssClasses(vararg classes: String) = combine(
    apply = { it.cssClasses = classes },
    undo = { it.cssClasses = emptyArray() }
)

@Suppress("unused")
fun Modifier.cssClasses(classes: List<String>) = combine(
    apply = { it.cssClasses = classes.toTypedArray() },
    undo = { it.cssClasses = emptyArray() }
)