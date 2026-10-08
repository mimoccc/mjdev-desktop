package org.psippr.desktop.managers.volume.pactl

/**
 * Keys of the `Properties` section used to name devices.
 *
 * @property key property name
 */
enum class PactlProperty(
    val key: String,
) {
    /** ALSA PCM id (`HDMI1 (*)`, `HDA Analog (*)`, `DMIC (*)`). */
    ALSA_ID("alsa.id"),

    /** `monitor` for sink monitors, which are not real inputs. */
    DEVICE_CLASS("device.class"),

    /** `true` for the raw devices of the Pro Audio profile. */
    PROFILE_PRO("device.profile.pro"),

    /** Card description. */
    DEVICE_DESCRIPTION("device.description"),
}
