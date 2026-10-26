package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hive_photos",
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
data class HivePhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hiveId: Long,
    val photoUri: String, // Content URI, local file path or base64/preset
    val dateTaken: Long = System.currentTimeMillis(),
    val category: String = "Geral da Caixa", // "Ninho & Postura", "Melgueira & Mel", "Rainha & Marcação", "Alvado & Entrada", "Sanidade & Diagnóstico", "Geral da Caixa"
    val caption: String = "",
    val inspectorName: String = "Apicultor",
    val notes: String = ""
)
