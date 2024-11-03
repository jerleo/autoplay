package de.jerleo.autoplay

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

data class Device(val name: String, val address: String)

class Bluetooth(private val main: Main) : BroadcastReceiver() {

    private val manager: BluetoothManager by lazy {
        main.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val adapter: BluetoothAdapter by lazy { manager.adapter }

    fun isEnabled() = adapter.isEnabled == true

    // Get bluetooth devices with audio profile
    val devices: List<Device> by lazy {

        @SuppressLint("MissingPermission")
        adapter.bondedDevices
            .filter { it.bluetoothClass.hasService(BluetoothClass.Service.AUDIO) }
            .map { Device(it.alias ?: it.name, it.address) }
            .sortedBy { it.name }
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        // Handle bluetooth events
        when (intent?.action) {

            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                Log.i(Main.TAG, "Bluetooth state has changed")
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR
                )
                when (state) {
                    BluetoothAdapter.STATE_ON -> main.settings.addDevices()
                    BluetoothAdapter.STATE_OFF -> main.settings.removeDevices()
                }
            }

            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                Log.i(Main.TAG, "Established connection")
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                devices.find { it.address == device?.address }?.let { main.startPlayback(it) }
            }
        }
    }

}