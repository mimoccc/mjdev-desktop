package org.psippr.desktop.managers.volume.pactl

import org.psippr.desktop.managers.volume.AudioDeviceKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression tests: Sound settings must list readable, distinct output and input devices. */
class PactlDevicesTest {
    private val proSinks =
        """
        Sink #58
        	State: SUSPENDED
        	Name: alsa_output.pci-0000_00_1f.3-platform-skl_hda_dsp_generic.pro-output-0
        	Description: Tiger Lake-LP Smart Sound Technology Audio Controller Pro
        	Mute: no
        	Volume: front-left: 65536 / 100% / 0.00 dB,   front-right: 65536 / 100% / 0.00 dB
        	        balance 0.00
        	Properties:
        		alsa.id = "HDA Analog (*)"
        		device.class = "sound"
        		device.description = "Tiger Lake-LP Smart Sound Technology Audio Controller"
        		device.profile.pro = "true"
        	Formats:
        		pcm

        Sink #59
        	Name: alsa_output.pci-0000_00_1f.3-platform-skl_hda_dsp_generic.pro-output-3
        	Description: Tiger Lake-LP Smart Sound Technology Audio Controller Pro 3
        	Properties:
        		alsa.id = "HDMI1 (*)"
        		device.class = "sound"
        		device.description = "Tiger Lake-LP Smart Sound Technology Audio Controller"
        		device.profile.pro = "true"
        """.trimIndent()

    private val portSinks =
        """
        Sink #1
        	Name: alsa_output.pci-0000_00_1f.3.analog-stereo
        	Description: Built-in Audio Analog Stereo
        	Properties:
        		device.class = "sound"
        	Ports:
        		analog-output-speaker: Speakers (type: Speaker, priority: 10000, availability unknown)
        		analog-output-headphones: Headphones (type: Headphones, priority: 9900, not available)
        		analog-output-lineout: Line Out (type: Line, priority: 9000, available)
        	Active Port: analog-output-lineout
        """.trimIndent()

    private val sources =
        """
        Source #62
        	Name: alsa_output.pci-0000_00_1f.3.pro-output-0.monitor
        	Description: Monitor of Pro
        	Properties:
        		device.class = "monitor"

        Source #64
        	Name: alsa_input.pci-0000_00_1f.3.pro-input-6
        	Description: Tiger Lake-LP Smart Sound Technology Audio Controller Pro 6
        	Properties:
        		alsa.id = "DMIC (*)"
        		device.class = "sound"
        		device.description = "Tiger Lake-LP Smart Sound Technology Audio Controller"
        		device.profile.pro = "true"
        """.trimIndent()

    private val cards =
        """
        Card #54
        	Name: alsa_card.pci-0000_00_1f.3-platform-skl_hda_dsp_generic
        	Properties:
        		device.description = "Tiger Lake-LP Smart Sound Technology Audio Controller"
        	Profiles:
        		off: Off (sinks: 0, sources: 0, priority: 0, available: yes)
        		output:stereo-fallback+input:stereo-fallback: Stereo Output + Stereo Input (sinks: 1, sources: 1, priority: 5151, available: yes)
        		input:stereo-fallback: Stereo Input (sinks: 0, sources: 1, priority: 51, available: no)
        		pro-audio: Pro Audio (sinks: 5, sources: 3, priority: 1, available: yes)
        	Active Profile: pro-audio
        """.trimIndent()

    @Test
    fun proAudioOutputsGetDistinctReadableNames() {
        val default = "alsa_output.pci-0000_00_1f.3-platform-skl_hda_dsp_generic.pro-output-3"
        val devices = PactlDevices.devices(PactlParser.parse(proSinks), PactlTarget.SINK, default)
        assertEquals(2, devices.size)
        assertTrue(devices[0].label.startsWith("HDA Analog"))
        assertTrue(devices[1].label.startsWith("HDMI1"))
        assertEquals(listOf(false, true), devices.map { device -> device.isDefault })
        assertTrue(devices.all { device -> device.kind == AudioDeviceKind.OUTPUT && device.port == null })
    }

    @Test
    fun portsBecomeChoicesAndUnpluggedOnesAreHidden() {
        val name = "alsa_output.pci-0000_00_1f.3.analog-stereo"
        val devices = PactlDevices.devices(PactlParser.parse(portSinks), PactlTarget.SINK, name)
        assertEquals(listOf("analog-output-speaker", "analog-output-lineout"), devices.map { device -> device.port })
        assertEquals("Speakers – Built-in Audio Analog Stereo", devices[0].label)
        assertEquals(listOf(false, true), devices.map { device -> device.isDefault })
    }

    @Test
    fun inputsSkipSinkMonitors() {
        val devices = PactlDevices.devices(PactlParser.parse(sources), PactlTarget.SOURCE, "")
        assertEquals(1, devices.size)
        assertTrue(devices[0].label.startsWith("DMIC"))
        assertEquals(AudioDeviceKind.INPUT, devices[0].kind)
    }

    @Test
    fun profilesSkipOffAndUnavailable() {
        val profiles = PactlDevices.profiles(PactlParser.parse(cards))
        assertEquals(listOf("output:stereo-fallback+input:stereo-fallback", "pro-audio"), profiles.map { p -> p.id })
        assertEquals("Stereo Output + Stereo Input", profiles[0].label)
        assertTrue(profiles[1].isActive)
        assertFalse(profiles[0].isActive)
    }
}
