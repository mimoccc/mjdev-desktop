package org.psippr.desktop.managers.theme.linux.icons

/**
 * Contexts of the freedesktop icon naming spec: each is one folder per size in an icon theme.
 *
 * @property dirName folder of the context inside a size folder (`48x48/places`)
 * @property title `Context=` value in `index.theme`
 */
enum class IconContext(
    val dirName: String,
    val title: String,
) {
    /** Actions (menus, toolbars). */
    ACTIONS("actions", "Actions"),

    /** App icons. */
    APPS("apps", "Applications"),

    /** App menu categories. */
    CATEGORIES("categories", "Categories"),

    /** Hardware. */
    DEVICES("devices", "Devices"),

    /** Small marks on files. */
    EMBLEMS("emblems", "Emblems"),

    /** File types. */
    MIMETYPES("mimetypes", "MimeTypes"),

    /** Folders and places. */
    PLACES("places", "Places"),

    /** States (battery, network, dialogs). */
    STATUS("status", "Status"),
}
