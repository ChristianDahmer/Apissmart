package com.example.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.withTransaction
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupManager(private val context: Context) {

    private val db = ApisSmartDatabase.getInstance(context)

    suspend fun getCurrentDatabaseSummary(): BackupSummary = withContext(Dispatchers.IO) {
        val apiaries = db.apiaryDao().getAllApiariesOnce()
        val hives = db.hiveDao().getAllHivesOnce()
        val inspections = db.inspectionDao().getAllInspectionsOnce()
        val harvests = db.harvestDao().getAllHarvestsOnce()
        val inventory = db.inventoryDao().getAllInventoryOnce()
        val tasks = db.taskScheduleDao().getAllTasksOnce()
        val finance = db.financialTransactionDao().getAllTransactionsOnce()
        val queen = db.queenHistoryDao().getAllQueenHistoryOnce()
        val weights = db.hiveWeightDao().getAllWeightsOnce()
        val photos = db.hivePhotoDao().getAllPhotosOnce()
        val traps = db.beeTrapDao().getAllTrapsOnce()
        val tags = db.bluetoothTagDao().getAllTagsOnce()
        val goals = db.qualitySealDao().getAllGoalsOnce()
        val observations = db.floralObservationDao().getAllObservationsOnce()
        val consultations = db.aiConsultationDao().getAllConsultationsOnce()

        val total = apiaries.size + hives.size + inspections.size + harvests.size +
                inventory.size + tasks.size + finance.size + queen.size +
                weights.size + photos.size + traps.size + tags.size +
                goals.size + observations.size + consultations.size

        BackupSummary(
            apiariesCount = apiaries.size,
            hivesCount = hives.size,
            inspectionsCount = inspections.size,
            harvestsCount = harvests.size,
            inventoryCount = inventory.size,
            tasksCount = tasks.size,
            financialCount = finance.size,
            queenHistoryCount = queen.size,
            weightsCount = weights.size,
            photosCount = photos.size,
            trapsCount = traps.size,
            tagsCount = tags.size,
            goalsCount = goals.size,
            observationsCount = observations.size,
            consultationsCount = consultations.size,
            totalCount = total,
            backupDateMillis = System.currentTimeMillis()
        )
    }

    suspend fun generateBackupJson(): Pair<String, BackupSummary> = withContext(Dispatchers.IO) {
        val apiaries = db.apiaryDao().getAllApiariesOnce()
        val hives = db.hiveDao().getAllHivesOnce()
        val inspections = db.inspectionDao().getAllInspectionsOnce()
        val harvests = db.harvestDao().getAllHarvestsOnce()
        val inventory = db.inventoryDao().getAllInventoryOnce()
        val tasks = db.taskScheduleDao().getAllTasksOnce()
        val finance = db.financialTransactionDao().getAllTransactionsOnce()
        val queen = db.queenHistoryDao().getAllQueenHistoryOnce()
        val weights = db.hiveWeightDao().getAllWeightsOnce()
        val photos = db.hivePhotoDao().getAllPhotosOnce()
        val traps = db.beeTrapDao().getAllTrapsOnce()
        val tags = db.bluetoothTagDao().getAllTagsOnce()
        val goals = db.qualitySealDao().getAllGoalsOnce()
        val observations = db.floralObservationDao().getAllObservationsOnce()
        val consultations = db.aiConsultationDao().getAllConsultationsOnce()

        val total = apiaries.size + hives.size + inspections.size + harvests.size +
                inventory.size + tasks.size + finance.size + queen.size +
                weights.size + photos.size + traps.size + tags.size +
                goals.size + observations.size + consultations.size

        val now = System.currentTimeMillis()
        val summary = BackupSummary(
            apiariesCount = apiaries.size,
            hivesCount = hives.size,
            inspectionsCount = inspections.size,
            harvestsCount = harvests.size,
            inventoryCount = inventory.size,
            tasksCount = tasks.size,
            financialCount = finance.size,
            queenHistoryCount = queen.size,
            weightsCount = weights.size,
            photosCount = photos.size,
            trapsCount = traps.size,
            tagsCount = tags.size,
            goalsCount = goals.size,
            observationsCount = observations.size,
            consultationsCount = consultations.size,
            totalCount = total,
            backupDateMillis = now
        )

        val root = JSONObject()

        // Metadata
        val meta = JSONObject().apply {
            put("appName", "ApisSmart")
            put("backupType", "FULL_SYSTEM_BACKUP")
            put("schemaVersion", 8)
            put("appVersion", "1.0.0")
            put("timestamp", now)
            put("dateFormatted", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(now)))
        }
        root.put("metadata", meta)

        // Summary
        val sumObj = JSONObject().apply {
            put("apiariesCount", summary.apiariesCount)
            put("hivesCount", summary.hivesCount)
            put("inspectionsCount", summary.inspectionsCount)
            put("harvestsCount", summary.harvestsCount)
            put("inventoryCount", summary.inventoryCount)
            put("tasksCount", summary.tasksCount)
            put("financialCount", summary.financialCount)
            put("queenHistoryCount", summary.queenHistoryCount)
            put("weightsCount", summary.weightsCount)
            put("photosCount", summary.photosCount)
            put("trapsCount", summary.trapsCount)
            put("tagsCount", summary.tagsCount)
            put("goalsCount", summary.goalsCount)
            put("observationsCount", summary.observationsCount)
            put("consultationsCount", summary.consultationsCount)
            put("totalEntitiesCount", summary.totalCount)
        }
        root.put("summary", sumObj)

        // Data arrays
        val dataObj = JSONObject()

        // Apiaries
        val apiariesArr = JSONArray()
        apiaries.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("location", it.location)
                put("latitude", it.latitude)
                put("longitude", it.longitude)
                put("predominantFlora", it.predominantFlora)
                put("capacity", it.capacity)
                put("notes", it.notes)
                put("createdAt", it.createdAt)
            }
            apiariesArr.put(obj)
        }
        dataObj.put("apiaries", apiariesArr)

        // Hives
        val hivesArr = JSONArray()
        hives.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("apiaryId", it.apiaryId)
                put("hiveNumber", it.hiveNumber)
                put("beeSpecies", it.beeSpecies)
                put("speciesCategory", it.speciesCategory)
                put("hiveType", it.hiveType)
                put("hiveDimensions", it.hiveDimensions)
                put("queenStatus", it.queenStatus)
                put("queenColor", it.queenColor)
                put("queenLineage", it.queenLineage)
                put("queenAgeMonths", it.queenAgeMonths)
                put("queenIntroductionDate", it.queenIntroductionDate)
                put("strengthLevel", it.strengthLevel)
                put("temperament", it.temperament)
                put("framesCount", it.framesCount)
                put("supersCount", it.supersCount)
                put("framesPerSuper", it.framesPerSuper)
                put("estimatedYieldPerFrameKg", it.estimatedYieldPerFrameKg)
                put("currentWeightKg", it.currentWeightKg)
                put("tareWeightKg", it.tareWeightKg)
                put("estimatedHoneyStockKg", it.estimatedHoneyStockKg)
                put("healthStatus", it.healthStatus)
                put("lastInspectionDate", it.lastInspectionDate)
                put("qrCode", it.qrCode)
                put("latitude", it.latitude)
                put("longitude", it.longitude)
                put("altitude", it.altitude)
                put("locationAccuracy", it.locationAccuracy)
                put("locationTimestamp", it.locationTimestamp)
                put("notes", it.notes)
            }
            hivesArr.put(obj)
        }
        dataObj.put("hives", hivesArr)

        // BeeTraps
        val trapsArr = JSONArray()
        traps.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("trapType", it.trapType)
                put("targetSpecies", it.targetSpecies)
                put("attractantUsed", it.attractantUsed)
                put("installationDate", it.installationDate)
                put("status", it.status)
                put("latitude", it.latitude)
                put("longitude", it.longitude)
                put("altitude", it.altitude)
                put("locationAccuracy", it.locationAccuracy)
                put("locationDescription", it.locationDescription)
                put("apiaryId", it.apiaryId)
                put("captureDate", it.captureDate)
                put("transferredHiveId", it.transferredHiveId)
                put("lastCheckedDate", it.lastCheckedDate)
                put("checkCount", it.checkCount)
                put("notes", it.notes)
            }
            trapsArr.put(obj)
        }
        dataObj.put("beeTraps", trapsArr)

        // Inspections
        val inspectionsArr = JSONArray()
        inspections.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("hiveId", it.hiveId)
                put("apiaryId", it.apiaryId)
                put("date", it.date)
                put("inspectorName", it.inspectorName)
                put("queenSeen", it.queenSeen)
                put("broodPattern", it.broodPattern)
                put("eggsSeen", it.eggsSeen)
                put("larvaeSeen", it.larvaeSeen)
                put("cappedBroodSeen", it.cappedBroodSeen)
                put("honeyStores", it.honeyStores)
                put("pollenStores", it.pollenStores)
                put("pestDetected", it.pestDetected)
                put("pestSeverity", it.pestSeverity)
                put("temperament", it.temperament)
                put("feedCondition", it.feedCondition)
                put("notesRaw", it.notesRaw)
                put("aiDiagnosticMarkdown", it.aiDiagnosticMarkdown)
                put("aiSanitaryAlert", it.aiSanitaryAlert)
                put("actionsTaken", it.actionsTaken)
            }
            inspectionsArr.put(obj)
        }
        dataObj.put("inspections", inspectionsArr)

        // Harvests
        val harvestsArr = JSONArray()
        harvests.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("apiaryId", it.apiaryId)
                put("hiveId", it.hiveId)
                put("date", it.date)
                put("productType", it.productType)
                put("quantityKg", it.quantityKg)
                put("moisturePercent", it.moisturePercent)
                put("floralSource", it.floralSource)
                put("supersHarvestedCount", it.supersHarvestedCount)
                put("framesHarvestedCount", it.framesHarvestedCount)
                put("framesPerSuper", it.framesPerSuper)
                put("avgYieldPerFrameKg", it.avgYieldPerFrameKg)
                put("batchNumber", it.batchNumber)
                put("notes", it.notes)
            }
            harvestsArr.put(obj)
        }
        dataObj.put("harvests", harvestsArr)

        // Inventory
        val inventoryArr = JSONArray()
        inventory.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("category", it.category)
                put("quantity", it.quantity)
                put("unit", it.unit)
                put("minThreshold", it.minThreshold)
                put("lastRestockDate", it.lastRestockDate)
                put("notes", it.notes)
            }
            inventoryArr.put(obj)
        }
        dataObj.put("inventory", inventoryArr)

        // Tasks
        val tasksArr = JSONArray()
        tasks.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("title", it.title)
                put("description", it.description)
                put("taskType", it.taskType)
                put("apiaryId", it.apiaryId)
                put("hiveId", it.hiveId)
                put("apiaryName", it.apiaryName)
                put("hiveNumber", it.hiveNumber)
                put("dueDate", it.dueDate)
                put("priority", it.priority)
                put("isCompleted", it.isCompleted)
                put("completedDate", it.completedDate)
            }
            tasksArr.put(obj)
        }
        dataObj.put("tasks", tasksArr)

        // AI Consultations
        val consultationsArr = JSONArray()
        consultations.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("userPrompt", it.userPrompt)
                put("assistantResponse", it.assistantResponse)
                put("isSanitaryAlert", it.isSanitaryAlert)
                put("contextInfo", it.contextInfo)
                put("timestamp", it.timestamp)
            }
            consultationsArr.put(obj)
        }
        dataObj.put("aiConsultations", consultationsArr)

        // Queen History
        val queenArr = JSONArray()
        queen.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("hiveId", it.hiveId)
                put("date", it.date)
                put("previousQueenLineage", it.previousQueenLineage)
                put("previousQueenColor", it.previousQueenColor)
                put("newQueenLineage", it.newQueenLineage)
                put("newQueenColor", it.newQueenColor)
                put("reason", it.reason)
                put("introductionMethod", it.introductionMethod)
                put("acceptanceConfirmed", it.acceptanceConfirmed)
                put("notes", it.notes)
            }
            queenArr.put(obj)
        }
        dataObj.put("queenHistory", queenArr)

        // Hive Weights
        val weightsArr = JSONArray()
        weights.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("hiveId", it.hiveId)
                put("date", it.date)
                put("totalWeightKg", it.totalWeightKg)
                put("tareWeightKg", it.tareWeightKg)
                put("estimatedHoneyKg", it.estimatedHoneyKg)
                put("weightDeltaKg", it.weightDeltaKg)
                put("weightStatus", it.weightStatus)
                put("notes", it.notes)
            }
            weightsArr.put(obj)
        }
        dataObj.put("hiveWeights", weightsArr)

        // Hive Photos
        val photosArr = JSONArray()
        photos.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("hiveId", it.hiveId)
                put("photoUri", it.photoUri)
                put("dateTaken", it.dateTaken)
                put("category", it.category)
                put("caption", it.caption)
                put("inspectorName", it.inspectorName)
                put("notes", it.notes)
            }
            photosArr.put(obj)
        }
        dataObj.put("hivePhotos", photosArr)

        // Financial Transactions
        val financeArr = JSONArray()
        finance.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("type", it.type)
                put("category", it.category)
                put("amount", it.amount)
                put("dateMillis", it.dateMillis)
                put("description", it.description)
                put("apiaryId", it.apiaryId)
                put("paymentStatus", it.paymentStatus)
                put("paymentMethod", it.paymentMethod)
                put("quantityKgOrUnits", it.quantityKgOrUnits)
            }
            financeArr.put(obj)
        }
        dataObj.put("financialTransactions", financeArr)

        // Bluetooth Tags
        val tagsArr = JSONArray()
        tags.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("tagMacAddress", it.tagMacAddress)
                put("tagName", it.tagName)
                put("tagType", it.tagType)
                put("hiveId", it.hiveId)
                put("apiaryId", it.apiaryId)
                put("assignedItemName", it.assignedItemName)
                put("lastSeenTimestamp", it.lastSeenTimestamp)
                put("lastRssi", it.lastRssi)
                put("lastDistanceMeters", it.lastDistanceMeters)
                put("lastLatitude", it.lastLatitude)
                put("lastLongitude", it.lastLongitude)
                put("batteryPercent", it.batteryPercent)
                put("temperatureC", it.temperatureC)
                put("humidityPercent", it.humidityPercent)
                put("txPower", it.txPower)
                put("isAntiTheftEnabled", it.isAntiTheftEnabled)
                put("isOutOfRangeAlert", it.isOutOfRangeAlert)
                put("alertDistanceThresholdMeters", it.alertDistanceThresholdMeters)
                put("notes", it.notes)
            }
            tagsArr.put(obj)
        }
        dataObj.put("bluetoothTags", tagsArr)

        // Quality Seal Goals
        val goalsArr = JSONArray()
        goals.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("sealType", it.sealType)
                put("title", it.title)
                put("description", it.description)
                put("requirementCategory", it.requirementCategory)
                put("isCompleted", it.isCompleted)
                put("completedAt", it.completedAt)
                put("verificationEvidence", it.verificationEvidence)
                put("isMandatory", it.isMandatory)
                put("displayOrder", it.displayOrder)
                put("notes", it.notes)
            }
            goalsArr.put(obj)
        }
        dataObj.put("qualitySealGoals", goalsArr)

        // Floral Observations
        val observationsArr = JSONArray()
        observations.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("apiaryId", it.apiaryId)
                put("plantName", it.plantName)
                put("scientificName", it.scientificName)
                put("resourceType", it.resourceType)
                put("startMonth", it.startMonth)
                put("endMonth", it.endMonth)
                put("abundance", it.abundance)
                put("honeyCharacteristics", it.honeyCharacteristics)
                put("notes", it.notes)
                put("createdAt", it.createdAt)
            }
            observationsArr.put(obj)
        }
        dataObj.put("floralObservations", observationsArr)

        root.put("data", dataObj)

        Pair(root.toString(2), summary)
    }

    suspend fun exportBackupToUri(uri: Uri): BackupRestoreResult = withContext(Dispatchers.IO) {
        try {
            val (jsonContent, summary) = generateBackupJson()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonContent.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            } ?: return@withContext BackupRestoreResult(
                success = false,
                message = "Não foi possível abrir o arquivo de destino para escrita.",
                error = "OutputStream nulo para a URI: $uri"
            )

            BackupRestoreResult(
                success = true,
                message = "Backup completo exportado com sucesso! (${summary.totalCount} registros)",
                summary = summary
            )
        } catch (e: Exception) {
            BackupRestoreResult(
                success = false,
                message = "Falha ao gravar arquivo de backup: ${e.localizedMessage}",
                error = e.stackTraceToString()
            )
        }
    }

    suspend fun createLocalBackupFile(): Pair<File, BackupSummary> = withContext(Dispatchers.IO) {
        val (jsonContent, summary) = generateBackupJson()
        val backupDir = File(context.cacheDir, "backups").apply { if (!exists()) mkdirs() }
        val dateStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val backupFile = File(backupDir, "apissmart_backup_$dateStamp.json")

        FileOutputStream(backupFile).use { out ->
            out.write(jsonContent.toByteArray(Charsets.UTF_8))
            out.flush()
        }

        Pair(backupFile, summary)
    }

    fun getGoogleDriveShareIntent(backupFile: File): Intent {
        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            backupFile
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "Backup ApisSmart - ${backupFile.name}")
            putExtra(
                Intent.EXTRA_TEXT,
                "Backup completo do banco de dados ApisSmart para salvar no Google Drive ou armazenamento em nuvem."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    suspend fun restoreBackupFromUri(uri: Uri, wipeExisting: Boolean = true): BackupRestoreResult = withContext(Dispatchers.IO) {
        try {
            val jsonContent = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).readText()
            } ?: return@withContext BackupRestoreResult(
                success = false,
                message = "Não foi possível ler o arquivo selecionado.",
                error = "InputStream nulo para a URI: $uri"
            )

            restoreBackupFromJson(jsonContent, wipeExisting)
        } catch (e: Exception) {
            BackupRestoreResult(
                success = false,
                message = "Erro ao processar arquivo de restauração: ${e.localizedMessage}",
                error = e.stackTraceToString()
            )
        }
    }

    suspend fun restoreBackupFromJson(jsonContent: String, wipeExisting: Boolean = true): BackupRestoreResult = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonContent)

            // Validate root structure
            if (!root.has("data")) {
                return@withContext BackupRestoreResult(
                    success = false,
                    message = "Arquivo de backup inválido: bloco de dados ('data') ausente.",
                    error = "JSON sem chave 'data'"
                )
            }

            val dataObj = root.getJSONObject("data")

            // Parse tables
            val apiaries = parseApiaries(dataObj.optJSONArray("apiaries"))
            val hives = parseHives(dataObj.optJSONArray("hives"))
            val beeTraps = parseBeeTraps(dataObj.optJSONArray("beeTraps"))
            val inspections = parseInspections(dataObj.optJSONArray("inspections"))
            val harvests = parseHarvests(dataObj.optJSONArray("harvests"))
            val inventory = parseInventory(dataObj.optJSONArray("inventory"))
            val tasks = parseTasks(dataObj.optJSONArray("tasks"))
            val consultations = parseConsultations(dataObj.optJSONArray("aiConsultations"))
            val queenHistory = parseQueenHistory(dataObj.optJSONArray("queenHistory"))
            val hiveWeights = parseHiveWeights(dataObj.optJSONArray("hiveWeights"))
            val hivePhotos = parseHivePhotos(dataObj.optJSONArray("hivePhotos"))
            val financialTransactions = parseFinancialTransactions(dataObj.optJSONArray("financialTransactions"))
            val bluetoothTags = parseBluetoothTags(dataObj.optJSONArray("bluetoothTags"))
            val qualitySealGoals = parseQualitySealGoals(dataObj.optJSONArray("qualitySealGoals"))
            val floralObservations = parseFloralObservations(dataObj.optJSONArray("floralObservations"))

            val totalRestored = apiaries.size + hives.size + inspections.size + harvests.size +
                    inventory.size + tasks.size + financialTransactions.size + queenHistory.size +
                    hiveWeights.size + hivePhotos.size + beeTraps.size + bluetoothTags.size +
                    qualitySealGoals.size + floralObservations.size + consultations.size

            // Execute in database transaction to prevent partial state on error
            db.withTransaction {
                if (wipeExisting) {
                    // Wipe in order to avoid foreign key violations:
                    db.hivePhotoDao().deleteAllPhotos()
                    db.hiveWeightDao().deleteAllWeights()
                    db.queenHistoryDao().deleteAllQueenHistory()
                    db.inspectionDao().deleteAllInspections()
                    db.bluetoothTagDao().deleteAllTags()
                    db.harvestDao().deleteAllHarvests()
                    db.floralObservationDao().deleteAllObservations()
                    db.hiveDao().deleteAllHives()
                    db.apiaryDao().deleteAllApiaries()
                    db.beeTrapDao().deleteAllTraps()
                    db.inventoryDao().deleteAllInventory()
                    db.taskScheduleDao().deleteAllTasks()
                    db.financialTransactionDao().deleteAllTransactions()
                    db.aiConsultationDao().deleteAllConsultations()
                    db.qualitySealDao().deleteAllGoals()
                }

                // Insert parents first
                if (apiaries.isNotEmpty()) db.apiaryDao().insertApiaries(apiaries)
                if (hives.isNotEmpty()) db.hiveDao().insertHives(hives)

                // Insert child tables
                if (inspections.isNotEmpty()) db.inspectionDao().insertInspections(inspections)
                if (harvests.isNotEmpty()) db.harvestDao().insertHarvests(harvests)
                if (queenHistory.isNotEmpty()) db.queenHistoryDao().insertQueenEvents(queenHistory)
                if (hiveWeights.isNotEmpty()) db.hiveWeightDao().insertWeights(hiveWeights)
                if (hivePhotos.isNotEmpty()) db.hivePhotoDao().insertPhotos(hivePhotos)
                if (bluetoothTags.isNotEmpty()) db.bluetoothTagDao().insertTags(bluetoothTags)
                if (floralObservations.isNotEmpty()) db.floralObservationDao().insertObservations(floralObservations)
                if (beeTraps.isNotEmpty()) db.beeTrapDao().insertTraps(beeTraps)
                if (inventory.isNotEmpty()) db.inventoryDao().insertInventory(inventory)
                if (tasks.isNotEmpty()) db.taskScheduleDao().insertTasks(tasks)
                if (financialTransactions.isNotEmpty()) db.financialTransactionDao().insertTransactions(financialTransactions)
                if (consultations.isNotEmpty()) db.aiConsultationDao().insertConsultations(consultations)
                if (qualitySealGoals.isNotEmpty()) db.qualitySealDao().insertGoals(qualitySealGoals)
            }

            val summary = BackupSummary(
                apiariesCount = apiaries.size,
                hivesCount = hives.size,
                inspectionsCount = inspections.size,
                harvestsCount = harvests.size,
                inventoryCount = inventory.size,
                tasksCount = tasks.size,
                financialCount = financialTransactions.size,
                queenHistoryCount = queenHistory.size,
                weightsCount = hiveWeights.size,
                photosCount = hivePhotos.size,
                trapsCount = beeTraps.size,
                tagsCount = bluetoothTags.size,
                goalsCount = qualitySealGoals.size,
                observationsCount = floralObservations.size,
                consultationsCount = consultations.size,
                totalCount = totalRestored,
                backupDateMillis = root.optJSONObject("metadata")?.optLong("timestamp") ?: System.currentTimeMillis()
            )

            BackupRestoreResult(
                success = true,
                message = "Backup restaurado com sucesso! $totalRestored registros recuperados.",
                summary = summary
            )
        } catch (e: Exception) {
            BackupRestoreResult(
                success = false,
                message = "Falha durante restauração: ${e.localizedMessage}",
                error = e.stackTraceToString()
            )
        }
    }

    // --- Parser Helpers ---

    private fun JSONObject.optDoubleOrNull(key: String): Double? =
        if (has(key) && !isNull(key)) optDouble(key) else null

    private fun JSONObject.optLongOrNull(key: String): Long? =
        if (has(key) && !isNull(key)) optLong(key) else null

    private fun JSONObject.optFloatOrNull(key: String): Float? =
        if (has(key) && !isNull(key)) optDouble(key).toFloat() else null

    private fun JSONObject.optIntOrNull(key: String): Int? =
        if (has(key) && !isNull(key)) optInt(key) else null

    private fun parseApiaries(arr: JSONArray?): List<ApiaryEntity> {
        if (arr == null) return emptyList()
        val list = mutableListOf<ApiaryEntity>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                ApiaryEntity(
                    id = o.optLong("id", 0L),
                    name = o.optString("name", "Apiário"),
                    location = o.optString("location", ""),
                    latitude = o.optDoubleOrNull("latitude"),
                    longitude = o.optDoubleOrNull("longitude"),
                    predominantFlora = o.optString("predominantFlora", "Silvestre"),
                    capacity = o.optInt("capacity", 10),
                    notes = o.optString("notes", ""),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    private fun parseHives(arr: JSONArray?): List<HiveEntity> {
        if (arr == null) return emptyList()
        val list = mutableListOf<HiveEntity>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                HiveEntity(
                    id = o.optLong("id", 0L),
                    apiaryId = o.optLong("apiaryId", 1L),
                    hiveNumber = o.optString("hiveNumber", "Colmeia"),
                    beeSpecies = o.optString("beeSpecies", "Apis mellifera (Africanizada)"),
                    speciesCategory = o.optString("speciesCategory", "Apis (Com Ferrão)"),
                    hiveType = o.optString("hiveType", "Langstroth"),
                    hiveDimensions = o.optString("hiveDimensions", "Padrão 10 Quadros (448x232mm)"),
                    queenStatus = o.optString("queenStatus", "Presente e Marcada"),
                    queenColor = o.optString("queenColor", "Branco (2026)"),
                    queenLineage = o.optString("queenLineage", "Italiana (Apis mellifera ligustica)"),
                    queenAgeMonths = o.optInt("queenAgeMonths", 4),
                    queenIntroductionDate = o.optLongOrNull("queenIntroductionDate"),
                    strengthLevel = o.optString("strengthLevel", "Forte"),
                    temperament = o.optString("temperament", "Mansa"),
                    framesCount = o.optInt("framesCount", 10),
                    supersCount = o.optInt("supersCount", 1),
                    framesPerSuper = o.optInt("framesPerSuper", 10),
                    estimatedYieldPerFrameKg = o.optDouble("estimatedYieldPerFrameKg", 2.2),
                    currentWeightKg = o.optDoubleOrNull("currentWeightKg"),
                    tareWeightKg = o.optDouble("tareWeightKg", 18.0),
                    estimatedHoneyStockKg = o.optDoubleOrNull("estimatedHoneyStockKg"),
                    healthStatus = o.optString("healthStatus", "Excelente"),
                    lastInspectionDate = o.optLongOrNull("lastInspectionDate"),
                    qrCode = o.optString("qrCode", ""),
                    latitude = o.optDoubleOrNull("latitude"),
                    longitude = o.optDoubleOrNull("longitude"),
                    altitude = o.optDoubleOrNull("altitude"),
                    locationAccuracy = o.optFloatOrNull("locationAccuracy"),
                    locationTimestamp = o.optLongOrNull("locationTimestamp"),
                    notes = o.optString("notes", "")
                )
            )
        }
        return list
    }

    private fun parseBeeTraps(arr: JSONArray?): List<BeeTrapEntity> {
        if (arr == null) return emptyList()
        val list = mutableListOf<BeeTrapEntity>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                BeeTrapEntity(
                    id = o.optLong("id", 0L),
                    name = o.optString("name", "Isca PET"),
                    trapType = o.optString("trapType", "Garrafa PET 2L"),
                    targetSpecies = o.optString("targetSpecies", "Apis mellifera (Africanizada)"),
                    attractantUsed = o.optString("attractantUsed", "Própolis de Jataí + Álcool"),
                    installationDate = o.optLong("installationDate", System.currentTimeMillis()),
                    status = o.optString("status", "Armada"),
                    latitude = o.optDoubleOrNull("latitude"),
                    longitude = o.optDoubleOrNull("longitude"),
                    altitude = o.optDoubleOrNull("altitude"),
                    locationAccuracy = o.optFloatOrNull("locationAccuracy"),
                    locationDescription = o.optString("locationDescription", ""),
                    apiaryId = o.optLongOrNull("apiaryId"),
                    captureDate = o.optLongOrNull("captureDate"),
                    transferredHiveId = o.optLongOrNull("transferredHiveId"),
                    lastCheckedDate = o.optLongOrNull("lastCheckedDate"),
                    checkCount = o.optInt("checkCount", 0),
                    notes = o.optString("notes", "")
                )
            )
        }
        return list
    }

    private fun parseInspections(arr: JSONArray?): List<InspectionEntity> {
        if (arr == null) return emptyList()
        val list = mutableListOf<InspectionEntity>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                InspectionEntity(
                    id = o.optLong("id", 0L),
                    hiveId = o.optLong("hiveId", 1L),
                    apiaryId = o.optLong("apiaryId", 1L),
                    date = o.optLong("date", System.currentTimeMillis()),
                    inspectorName = o.optString("inspectorName", "Apicultor Responsável"),
                    queenSeen = o.optBoolean("queenSeen", true),
                    broodPattern = o.optString("broodPattern", "Excelente uniforme"),
                    eggsSeen = o.optBoolean("eggsSeen", true),
                    larvaeSeen = o.optBoolean("larvaeSeen", true),
                    cappedBroodSeen = o.optBoolean("cappedBroodSeen", true),
                    honeyStores = o.optString("honeyStores", "Suficiente"),
                    pollenStores = o.optString("pollenStores", "Suficiente"),
                    pestDetected = o.optString("pestDetected", "Nenhuma"),
                    pestSeverity = o.optString("pestSeverity", "Nenhuma"),
                    temperament = o.optString("temperament", "Mansa"),
                    feedCondition = o.optString("feedCondition", "Alimentador Vazio"),
                    notesRaw = o.optString("notesRaw", ""),
                    aiDiagnosticMarkdown = if (o.has("aiDiagnosticMarkdown") && !o.isNull("aiDiagnosticMarkdown")) o.optString("aiDiagnosticMarkdown") else null,
                    aiSanitaryAlert = o.optBoolean("aiSanitaryAlert", false),
                    actionsTaken = o.optString("actionsTaken", "")
                )
            )
        }
        return list
    }

    private fun parseHarvests(arr: JSONArray?): List<HarvestEntity> {
        if (arr == null) return emptyList()
        val list = mutableListOf<HarvestEntity>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                HarvestEntity(
                    id = o.optLong("id", 0L),
                    apiaryId = o.optLong("apiaryId", 1L),
                    hiveId = o.optLongOrNull("hiveId"),
                    date = o.optLong("date", System.currentTimeMillis()),
                    productType = o.optString("productType", "Mel"),
                    quantityKg = o.optDouble("quantityKg", 0.0),
                    moisturePercent = o.optDoubleOrNull("moisturePercent") ?: 17.5,
                    floralSource = o.optString("floralSource", "Silvestre"),
                    supersHarvestedCount = o.optIntOrNull("supersHarvestedCount"),
                    framesHarvestedCount = o.optIntOrNull("framesHarvestedCount"),
                    framesPerSuper = o.optIntOrNull("framesPerSuper"),
                    avgYieldPerFrameKg = o.optDoubleOrNull("avgYieldPerFrameKg"),
                    batchNumber = o.optString("batchNumber", ""),
                    notes = o.optString("notes", "")
                )
            )
        }
        return list
    }

    private fun parseInventory(arr: JSONArray?): List<InventoryEntity> {
        if (arr == null) return emptyList()
        val list = mutableListOf<InventoryEntity>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                InventoryEntity(
                    id = o.optLong("id", 0L),
                    name = o.optString("name", "Item"),
                    category = o.optString("category", "Geral"),
                    quantity = o.optInt("quantity", 0),
                    unit = o.optString("unit", "un"),
                    minThreshold = o.optInt("minThreshold", 5),
                    lastRestockDate = o.optLong("lastRestockDate", System.currentTimeMillis()),
                    notes = o.optString("notes", "")
                )
            )
        }
        return list
    }

    private fun parseTasks(arr: JSONArray?): List<TaskScheduleEntity> {
        if (arr == null) return emptyList()
        val list = mutableListOf<TaskScheduleEntity>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                TaskScheduleEntity(
                    id = o.optLong("id", 0L),
                    title = o.optString("title", "Tarefa"),
                    description = o.optString("description", ""),
                    taskType = o.optString("taskType", "Revisão / Inspeção"),
                    apiaryId = o.optLongOrNull("apiaryId"),
                    hiveId = o.optLongOrNull("hiveId"),
                    apiaryName = if (o.has("apiaryName") && !o.isNull("apiaryName")) o.optString("apiaryName") else null,
                    hiveNumber = if (o.has("hiveNumber") && !o.isNull("hiveNumber")) o.optString("hiveNumber") else null,
                    dueDate = o.optLong("dueDate", System.currentTimeMillis()),
                    priority = o.optString("priority", "MÉDIA"),
                    isCompleted = o.optBoolean("isCompleted", false),
                    completedDate = o.optLongOrNull("completedDate")
                )
            )
        }
        return list
    }

    private fun parseConsultations(arr: JSONArray?): List<AiConsultationEntity> {
        if (arr == null) return emptyList()
        val list = mutableListOf<AiConsultationEntity>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                AiConsultationEntity(
                    id = o.optLong("id", 0L),
                    userPrompt = o.optString("userPrompt", ""),
                    assistantResponse = o.optString("assistantResponse", ""),
                    isSanitaryAlert = o.optBoolean("isSanitaryAlert", false),
                    contextInfo = o.optString("contextInfo", ""),
                    timestamp = o.optLong("timestamp", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    private fun parseQueenHistory(arr: JSONArray?): List<QueenHistoryEntity> {
        if (arr == null) return emptyList()
        val list = mutableListOf<QueenHistoryEntity>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                QueenHistoryEntity(
                    id = o.optLong("id", 0L),
                    hiveId = o.optLong("hiveId", 1L),
                    date = o.optLong("date", System.currentTimeMillis()),
                    previousQueenLineage = o.optString("previousQueenLineage", ""),
                    previousQueenColor = o.optString("previousQueenColor", ""),
                    newQueenLineage = o.optString("newQueenLineage", ""),
                    newQueenColor = o.optString("newQueenColor", ""),
                    reason = o.optString("reason", "Substituição por Idade Avançada"),
                    introductionMethod = o.optString("introductionMethod", "Gaiola de Introdução com Pasta Cândi"),
                    acceptanceConfirmed = o.optBoolean("acceptanceConfirmed", true),
                    notes = o.optString("notes", "")
                )
            )
        }
        return list
    }

    private fun parseHiveWeights(arr: JSONArray?): List<HiveWeightEntity> {
        if (arr == null) return emptyList()
        val list = mutableListOf<HiveWeightEntity>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                HiveWeightEntity(
                    id = o.optLong("id", 0L),
                    hiveId = o.optLong("hiveId", 1L),
                    date = o.optLong("date", System.currentTimeMillis()),
                    totalWeightKg = o.optDouble("totalWeightKg", 20.0),
                    tareWeightKg = o.optDouble("tareWeightKg", 18.0),
                    estimatedHoneyKg = o.optDouble("estimatedHoneyKg", 2.0),
                    weightDeltaKg = o.optDoubleOrNull("weightDeltaKg"),
                    weightStatus = o.optString("weightStatus", "Estável"),
                    notes = o.optString("notes", "")
                )
            )
        }
        return list
    }

    private fun parseHivePhotos(arr: JSONArray?): List<HivePhotoEntity> {
        if (arr == null) return emptyList()
        val list = mutableListOf<HivePhotoEntity>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                HivePhotoEntity(
                    id = o.optLong("id", 0L),
                    hiveId = o.optLong("hiveId", 1L),
                    photoUri = o.optString("photoUri", ""),
                    dateTaken = o.optLong("dateTaken", System.currentTimeMillis()),
                    category = o.optString("category", "Geral da Caixa"),
                    caption = o.optString("caption", ""),
                    inspectorName = o.optString("inspectorName", "Apicultor"),
                    notes = o.optString("notes", "")
                )
            )
        }
        return list
    }

    private fun parseFinancialTransactions(arr: JSONArray?): List<FinancialTransactionEntity> {
        if (arr == null) return emptyList()
        val list = mutableListOf<FinancialTransactionEntity>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                FinancialTransactionEntity(
                    id = o.optLong("id", 0L),
                    type = o.optString("type", "RECEITA"),
                    category = o.optString("category", "Venda de Mel"),
                    amount = o.optDouble("amount", 0.0),
                    dateMillis = o.optLong("dateMillis", System.currentTimeMillis()),
                    description = o.optString("description", ""),
                    apiaryId = o.optLongOrNull("apiaryId"),
                    paymentStatus = o.optString("paymentStatus", "PAGO"),
                    paymentMethod = o.optString("paymentMethod", "PIX"),
                    quantityKgOrUnits = o.optDoubleOrNull("quantityKgOrUnits")
                )
            )
        }
        return list
    }

    private fun parseBluetoothTags(arr: JSONArray?): List<BluetoothTagEntity> {
        if (arr == null) return emptyList()
        val list = mutableListOf<BluetoothTagEntity>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                BluetoothTagEntity(
                    id = o.optLong("id", 0L),
                    tagMacAddress = o.optString("tagMacAddress", ""),
                    tagName = o.optString("tagName", "Tag BLE"),
                    tagType = o.optString("tagType", "iBeacon"),
                    hiveId = o.optLongOrNull("hiveId"),
                    apiaryId = o.optLongOrNull("apiaryId"),
                    assignedItemName = o.optString("assignedItemName", ""),
                    lastSeenTimestamp = o.optLong("lastSeenTimestamp", System.currentTimeMillis()),
                    lastRssi = o.optInt("lastRssi", -62),
                    lastDistanceMeters = o.optDouble("lastDistanceMeters", 1.4),
                    lastLatitude = o.optDoubleOrNull("lastLatitude"),
                    lastLongitude = o.optDoubleOrNull("lastLongitude"),
                    batteryPercent = o.optIntOrNull("batteryPercent"),
                    temperatureC = o.optDoubleOrNull("temperatureC"),
                    humidityPercent = o.optDoubleOrNull("humidityPercent"),
                    txPower = o.optInt("txPower", -59),
                    isAntiTheftEnabled = o.optBoolean("isAntiTheftEnabled", true),
                    isOutOfRangeAlert = o.optBoolean("isOutOfRangeAlert", false),
                    alertDistanceThresholdMeters = o.optDouble("alertDistanceThresholdMeters", 15.0),
                    notes = o.optString("notes", "")
                )
            )
        }
        return list
    }

    private fun parseQualitySealGoals(arr: JSONArray?): List<QualitySealGoalEntity> {
        if (arr == null) return emptyList()
        val list = mutableListOf<QualitySealGoalEntity>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                QualitySealGoalEntity(
                    id = o.optLong("id", 0L),
                    sealType = o.optString("sealType", "ARTE"),
                    title = o.optString("title", "Requisito"),
                    description = o.optString("description", ""),
                    requirementCategory = o.optString("requirementCategory", "Boas Práticas & BPF"),
                    isCompleted = o.optBoolean("isCompleted", false),
                    completedAt = o.optLongOrNull("completedAt"),
                    verificationEvidence = o.optString("verificationEvidence", ""),
                    isMandatory = o.optBoolean("isMandatory", true),
                    displayOrder = o.optInt("displayOrder", 0),
                    notes = o.optString("notes", "")
                )
            )
        }
        return list
    }

    private fun parseFloralObservations(arr: JSONArray?): List<FloralObservationEntity> {
        if (arr == null) return emptyList()
        val list = mutableListOf<FloralObservationEntity>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                FloralObservationEntity(
                    id = o.optLong("id", 0L),
                    apiaryId = o.optLong("apiaryId", 1L),
                    plantName = o.optString("plantName", "Planta"),
                    scientificName = o.optString("scientificName", ""),
                    resourceType = o.optString("resourceType", "NECTAR_AND_POLLEN"),
                    startMonth = o.optInt("startMonth", 1),
                    endMonth = o.optInt("endMonth", 12),
                    abundance = o.optString("abundance", "MÉDIA"),
                    honeyCharacteristics = o.optString("honeyCharacteristics", ""),
                    notes = o.optString("notes", ""),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }
        return list
    }
}
