/**
 * Display managers [SwitchGreeterTask] can make the boot login screen.
 *
 * @property arg argument of `session/switch-greeter.sh` selecting it
 */
enum class GreeterChoice(
    val arg: String,
) {
    /** The psippr login screen (greetd running psippr-greeter). */
    Psippr("psippr"),

    /** GNOME's gdm. */
    Gdm("gdm"),
}
