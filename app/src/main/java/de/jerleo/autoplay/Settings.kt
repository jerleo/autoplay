package de.jerleo.autoplay

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat

class Settings : PreferenceFragmentCompat() {

    companion object {
        private const val KEY_DELAY = "delay"
        private const val KEY_DEVICES = "devices"
        private const val KEY_VOLUME = "volume"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        // Add static preferences
        addPreferencesFromResource(R.xml.settings)

        // Add dynamic preferences with bluetooth enabled
        if (Bluetooth.isEnabled())
            addDevices()
    }

    fun addDevices() {

        // Avoid adding twice
        if (findPreference(KEY_DEVICES) != null)
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
            parent.addPreference(
                SwitchPreferenceCompat(context).apply {
                    key = it.address
                    title = it.name
                }
            )
        }
    }

    fun removeDevices() {

        (findPreference(KEY_DEVICES) as PreferenceCategory).let {
            preferenceScreen.removePreference(it)
        }
    }

    // Get delay preference value
    fun delay() = (findPreference(KEY_DELAY) as SeekBarPreference).value

    // Get volume preference value
    fun volume() = (findPreference(KEY_VOLUME) as SeekBarPreference).value

    // Get checked preference of bluetooth device
    fun isChecked(device: BluetoothDevice?) =

        findPreference(device?.address)?.let {
            (it as SwitchPreferenceCompat).isChecked
        } ?: false

}