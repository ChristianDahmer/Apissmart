package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "harvests",
    foreignKeys = [
        ForeignKey(
            entity = ApiaryEntity::class,
            parentColumns = ["id"],
            childColumns = ["apiaryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["apiaryId"])]
)
data class HarvestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val apiaryId: Long,
    val hiveId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val productType: String = "Mel", // Mel, Própolis Verde, Própolis Marrom, Cera Alveolada, Pólen Apícola, Geleia Real, Apitoxina
    val quantityKg: Double,
    val moisturePercent: Double? = 17.5, // % umidade (refratômetro)
    val floralSource: String = "Silvestre", // Silvestre, Eucalipto, Flor de Laranjeira, Alecrim, etc.
    val supersHarvestedCount: Int? = 1, // Melgueiras colhidas
    val framesHarvestedCount: Int? = 10, // Total de quadros colhidos
    val framesPerSuper: Int? = 10, // Configuração de quadros por melgueira (8, 9, 10)
    val avgYieldPerFrameKg: Double? = 2.2, // Média colhida por quadro (kg/quadro)
    val batchNumber: String = "",
    val notes: String = ""
)
