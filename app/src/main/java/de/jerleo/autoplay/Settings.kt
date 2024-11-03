package de.jerleo.autoplay

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat

class Settings(
    private val main: Main
) : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val DELIMITER = "-"
        private const val DELAY = "delay"
        private const val DEVICES = "devices"
        private const val VOLUME = "volume"
        private const val VOL_STEP = 5
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        // Add static preferences
        addPreferencesFromResource(R.xml.settings)

        // Set seek bar increment
        val volume = findPreference(VOLUME) as? SeekBarPreference
        volume?.setOnPreferenceChangeListener { preference, newValue ->
            (preference as SeekBarPreference).value = (newValue as Int) / VOL_STEP * VOL_STEP
            false
        }
        addDevices()
    }

    override fun onSharedPreferenceChanged(sp: SharedPreferences?, key: String?) {

        // Change device summary
        key?.let { setSummary(it, true) }
    }

    private fun setSummary(key: String, change: Boolean) {

        // Check for device or return
        val device = findPreference(key) as? SwitchPreferenceCompat ?: return

        // Remove summary for unchecked device
        if (!device.isChecked) {
            device.summary = ""
            return
        }

        // Get hidden preferences
        val volume =
            (findPreference("${device.key}$DELIMITER$VOLUME") as? SeekBarPreference)
        val delay = (findPreference("${device.key}$DELIMITER$DELAY") as? SeekBarPreference)

        // Preference change requested
        if (change) {
            volume?.value = volume()
            delay?.value = delay()
        }

        // Set device summary
        device.summary =
            "${getString(R.string.volume)} ${volume?.value} % " + "${getString(R.string.after)} ${delay?.value} ${
                getString(
                    R.string.seconds
                )
            }"
    }

    fun addDevices() {

        // Avoid adding twice
        if ((findPreference(DEVICES) as? SwitchPreferenceCompat) != null) return

        // Add preference header
        val parent = PreferenceCategory(main).apply {
            key = DEVICES
            title = getString(R.string.devices)
        }
        preferenceScreen.addPreference(parent)

        // Add devices with audio profile
        main.bluetooth.devices.forEach {

            // Add switch preference for device
            parent.addPreference(SwitchPreferenceCompat(main).apply {
                key = it.address
                title = it.name
            })

            // Add hidden seek bar for volume
            parent.addPreference(SeekBarPreference(main).apply {
                key = "${it.address}$DELIMITER$VOLUME"
                isVisible = false
            })

            // Add hidden seek bar for delay
            parent.addPreference(SeekBarPreference(main).apply {
                key = "${it.address}$DELIMITER$DELAY"
                isVisible = false
            })

            // Show summary for activated device
            setSummary(it.address, false)
        }
    }

    fun removeDevices() {

        (findPreference(DEVICES) as? PreferenceCategory).let {
            it?.let { it1 -> preferenceScreen.removePreference(it1) }
        }
    }

    // Get global delay preference value
    private fun delay() = (findPreference(DELAY) as? SeekBarPreference)!!.value

    // Get global volume preference value
    private fun volume() = (findPreference(VOLUME) as? SeekBarPreference)!!.value

    // Get device delay preference value
    fun delay(device: Device) =
        (findPreference("${device.address}$DELIMITER$DELAY") as? SeekBarPreference)?.value

    // Get device volume preference value
    fun volume(device: Device) =
        (findPreference("${device.address}$DELIMITER$VOLUME") as? SeekBarPreference)?.value

    // Get checked preference of bluetooth device
    fun isChecked(device: Device) =
        (findPreference(device.address) as? SwitchPreferenceCompat)?.isChecked

}