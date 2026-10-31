package org.psippr.desktop.managers.theme.linux.icons

/**
 * Freedesktop icon names (icon naming spec) and the icon font glyph drawn for each. Names not
 * listed, and every `-symbolic` icon, come from the theme the generated one inherits.
 *
 * @property iconName name apps ask for (`folder`, `text-x-generic`)
 * @property context spec context, the folder the icon goes to
 * @property glyph glyph name in the icon font (Material Symbols names)
 */
enum class FreedesktopIcon(
    val iconName: String,
    val context: IconContext,
    val glyph: String,
) {
    /** `folder`. */
    FOLDER("folder", IconContext.PLACES, "folder"),

    /** `folder-open`. */
    FOLDER_OPEN("folder-open", IconContext.PLACES, "folder_open"),

    /** `inode-directory`. */
    INODE_DIRECTORY("inode-directory", IconContext.PLACES, "folder"),

    /** `user-home`. */
    USER_HOME("user-home", IconContext.PLACES, "home"),

    /** `user-desktop`. */
    USER_DESKTOP("user-desktop", IconContext.PLACES, "desktop_windows"),

    /** `folder-documents`. */
    FOLDER_DOCUMENTS("folder-documents", IconContext.PLACES, "description"),

    /** `folder-download`. */
    FOLDER_DOWNLOAD("folder-download", IconContext.PLACES, "download"),

    /** `folder-music`. */
    FOLDER_MUSIC("folder-music", IconContext.PLACES, "library_music"),

    /** `folder-pictures`. */
    FOLDER_PICTURES("folder-pictures", IconContext.PLACES, "photo_library"),

    /** `folder-videos`. */
    FOLDER_VIDEOS("folder-videos", IconContext.PLACES, "video_library"),

    /** `folder-publicshare`. */
    FOLDER_PUBLICSHARE("folder-publicshare", IconContext.PLACES, "folder_shared"),

    /** `folder-templates`. */
    FOLDER_TEMPLATES("folder-templates", IconContext.PLACES, "file_copy"),

    /** `folder-remote`. */
    FOLDER_REMOTE("folder-remote", IconContext.PLACES, "cloud"),

    /** `user-trash`. */
    USER_TRASH("user-trash", IconContext.PLACES, "delete"),

    /** `user-trash-full`. */
    USER_TRASH_FULL("user-trash-full", IconContext.PLACES, "delete"),

    /** `user-bookmarks`. */
    USER_BOOKMARKS("user-bookmarks", IconContext.PLACES, "bookmark"),

    /** `network-workgroup`. */
    NETWORK_WORKGROUP("network-workgroup", IconContext.PLACES, "lan"),

    /** `network-server`. */
    NETWORK_SERVER("network-server", IconContext.PLACES, "dns"),

    /** `start-here`. */
    START_HERE("start-here", IconContext.PLACES, "apps"),

    /** `text-x-generic`. */
    TEXT_X_GENERIC("text-x-generic", IconContext.MIMETYPES, "description"),

    /** `text-x-script`. */
    TEXT_X_SCRIPT("text-x-script", IconContext.MIMETYPES, "code"),

    /** `text-html`. */
    TEXT_HTML("text-html", IconContext.MIMETYPES, "html"),

    /** `application-x-executable`. */
    APPLICATION_X_EXECUTABLE("application-x-executable", IconContext.MIMETYPES, "terminal"),

    /** `application-x-addon`. */
    APPLICATION_X_ADDON("application-x-addon", IconContext.MIMETYPES, "extension"),

    /** `application-x-firmware`. */
    APPLICATION_X_FIRMWARE("application-x-firmware", IconContext.MIMETYPES, "memory"),

    /** `application-x-generic`. */
    APPLICATION_X_GENERIC("application-x-generic", IconContext.MIMETYPES, "draft"),

    /** `application-pdf`. */
    APPLICATION_PDF("application-pdf", IconContext.MIMETYPES, "picture_as_pdf"),

    /** `application-x-archive`. */
    APPLICATION_X_ARCHIVE("application-x-archive", IconContext.MIMETYPES, "folder_zip"),

    /** `application-zip`. */
    APPLICATION_ZIP("application-zip", IconContext.MIMETYPES, "folder_zip"),

    /** `audio-x-generic`. */
    AUDIO_X_GENERIC("audio-x-generic", IconContext.MIMETYPES, "audio_file"),

    /** `video-x-generic`. */
    VIDEO_X_GENERIC("video-x-generic", IconContext.MIMETYPES, "video_file"),

    /** `image-x-generic`. */
    IMAGE_X_GENERIC("image-x-generic", IconContext.MIMETYPES, "image"),

    /** `package-x-generic`. */
    PACKAGE_X_GENERIC("package-x-generic", IconContext.MIMETYPES, "inventory_2"),

    /** `font-x-generic`. */
    FONT_X_GENERIC("font-x-generic", IconContext.MIMETYPES, "font_download"),

    /** `x-office-document`. */
    X_OFFICE_DOCUMENT("x-office-document", IconContext.MIMETYPES, "article"),

    /** `x-office-spreadsheet`. */
    X_OFFICE_SPREADSHEET("x-office-spreadsheet", IconContext.MIMETYPES, "table_chart"),

    /** `x-office-presentation`. */
    X_OFFICE_PRESENTATION("x-office-presentation", IconContext.MIMETYPES, "slideshow"),

    /** `x-office-drawing`. */
    X_OFFICE_DRAWING("x-office-drawing", IconContext.MIMETYPES, "draw"),

    /** `x-office-calendar`. */
    X_OFFICE_CALENDAR("x-office-calendar", IconContext.MIMETYPES, "calendar_month"),

    /** `x-office-address-book`. */
    X_OFFICE_ADDRESS_BOOK("x-office-address-book", IconContext.MIMETYPES, "contacts"),

    /** `computer`. */
    COMPUTER("computer", IconContext.DEVICES, "computer"),

    /** `drive-harddisk`. */
    DRIVE_HARDDISK("drive-harddisk", IconContext.DEVICES, "hard_drive"),

    /** `drive-optical`. */
    DRIVE_OPTICAL("drive-optical", IconContext.DEVICES, "album"),

    /** `drive-removable-media`. */
    DRIVE_REMOVABLE_MEDIA("drive-removable-media", IconContext.DEVICES, "usb"),

    /** `media-flash`. */
    MEDIA_FLASH("media-flash", IconContext.DEVICES, "sd_card"),

    /** `media-optical`. */
    MEDIA_OPTICAL("media-optical", IconContext.DEVICES, "album"),

    /** `input-keyboard`. */
    INPUT_KEYBOARD("input-keyboard", IconContext.DEVICES, "keyboard"),

    /** `input-mouse`. */
    INPUT_MOUSE("input-mouse", IconContext.DEVICES, "mouse"),

    /** `input-gaming`. */
    INPUT_GAMING("input-gaming", IconContext.DEVICES, "sports_esports"),

    /** `input-tablet`. */
    INPUT_TABLET("input-tablet", IconContext.DEVICES, "tablet"),

    /** `audio-card`. */
    AUDIO_CARD("audio-card", IconContext.DEVICES, "speaker"),

    /** `audio-input-microphone`. */
    AUDIO_INPUT_MICROPHONE("audio-input-microphone", IconContext.DEVICES, "mic"),

    /** `camera-photo`. */
    CAMERA_PHOTO("camera-photo", IconContext.DEVICES, "photo_camera"),

    /** `camera-web`. */
    CAMERA_WEB("camera-web", IconContext.DEVICES, "videocam"),

    /** `phone`. */
    PHONE("phone", IconContext.DEVICES, "smartphone"),

    /** `printer`. */
    PRINTER("printer", IconContext.DEVICES, "print"),

    /** `network-wired`. */
    NETWORK_WIRED("network-wired", IconContext.DEVICES, "settings_ethernet"),

    /** `network-wireless`. */
    NETWORK_WIRELESS("network-wireless", IconContext.DEVICES, "wifi"),

    /** `video-display`. */
    VIDEO_DISPLAY("video-display", IconContext.DEVICES, "monitor"),

    /** `multimedia-player`. */
    MULTIMEDIA_PLAYER("multimedia-player", IconContext.DEVICES, "play_circle"),

    /** `battery`. */
    BATTERY("battery", IconContext.DEVICES, "battery_full"),

    /** `accessories-calculator`. */
    ACCESSORIES_CALCULATOR("accessories-calculator", IconContext.APPS, "calculate"),

    /** `accessories-text-editor`. */
    ACCESSORIES_TEXT_EDITOR("accessories-text-editor", IconContext.APPS, "edit_note"),

    /** `help-browser`. */
    HELP_BROWSER("help-browser", IconContext.APPS, "help"),

    /** `multimedia-volume-control`. */
    MULTIMEDIA_VOLUME_CONTROL("multimedia-volume-control", IconContext.APPS, "volume_up"),

    /** `preferences-desktop-theme`. */
    PREFERENCES_DESKTOP_THEME("preferences-desktop-theme", IconContext.APPS, "palette"),

    /** `system-file-manager`. */
    SYSTEM_FILE_MANAGER("system-file-manager", IconContext.APPS, "folder"),

    /** `system-software-install`. */
    SYSTEM_SOFTWARE_INSTALL("system-software-install", IconContext.APPS, "install_desktop"),

    /** `system-users`. */
    SYSTEM_USERS("system-users", IconContext.APPS, "group"),

    /** `utilities-terminal`. */
    UTILITIES_TERMINAL("utilities-terminal", IconContext.APPS, "terminal"),

    /** `utilities-system-monitor`. */
    UTILITIES_SYSTEM_MONITOR("utilities-system-monitor", IconContext.APPS, "monitor_heart"),

    /** `internet-mail`. */
    INTERNET_MAIL("internet-mail", IconContext.APPS, "mail"),

    /** `internet-web-browser`. */
    INTERNET_WEB_BROWSER("internet-web-browser", IconContext.APPS, "public"),

    /** `internet-chat`. */
    INTERNET_CHAT("internet-chat", IconContext.APPS, "chat"),

    /** `applications-accessories`. */
    APPLICATIONS_ACCESSORIES("applications-accessories", IconContext.CATEGORIES, "handyman"),

    /** `applications-development`. */
    APPLICATIONS_DEVELOPMENT("applications-development", IconContext.CATEGORIES, "code"),

    /** `applications-games`. */
    APPLICATIONS_GAMES("applications-games", IconContext.CATEGORIES, "sports_esports"),

    /** `applications-graphics`. */
    APPLICATIONS_GRAPHICS("applications-graphics", IconContext.CATEGORIES, "palette"),

    /** `applications-internet`. */
    APPLICATIONS_INTERNET("applications-internet", IconContext.CATEGORIES, "public"),

    /** `applications-multimedia`. */
    APPLICATIONS_MULTIMEDIA("applications-multimedia", IconContext.CATEGORIES, "perm_media"),

    /** `applications-office`. */
    APPLICATIONS_OFFICE("applications-office", IconContext.CATEGORIES, "work"),

    /** `applications-other`. */
    APPLICATIONS_OTHER("applications-other", IconContext.CATEGORIES, "apps"),

    /** `applications-science`. */
    APPLICATIONS_SCIENCE("applications-science", IconContext.CATEGORIES, "science"),

    /** `applications-system`. */
    APPLICATIONS_SYSTEM("applications-system", IconContext.CATEGORIES, "settings"),

    /** `applications-utilities`. */
    APPLICATIONS_UTILITIES("applications-utilities", IconContext.CATEGORIES, "build"),

    /** `preferences-desktop`. */
    PREFERENCES_DESKTOP("preferences-desktop", IconContext.CATEGORIES, "tune"),

    /** `preferences-system`. */
    PREFERENCES_SYSTEM("preferences-system", IconContext.CATEGORIES, "settings"),

    /** `preferences-other`. */
    PREFERENCES_OTHER("preferences-other", IconContext.CATEGORIES, "tune"),

    /** `system-help`. */
    SYSTEM_HELP("system-help", IconContext.CATEGORIES, "help"),

    /** `document-new`. */
    DOCUMENT_NEW("document-new", IconContext.ACTIONS, "note_add"),

    /** `document-open`. */
    DOCUMENT_OPEN("document-open", IconContext.ACTIONS, "file_open"),

    /** `document-save`. */
    DOCUMENT_SAVE("document-save", IconContext.ACTIONS, "save"),

    /** `document-save-as`. */
    DOCUMENT_SAVE_AS("document-save-as", IconContext.ACTIONS, "save_as"),

    /** `document-print`. */
    DOCUMENT_PRINT("document-print", IconContext.ACTIONS, "print"),

    /** `document-properties`. */
    DOCUMENT_PROPERTIES("document-properties", IconContext.ACTIONS, "info"),

    /** `edit-copy`. */
    EDIT_COPY("edit-copy", IconContext.ACTIONS, "content_copy"),

    /** `edit-cut`. */
    EDIT_CUT("edit-cut", IconContext.ACTIONS, "content_cut"),

    /** `edit-paste`. */
    EDIT_PASTE("edit-paste", IconContext.ACTIONS, "content_paste"),

    /** `edit-delete`. */
    EDIT_DELETE("edit-delete", IconContext.ACTIONS, "delete"),

    /** `edit-undo`. */
    EDIT_UNDO("edit-undo", IconContext.ACTIONS, "undo"),

    /** `edit-redo`. */
    EDIT_REDO("edit-redo", IconContext.ACTIONS, "redo"),

    /** `edit-find`. */
    EDIT_FIND("edit-find", IconContext.ACTIONS, "search"),

    /** `edit-clear`. */
    EDIT_CLEAR("edit-clear", IconContext.ACTIONS, "clear_all"),

    /** `edit-select-all`. */
    EDIT_SELECT_ALL("edit-select-all", IconContext.ACTIONS, "select_all"),

    /** `list-add`. */
    LIST_ADD("list-add", IconContext.ACTIONS, "add"),

    /** `list-remove`. */
    LIST_REMOVE("list-remove", IconContext.ACTIONS, "remove"),

    /** `go-home`. */
    GO_HOME("go-home", IconContext.ACTIONS, "home"),

    /** `go-up`. */
    GO_UP("go-up", IconContext.ACTIONS, "arrow_upward"),

    /** `go-down`. */
    GO_DOWN("go-down", IconContext.ACTIONS, "arrow_downward"),

    /** `go-previous`. */
    GO_PREVIOUS("go-previous", IconContext.ACTIONS, "arrow_back"),

    /** `go-next`. */
    GO_NEXT("go-next", IconContext.ACTIONS, "arrow_forward"),

    /** `view-refresh`. */
    VIEW_REFRESH("view-refresh", IconContext.ACTIONS, "refresh"),

    /** `window-close`. */
    WINDOW_CLOSE("window-close", IconContext.ACTIONS, "close"),

    /** `application-exit`. */
    APPLICATION_EXIT("application-exit", IconContext.ACTIONS, "logout"),

    /** `system-shutdown`. */
    SYSTEM_SHUTDOWN("system-shutdown", IconContext.ACTIONS, "power_settings_new"),

    /** `system-reboot`. */
    SYSTEM_REBOOT("system-reboot", IconContext.ACTIONS, "restart_alt"),

    /** `system-log-out`. */
    SYSTEM_LOG_OUT("system-log-out", IconContext.ACTIONS, "logout"),

    /** `system-lock-screen`. */
    SYSTEM_LOCK_SCREEN("system-lock-screen", IconContext.ACTIONS, "lock"),

    /** `system-search`. */
    SYSTEM_SEARCH("system-search", IconContext.ACTIONS, "search"),

    /** `process-stop`. */
    PROCESS_STOP("process-stop", IconContext.ACTIONS, "stop_circle"),

    /** `media-playback-start`. */
    MEDIA_PLAYBACK_START("media-playback-start", IconContext.ACTIONS, "play_arrow"),

    /** `media-playback-pause`. */
    MEDIA_PLAYBACK_PAUSE("media-playback-pause", IconContext.ACTIONS, "pause"),

    /** `media-playback-stop`. */
    MEDIA_PLAYBACK_STOP("media-playback-stop", IconContext.ACTIONS, "stop"),

    /** `media-skip-forward`. */
    MEDIA_SKIP_FORWARD("media-skip-forward", IconContext.ACTIONS, "skip_next"),

    /** `media-skip-backward`. */
    MEDIA_SKIP_BACKWARD("media-skip-backward", IconContext.ACTIONS, "skip_previous"),

    /** `folder-new`. */
    FOLDER_NEW("folder-new", IconContext.ACTIONS, "create_new_folder"),

    /** `zoom-in`. */
    ZOOM_IN("zoom-in", IconContext.ACTIONS, "zoom_in"),

    /** `zoom-out`. */
    ZOOM_OUT("zoom-out", IconContext.ACTIONS, "zoom_out"),

    /** `dialog-information`. */
    DIALOG_INFORMATION("dialog-information", IconContext.STATUS, "info"),

    /** `dialog-warning`. */
    DIALOG_WARNING("dialog-warning", IconContext.STATUS, "warning"),

    /** `dialog-error`. */
    DIALOG_ERROR("dialog-error", IconContext.STATUS, "error"),

    /** `dialog-question`. */
    DIALOG_QUESTION("dialog-question", IconContext.STATUS, "help"),

    /** `battery-full`. */
    BATTERY_FULL("battery-full", IconContext.STATUS, "battery_full"),

    /** `battery-low`. */
    BATTERY_LOW("battery-low", IconContext.STATUS, "battery_2_bar"),

    /** `battery-empty`. */
    BATTERY_EMPTY("battery-empty", IconContext.STATUS, "battery_alert"),

    /** `network-offline`. */
    NETWORK_OFFLINE("network-offline", IconContext.STATUS, "signal_wifi_off"),

    /** `audio-volume-high`. */
    AUDIO_VOLUME_HIGH("audio-volume-high", IconContext.STATUS, "volume_up"),

    /** `audio-volume-low`. */
    AUDIO_VOLUME_LOW("audio-volume-low", IconContext.STATUS, "volume_down"),

    /** `audio-volume-muted`. */
    AUDIO_VOLUME_MUTED("audio-volume-muted", IconContext.STATUS, "volume_off"),

    /** `mail-unread`. */
    MAIL_UNREAD("mail-unread", IconContext.STATUS, "mark_email_unread"),

    /** `security-high`. */
    SECURITY_HIGH("security-high", IconContext.STATUS, "verified_user"),

    /** `weather-clear`. */
    WEATHER_CLEAR("weather-clear", IconContext.STATUS, "sunny"),

    /** `appointment-soon`. */
    APPOINTMENT_SOON("appointment-soon", IconContext.STATUS, "event"),

    /** `emblem-favorite`. */
    EMBLEM_FAVORITE("emblem-favorite", IconContext.EMBLEMS, "favorite"),

    /** `emblem-important`. */
    EMBLEM_IMPORTANT("emblem-important", IconContext.EMBLEMS, "priority_high"),

    /** `emblem-symbolic-link`. */
    EMBLEM_SYMBOLIC_LINK("emblem-symbolic-link", IconContext.EMBLEMS, "link"),

    /** `emblem-shared`. */
    EMBLEM_SHARED("emblem-shared", IconContext.EMBLEMS, "share"),

    /** `emblem-readonly`. */
    EMBLEM_READONLY("emblem-readonly", IconContext.EMBLEMS, "lock"),

    /** `emblem-default`. */
    EMBLEM_DEFAULT("emblem-default", IconContext.EMBLEMS, "check_circle"),
}
