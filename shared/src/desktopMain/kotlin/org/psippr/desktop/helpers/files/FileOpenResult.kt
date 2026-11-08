package org.psippr.desktop.helpers.files

/**
 * Outcome of opening a file with the app associated to its type.
 *
 * @property message what to tell the user; blank when there is nothing to say
 */
enum class FileOpenResult(
    val message: String,
) {
    /** The associated app was started with the file. */
    OPENED(""),

    /** No app is associated with the file's type. */
    NO_APP("No app is set to open this kind of file."),

    /** An app is associated but could not be started. */
    FAILED("The app for this file could not be started."),
}
