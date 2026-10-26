package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val taskType: String, // Alimentação Artificial, Tratamento Sanitário, Troca de Rainha, Divisão de Enxame, Revisão / Inspeção, Colheita, Troca de Cera
    val apiaryId: Long? = null,
    val hiveId: Long? = null,
    val apiaryName: String? = null,
    val hiveNumber: String? = null,
    val dueDate: Long,
    val priority: String = "MÉDIA", // BAIXA, MÉDIA, ALTA, URGENTE_SANITÁRIA
    val isCompleted: Boolean = false,
    val completedDate: Long? = null
)
