package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bee_traps")
data class BeeTrapEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g., "Isca PET 2L - Córrego do Cedro", "Caixa-Isca Ninho 03"
    val trapType: String = "Garrafa PET 2L", // Garrafa PET 2L, Garrafa PET 5L, Caixa de Papelão Resinada, Caixa Núcleo Madeira, Caixa Bambu, Caixa INPA
    val targetSpecies: String = "Apis mellifera (Africanizada)", // Jataí, Mandaçaia, Uruçu, Apis, etc.
    val attractantUsed: String = "Própolis de Jataí + Álcool", // Atrativo Feromônio Apis, Própolis + Capim Cidreira, Cera + Própolis
    val installationDate: Long = System.currentTimeMillis(),
    val status: String = "Armada", // Armada, Capturada, Transferida, Desarmada
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val locationAccuracy: Float? = null,
    val locationDescription: String = "", // e.g. "Presa em forquilha de eucalipto a 2,5m, virada para o leste"
    val apiaryId: Long? = null, // Apiário próximo de referência (opcional)
    val captureDate: Long? = null,
    val transferredHiveId: Long? = null,
    val lastCheckedDate: Long? = null,
    val checkCount: Int = 0,
    val notes: String = ""
)
