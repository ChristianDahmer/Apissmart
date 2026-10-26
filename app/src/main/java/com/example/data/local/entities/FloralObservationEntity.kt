package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "floral_observations")
data class FloralObservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val apiaryId: Long,
    val plantName: String,
    val scientificName: String = "",
    val resourceType: String = "NECTAR_AND_POLLEN", // NECTAR, POLLEN, NECTAR_AND_POLLEN, RESIN, HONEYDEW
    val startMonth: Int = 1, // 1 to 12
    val endMonth: Int = 12,
    val abundance: String = "MÉDIA", // BAIXA, MÉDIA, ALTA, MUITO_ALTA
    val honeyCharacteristics: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
