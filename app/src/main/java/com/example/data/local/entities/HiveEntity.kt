package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hives",
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
data class HiveEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val apiaryId: Long,
    val hiveNumber: String, // e.g., "Colmeia 01", "C-04", "Jataí #02"
    val beeSpecies: String = "Apis mellifera (Africanizada)", // Apis mellifera, Jataí, Mandaçaia, Uruçu, Tiúba, Tubuna, etc.
    val speciesCategory: String = "Apis (Com Ferrão)", // Apis (Com Ferrão), Meliponíneo (Sem Ferrão / ASF)
    val hiveType: String = "Langstroth", // Langstroth, Dadant-Blatt, Top-Bar, Warré, Layens, Schenk, Caixa INPA, Caixa Fernando Oliveira, Caixa PNN, etc.
    val hiveDimensions: String = "Padrão 10 Quadros (448x232mm)", // Dimensões do ninho/melgueira
    val queenStatus: String = "Presente e Marcada", // Presente e Marcada, Presente sem marcação, Ausente / Órfã, Troca Necessária, Virgem
    val queenColor: String = "Branco (2026)", // Verde (2024), Azul (2025), Branco (2026), Amarelo (2027), Vermelho (2028)
    val queenLineage: String = "Italiana (Apis mellifera ligustica)", // Africanizada, Carnica, Buckfast, Mandaçaia MQA, etc.
    val queenAgeMonths: Int = 4,
    val queenIntroductionDate: Long? = null, // Data da última troca/introdução da rainha
    val strengthLevel: String = "Forte", // Forte, Média, Fraca, Crítica
    val temperament: String = "Mansa", // Mansa, Moderada, Agressiva, Muito Agressiva
    val framesCount: Int = 10, // Quadros no ninho
    val supersCount: Int = 1, // Número de melgueiras / sobre-ninhos
    val framesPerSuper: Int = 10, // Quadros por melgueira (ex: 8, 9, 10 quadros)
    val estimatedYieldPerFrameKg: Double = 2.2, // Estimativa de kg de mel por quadro operculado
    val currentWeightKg: Double? = null, // Último peso bruto aferido da colmeia
    val tareWeightKg: Double = 18.0, // Peso da estrutura vazia (madeira + quadros + cera + abelhas)
    val estimatedHoneyStockKg: Double? = null, // currentWeightKg - tareWeightKg (estoque de mel atual)
    val healthStatus: String = "Excelente", // Excelente, Boa, Alerta Sanitário, Em Tratamento
    val lastInspectionDate: Long? = null,
    val qrCode: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val locationAccuracy: Float? = null,
    val locationTimestamp: Long? = null,
    val notes: String = ""
)
