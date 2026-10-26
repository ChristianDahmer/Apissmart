package com.example.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import com.example.util.LocationHelper
import java.nio.ByteBuffer
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt

data class DiscoveredBleTag(
    val macAddress: String,
    val name: String,
    val rssi: Int,
    val distanceMeters: Double,
    val txPower: Int = -59,
    val batteryPercent: Int? = null,
    val temperatureC: Double? = null,
    val humidityPercent: Double? = null,
    val tagType: String = "BLE Beacon",
    val isRegistered: Boolean = false,
    val assignedHiveName: String? = null,
    val lastSeenMillis: Long = System.currentTimeMillis()
)

class BluetoothTagTracker(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var bleScanner: BluetoothLeScanner? = null

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredTags = MutableStateFlow<Map<String, DiscoveredBleTag>>(emptyMap())
    val discoveredTags: StateFlow<Map<String, DiscoveredBleTag>> = _discoveredTags.asStateFlow()

    private val _isBluetoothSupported = MutableStateFlow(bluetoothAdapter != null)
    val isBluetoothSupported: StateFlow<Boolean> = _isBluetoothSupported.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(bluetoothAdapter?.isEnabled == true)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _isSimulationActive = MutableStateFlow(false)
    val isSimulationActive: StateFlow<Boolean> = _isSimulationActive.asStateFlow()

    private var simulationJob: Job? = null
    private var toneGenerator: ToneGenerator? = null
    private var lastFeedbackTime = 0L

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
        } catch (_: Exception) {}
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { handleScanResult(it) }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { handleScanResult(it) }
        }

        override fun onScanFailed(errorCode: Int) {
            _isScanning.value = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleScanResult(result: ScanResult) {
        val device = result.device ?: return
        val mac = device.address ?: return
        val rawRssi = result.rssi
        val scanRecord = result.scanRecord

        // Extrai nome do dispositivo ou do pacote de anúncio
        val rawName = try {
            device.name
        } catch (_: SecurityException) {
            null
        } ?: scanRecord?.deviceName ?: "Tag BLE (${mac.takeLast(5)})"

        val txPower = if (scanRecord?.txPowerLevel != null && scanRecord.txPowerLevel != Int.MIN_VALUE) {
            scanRecord.txPowerLevel
        } else {
            -59
        }

        val telemetry = parseAdvertisementPayload(scanRecord?.bytes)
        val distance = calculateDistanceMeters(rawRssi, txPower)

        val tagType = determineTagType(rawName, scanRecord?.bytes)

        val updatedTag = DiscoveredBleTag(
            macAddress = mac,
            name = rawName,
            rssi = rawRssi,
            distanceMeters = distance,
            txPower = txPower,
            batteryPercent = telemetry.batteryPercent,
            temperatureC = telemetry.temperatureC,
            humidityPercent = telemetry.humidityPercent,
            tagType = tagType,
            lastSeenMillis = System.currentTimeMillis()
        )

        val currentMap = _discoveredTags.value.toMutableMap()
        currentMap[mac] = updatedTag
        _discoveredTags.value = currentMap
    }

    fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val scanPerm = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_SCAN
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val connectPerm = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            scanPerm && connectPerm
        } else {
            val fineLoc = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            fineLoc
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!hasPermissions() || bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            // Se Bluetooth desligado ou permissão pendente, ainda podemos rodar simulação assistida
            return
        }

        try {
            bleScanner = bluetoothAdapter.bluetoothLeScanner
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build()

            val filters = emptyList<ScanFilter>()
            bleScanner?.startScan(filters, settings, scanCallback)
            _isScanning.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _isScanning.value = false
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        try {
            if (_isScanning.value && bleScanner != null) {
                bleScanner?.stopScan(scanCallback)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isScanning.value = false
        }
    }

    /**
     * Calcula a distância aproximada em metros utilizando o modelo de atenuação de sinal logarítmico (Log-distance path loss model)
     * Distância = 10 ^ ((TxPower - RSSI) / (10 * n))
     * n = coeficiente de atenuação do ambiente apícola (2.2 a 2.5 em campo aberto/apiário)
     */
    fun calculateDistanceMeters(rssi: Int, txPower: Int = -59, pathLossExponent: Double = 2.4): Double {
        if (rssi >= 0 || rssi < -110) return 30.0
        val exponent = (txPower - rssi) / (10.0 * pathLossExponent)
        val rawDistance = 10.0.pow(exponent)
        val clamped = rawDistance.coerceIn(0.2, 50.0)
        return (clamped * 10.0).roundToInt() / 10.0
    }

    /**
     * Bip de Proximidade e Vibração Tátil estilo "Quente ou Frio"
     * Frequência aumenta drasticamente conforme o apicultor se aproxima da colmeia/tag.
     */
    fun triggerProximityFeedback(distanceMeters: Double, isAudioEnabled: Boolean = true, isHapticEnabled: Boolean = true) {
        val now = System.currentTimeMillis()
        val intervalMs = when {
            distanceMeters < 0.8 -> 180L // Muito perto (< 1m): bip contínuo e rápido
            distanceMeters < 1.8 -> 350L // 1 a 2 metros
            distanceMeters < 3.5 -> 650L // 2 a 4 metros
            distanceMeters < 6.0 -> 1100L // 4 a 6 metros
            distanceMeters < 10.0 -> 1800L // 6 a 10 metros
            else -> 3000L // > 10 metros
        }

        if (now - lastFeedbackTime < intervalMs) return
        lastFeedbackTime = now

        if (isAudioEnabled) {
            try {
                val toneType = if (distanceMeters < 1.5) ToneGenerator.TONE_PROP_BEEP2 else ToneGenerator.TONE_PROP_BEEP
                toneGenerator?.startTone(toneType, 70)
            } catch (_: Exception) {}
        }

        if (isHapticEnabled) {
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    manager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }

                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val amplitude = when {
                            distanceMeters < 1.0 -> 255
                            distanceMeters < 2.5 -> 180
                            else -> 100
                        }
                        vibrator.vibrate(VibrationEffect.createOneShot(45, amplitude))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(40)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * Inicia a Simulação de Campo Apícola para testes em emulador ou sem tags físicas no momento.
     * Simula aproximação/afastamento em tempo real de beacons de colmeias com sensores de temperatura e umidade.
     */
    fun startSimulation(registeredTagsMacs: List<String> = emptyList()) {
        if (_isSimulationActive.value) return
        _isSimulationActive.value = true

        simulationJob?.cancel()
        simulationJob = scope.launch {
            var step = 0
            while (_isSimulationActive.value) {
                step++
                val simulatedTags = buildSimulatedTags(step, registeredTagsMacs)
                val current = _discoveredTags.value.toMutableMap()
                simulatedTags.forEach { tag ->
                    current[tag.macAddress] = tag
                }
                _discoveredTags.value = current
                delay(1200L)
            }
        }
    }

    fun stopSimulation() {
        _isSimulationActive.value = false
        simulationJob?.cancel()
        simulationJob = null
    }

    private fun buildSimulatedTags(step: Int, registeredTagsMacs: List<String>): List<DiscoveredBleTag> {
        // Variação de distância senoidal para simular caminhada no apiário
        val dist1 = (1.4 + 1.2 * kotlin.math.sin(step * 0.25)).coerceIn(0.4, 4.5)
        val rssi1 = (-52 - (dist1 * 6.5).toInt()).coerceIn(-85, -42)

        val dist2 = (3.8 + 2.0 * kotlin.math.cos(step * 0.2)).coerceIn(1.2, 8.5)
        val rssi2 = (-64 - (dist2 * 5.0).toInt()).coerceIn(-92, -55)

        val dist3 = (8.2 + 3.0 * kotlin.math.sin(step * 0.15 + 1.0)).coerceIn(4.0, 15.0)
        val rssi3 = (-76 - (dist3 * 4.0).toInt()).coerceIn(-98, -68)

        val dist4 = 18.5 // Tag de colmeia distante / limite de sinal
        val rssi4 = -91

        return listOf(
            DiscoveredBleTag(
                macAddress = "C4:7C:8D:11:22:33",
                name = "SmartTag Colmeia 01 (Ninho)",
                rssi = rssi1,
                distanceMeters = (dist1 * 10.0).roundToInt() / 10.0,
                txPower = -59,
                batteryPercent = 94,
                temperatureC = 34.8 + 0.2 * kotlin.math.sin(step * 0.1),
                humidityPercent = 56.0 + 1.0 * kotlin.math.cos(step * 0.1),
                tagType = "SmartTag Apis Pro",
                isRegistered = true,
                assignedHiveName = "Colmeia 01 (Sol Nascente)"
            ),
            DiscoveredBleTag(
                macAddress = "E2:15:F8:44:55:66",
                name = "RuuviTag Sensor Colmeia 02",
                rssi = rssi2,
                distanceMeters = (dist2 * 10.0).roundToInt() / 10.0,
                txPower = -59,
                batteryPercent = 88,
                temperatureC = 35.2 + 0.15 * kotlin.math.sin(step * 0.12),
                humidityPercent = 58.5,
                tagType = "RuuviTag Pro (Sensor)",
                isRegistered = true,
                assignedHiveName = "Colmeia 02 (Sol Nascente)"
            ),
            DiscoveredBleTag(
                macAddress = "FA:BC:12:77:88:99",
                name = "Beacon Anti-Furto Isca #01",
                rssi = rssi3,
                distanceMeters = (dist3 * 10.0).roundToInt() / 10.0,
                txPower = -62,
                batteryPercent = 99,
                temperatureC = 26.8,
                humidityPercent = 68.0,
                tagType = "iBeacon / Anti-Furto",
                isRegistered = true,
                assignedHiveName = "Isca PET #01 (Borda)"
            ),
            DiscoveredBleTag(
                macAddress = "D8:A9:8B:99:AA:01",
                name = "Inkbird IBS-TH2 (Nova Tag)",
                rssi = -70,
                distanceMeters = 3.2,
                txPower = -59,
                batteryPercent = 96,
                temperatureC = 33.9,
                humidityPercent = 61.0,
                tagType = "Inkbird Sensor BLE",
                isRegistered = false,
                assignedHiveName = null
            ),
            DiscoveredBleTag(
                macAddress = "B4:52:7D:66:33:EE",
                name = "BLE Tag Tile Mate",
                rssi = -78,
                distanceMeters = 5.5,
                txPower = -60,
                batteryPercent = 82,
                temperatureC = null,
                humidityPercent = null,
                tagType = "Tile / SmartTag",
                isRegistered = false,
                assignedHiveName = null
            )
        )
    }

    private data class ParsedTelemetry(
        val batteryPercent: Int? = null,
        val temperatureC: Double? = null,
        val humidityPercent: Double? = null
    )

    private fun parseAdvertisementPayload(bytes: ByteArray?): ParsedTelemetry {
        if (bytes == null || bytes.size < 6) return ParsedTelemetry()

        // Decodificador simplificado para formatos comuns de telemetria BLE (RuuviTag, Xiaomi Mijia, BTHome)
        var battery: Int? = null
        var temp: Double? = null
        var hum: Double? = null

        var index = 0
        while (index < bytes.size - 2) {
            val length = bytes[index].toInt() and 0xFF
            if (length == 0 || index + length >= bytes.size) break

            val type = bytes[index + 1].toInt() and 0xFF

            // Manufacturer Specific Data (0xFF)
            if (type == 0xFF && length >= 5) {
                val companyId = ((bytes[index + 3].toInt() and 0xFF) shl 8) or (bytes[index + 2].toInt() and 0xFF)

                // Ruuvi Innovations (0x0499)
                if (companyId == 0x0499 && length >= 18) {
                    val dataFormat = bytes[index + 4].toInt() and 0xFF
                    if (dataFormat == 5) { // Ruuvi RAWv2
                        val rawTemp = ((bytes[index + 5].toInt() shl 8) or (bytes[index + 6].toInt() and 0xFF)).toShort()
                        temp = (rawTemp * 0.005 * 10.0).roundToInt() / 10.0

                        val rawHum = ((bytes[index + 7].toInt() and 0xFF) shl 8) or (bytes[index + 8].toInt() and 0xFF)
                        hum = (rawHum * 0.0025 * 10.0).roundToInt() / 10.0

                        val powerInfo = ((bytes[index + 15].toInt() and 0xFF) shl 8) or (bytes[index + 16].toInt() and 0xFF)
                        val batteryMv = (powerInfo ushr 5) + 1600
                        battery = (((batteryMv - 2200) / 800.0) * 100.0).toInt().coerceIn(0, 100)
                    }
                }
            }

            index += length + 1
        }

        return ParsedTelemetry(battery, temp, hum)
    }

    private fun determineTagType(name: String, bytes: ByteArray?): String {
        val lower = name.lowercase(Locale.ROOT)
        return when {
            lower.contains("ruuvi") -> "RuuviTag Pro (Sensor)"
            lower.contains("inkbird") -> "Inkbird BLE (Sensor)"
            lower.contains("smarttag") -> "Samsung SmartTag"
            lower.contains("airtag") -> "AirTag Beacon"
            lower.contains("tile") -> "Tile BLE"
            lower.contains("ibeacon") -> "Apple iBeacon"
            lower.contains("mijia") || lower.contains("lywsd") -> "Xiaomi Sensor BLE"
            lower.contains("colmeia") || lower.contains("apis") -> "ApisSmart Tag"
            else -> "Tag BLE Genérica"
        }
    }
}
