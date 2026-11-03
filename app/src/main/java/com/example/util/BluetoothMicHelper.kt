package com.example.util

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BluetoothAudioState(
    val isScoAudioConnected: Boolean = false,
    val isHeadsetConnected: Boolean = false,
    val activeDeviceName: String? = null
)

class BluetoothMicHelper(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val _state = MutableStateFlow(BluetoothAudioState())
    val state: StateFlow<BluetoothAudioState> = _state.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            when (intent?.action) {
                AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                    val scoState = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR)
                    val isConnected = scoState == AudioManager.SCO_AUDIO_STATE_CONNECTED
                    _state.update { it.copy(isScoAudioConnected = isConnected) }
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = try { device?.name } catch (_: SecurityException) { null }
                    _state.update {
                        it.copy(
                            isHeadsetConnected = true,
                            activeDeviceName = deviceName ?: it.activeDeviceName ?: "Dispositivo Bluetooth"
                        )
                    }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    _state.update {
                        it.copy(
                            isHeadsetConnected = false,
                            isScoAudioConnected = false,
                            activeDeviceName = null
                        )
                    }
                }
                Intent.ACTION_HEADSET_PLUG -> {
                    val plugState = intent.getIntExtra("state", 0)
                    if (plugState == 1) {
                        _state.update {
                            it.copy(
                                isHeadsetConnected = true,
                                activeDeviceName = it.activeDeviceName ?: "Fone com Microfone"
                            )
                        }
                    }
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(Intent.ACTION_HEADSET_PLUG)
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(receiver, filter)
            }
        } catch (_: SecurityException) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
                }
            } catch (_: Exception) {
                // Ignore fallback
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Initial check for headset or bluetooth audio
        audioManager?.let { am ->
            val isScoOn = am.isBluetoothScoOn
            val isWiredOn = am.isWiredHeadsetOn
            _state.update {
                it.copy(
                    isScoAudioConnected = isScoOn,
                    isHeadsetConnected = isScoOn || isWiredOn,
                    activeDeviceName = if (isScoOn) "Fone Bluetooth" else if (isWiredOn) "Fone P2/USB-C" else null
                )
            }
        }
    }

    fun startBluetoothSco() {
        try {
            audioManager?.let { am ->
                am.mode = AudioManager.MODE_IN_COMMUNICATION
                am.startBluetoothSco()
                am.isBluetoothScoOn = true
                _state.update { it.copy(isScoAudioConnected = true) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopBluetoothSco() {
        try {
            audioManager?.let { am ->
                am.stopBluetoothSco()
                am.isBluetoothScoOn = false
                am.mode = AudioManager.MODE_NORMAL
                _state.update { it.copy(isScoAudioConnected = false) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        stopBluetoothSco()
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            // Already unregistered or error
        }
    }
}
