package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_consultations")
data class AiConsultationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userPrompt: String,
    val assistantResponse: String,
    val isSanitaryAlert: Boolean = false,
    val contextInfo: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
