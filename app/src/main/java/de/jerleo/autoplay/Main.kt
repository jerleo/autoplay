package de.jerleo.autoplay

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager

class Main : AppCompatActivity() {

    companion object {
        const val TAG = "AutoPlay"
        private const val BLUETOOTH_ENABLE = 100
        private const val REQUEST_CODE = 101
    }

    private val audio = Audio(this)
    private val notification = Notification(this)

    val bluetooth = Bluetooth(this)
    val settings = Settings(this)

    fun showNotification() {
        notification.show(getString(R.string.bluetooth), getString(R.string.waiting))
    }

    fun startPlayback(device: Device) {
        if (settings.isChecked(device) == false) return
        if (audio.startPlayback(device)) {
            Log.i(TAG, "Launching playback")
            val text: String = getString(R.string.launching) + ": " + device.name
            notification.show(getString(R.string.playback), text)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        if (requestCode == BLUETOOTH_ENABLE) setup()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE
            && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        )
            setup()
    }

    @SuppressLint("MissingPermission")
    fun enableBluetooth() {
        startActivityForResult(
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
            BLUETOOTH_ENABLE
        )
    }

    fun setup() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_settings, settings)
            .commit()

        // Monitor preference changes
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(settings)

        Log.i(TAG, "Listening for bluetooth events")
        registerReceiver(bluetooth, IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        })
        showNotification()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main)

        if (checkSelfPermission(BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            if (bluetooth.isEnabled()) {
                setup()
            } else {
                enableBluetooth()
            }
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(BLUETOOTH_CONNECT, POST_NOTIFICATIONS), REQUEST_CODE
            )
        }
    }

}