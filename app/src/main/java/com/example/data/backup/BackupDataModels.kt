package com.example.data.backup

data class BackupSummary(
    val apiariesCount: Int = 0,
    val hivesCount: Int = 0,
    val inspectionsCount: Int = 0,
    val harvestsCount: Int = 0,
    val inventoryCount: Int = 0,
    val tasksCount: Int = 0,
    val financialCount: Int = 0,
    val queenHistoryCount: Int = 0,
    val weightsCount: Int = 0,
    val photosCount: Int = 0,
    val trapsCount: Int = 0,
    val tagsCount: Int = 0,
    val goalsCount: Int = 0,
    val observationsCount: Int = 0,
    val consultationsCount: Int = 0,
    val totalCount: Int = 0,
    val backupDateMillis: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0.0",
    val schemaVersion: Int = 8
)

data class BackupRestoreResult(
    val success: Boolean,
    val message: String,
    val summary: BackupSummary? = null,
    val error: String? = null
)
