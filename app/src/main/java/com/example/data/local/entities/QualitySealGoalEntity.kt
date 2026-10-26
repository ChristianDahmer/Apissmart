package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quality_seal_goals")
data class QualitySealGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sealType: String, // "ARTE", "ORGANICO", "IG"
    val title: String,
    val description: String,
    val requirementCategory: String, // "Legislação & Inspeção", "Boas Práticas & BPF", "Sanidade & Manejo", "Rastreabilidade", "Análise Laboratorial"
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val verificationEvidence: String = "", // Documento, laudo ou registro que comprova
    val isMandatory: Boolean = true, // Se é requisito eliminatório
    val displayOrder: Int = 0,
    val notes: String = ""
)
