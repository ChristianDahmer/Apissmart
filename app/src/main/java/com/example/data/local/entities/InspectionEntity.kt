package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inspections",
    foreignKeys = [
        ForeignKey(
            entity = HiveEntity::class,
            parentColumns = ["id"],
            childColumns = ["hiveId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["hiveId"]), Index(value = ["apiaryId"])]
)
data class InspectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hiveId: Long,
    val apiaryId: Long,
    val date: Long = System.currentTimeMillis(),
    val inspectorName: String = "Apicultor Responsável",
    val queenSeen: Boolean = true,
    val broodPattern: String = "Excelente uniforme", // Excelente uniforme, Regular / Falhada, Ausente, Cria Zanganeira
    val eggsSeen: Boolean = true,
    val larvaeSeen: Boolean = true,
    val cappedBroodSeen: Boolean = true,
    val honeyStores: String = "Suficiente", // Abundante, Suficiente, Escasso, Crítico
    val pollenStores: String = "Suficiente", // Abundante, Suficiente, Escasso
    val pestDetected: String = "Nenhuma", // Nenhuma, Varroa Destructor, Traça da Cera, Besouro das Colmeias (Aethina tumida), Loque Americana, Loque Europeia, Cria Giz
    val pestSeverity: String = "Nenhuma", // Nenhuma, Leve, Moderada, Severa
    val temperament: String = "Mansa", // Mansa, Moderada, Defensiva/Agressiva
    val feedCondition: String = "Alimentador Vazio", // Vazio, Cheio, Não instalado
    val notesRaw: String = "",
    val aiDiagnosticMarkdown: String? = null,
    val aiSanitaryAlert: Boolean = false,
    val actionsTaken: String = ""
)
