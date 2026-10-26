package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bluetooth_tags",
    foreignKeys = [
        ForeignKey(
            entity = HiveEntity::class,
            parentColumns = ["id"],
            childColumns = ["hiveId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ApiaryEntity::class,
            parentColumns = ["id"],
            childColumns = ["apiaryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["tagMacAddress"], unique = true),
        Index(value = ["hiveId"]),
        Index(value = ["apiaryId"])
    ]
)
data class BluetoothTagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tagMacAddress: String, // Endereço MAC ou identificador BLE (ex: "C4:7C:8D:67:12:34")
    val tagName: String, // Nome customizado (ex: "SmartTag Colmeia 01")
    val tagType: String = "iBeacon", // iBeacon, Eddystone, RuuviTag Pro, SmartTag / Tile, BLE Genérico
    val hiveId: Long? = null, // Vínculo com a Colmeia
    val apiaryId: Long? = null, // Vínculo com o Apiário
    val assignedItemName: String = "", // Nome legível da colmeia/item vinculado
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val lastRssi: Int = -62, // Potência de sinal em dBm (-100 a -30)
    val lastDistanceMeters: Double = 1.4, // Distância estimada em metros calculada pelo RSSI
    val lastLatitude: Double? = null, // Última coordenada GPS onde o smartphone detectou a tag
    val lastLongitude: Double? = null,
    val batteryPercent: Int? = 95, // Nível de bateria da tag BLE (0 a 100%)
    val temperatureC: Double? = null, // Telemetria de sensor BLE (Temperatura interna da colmeia em °C)
    val humidityPercent: Double? = null, // Telemetria de sensor BLE (Umidade interna em %)
    val txPower: Int = -59, // Calibração de sinal a 1 metro
    val isAntiTheftEnabled: Boolean = true, // Alerta se a colmeia for movimentada ou sair do alcance
    val isOutOfRangeAlert: Boolean = false, // Status de alerta de perda de sinal
    val alertDistanceThresholdMeters: Double = 15.0, // Distância máxima antes de disparar alerta
    val notes: String = ""
)
