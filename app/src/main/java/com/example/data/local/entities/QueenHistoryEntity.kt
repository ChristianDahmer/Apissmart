package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "queen_history",
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
data class QueenHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hiveId: Long,
    val date: Long = System.currentTimeMillis(),
    val previousQueenLineage: String,
    val previousQueenColor: String,
    val newQueenLineage: String,
    val newQueenColor: String,
    val reason: String = "Substituição por Idade Avançada", // Idade Avançada, Baixa Postura / Falhada, Defensividade / Agressividade, Enxameação, Morte Acidental
    val introductionMethod: String = "Gaiola de Introdução com Pasta Cândi", // Gaiola com Cândi, Realeira Madura, Introdução Direta, Puxada Natural
    val acceptanceConfirmed: Boolean = true,
    val notes: String = ""
)
