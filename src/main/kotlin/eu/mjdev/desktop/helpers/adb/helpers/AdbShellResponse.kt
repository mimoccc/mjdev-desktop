package eu.mjdev.desktop.helpers.adb.helpers

class AdbShellResponse(
    val output: String,
    val errorOutput: String,
    val exitCode: Int
) {
    val allOutput: String by lazy { "$output$errorOutput" }

    override fun toString() = "Shell response ($exitCode):\n$allOutput"
}