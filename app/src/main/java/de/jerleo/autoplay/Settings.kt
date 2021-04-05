package de.jerleo.autoplay

import android.bluetooth.BluetoothDevice
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat

class Settings : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val KEY_DELAY = "delay"
        private const val KEY_DEVICES = "devices"
        private const val KEY_VOLUME = "volume"
        private const val VOL_STEP = 5
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        // Add static preferences
        addPreferencesFromResource(R.xml.settings)

        // Set seek bar increment
        val volume = findPreference(KEY_VOLUME) as? SeekBarPreference
        volume?.setOnPreferenceChangeListener { preference, newValue ->
            (preference as SeekBarPreference).value = (newValue as Int) / VOL_STEP * VOL_STEP
            false
        }

        // Add dynamic preferences with bluetooth enabled
        if (Bluetooth.isEnabled())
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
        val volume = (findPreference("${device.key}-$KEY_VOLUME") as? SeekBarPreference)
        val delay = (findPreference("${device.key}-$KEY_DELAY") as? SeekBarPreference)

        // Preference change requested
        if (change) {
            volume?.value = volume()
            delay?.value = delay()
        }

        // Set device summary
        device.summary = "${getString(R.string.volume)} ${volume?.value} % " +
                "${getString(R.string.after)} ${delay?.value} ${getString(R.string.seconds)}"
    }

    fun addDevices() {

        // Avoid adding twice
        if ((findPreference(KEY_DEVICES) as? SwitchPreferenceCompat) != null)
            return

        // Add preference header
        val context = preferenceScreen.context
        val parent = PreferenceCategory(context).apply {
            key = KEY_DEVICES
            title = getString(R.string.devices)
        }
        preferenceScreen.addPreference(parent)

        // Add devices with audio profile
        Bluetooth.devices().forEach {

            // Add switch preference for device
            parent.addPreference(SwitchPreferenceCompat(context).apply {
                key = it.address
                title = it.name
            })

            // Add hidden seek bar for volume
            parent.addPreference(SeekBarPreference(context).apply {
                key = "${it.address}-$KEY_VOLUME"
                isVisible = false
            })

            // Add hidden seek bar for delay
            parent.addPreference(SeekBarPreference(context).apply {
                key = "${it.address}-$KEY_DELAY"
                isVisible = false
            })

            // Show summary for activated device
            setSummary(it.address, false)
        }
    }

    fun removeDevices() {

        (findPreference(KEY_DEVICES) as? PreferenceCategory).let {
            preferenceScreen.removePreference(it)
        }
    }

    // Get global delay preference value
    private fun delay() = (findPreference(KEY_DELAY) as? SeekBarPreference)!!.value

    // Get global volume preference value
    private fun volume() = (findPreference(KEY_VOLUME) as? SeekBarPreference)!!.value

    // Get device delay preference value
    fun delay(device: String) =
        (findPreference("$device-$KEY_DELAY") as? SeekBarPreference)?.value

    // Get device volume preference value
    fun volume(device: String) =
        (findPreference("$device-$KEY_VOLUME") as? SeekBarPreference)?.value

    // Get checked preference of bluetooth device
    fun isChecked(device: BluetoothDevice?) =

        device?.address?.let {
            findPreference(it) as? SwitchPreferenceCompat
        }.let {
            it?.isChecked
        } ?: false
}