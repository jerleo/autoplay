package de.jerleo.autoplay

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media.app.NotificationCompat as MediaNotificationCompat

class Main : AppCompatActivity() {

    companion object {
        const val TAG = "AutoPlay"
    }

    private val settings = Settings()
    private val audio = Audio(this, settings)
    private val notification = Notification(this)

    fun addDevices() {

        // Add bluetooth devices on state change
        settings.addDevices()
    }

    fun removeDevices() {

        // Remove bluetooth devices on state change
        settings.removeDevices()
    }

    fun startPlayback(device: BluetoothDevice) {

        if (settings.isChecked(device))
            if (audio.startPlayback()) {

                Log.i(Main.TAG, "Launching playback")
                val text: String = getString(R.string.launching) +
                        ": " + device.name
                notification.show(getString(R.string.playback), text)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        // Show settings
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, settings)
            .commit()

        Log.i(Main.TAG, "Listening for bluetooth events")
        registerReceiver(
            Bluetooth,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            })

        // Turn on bluetooth
        Bluetooth.enable()

        notification.show(
            getString(R.string.bluetooth),
            getString(R.string.waiting)
        )
    }

    override fun onDestroy() {

        Log.i(Main.TAG, "Stopped listening for bluetooth events")
        unregisterReceiver(Bluetooth)
        notification.cancel()
        super.onDestroy()
    }

}