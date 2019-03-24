package de.jerleo.autoplay

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

object Bluetooth : BroadcastReceiver() {

    private val adapter = BluetoothAdapter.getDefaultAdapter()

    fun enable() = adapter?.enable()

    fun isEnabled() = adapter?.isEnabled ?: false

    // Get bluetooth devices with audio profile
    fun devices() =
        adapter.bondedDevices
            .filter { it.bluetoothClass.hasService(BluetoothClass.Service.AUDIO) }
            .sortedBy { it.name }

    // Handle bluetooth events
    override fun onReceive(context: Context?, intent: Intent?) {

        val main = context as Main
        val action = intent?.action

        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {

            Log.i(Main.TAG, "Bluetooth state has changed")
            val state = intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE,
                BluetoothAdapter.ERROR
            )
            when (state) {
                BluetoothAdapter.STATE_ON -> main.addDevices()
                BluetoothAdapter.STATE_OFF -> main.removeDevices()
            }
        }

        if (action == BluetoothDevice.ACTION_ACL_CONNECTED) {

            Log.i(Main.TAG, "Established connection")
            val device = intent.getParcelableExtra<BluetoothDevice>(
                BluetoothDevice.EXTRA_DEVICE
            )
            main.startPlayback(device)
        }
    }

}