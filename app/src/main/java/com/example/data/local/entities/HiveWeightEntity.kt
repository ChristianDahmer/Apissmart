package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hive_weights",
    foreignKeys = [
        ForeignKey(
            entity = HiveEntity::class,
            parentColumns = ["id"],
            childColumns = ["hiveId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["hiveId"])]
)
data class HiveWeightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hiveId: Long,
    val date: Long = System.currentTimeMillis(),
    val totalWeightKg: Double, // Peso total bruto aferido na balança
    val tareWeightKg: Double = 18.0, // Tara (madeiramento + quadros vazios + cera + enxame base)
    val estimatedHoneyKg: Double, // totalWeightKg - tareWeightKg (estoque de mel disponível)
    val weightDeltaKg: Double? = null, // Variação em relação à pesagem anterior (+ ganho na florada, - consumo)
    val weightStatus: String = "Estável", // Ganhando Peso (Florada Ativa), Estável, Perda de Peso (Entressafra), Reserva Crítica / Alimentar
    val notes: String = ""
)
