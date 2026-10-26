package com.example.data.repository

import android.content.Context
import com.example.data.ai.ApisSmartAiClient
import com.example.data.ai.StructuredInspectionResult
import com.example.data.local.ApisSmartDatabase
import com.example.data.local.entities.AiConsultationEntity
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.BeeTrapEntity
import com.example.data.local.entities.BluetoothTagEntity
import com.example.data.local.entities.FinancialTransactionEntity
import com.example.data.local.entities.FloralObservationEntity
import com.example.data.local.entities.HarvestEntity
import com.example.data.local.entities.HiveEntity
import com.example.data.local.entities.HivePhotoEntity
import com.example.data.local.entities.HiveWeightEntity
import com.example.data.local.entities.InspectionEntity
import com.example.data.local.entities.InventoryEntity
import com.example.data.local.entities.QualitySealGoalEntity
import com.example.data.local.entities.QueenHistoryEntity
import com.example.data.local.entities.TaskScheduleEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApisSmartRepository(context: Context) {
    private val db = ApisSmartDatabase.getInstance(context)
    private val apiaryDao = db.apiaryDao()
    private val hiveDao = db.hiveDao()
    private val beeTrapDao = db.beeTrapDao()
    private val inspectionDao = db.inspectionDao()
    private val harvestDao = db.harvestDao()
    private val inventoryDao = db.inventoryDao()
    private val taskDao = db.taskScheduleDao()
    private val consultationDao = db.aiConsultationDao()
    private val queenHistoryDao = db.queenHistoryDao()
    private val hiveWeightDao = db.hiveWeightDao()
    private val hivePhotoDao = db.hivePhotoDao()
    private val financialTransactionDao = db.financialTransactionDao()
    private val bluetoothTagDao = db.bluetoothTagDao()
    private val qualitySealDao = db.qualitySealDao()
    private val floralObservationDao = db.floralObservationDao()

    // --- Floral Observations ---
    val allFloralObservations: Flow<List<FloralObservationEntity>> = floralObservationDao.getAllObservations()
    fun getFloralObservationsByApiary(apiaryId: Long): Flow<List<FloralObservationEntity>> = floralObservationDao.getObservationsByApiary(apiaryId)
    suspend fun insertFloralObservation(observation: FloralObservationEntity): Long = floralObservationDao.insertObservation(observation)
    suspend fun updateFloralObservation(observation: FloralObservationEntity) = floralObservationDao.updateObservation(observation)
    suspend fun deleteFloralObservation(observation: FloralObservationEntity) = floralObservationDao.deleteObservation(observation)
    suspend fun deleteFloralObservationById(id: Long) = floralObservationDao.deleteObservationById(id)

    // --- Financial Management ---
    val allFinancialTransactions: Flow<List<FinancialTransactionEntity>> = financialTransactionDao.getAllTransactions()
    fun getFinancialTransactionsByApiary(apiaryId: Long): Flow<List<FinancialTransactionEntity>> = financialTransactionDao.getTransactionsByApiary(apiaryId)
    suspend fun insertFinancialTransaction(transaction: FinancialTransactionEntity): Long = financialTransactionDao.insertTransaction(transaction)
    suspend fun updateFinancialTransaction(transaction: FinancialTransactionEntity) = financialTransactionDao.updateTransaction(transaction)
    suspend fun deleteFinancialTransaction(transaction: FinancialTransactionEntity) = financialTransactionDao.deleteTransaction(transaction)
    suspend fun deleteFinancialTransactionById(id: Long) = financialTransactionDao.deleteTransactionById(id)

    // --- Apiaries ---
    val allApiaries: Flow<List<ApiaryEntity>> = apiaryDao.getAllApiaries()
    fun getApiaryById(id: Long): Flow<ApiaryEntity?> = apiaryDao.getApiaryById(id)
    suspend fun insertApiary(apiary: ApiaryEntity): Long = apiaryDao.insertApiary(apiary)
    suspend fun updateApiary(apiary: ApiaryEntity) = apiaryDao.updateApiary(apiary)
    suspend fun deleteApiary(apiary: ApiaryEntity) = apiaryDao.deleteApiary(apiary)

    // --- Hives ---
    val allHives: Flow<List<HiveEntity>> = hiveDao.getAllHives()
    fun getHivesByApiary(apiaryId: Long): Flow<List<HiveEntity>> = hiveDao.getHivesByApiary(apiaryId)
    fun getHiveById(id: Long): Flow<HiveEntity?> = hiveDao.getHiveById(id)
    val sanitaryAlertCount: Flow<Int> = hiveDao.getSanitaryAlertHivesCount()
    suspend fun insertHive(hive: HiveEntity): Long = hiveDao.insertHive(hive)
    suspend fun updateHive(hive: HiveEntity) = hiveDao.updateHive(hive)
    suspend fun deleteHive(hive: HiveEntity) = hiveDao.deleteHive(hive)

    // --- Queen History & Replacement ---
    fun getQueenHistoryByHive(hiveId: Long): Flow<List<QueenHistoryEntity>> = queenHistoryDao.getQueenHistoryByHive(hiveId)

    suspend fun recordQueenChange(
        hiveId: Long,
        previousLineage: String,
        previousColor: String,
        newLineage: String,
        newColor: String,
        reason: String,
        method: String,
        notes: String
    ): Long {
        val now = System.currentTimeMillis()
        val event = QueenHistoryEntity(
            hiveId = hiveId,
            date = now,
            previousQueenLineage = previousLineage,
            previousQueenColor = previousColor,
            newQueenLineage = newLineage,
            newQueenColor = newColor,
            reason = reason,
            introductionMethod = method,
            acceptanceConfirmed = true,
            notes = notes
        )
        val eventId = queenHistoryDao.insertQueenEvent(event)

        // Update current hive queen specs
        val hive = hiveDao.getHiveByIdOnce(hiveId)
        if (hive != null) {
            hiveDao.updateHive(
                hive.copy(
                    queenLineage = newLineage,
                    queenColor = newColor,
                    queenStatus = "Presente e Marcada",
                    queenAgeMonths = 1,
                    queenIntroductionDate = now
                )
            )
        }
        return eventId
    }

    suspend fun deleteQueenEvent(event: QueenHistoryEntity) = queenHistoryDao.deleteQueenEvent(event)

    // --- Hive Weight & Honey Stock Estimation ---
    fun getWeightsByHive(hiveId: Long): Flow<List<HiveWeightEntity>> = hiveWeightDao.getWeightsByHive(hiveId)

    suspend fun recordHiveWeight(
        hiveId: Long,
        totalWeightKg: Double,
        tareWeightKg: Double,
        notes: String
    ): Long {
        val now = System.currentTimeMillis()
        val previousWeight = hiveWeightDao.getLatestWeightForHive(hiveId)
        val weightDelta = if (previousWeight != null) totalWeightKg - previousWeight.totalWeightKg else null

        val estimatedHoney = (totalWeightKg - tareWeightKg).coerceAtLeast(0.0)

        val status = when {
            estimatedHoney < 4.0 -> "Reserva Crítica / Alimentar"
            weightDelta != null && weightDelta > 1.5 -> "Ganhando Peso (Florada Ativa)"
            weightDelta != null && weightDelta < -1.5 -> "Perda de Peso (Entressafra)"
            else -> "Estável"
        }

        val weightEntity = HiveWeightEntity(
            hiveId = hiveId,
            date = now,
            totalWeightKg = totalWeightKg,
            tareWeightKg = tareWeightKg,
            estimatedHoneyKg = estimatedHoney,
            weightDeltaKg = weightDelta,
            weightStatus = status,
            notes = notes
        )
        val weightId = hiveWeightDao.insertWeight(weightEntity)

        // Update hive current weight and estimated honey stock
        val hive = hiveDao.getHiveByIdOnce(hiveId)
        if (hive != null) {
            hiveDao.updateHive(
                hive.copy(
                    currentWeightKg = totalWeightKg,
                    tareWeightKg = tareWeightKg,
                    estimatedHoneyStockKg = estimatedHoney
                )
            )
        }
        return weightId
    }

    suspend fun deleteHiveWeight(weight: HiveWeightEntity) = hiveWeightDao.deleteWeight(weight)

    // --- Hive Field Photos & Visual Monitoring ---
    fun getPhotosByHive(hiveId: Long): Flow<List<HivePhotoEntity>> = hivePhotoDao.getPhotosByHive(hiveId)
    fun getAllPhotos(): Flow<List<HivePhotoEntity>> = hivePhotoDao.getAllPhotos()
    fun getPhotosCountForHive(hiveId: Long): Flow<Int> = hivePhotoDao.getPhotosCountForHive(hiveId)
    fun getLatestPhotoForHive(hiveId: Long): Flow<HivePhotoEntity?> = hivePhotoDao.getLatestPhotoForHive(hiveId)

    suspend fun insertHivePhoto(photo: HivePhotoEntity): Long = hivePhotoDao.insertPhoto(photo)
    suspend fun saveHivePhoto(
        hiveId: Long,
        photoUri: String,
        category: String,
        caption: String,
        notes: String,
        inspectorName: String = "Apicultor"
    ): Long {
        val entity = HivePhotoEntity(
            hiveId = hiveId,
            photoUri = photoUri,
            dateTaken = System.currentTimeMillis(),
            category = category,
            caption = caption,
            inspectorName = inspectorName,
            notes = notes
        )
        return hivePhotoDao.insertPhoto(entity)
    }

    suspend fun deleteHivePhoto(photo: HivePhotoEntity) = hivePhotoDao.deletePhoto(photo)

    // --- Bee Traps (Iscas de Abelhas) ---
    val allBeeTraps: Flow<List<BeeTrapEntity>> = beeTrapDao.getAllTraps()
    val capturedTrapsCount: Flow<Int> = beeTrapDao.getCapturedTrapsCount()
    val activeTrapsCount: Flow<Int> = beeTrapDao.getActiveTrapsCount()
    suspend fun insertBeeTrap(trap: BeeTrapEntity): Long = beeTrapDao.insertTrap(trap)
    suspend fun updateBeeTrap(trap: BeeTrapEntity) = beeTrapDao.updateTrap(trap)
    suspend fun deleteBeeTrap(trap: BeeTrapEntity) = beeTrapDao.deleteTrap(trap)

    suspend fun transferTrapToHive(
        trap: BeeTrapEntity,
        apiaryId: Long,
        hiveNumber: String,
        hiveType: String,
        beeSpecies: String,
        speciesCategory: String,
        framesCount: Int,
        notes: String
    ): Long {
        val newHive = HiveEntity(
            apiaryId = apiaryId,
            hiveNumber = hiveNumber,
            beeSpecies = beeSpecies,
            speciesCategory = speciesCategory,
            hiveType = hiveType,
            hiveDimensions = if (speciesCategory.contains("Melipon")) "Caixa Modular INPA / Vertical" else "Ninho Padrão 10q",
            queenStatus = "Presente sem marcação",
            queenColor = "Branco (2026)",
            queenLineage = "$beeSpecies (Captura Isca: ${trap.name})",
            strengthLevel = "Média",
            temperament = "Mansa",
            framesCount = framesCount,
            supersCount = 0,
            framesPerSuper = if (speciesCategory.contains("Melipon")) 6 else 10,
            healthStatus = "Excelente",
            qrCode = "APIS-TRAP-${trap.id}-${System.currentTimeMillis() % 1000}",
            latitude = trap.latitude,
            longitude = trap.longitude,
            altitude = trap.altitude,
            locationAccuracy = trap.locationAccuracy,
            locationTimestamp = System.currentTimeMillis(),
            notes = "Origem: Captura na isca '${trap.name}' em ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(trap.captureDate ?: System.currentTimeMillis()))}. $notes"
        )
        val hiveId = hiveDao.insertHive(newHive)

        val updatedTrap = trap.copy(
            status = "Transferida",
            transferredHiveId = hiveId,
            notes = "${trap.notes} | Transferida para Colmeia #$hiveNumber em ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}"
        )
        beeTrapDao.updateTrap(updatedTrap)
        return hiveId
    }

    // --- Inspections ---
    val allInspections: Flow<List<InspectionEntity>> = inspectionDao.getAllInspections()
    fun getInspectionsByHive(hiveId: Long): Flow<List<InspectionEntity>> = inspectionDao.getInspectionsByHive(hiveId)
    fun getInspectionsByApiary(apiaryId: Long): Flow<List<InspectionEntity>> = inspectionDao.getInspectionsByApiary(apiaryId)
    suspend fun insertInspection(inspection: InspectionEntity): Long = inspectionDao.insertInspection(inspection)
    suspend fun updateInspection(inspection: InspectionEntity) = inspectionDao.updateInspection(inspection)
    suspend fun deleteInspection(inspection: InspectionEntity) = inspectionDao.deleteInspection(inspection)

    // --- Harvests ---
    val allHarvests: Flow<List<HarvestEntity>> = harvestDao.getAllHarvests()
    fun getHarvestsByApiary(apiaryId: Long): Flow<List<HarvestEntity>> = harvestDao.getHarvestsByApiary(apiaryId)
    val totalHoneyHarvested: Flow<Double?> = harvestDao.getTotalHoneyHarvested()
    suspend fun insertHarvest(harvest: HarvestEntity): Long = harvestDao.insertHarvest(harvest)
    suspend fun updateHarvest(harvest: HarvestEntity) = harvestDao.updateHarvest(harvest)
    suspend fun deleteHarvest(harvest: HarvestEntity) = harvestDao.deleteHarvest(harvest)

    // --- Inventory ---
    val allInventory: Flow<List<InventoryEntity>> = inventoryDao.getAllInventory()
    val lowStockInventory: Flow<List<InventoryEntity>> = inventoryDao.getLowStockInventory()
    suspend fun insertInventoryItem(item: InventoryEntity): Long = inventoryDao.insertItem(item)
    suspend fun updateInventoryItem(item: InventoryEntity) = inventoryDao.updateItem(item)
    suspend fun deleteInventoryItem(item: InventoryEntity) = inventoryDao.deleteItem(item)

    // --- Tasks ---
    val allTasks: Flow<List<TaskScheduleEntity>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<TaskScheduleEntity>> = taskDao.getPendingTasks()
    val pendingTasksCount: Flow<Int> = taskDao.getPendingTasksCount()
    suspend fun insertTask(task: TaskScheduleEntity): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskScheduleEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskScheduleEntity) = taskDao.deleteTask(task)

    // --- AI Consultations ---
    val allConsultations: Flow<List<AiConsultationEntity>> = consultationDao.getAllConsultations()
    suspend fun insertConsultation(consultation: AiConsultationEntity): Long = consultationDao.insertConsultation(consultation)
    suspend fun clearConsultationHistory() = consultationDao.clearHistory()

    // --- High Level AI Operations ---
    suspend fun analyzeAndSaveFieldInspection(
        rawNotes: String,
        apiaryId: Long,
        hiveId: Long,
        inspectorName: String = "Apicultor Responsável"
    ): StructuredInspectionResult {
        val apiary = apiaryDao.getApiaryByIdOnce(apiaryId)
        val hive = hiveDao.getHiveByIdOnce(hiveId)

        val apiaryName = apiary?.name ?: "Apiário"
        val hiveNumber = hive?.hiveNumber ?: "Colmeia"

        val aiResult = ApisSmartAiClient.analyzeFieldInspection(rawNotes, apiaryName, hiveNumber)

        saveInspectionAndTasks(aiResult, apiaryId, hiveId, apiaryName, hiveNumber, rawNotes, inspectorName)
        return aiResult
    }

    suspend fun analyzeAndSaveCombImage(
        base64Image: String,
        mimeType: String = "image/jpeg",
        apiaryId: Long,
        hiveId: Long,
        inspectorName: String = "Apicultor Responsável"
    ): StructuredInspectionResult {
        val apiary = apiaryDao.getApiaryByIdOnce(apiaryId)
        val hive = hiveDao.getHiveByIdOnce(hiveId)

        val apiaryName = apiary?.name ?: "Apiário"
        val hiveNumber = hive?.hiveNumber ?: "Colmeia"

        val aiResult = ApisSmartAiClient.analyzeCombImage(base64Image, mimeType, apiaryName, hiveNumber)

        saveInspectionAndTasks(
            aiResult,
            apiaryId,
            hiveId,
            apiaryName,
            hiveNumber,
            rawNotes = "Análise óptica de imagem do favo (${hiveNumber})",
            inspectorName = inspectorName
        )
        return aiResult
    }

    private suspend fun saveInspectionAndTasks(
        aiResult: StructuredInspectionResult,
        apiaryId: Long,
        hiveId: Long,
        apiaryName: String,
        hiveNumber: String,
        rawNotes: String,
        inspectorName: String
    ) {
        val now = System.currentTimeMillis()

        val inspection = InspectionEntity(
            hiveId = hiveId,
            apiaryId = apiaryId,
            date = now,
            inspectorName = inspectorName,
            queenSeen = aiResult.queenSeen,
            broodPattern = aiResult.broodPattern,
            eggsSeen = aiResult.eggsSeen,
            larvaeSeen = aiResult.larvaeSeen,
            cappedBroodSeen = aiResult.cappedBroodSeen,
            honeyStores = aiResult.honeyStores,
            pollenStores = aiResult.pollenStores,
            pestDetected = aiResult.pestDetected,
            pestSeverity = aiResult.pestSeverity,
            temperament = aiResult.temperament,
            feedCondition = aiResult.feedCondition,
            notesRaw = rawNotes,
            aiDiagnosticMarkdown = aiResult.diagnosticMarkdown,
            aiSanitaryAlert = aiResult.isSanitaryAlert,
            actionsTaken = aiResult.recommendedTasks.joinToString("; ")
        )
        inspectionDao.insertInspection(inspection)

        // Update hive health status and last inspection
        val currentHive = hiveDao.getHiveByIdOnce(hiveId)
        if (currentHive != null) {
            val updatedHealth = if (aiResult.isSanitaryAlert) "Alerta Sanitário" else if (aiResult.pestSeverity != "Nenhuma") "Em Tratamento" else "Boa"
            val updatedQueenStatus = if (aiResult.queenSeen) currentHive.queenStatus else "Não Avistada"
            hiveDao.updateHive(
                currentHive.copy(
                    healthStatus = updatedHealth,
                    queenStatus = updatedQueenStatus,
                    temperament = if (aiResult.temperament != "Não avaliado") aiResult.temperament else currentHive.temperament,
                    lastInspectionDate = now
                )
            )
        }

        // Insert AI generated tasks into Schedule
        for ((index, taskTitle) in aiResult.recommendedTasks.withIndex()) {
            val due = now + ((index + 1) * 3 * 24 * 60 * 60 * 1000L)
            taskDao.insertTask(
                TaskScheduleEntity(
                    title = taskTitle,
                    description = "Manejo preventivo/corretivo gerado automaticamente pelo diagnóstico inteligente ApisSmart.",
                    taskType = if (aiResult.isSanitaryAlert) "Tratamento Sanitário" else "Manejo Técnico",
                    apiaryId = apiaryId,
                    hiveId = hiveId,
                    apiaryName = apiaryName,
                    hiveNumber = hiveNumber,
                    dueDate = due,
                    priority = if (aiResult.isSanitaryAlert) "ALTA" else "MÉDIA",
                    isCompleted = false
                )
            )
        }
    }

    suspend fun askAssistant(userQuery: String, contextInfo: String = ""): String {
        val apiaries = apiaryDao.getAllApiariesOnce()
        val hives = hiveDao.getAllHivesOnce()
        val traps = beeTrapDao.getAllTrapsOnce()
        val harvests = harvestDao.getAllHarvestsOnce()
        val totalHoney = harvests.filter { it.productType == "Mel" }.sumOf { it.quantityKg }

        val contextBuilder = StringBuilder()
        contextBuilder.appendLine("DADOS DO SISTEMA APISSMART:")
        contextBuilder.appendLine("- Total de Apiários: ${apiaries.size}")
        contextBuilder.appendLine("- Total de Colmeias: ${hives.size}")
        contextBuilder.appendLine("- Total de Iscas Cadastradas: ${traps.size} (Capturadas: ${traps.count { it.status == "Capturada" }})")
        contextBuilder.appendLine("- Produção Total de Mel: %.1f kg".format(Locale.US, totalHoney))
        if (hives.isNotEmpty()) {
            val avgPerHive = totalHoney / hives.size
            contextBuilder.appendLine("- Média de Produção por Colmeia: %.1f kg/colmeia".format(Locale.US, avgPerHive))
        }

        if (contextInfo.isNotBlank()) {
            contextBuilder.appendLine("- Contexto da Tela: $contextInfo")
        }

        val response = ApisSmartAiClient.askAssistant(userQuery, contextBuilder.toString())

        consultationDao.insertConsultation(
            AiConsultationEntity(
                userPrompt = userQuery,
                assistantResponse = response,
                timestamp = System.currentTimeMillis()
            )
        )

        return response
    }

    suspend fun deductNutritionIngredients(sugarKg: Int, yeastKg: Int, soyKg: Int): String {
        val inventory = inventoryDao.getAllInventoryOnce()
        val messages = mutableListOf<String>()

        if (sugarKg > 0) {
            val sugarItem = inventory.find { it.name.contains("Açúcar", ignoreCase = true) || it.name.contains("Xarope", ignoreCase = true) }
            if (sugarItem != null) {
                val newQty = (sugarItem.quantity - sugarKg).coerceAtLeast(0)
                inventoryDao.updateItem(sugarItem.copy(quantity = newQty))
                messages.add("$sugarKg kg de ${sugarItem.name} deduzidos (Restam: $newQty ${sugarItem.unit})")
            } else {
                messages.add("⚠️ Açúcar não localizado no estoque para dedução automática.")
            }
        }

        if (yeastKg > 0) {
            val yeastItem = inventory.find { it.name.contains("Levedura", ignoreCase = true) || it.name.contains("Bife", ignoreCase = true) }
            if (yeastItem != null) {
                val newQty = (yeastItem.quantity - yeastKg).coerceAtLeast(0)
                inventoryDao.updateItem(yeastItem.copy(quantity = newQty))
                messages.add("$yeastKg kg de ${yeastItem.name} deduzidos (Restam: $newQty ${yeastItem.unit})")
            }
        }

        return if (messages.isEmpty()) "Nenhum insumo selecionado para dedução." else messages.joinToString("\n")
    }

    suspend fun generateFieldBookExportCsv(): String {
        val apiaries = apiaryDao.getAllApiariesOnce()
        val hives = hiveDao.getAllHivesOnce()
        val traps = beeTrapDao.getAllTrapsOnce()
        val inspections = inspectionDao.getAllInspectionsOnce()
        val harvests = harvestDao.getAllHarvestsOnce()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val sb = StringBuilder()
        sb.appendLine("=== CADERNO DE CAMPO APÍCOLA - APISSMART ===")
        sb.appendLine("Data de Emissão: ${dateFormat.format(Date())}")
        sb.appendLine()

        sb.appendLine("--- COLMEIAS, RAINHA ATUAL & ESTOQUE DE MEL ---")
        sb.appendLine("ID,Apiário,Número,Espécie,Categoria,Tipo Caixa,Rainha Atual,Cor,Status Rainha,Idade(m),Peso Atual(kg),Tara(kg),Estoque Mel(kg),Sanidade,Latitude,Longitude,Altitude(m)")
        for (h in hives) {
            val apiaryName = apiaries.find { it.id == h.apiaryId }?.name ?: "N/A"
            val lat = h.latitude?.toString() ?: ""
            val lon = h.longitude?.toString() ?: ""
            val alt = h.altitude?.toString() ?: ""
            val weight = h.currentWeightKg?.toString() ?: "N/A"
            val honeyStock = h.estimatedHoneyStockKg?.toString() ?: "N/A"
            sb.appendLine("${h.id},\"$apiaryName\",\"${h.hiveNumber}\",\"${h.beeSpecies}\",\"${h.speciesCategory}\",\"${h.hiveType}\",\"${h.queenLineage}\",\"${h.queenColor}\",\"${h.queenStatus}\",${h.queenAgeMonths},$weight,${h.tareWeightKg},$honeyStock,\"${h.healthStatus}\",$lat,$lon,$alt")
        }
        sb.appendLine()

        sb.appendLine("--- ISCAS DE ABELHAS (SWARM TRAPS) ---")
        sb.appendLine("ID,Nome Isca,Tipo Isca,Espécie Alvo,Atrativo,Status,Data Instalação,Data Captura,Latitude,Longitude,Descrição Local")
        for (t in traps) {
            val instDate = dateFormat.format(Date(t.installationDate))
            val capDate = t.captureDate?.let { dateFormat.format(Date(it)) } ?: ""
            val lat = t.latitude?.toString() ?: ""
            val lon = t.longitude?.toString() ?: ""
            sb.appendLine("${t.id},\"${t.name}\",\"${t.trapType}\",\"${t.targetSpecies}\",\"${t.attractantUsed}\",\"${t.status}\",\"$instDate\",\"$capDate\",$lat,$lon,\"${t.locationDescription}\"")
        }
        sb.appendLine()

        sb.appendLine("--- HISTÓRICO DE COLHEITAS & PRODUÇÃO ---")
        sb.appendLine("ID,Data,Apiário,Colmeia,Produto,Quantidade(kg),Umidade(%),Melgueiras,Quadros,Média(kg/quadro),Florada,Lote")
        for (har in harvests) {
            val apiaryName = apiaries.find { it.id == har.apiaryId }?.name ?: "N/A"
            val hiveNum = hives.find { it.id == har.hiveId }?.hiveNumber ?: "Geral/Apiário"
            val hDate = dateFormat.format(Date(har.date))
            val moist = har.moisturePercent?.toString() ?: "N/A"
            val supers = har.supersHarvestedCount ?: 1
            val frames = har.framesHarvestedCount ?: 0
            val avgFrame = har.avgYieldPerFrameKg?.toString() ?: "N/A"
            sb.appendLine("${har.id},\"$hDate\",\"$apiaryName\",\"$hiveNum\",\"${har.productType}\",${har.quantityKg},$moist,$supers,$frames,$avgFrame,\"${har.floralSource}\",\"${har.batchNumber}\"")
        }
        sb.appendLine()

        sb.appendLine("--- HISTÓRICO DE INSPEÇÕES DE CAMPO ---")
        sb.appendLine("ID,Data,Apiário,Colmeia,Inspetor,Rainha Vista,Padrão Cria,Praga,Severidade,Alerta Sanitário,Notas")
        for (insp in inspections) {
            val apiaryName = apiaries.find { it.id == insp.apiaryId }?.name ?: "N/A"
            val hiveNum = hives.find { it.id == insp.hiveId }?.hiveNumber ?: "N/A"
            val iDate = dateFormat.format(Date(insp.date))
            val notesClean = insp.notesRaw.replace("\"", "\"\"").replace("\n", " ")
            sb.appendLine("${insp.id},\"$iDate\",\"$apiaryName\",\"$hiveNum\",\"${insp.inspectorName}\",${insp.queenSeen},\"${insp.broodPattern}\",\"${insp.pestDetected}\",\"${insp.pestSeverity}\",${insp.aiSanitaryAlert},\"$notesClean\"")
        }

        return sb.toString()
    }

    suspend fun generateTraceabilityReportText(): String {
        val apiaries = apiaryDao.getAllApiariesOnce()
        val hives = hiveDao.getAllHivesOnce()
        val harvests = harvestDao.getAllHarvestsOnce()
        val totalMel = harvests.filter { it.productType == "Mel" }.sumOf { it.quantityKg }
        val avgMelPerHive = if (hives.isNotEmpty()) totalMel / hives.size else 0.0

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val sb = StringBuilder()
        sb.appendLine("📜 LAUDO DE RASTREABILIDADE & PRODUÇÃO APÍCOLA")
        sb.appendLine("Sistema ApisSmart - Gestão Técnica Integrada")
        sb.appendLine("Emitido em: ${dateFormat.format(Date())}")
        sb.appendLine("--------------------------------------------------")
        sb.appendLine("📊 BALANÇO DE PRODUÇÃO & MÉDIAS:")
        sb.appendLine("• Produção Total de Mel: %.1f kg".format(Locale.US, totalMel))
        sb.appendLine("• Total de Colmeias Ativas: ${hives.size}")
        sb.appendLine("• Média de Produção por Colmeia: %.2f kg/colmeia".format(Locale.US, avgMelPerHive))
        sb.appendLine("--------------------------------------------------")
        sb.appendLine("🍯 LOTES RASTREADOS E CONFORMIDADE:")

        if (harvests.isEmpty()) {
            sb.appendLine("Nenhum lote colhido até o momento.")
        } else {
            for (h in harvests) {
                val apiary = apiaries.find { it.id == h.apiaryId }?.name ?: "Apiário Não Identificado"
                val hive = hives.find { it.id == h.hiveId }?.hiveNumber ?: "Múltiplas Colmeias"
                val statusUmidade = when {
                    h.moisturePercent == null -> "Não medida"
                    h.moisturePercent <= 19.5 -> "✅ CONFORME (${h.moisturePercent}%)"
                    else -> "🚨 ALERTA: ${h.moisturePercent}% (Acima de 19.5%)"
                }
                sb.appendLine("• Lote: ${h.batchNumber.ifBlank { "LOTE-${h.id}" }}")
                sb.appendLine("  - Data: ${dateFormat.format(Date(h.date))}")
                sb.appendLine("  - Produto: ${h.productType} (${h.quantityKg} kg)")
                sb.appendLine("  - Melgueiras / Quadros: ${h.supersHarvestedCount ?: 1} melg. / ${h.framesHarvestedCount ?: 0} quadros (Média: ${h.avgYieldPerFrameKg ?: "N/A"} kg/quadro)")
                sb.appendLine("  - Origem: $apiary | $hive")
                sb.appendLine("  - Florada Predominante: ${h.floralSource}")
                sb.appendLine("  - Umidade (Refratômetro): $statusUmidade")
                sb.appendLine()
            }
        }
        sb.appendLine("--------------------------------------------------")
        sb.appendLine("Rastreabilidade garantida com registro georreferenciado.")
        return sb.toString()
    }

    // --- Bluetooth Tag Tracking & Traceability ---
    val allBluetoothTags: Flow<List<BluetoothTagEntity>> = bluetoothTagDao.getAllTags()
    val outOfRangeAlertCount: Flow<Int> = bluetoothTagDao.getOutOfRangeAlertCount()

    fun getBluetoothTagById(id: Long): Flow<BluetoothTagEntity?> = bluetoothTagDao.getTagById(id)
    fun getBluetoothTagByHiveId(hiveId: Long): Flow<BluetoothTagEntity?> = bluetoothTagDao.getTagByHiveId(hiveId)
    suspend fun getBluetoothTagByHiveIdOnce(hiveId: Long): BluetoothTagEntity? = bluetoothTagDao.getTagByHiveIdOnce(hiveId)
    suspend fun getBluetoothTagByMac(mac: String): BluetoothTagEntity? = bluetoothTagDao.getTagByMac(mac)

    suspend fun insertBluetoothTag(tag: BluetoothTagEntity): Long = bluetoothTagDao.insertTag(tag)
    suspend fun updateBluetoothTag(tag: BluetoothTagEntity) = bluetoothTagDao.updateTag(tag)
    suspend fun deleteBluetoothTag(tag: BluetoothTagEntity) = bluetoothTagDao.deleteTag(tag)
    suspend fun deleteBluetoothTagById(id: Long) = bluetoothTagDao.deleteTagById(id)

    suspend fun linkTagToHive(tagId: Long, hive: HiveEntity) {
        val existing = bluetoothTagDao.getTagByIdOnce(tagId) ?: return
        val updated = existing.copy(
            hiveId = hive.id,
            apiaryId = hive.apiaryId,
            assignedItemName = hive.hiveNumber,
            lastLatitude = hive.latitude ?: existing.lastLatitude,
            lastLongitude = hive.longitude ?: existing.lastLongitude
        )
        bluetoothTagDao.updateTag(updated)
    }

    suspend fun unlinkTag(tagId: Long) {
        val existing = bluetoothTagDao.getTagByIdOnce(tagId) ?: return
        val updated = existing.copy(
            hiveId = null,
            apiaryId = null,
            assignedItemName = ""
        )
        bluetoothTagDao.updateTag(updated)
    }

    suspend fun updateTagTelemetry(
        macAddress: String,
        rssi: Int,
        distanceMeters: Double,
        latitude: Double?,
        longitude: Double?,
        battery: Int?,
        temperatureC: Double?,
        humidityPercent: Double?,
        isOutOfRange: Boolean = false
    ) {
        bluetoothTagDao.updateTelemetry(
            macAddress = macAddress,
            timestamp = System.currentTimeMillis(),
            rssi = rssi,
            distance = distanceMeters,
            latitude = latitude,
            longitude = longitude,
            battery = battery,
            temperature = temperatureC,
            humidity = humidityPercent,
            isOutOfRange = isOutOfRange
        )
    }

    // --- Quality Seal Goals & Certifications (Selo ARTE, Orgânico, Indicação Geográfica) ---
    val allQualitySealGoals: Flow<List<QualitySealGoalEntity>> = qualitySealDao.getAllGoals()
    val completedSealGoalsCount: Flow<Int> = qualitySealDao.getCompletedGoalsCount()

    fun getGoalsBySeal(sealType: String): Flow<List<QualitySealGoalEntity>> = qualitySealDao.getGoalsBySeal(sealType)

    suspend fun toggleSealGoal(goalId: Long, completed: Boolean) {
        val now = if (completed) System.currentTimeMillis() else null
        qualitySealDao.toggleGoalCompletion(goalId, completed, now)
    }

    suspend fun updateSealGoal(goal: QualitySealGoalEntity) = qualitySealDao.updateGoal(goal)
    suspend fun insertSealGoal(goal: QualitySealGoalEntity): Long = qualitySealDao.insertGoal(goal)
    suspend fun deleteSealGoal(goalId: Long) = qualitySealDao.deleteGoal(goalId)

    suspend fun generateQualitySealComplianceReport(sealType: String): String {
        val goals = qualitySealDao.getAllGoalsOnce().filter { it.sealType.equals(sealType, ignoreCase = true) }
        val completedCount = goals.count { it.isCompleted }
        val totalCount = goals.size
        val progressPercent = if (totalCount > 0) (completedCount * 100) / totalCount else 0

        val sealTitle = when (sealType.uppercase(Locale.ROOT)) {
            "ARTE" -> "Selo ARTE (Produto Artesanal Tradicional - SIM/SIE)"
            "ORGANICO" -> "Selo de Produto Orgânico (MAPA / Certificadora)"
            "IG" -> "Selo de Indicação Geográfica (INPI - IP / DO)"
            else -> "Selo de Qualidade Apícola"
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val sb = StringBuilder()
        sb.appendLine("📜 DOSSIÊ DE CONFORMIDADE PARA CERTIFICAÇÃO & SELO DE QUALIDADE")
        sb.appendLine("ApisSmart - Gestão Técnica Integrada e Rastreabilidade")
        sb.appendLine("Data de Emissão: ${dateFormat.format(Date())}")
        sb.appendLine("Certificação Alvo: $sealTitle")
        sb.appendLine("Status Global de Conformidade: $progressPercent% ($completedCount de $totalCount metas cumpridas)")
        sb.appendLine("--------------------------------------------------")
        sb.appendLine("📋 CHECKLIST TÉCNICO E AUDITORIA DE REQUISITOS:")
        sb.appendLine()

        goals.forEachIndexed { index, goal ->
            val statusIcon = if (goal.isCompleted) "✅ [CONFORME]" else "⏳ [PENDENTE]"
            sb.appendLine("${index + 1}. $statusIcon ${goal.title}")
            sb.appendLine("   Categoria: ${goal.requirementCategory}")
            sb.appendLine("   Exigência: ${goal.description}")
            if (goal.isCompleted && goal.verificationEvidence.isNotBlank()) {
                sb.appendLine("   Comprovação: ${goal.verificationEvidence}")
            }
            if (goal.completedAt != null) {
                sb.appendLine("   Data de Cumprimento: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(goal.completedAt))}")
            }
            sb.appendLine()
        }

        sb.appendLine("--------------------------------------------------")
        sb.appendLine("Validação gerada automaticamente pelo sistema ApisSmart com geolocalização e histórico de manejo.")
        return sb.toString()
    }
}
