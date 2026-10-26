package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AiConsultationDao
import com.example.data.local.dao.ApiaryDao
import com.example.data.local.dao.BeeTrapDao
import com.example.data.local.dao.BluetoothTagDao
import com.example.data.local.dao.FinancialTransactionDao
import com.example.data.local.dao.HarvestDao
import com.example.data.local.dao.HiveDao
import com.example.data.local.dao.FloralObservationDao
import com.example.data.local.dao.HivePhotoDao
import com.example.data.local.dao.HiveWeightDao
import com.example.data.local.dao.InspectionDao
import com.example.data.local.dao.InventoryDao
import com.example.data.local.dao.QualitySealDao
import com.example.data.local.dao.QueenHistoryDao
import com.example.data.local.dao.TaskScheduleDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ApiaryEntity::class,
        HiveEntity::class,
        BeeTrapEntity::class,
        InspectionEntity::class,
        HarvestEntity::class,
        InventoryEntity::class,
        TaskScheduleEntity::class,
        AiConsultationEntity::class,
        QueenHistoryEntity::class,
        HiveWeightEntity::class,
        HivePhotoEntity::class,
        FinancialTransactionEntity::class,
        BluetoothTagEntity::class,
        QualitySealGoalEntity::class,
        FloralObservationEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class ApisSmartDatabase : RoomDatabase() {
    abstract fun apiaryDao(): ApiaryDao
    abstract fun hiveDao(): HiveDao
    abstract fun beeTrapDao(): BeeTrapDao
    abstract fun inspectionDao(): InspectionDao
    abstract fun harvestDao(): HarvestDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun taskScheduleDao(): TaskScheduleDao
    abstract fun aiConsultationDao(): AiConsultationDao
    abstract fun queenHistoryDao(): QueenHistoryDao
    abstract fun hiveWeightDao(): HiveWeightDao
    abstract fun hivePhotoDao(): HivePhotoDao
    abstract fun financialTransactionDao(): FinancialTransactionDao
    abstract fun bluetoothTagDao(): BluetoothTagDao
    abstract fun qualitySealDao(): QualitySealDao
    abstract fun floralObservationDao(): FloralObservationDao

    companion object {
        @Volatile
        private var INSTANCE: ApisSmartDatabase? = null

        fun getInstance(context: Context): ApisSmartDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ApisSmartDatabase::class.java,
                    "apis_smart_database.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getInstance(context)
                    // Popula dados de teste iniciais apenas se for versão de DEBUG para facilitar o desenvolvimento
                    if (com.example.BuildConfig.DEBUG) {
                        populateInitialData(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // REMOVIDO: A limpeza automática em onOpen na versão release apagava os dados reais salvos pelo apicultor.
                // Agora o banco preserva os dados de produção de forma íntegra.
            }

            private suspend fun populateInitialData(database: ApisSmartDatabase) {
                val now = System.currentTimeMillis()
                val oneDay = 24 * 60 * 60 * 1000L

                // 1. Apiários
                val apiary1Id = database.apiaryDao().insertApiary(
                    ApiaryEntity(
                        name = "Apiário Sol Nascente",
                        location = "Serra da Mantiqueira, SP",
                        latitude = -22.7394,
                        longitude = -45.5913,
                        predominantFlora = "Eucalipto, Silvestre, Alecrim-do-campo",
                        capacity = 25,
                        notes = "Área ensolarada com boa proteção contra ventos frios do sul.",
                        createdAt = now - (30 * oneDay)
                    )
                )

                val apiary2Id = database.apiaryDao().insertApiary(
                    ApiaryEntity(
                        name = "Meliponário Vale das Flores",
                        location = "Campos do Jordão, SP",
                        latitude = -22.7388,
                        longitude = -45.5920,
                        predominantFlora = "Flor de Laranjeira, Assa-peixe, Trevo Branco, Hortênsias",
                        capacity = 18,
                        notes = "Área mista com meliponíneos e colônias de Apis selecionadas.",
                        createdAt = now - (20 * oneDay)
                    )
                )

                // 2. Colmeias com Peso Atual e Estoque de Mel Estimado
                val hive1Id = database.hiveDao().insertHive(
                    HiveEntity(
                        apiaryId = apiary1Id,
                        hiveNumber = "Colmeia 01",
                        beeSpecies = "Apis mellifera ligustica (Italiana)",
                        speciesCategory = "Apis (Com Ferrão)",
                        hiveType = "Langstroth (Padrão 10 Quadros)",
                        hiveDimensions = "Ninho 10q (448x232mm) + Melgueira 10q (448x137mm)",
                        queenStatus = "Presente e Marcada",
                        queenColor = "Branco (2026)",
                        queenLineage = "Italiana Selecionada F1",
                        queenAgeMonths = 4,
                        queenIntroductionDate = now - (120 * oneDay),
                        strengthLevel = "Forte",
                        temperament = "Mansa",
                        framesCount = 10,
                        supersCount = 2,
                        framesPerSuper = 9,
                        estimatedYieldPerFrameKg = 2.4,
                        currentWeightKg = 44.5,
                        tareWeightKg = 22.0,
                        estimatedHoneyStockKg = 22.5,
                        healthStatus = "Excelente",
                        lastInspectionDate = now - (5 * oneDay),
                        qrCode = "APIS-SOL-01",
                        latitude = -22.73942,
                        longitude = -45.59128,
                        altitude = 1240.0,
                        locationAccuracy = 3.5f,
                        locationTimestamp = now - (5 * oneDay),
                        notes = "Colônia matriz vigorosa. Melgueiras com espaçamento de 9 quadros."
                    )
                )

                val hive2Id = database.hiveDao().insertHive(
                    HiveEntity(
                        apiaryId = apiary1Id,
                        hiveNumber = "Colmeia 02",
                        beeSpecies = "Apis mellifera (Africanizada)",
                        speciesCategory = "Apis (Com Ferrão)",
                        hiveType = "Langstroth (Padrão 10 Quadros)",
                        hiveDimensions = "Ninho 10q + Melgueira 8q",
                        queenStatus = "Presente e Marcada",
                        queenColor = "Azul (2025)",
                        queenLineage = "Africanizada Selecionada Própolis",
                        queenAgeMonths = 10,
                        queenIntroductionDate = now - (300 * oneDay),
                        strengthLevel = "Forte",
                        temperament = "Moderada",
                        framesCount = 10,
                        supersCount = 1,
                        framesPerSuper = 8,
                        estimatedYieldPerFrameKg = 2.8,
                        currentWeightKg = 36.2,
                        tareWeightKg = 18.0,
                        estimatedHoneyStockKg = 18.2,
                        healthStatus = "Boa",
                        lastInspectionDate = now - (8 * oneDay),
                        qrCode = "APIS-SOL-02",
                        latitude = -22.73948,
                        longitude = -45.59135,
                        altitude = 1242.0,
                        locationAccuracy = 4.0f,
                        locationTimestamp = now - (8 * oneDay),
                        notes = "Alta produtividade de própolis verde e mel denso."
                    )
                )

                val hive3Id = database.hiveDao().insertHive(
                    HiveEntity(
                        apiaryId = apiary1Id,
                        hiveNumber = "Colmeia 04",
                        beeSpecies = "Apis mellifera (Africanizada)",
                        speciesCategory = "Apis (Com Ferrão)",
                        hiveType = "Langstroth (Padrão 10 Quadros)",
                        hiveDimensions = "Ninho 10q",
                        queenStatus = "Troca Necessária",
                        queenColor = "Verde (2024)",
                        queenLineage = "Italiana cruzada velha",
                        queenAgeMonths = 18,
                        queenIntroductionDate = now - (540 * oneDay),
                        strengthLevel = "Fraca",
                        temperament = "Defensiva",
                        framesCount = 7,
                        supersCount = 0,
                        framesPerSuper = 10,
                        estimatedYieldPerFrameKg = 1.8,
                        currentWeightKg = 19.5,
                        tareWeightKg = 16.0,
                        estimatedHoneyStockKg = 3.5, // Estoque crítico!
                        healthStatus = "Alerta Sanitário",
                        lastInspectionDate = now - (1 * oneDay),
                        qrCode = "APIS-SOL-04",
                        latitude = -22.73955,
                        longitude = -45.59142,
                        altitude = 1239.0,
                        locationAccuracy = 5.0f,
                        locationTimestamp = now - (1 * oneDay),
                        notes = "Postura falhada e traça nos quadros laterais."
                    )
                )

                val hive4Id = database.hiveDao().insertHive(
                    HiveEntity(
                        apiaryId = apiary2Id,
                        hiveNumber = "Mandaçaia M-01",
                        beeSpecies = "Melipona quadrifasciata (Mandaçaia MQA)",
                        speciesCategory = "Meliponíneo (Sem Ferrão / ASF)",
                        hiveType = "Caixa INPA Modular (ASF)",
                        hiveDimensions = "Ninho 20x20x7cm + Sobre-ninho 7cm + 2 Melgueiras 5cm",
                        queenStatus = "Presente e Marcada",
                        queenColor = "Branco (2026)",
                        queenLineage = "Mandaçaia Serra da Mantiqueira",
                        queenAgeMonths = 6,
                        queenIntroductionDate = now - (180 * oneDay),
                        strengthLevel = "Forte",
                        temperament = "Mansa",
                        framesCount = 4,
                        supersCount = 2,
                        framesPerSuper = 6,
                        estimatedYieldPerFrameKg = 0.5,
                        currentWeightKg = 7.8,
                        tareWeightKg = 4.2,
                        estimatedHoneyStockKg = 3.6,
                        healthStatus = "Excelente",
                        lastInspectionDate = now - (3 * oneDay),
                        qrCode = "ASF-MAND-01",
                        latitude = -22.73875,
                        longitude = -45.59210,
                        altitude = 1255.0,
                        locationAccuracy = 3.0f,
                        locationTimestamp = now - (3 * oneDay),
                        notes = "Colônia de Mandaçaia estabilizada em caixa INPA 20x20."
                    )
                )

                // 3. Histórico de Trocas de Rainha (Queen History)
                database.queenHistoryDao().insertQueenEvent(
                    QueenHistoryEntity(
                        hiveId = hive1Id,
                        date = now - (120 * oneDay),
                        previousQueenLineage = "Africanizada rústica",
                        previousQueenColor = "Verde (2024)",
                        newQueenLineage = "Italiana Selecionada F1",
                        newQueenColor = "Branco (2026)",
                        reason = "Substituição para Ganho de Mansidão e Produtividade",
                        introductionMethod = "Gaiola de Introdução com Pasta Cândi",
                        acceptanceConfirmed = true,
                        notes = "Rainha fecundada introduzida após 48h de orfandade. Aceitação 100% confirmada."
                    )
                )

                database.queenHistoryDao().insertQueenEvent(
                    QueenHistoryEntity(
                        hiveId = hive3Id,
                        date = now - (540 * oneDay),
                        previousQueenLineage = "Enxame capturado",
                        previousQueenColor = "Amarelo (2022)",
                        newQueenLineage = "Italiana cruzada",
                        newQueenColor = "Verde (2024)",
                        reason = "Substituição por idade",
                        introductionMethod = "Realeira Madura Enxertada",
                        acceptanceConfirmed = true,
                        notes = "Rainha atual atingiu 18 meses com queda acentuada de postura. Recomenda-se nova troca imediata."
                    )
                )

                // 4. Histórico de Pesagens da Colmeia (Hive Weight Tracking)
                database.hiveWeightDao().insertWeight(
                    HiveWeightEntity(
                        hiveId = hive1Id,
                        date = now - (30 * oneDay),
                        totalWeightKg = 32.0,
                        tareWeightKg = 22.0,
                        estimatedHoneyKg = 10.0,
                        weightDeltaKg = 0.0,
                        weightStatus = "Estável",
                        notes = "Início da florada de Eucalipto."
                    )
                )
                database.hiveWeightDao().insertWeight(
                    HiveWeightEntity(
                        hiveId = hive1Id,
                        date = now - (15 * oneDay),
                        totalWeightKg = 39.0,
                        tareWeightKg = 22.0,
                        estimatedHoneyKg = 17.0,
                        weightDeltaKg = 7.0,
                        weightStatus = "Ganhando Peso (Florada Ativa)",
                        notes = "Ganho expressivo de peso diário (~460g/dia)."
                    )
                )
                database.hiveWeightDao().insertWeight(
                    HiveWeightEntity(
                        hiveId = hive1Id,
                        date = now - (5 * oneDay),
                        totalWeightKg = 44.5,
                        tareWeightKg = 22.0,
                        estimatedHoneyKg = 22.5,
                        weightDeltaKg = 5.5,
                        weightStatus = "Ganhando Peso (Florada Ativa)",
                        notes = "Melgueiras cheias e operculando. Pronta para colheita."
                    )
                )

                database.hiveWeightDao().insertWeight(
                    HiveWeightEntity(
                        hiveId = hive3Id,
                        date = now - (20 * oneDay),
                        totalWeightKg = 23.0,
                        tareWeightKg = 16.0,
                        estimatedHoneyKg = 7.0,
                        weightDeltaKg = 0.0,
                        weightStatus = "Estável",
                        notes = "Peso base de ninho."
                    )
                )
                database.hiveWeightDao().insertWeight(
                    HiveWeightEntity(
                        hiveId = hive3Id,
                        date = now - (1 * oneDay),
                        totalWeightKg = 19.5,
                        tareWeightKg = 16.0,
                        estimatedHoneyKg = 3.5,
                        weightDeltaKg = -3.5,
                        weightStatus = "Reserva Crítica / Alimentar",
                        notes = "Perda de 3.5kg. Reserva crítica de mel (menos de 4kg). Necessário fornecer alimentação artificial urgente."
                    )
                )

                // 5. Iscas de Abelhas (Bee Traps)
                database.beeTrapDao().insertTrap(
                    BeeTrapEntity(
                        name = "Isca PET 2L #01 - Borda do Eucaliptal",
                        trapType = "Garrafa PET 2L",
                        targetSpecies = "Tetragonisca angustula (Jataí)",
                        attractantUsed = "Extrato Alcoólico de Própolis de Jataí + Cera",
                        installationDate = now - (14 * oneDay),
                        status = "Capturada",
                        latitude = -22.73912,
                        longitude = -45.59080,
                        altitude = 1245.0,
                        locationAccuracy = 4.2f,
                        locationDescription = "Fixada em tronco de eucalipto a 2.5m de altura, entrada virada para Leste.",
                        apiaryId = apiary1Id,
                        captureDate = now - (2 * oneDay),
                        lastCheckedDate = now - (2 * oneDay),
                        checkCount = 3,
                        notes = "Enxameamento de Jataí capturado! Tubo de cera amarelo construído na entrada. Pronta para transferência."
                    )
                )

                // 6. Inspeções
                database.inspectionDao().insertInspection(
                    InspectionEntity(
                        hiveId = hive3Id,
                        apiaryId = apiary1Id,
                        date = now - (1 * oneDay),
                        inspectorName = "João Silva",
                        queenSeen = true,
                        broodPattern = "Regular / Falhada",
                        eggsSeen = true,
                        larvaeSeen = true,
                        cappedBroodSeen = true,
                        honeyStores = "Escasso",
                        pollenStores = "Escasso",
                        pestDetected = "Traça da Cera (Galleria mellonella)",
                        pestSeverity = "Moderada",
                        temperament = "Defensiva",
                        feedCondition = "Alimentador Vazio",
                        notesRaw = "Inspeção no Apiário Sol Nascente, Colmeia 04. Vi ovos do dia, mas a postura está falhada. Muitas traças nos quadros laterais. Alimentador interno está vazio.",
                        aiDiagnosticMarkdown = """
🚨 **Alerta Sanitário & Diagnóstico - Colmeia 04 (Apiário Sol Nascente)**

- **Status da Rainha:** Presente (postura recente identificada), porém com **baixa performance** (padrão de postura falhada). Avaliar substituição da rainha em até 15 dias.
- **Problema Detectado:** Infestação por Traça da Cera (*Galleria mellonella*).
- **Ações Imediatas Recomendadas:**
  1. Reduzir o espaço interno da caixa removendo quadros velhos/não povoados.
  2. Fortalecer o enxame com alimentação de incentivo.
  3. Raspar fundos e laterais afetadas.
- **Manejo Agendado:**
  - [ ] Alimentação proteico-energética (Prazo: Hoje)
  - [ ] Reinspeção de sanidade (Prazo: 7 dias)
                        """.trimIndent(),
                        aiSanitaryAlert = true,
                        actionsTaken = "Quadros velhos separados para descarte. Raspado o fundo da colmeia."
                    )
                )

                // 7. Colheitas (Histórico mensal para gráficos de crescimento de produção)
                // Colmeia 01 (Colheitas em Maio, Junho, Julho, Agosto, Setembro demonstrando crescimento vigoroso)
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary1Id,
                        hiveId = hive1Id,
                        date = now - (135 * oneDay), // ~Maio
                        productType = "Mel",
                        quantityKg = 24.5,
                        moisturePercent = 18.1,
                        floralSource = "Flores Silvestres",
                        supersHarvestedCount = 1,
                        framesHarvestedCount = 10,
                        framesPerSuper = 10,
                        avgYieldPerFrameKg = 2.45,
                        batchNumber = "LOTE-SOL-202605A",
                        notes = "Início da safra de outono."
                    )
                )
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary1Id,
                        hiveId = hive1Id,
                        date = now - (105 * oneDay), // ~Junho
                        productType = "Mel",
                        quantityKg = 31.0,
                        moisturePercent = 17.8,
                        floralSource = "Cipó-Uva e Eucalipto",
                        supersHarvestedCount = 2,
                        framesHarvestedCount = 16,
                        framesPerSuper = 8,
                        avgYieldPerFrameKg = 1.94,
                        batchNumber = "LOTE-SOL-202606A",
                        notes = "Crescimento acentuado após introdução de nova rainha."
                    )
                )
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary1Id,
                        hiveId = hive1Id,
                        date = now - (75 * oneDay), // ~Julho
                        productType = "Mel",
                        quantityKg = 38.4,
                        moisturePercent = 17.5,
                        floralSource = "Eucalipto Robusta",
                        supersHarvestedCount = 2,
                        framesHarvestedCount = 18,
                        framesPerSuper = 9,
                        avgYieldPerFrameKg = 2.13,
                        batchNumber = "LOTE-SOL-202607A",
                        notes = "Pico de florada de inverno."
                    )
                )
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary1Id,
                        hiveId = hive1Id,
                        date = now - (45 * oneDay), // ~Agosto
                        productType = "Mel",
                        quantityKg = 41.0,
                        moisturePercent = 17.0,
                        floralSource = "Eucalipto e Assa-Peixe",
                        supersHarvestedCount = 2,
                        framesHarvestedCount = 18,
                        framesPerSuper = 9,
                        avgYieldPerFrameKg = 2.28,
                        batchNumber = "LOTE-SOL-202608A",
                        notes = "Colmeia com excelente vigor e postura cheia."
                    )
                )
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary1Id,
                        hiveId = hive1Id,
                        date = now - (15 * oneDay), // ~Setembro
                        productType = "Mel",
                        quantityKg = 46.8,
                        moisturePercent = 16.9,
                        floralSource = "Eucalipto e Silvestre",
                        supersHarvestedCount = 2,
                        framesHarvestedCount = 18,
                        framesPerSuper = 9,
                        avgYieldPerFrameKg = 2.60,
                        batchNumber = "LOTE-SOL-202609A",
                        notes = "Recorde de colheita da temporada para a Colmeia 01!"
                    )
                )

                // Colmeia 02 (Colheitas em Maio, Junho, Julho, Agosto, Setembro)
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary1Id,
                        hiveId = hive2Id,
                        date = now - (135 * oneDay),
                        productType = "Mel",
                        quantityKg = 18.0,
                        moisturePercent = 18.5,
                        floralSource = "Flores Silvestres",
                        supersHarvestedCount = 1,
                        framesHarvestedCount = 9,
                        framesPerSuper = 9,
                        avgYieldPerFrameKg = 2.0,
                        batchNumber = "LOTE-SOL-202605B",
                        notes = "Enxame em desenvolvimento inicial."
                    )
                )
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary1Id,
                        hiveId = hive2Id,
                        date = now - (105 * oneDay),
                        productType = "Mel",
                        quantityKg = 23.5,
                        moisturePercent = 18.0,
                        floralSource = "Cipó-Uva",
                        supersHarvestedCount = 1,
                        framesHarvestedCount = 10,
                        framesPerSuper = 10,
                        avgYieldPerFrameKg = 2.35,
                        batchNumber = "LOTE-SOL-202606B",
                        notes = "Boa taxa de enchimento."
                    )
                )
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary1Id,
                        hiveId = hive2Id,
                        date = now - (75 * oneDay),
                        productType = "Mel",
                        quantityKg = 29.0,
                        moisturePercent = 17.6,
                        floralSource = "Eucalipto",
                        supersHarvestedCount = 2,
                        framesHarvestedCount = 16,
                        framesPerSuper = 8,
                        avgYieldPerFrameKg = 1.81,
                        batchNumber = "LOTE-SOL-202607B",
                        notes = "Produção regular e consistente."
                    )
                )
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary1Id,
                        hiveId = hive2Id,
                        date = now - (45 * oneDay),
                        productType = "Mel",
                        quantityKg = 33.2,
                        moisturePercent = 17.2,
                        floralSource = "Assa-Peixe",
                        supersHarvestedCount = 2,
                        framesHarvestedCount = 16,
                        framesPerSuper = 8,
                        avgYieldPerFrameKg = 2.07,
                        batchNumber = "LOTE-SOL-202608B",
                        notes = "Crescimento contínuo."
                    )
                )
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary1Id,
                        hiveId = hive2Id,
                        date = now - (15 * oneDay),
                        productType = "Mel",
                        quantityKg = 36.0,
                        moisturePercent = 17.1,
                        floralSource = "Eucalipto e Flores Silvestres",
                        supersHarvestedCount = 2,
                        framesHarvestedCount = 18,
                        framesPerSuper = 9,
                        avgYieldPerFrameKg = 2.00,
                        batchNumber = "LOTE-SOL-202609B",
                        notes = "Boa melada."
                    )
                )

                // Colmeia 04 (Colmeia com queda de produção antes do alerta sanitário)
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary1Id,
                        hiveId = hive3Id,
                        date = now - (135 * oneDay),
                        productType = "Mel",
                        quantityKg = 22.0,
                        moisturePercent = 18.2,
                        floralSource = "Silvestre",
                        supersHarvestedCount = 1,
                        framesHarvestedCount = 10,
                        framesPerSuper = 10,
                        avgYieldPerFrameKg = 2.2,
                        batchNumber = "LOTE-SOL-202605C",
                        notes = "Boa colheita inicial."
                    )
                )
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary1Id,
                        hiveId = hive3Id,
                        date = now - (105 * oneDay),
                        productType = "Mel",
                        quantityKg = 19.5,
                        moisturePercent = 18.4,
                        floralSource = "Cipó-Uva",
                        supersHarvestedCount = 1,
                        framesHarvestedCount = 9,
                        framesPerSuper = 9,
                        avgYieldPerFrameKg = 2.16,
                        batchNumber = "LOTE-SOL-202606C",
                        notes = "Pequena retração na produção."
                    )
                )
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary1Id,
                        hiveId = hive3Id,
                        date = now - (75 * oneDay),
                        productType = "Mel",
                        quantityKg = 14.0,
                        moisturePercent = 18.9,
                        floralSource = "Eucalipto",
                        supersHarvestedCount = 1,
                        framesHarvestedCount = 8,
                        framesPerSuper = 8,
                        avgYieldPerFrameKg = 1.75,
                        batchNumber = "LOTE-SOL-202607C",
                        notes = "Queda devido ao envelhecimento da rainha."
                    )
                )
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary1Id,
                        hiveId = hive3Id,
                        date = now - (45 * oneDay),
                        productType = "Mel",
                        quantityKg = 8.5,
                        moisturePercent = 19.2,
                        floralSource = "Silvestre",
                        supersHarvestedCount = 1,
                        framesHarvestedCount = 5,
                        framesPerSuper = 10,
                        avgYieldPerFrameKg = 1.70,
                        batchNumber = "LOTE-SOL-202608C",
                        notes = "Queda acentuada. Início de infestação de traças e orfandade parcial."
                    )
                )

                // Colmeia Mandaçaia M-01 (ASF - Meliponíneo)
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary2Id,
                        hiveId = hive4Id,
                        date = now - (105 * oneDay),
                        productType = "Mel de Melipona (ASF)",
                        quantityKg = 1.8,
                        moisturePercent = 23.5,
                        floralSource = "Mata Atlântica Nativa",
                        supersHarvestedCount = 1,
                        framesHarvestedCount = 4,
                        framesPerSuper = 4,
                        avgYieldPerFrameKg = 0.45,
                        batchNumber = "LOTE-ASF-202606A",
                        notes = "Mel líquido nobre de pote de cera."
                    )
                )
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary2Id,
                        hiveId = hive4Id,
                        date = now - (45 * oneDay),
                        productType = "Mel de Melipona (ASF)",
                        quantityKg = 2.4,
                        moisturePercent = 22.8,
                        floralSource = "Mata Atlântica Nativa",
                        supersHarvestedCount = 2,
                        framesHarvestedCount = 6,
                        framesPerSuper = 6,
                        avgYieldPerFrameKg = 0.40,
                        batchNumber = "LOTE-ASF-202608A",
                        notes = "Colheita por sucção com bomba a vácuo limpa."
                    )
                )

                // Subprodutos (Própolis, Cera)
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary1Id,
                        hiveId = hive1Id,
                        date = now - (20 * oneDay),
                        productType = "Própolis Verde",
                        quantityKg = 3.8,
                        moisturePercent = null,
                        floralSource = "Alecrim-do-Campo (Baccharis dracunculifolia)",
                        supersHarvestedCount = 0,
                        framesHarvestedCount = 0,
                        framesPerSuper = null,
                        avgYieldPerFrameKg = null,
                        batchNumber = "PROP-VERDE-202609",
                        notes = "Coleta por coletores de própolis (CPI)."
                    )
                )
                database.harvestDao().insertHarvest(
                    HarvestEntity(
                        apiaryId = apiary1Id,
                        hiveId = null,
                        date = now - (25 * oneDay),
                        productType = "Cera de Abelha",
                        quantityKg = 6.2,
                        moisturePercent = null,
                        floralSource = "Opérculos de Cera",
                        supersHarvestedCount = null,
                        framesHarvestedCount = null,
                        framesPerSuper = null,
                        avgYieldPerFrameKg = null,
                        batchNumber = "CERA-SOL-202609",
                        notes = "Derretimento e purificação de opérculos de colheita."
                    )
                )

                // 8. Estoque
                database.inventoryDao().insertItem(
                    InventoryEntity(name = "Balança Digital de Gancho (50kg)", category = "Equipamentos/EPI", quantity = 2, unit = "un", minThreshold = 1)
                )
                database.inventoryDao().insertItem(
                    InventoryEntity(name = "Gaiola de Introdução de Rainhas", category = "Manejo de Rainhas", quantity = 10, unit = "un", minThreshold = 3)
                )
                database.inventoryDao().insertItem(
                    InventoryEntity(name = "Marcador de Rainha (Tinta Branca 2026)", category = "Manejo de Rainhas", quantity = 3, unit = "un", minThreshold = 1)
                )

                // 9. Tarefas
                database.taskScheduleDao().insertTask(
                    TaskScheduleEntity(
                        title = "Troca de Rainha Urgente - Colmeia 04",
                        description = "Introduzir rainha nova fecundada em gaiola com cândi. Descartar rainha de 2024.",
                        taskType = "Troca de Rainha",
                        apiaryId = apiary1Id,
                        hiveId = hive3Id,
                        apiaryName = "Apiário Sol Nascente",
                        hiveNumber = "Colmeia 04",
                        dueDate = now + (2 * oneDay),
                        priority = "ALTA",
                        isCompleted = false
                    )
                )

                // 10. Fotos de Campo das Colmeias
                database.hivePhotoDao().insertPhoto(
                    HivePhotoEntity(
                        hiveId = hive1Id,
                        photoUri = "sample://comb_uniform_brood",
                        dateTaken = now - (3 * oneDay),
                        category = "Ninho & Postura",
                        caption = "Postura uniforme em espiral da rainha Buckfast",
                        inspectorName = "Carlos Silva",
                        notes = "Excelente padrão de opérculos, ausência de falhas ou células mortas."
                    )
                )
                database.hivePhotoDao().insertPhoto(
                    HivePhotoEntity(
                        hiveId = hive1Id,
                        photoUri = "sample://honey_supers",
                        dateTaken = now - (15 * oneDay),
                        category = "Melgueira & Mel",
                        caption = "Melgueira 100% operculada pré-colheita",
                        inspectorName = "Carlos Silva",
                        notes = "Umidade estimada de 17%, colheita realizada com sucesso."
                    )
                )
                database.hivePhotoDao().insertPhoto(
                    HivePhotoEntity(
                        hiveId = hive2Id,
                        photoUri = "sample://queen_marked",
                        dateTaken = now - (7 * oneDay),
                        category = "Rainha & Marcação",
                        caption = "Rainha Italiana F1 marcada de branco (2026)",
                        inspectorName = "Carlos Silva",
                        notes = "Excelente tamanho abdominal, acompanhada por cortejo abundante."
                    )
                )
                database.hivePhotoDao().insertPhoto(
                    HivePhotoEntity(
                        hiveId = hive4Id,
                        photoUri = "sample://entrance_activity",
                        dateTaken = now - (1 * oneDay),
                        category = "Alvado & Entrada",
                        caption = "Tubo de entrada característico de Jataí com batedoras ativas",
                        inspectorName = "Carlos Silva",
                        notes = "Grande fluxo de entrada de pólen amarelo e resina."
                    )
                )

                // 11. Gestão Financeira Inicial do Apiário
                database.financialTransactionDao().insertTransaction(
                    FinancialTransactionEntity(
                        type = "RECEITA",
                        category = "Venda de Mel",
                        amount = 3200.00,
                        dateMillis = now - (5 * oneDay),
                        description = "Venda de 80kg de Mel de Eucalipto a R$ 40/kg (Atacado local)",
                        apiaryId = apiary1Id,
                        paymentStatus = "PAGO",
                        paymentMethod = "PIX",
                        quantityKgOrUnits = 80.0
                    )
                )
                database.financialTransactionDao().insertTransaction(
                    FinancialTransactionEntity(
                        type = "RECEITA",
                        category = "Venda de Própolis",
                        amount = 950.00,
                        dateMillis = now - (8 * oneDay),
                        description = "Venda de 1.5kg de Própolis Verde Extramacia",
                        apiaryId = apiary1Id,
                        paymentStatus = "PAGO",
                        paymentMethod = "PIX",
                        quantityKgOrUnits = 1.5
                    )
                )
                database.financialTransactionDao().insertTransaction(
                    FinancialTransactionEntity(
                        type = "DESPESA",
                        category = "Insumos/Alimentação",
                        amount = 480.00,
                        dateMillis = now - (12 * oneDay),
                        description = "Compra de 100kg de Açúcar VHP + Promotor de Crescimento para xarope de entre-safra",
                        apiaryId = apiary1Id,
                        paymentStatus = "PAGO",
                        paymentMethod = "Cartão de Crédito",
                        quantityKgOrUnits = 100.0
                    )
                )
                database.financialTransactionDao().insertTransaction(
                    FinancialTransactionEntity(
                        type = "DESPESA",
                        category = "Equipamentos/Caixas",
                        amount = 1250.00,
                        dateMillis = now - (18 * oneDay),
                        description = "Aquisição de 5 novas caixas Langstroth completas com quadros e cera alveolada",
                        apiaryId = apiary2Id,
                        paymentStatus = "PAGO",
                        paymentMethod = "Boleto",
                        quantityKgOrUnits = 5.0
                    )
                )
                database.financialTransactionDao().insertTransaction(
                    FinancialTransactionEntity(
                        type = "RECEITA",
                        category = "Venda de Enxames/Rainhas",
                        amount = 1800.00,
                        dateMillis = now + (3 * oneDay),
                        description = "Reserva de 3 enxames de Abelha Sem Férpão (Jataí em caixas Racional INPA)",
                        apiaryId = apiary2Id,
                        paymentStatus = "PENDENTE",
                        paymentMethod = "PIX",
                        quantityKgOrUnits = 3.0
                    )
                )
                database.financialTransactionDao().insertTransaction(
                    FinancialTransactionEntity(
                        type = "DESPESA",
                        category = "Tratamento Sanitário",
                        amount = 220.00,
                        dateMillis = now - (22 * oneDay),
                        description = "Tiras de Ácido Oxálico e Óleos Essenciais para controle profilático de Varroa",
                        apiaryId = apiary1Id,
                        paymentStatus = "PAGO",
                        paymentMethod = "PIX",
                        quantityKgOrUnits = null
                    )
                )

                // 12. Rastreabilidade de Tags Bluetooth / Beacons de Colmeias
                database.bluetoothTagDao().insertTag(
                    BluetoothTagEntity(
                        tagMacAddress = "C4:7C:8D:11:22:33",
                        tagName = "SmartTag Colmeia 01 (Ninho)",
                        tagType = "SmartTag Apis Pro",
                        hiveId = hive1Id,
                        apiaryId = apiary1Id,
                        assignedItemName = "Colmeia 01 (Sol Nascente)",
                        lastSeenTimestamp = now - (2 * 60 * 1000L), // visto há 2 min
                        lastRssi = -54,
                        lastDistanceMeters = 1.2,
                        lastLatitude = -22.73942,
                        lastLongitude = -45.59128,
                        batteryPercent = 94,
                        temperatureC = 34.8,
                        humidityPercent = 56.0,
                        txPower = -59,
                        isAntiTheftEnabled = true,
                        isOutOfRangeAlert = false,
                        notes = "Tag BLE com sensor térmico fixada na tampa interna do ninho."
                    )
                )

                database.bluetoothTagDao().insertTag(
                    BluetoothTagEntity(
                        tagMacAddress = "E2:15:F8:44:55:66",
                        tagName = "RuuviTag Sensor Colmeia 02",
                        tagType = "RuuviTag Pro (Sensor)",
                        hiveId = hive2Id,
                        apiaryId = apiary1Id,
                        assignedItemName = "Colmeia 02 (Sol Nascente)",
                        lastSeenTimestamp = now - (8 * 60 * 1000L),
                        lastRssi = -68,
                        lastDistanceMeters = 3.5,
                        lastLatitude = -22.73948,
                        lastLongitude = -45.59135,
                        batteryPercent = 88,
                        temperatureC = 35.2,
                        humidityPercent = 58.5,
                        txPower = -59,
                        isAntiTheftEnabled = true,
                        isOutOfRangeAlert = false,
                        notes = "Monitoramento ambiental contínuo de temperatura e umidade da câmara de cria."
                    )
                )

                database.bluetoothTagDao().insertTag(
                    BluetoothTagEntity(
                        tagMacAddress = "FA:BC:12:77:88:99",
                        tagName = "Beacon Anti-Furto Isca #01",
                        tagType = "iBeacon / Anti-Furto",
                        hiveId = null,
                        apiaryId = apiary1Id,
                        assignedItemName = "Isca PET 2L #01 - Borda",
                        lastSeenTimestamp = now - (45 * 60 * 1000L),
                        lastRssi = -82,
                        lastDistanceMeters = 8.0,
                        lastLatitude = -22.73912,
                        lastLongitude = -45.59080,
                        batteryPercent = 99,
                        temperatureC = 26.5,
                        humidityPercent = 68.0,
                        txPower = -62,
                        isAntiTheftEnabled = true,
                        isOutOfRangeAlert = false,
                        notes = "Micro-beacon anti-furto oculto na fita de camuflagem da garrafa isca."
                    )
                )

                // 13. Metas para Selos de Qualidade & Certificações (Selo ARTE, Orgânico e Indicação Geográfica)
                // --- SELO ARTE (MAPA - Lei Federal nº 13.680/2018 & Dec. 11.099/2022) ---
                database.qualitySealDao().insertGoals(
                    listOf(
                        QualitySealGoalEntity(
                            sealType = "ARTE",
                            title = "Registro Ativo no SIM ou SIE",
                            description = "Estar vinculado a Serviço de Inspeção Municipal (SIM) ou Estadual (SIE) para habilitação do trânsito interestadual de produto artesanal.",
                            requirementCategory = "Legislação & Inspeção",
                            isCompleted = true,
                            completedAt = now - (15 * oneDay),
                            verificationEvidence = "Certificado de Registro SIM nº 2026/042",
                            displayOrder = 1
                        ),
                        QualitySealGoalEntity(
                            sealType = "ARTE",
                            title = "Manual de Boas Práticas Agropecuárias e de Fabricação (BPF)",
                            description = "Procedimentos operacionais padronizados (POP) de higiene, manejo das colmeias e sala de extração artesanal.",
                            requirementCategory = "Boas Práticas & BPF",
                            isCompleted = true,
                            completedAt = now - (10 * oneDay),
                            verificationEvidence = "Manual BPF ApisSmart assinado por RT",
                            displayOrder = 2
                        ),
                        QualitySealGoalEntity(
                            sealType = "ARTE",
                            title = "Extração e Desoperculação por Métodos Artesanais",
                            description = "Processamento sem pasteurização agressiva (máx. 45°C) e centrifugação limpa, preservando enzimas vivas e aroma natural.",
                            requirementCategory = "Boas Práticas & BPF",
                            isCompleted = true,
                            completedAt = now - (5 * oneDay),
                            verificationEvidence = "Laudo de centrifugação a frio",
                            displayOrder = 3
                        ),
                        QualitySealGoalEntity(
                            sealType = "ARTE",
                            title = "Controle de Umidade Máxima (≤ 19.5%)",
                            description = "Garantir que todos os lotes colhidos tenham umidade conforme RTIQ do MAPA para impedir fermentação.",
                            requirementCategory = "Análise Laboratorial",
                            isCompleted = true,
                            completedAt = now - (2 * oneDay),
                            verificationEvidence = "Registros de refratômetro no ApisSmart (média 17.5%)",
                            displayOrder = 4
                        ),
                        QualitySealGoalEntity(
                            sealType = "ARTE",
                            title = "Rastreabilidade Completa de Lote e Colmeia",
                            description = "Identificação inequívoca da colmeia de origem, data de colheita e lote até o consumidor final.",
                            requirementCategory = "Rastreabilidade",
                            isCompleted = true,
                            completedAt = now - (1 * oneDay),
                            verificationEvidence = "Módulo de rastreabilidade QR e relatórios do ApisSmart",
                            displayOrder = 5
                        ),
                        QualitySealGoalEntity(
                            sealType = "ARTE",
                            title = "Rotulagem Aprovada com Identificação Artesanal",
                            description = "Rótulo com dizeres obrigatórios, tabela nutricional, número SIM/SIE e carimbo com chancela do Selo ARTE.",
                            requirementCategory = "Legislação & Inspeção",
                            isCompleted = false,
                            completedAt = null,
                            verificationEvidence = "Layout aprovado pelo órgão sanitário",
                            displayOrder = 6
                        ),

                        // --- SELO DE PRODUTO ORGÂNICO (MAPA - Lei nº 10.831/2003) ---
                        QualitySealGoalEntity(
                            sealType = "ORGANICO",
                            title = "Raio de Isolamento de 3 km Livre de Agrotóxicos",
                            description = "Comprovação geográfica de que o raio de voo (3 km) não possui lavouras convencionais com defensivos ou transgênicos.",
                            requirementCategory = "Legislação & Inspeção",
                            isCompleted = true,
                            completedAt = now - (20 * oneDay),
                            verificationEvidence = "Mapeamento GPS e memorial descritivo da flora",
                            displayOrder = 1
                        ),
                        QualitySealGoalEntity(
                            sealType = "ORGANICO",
                            title = "Alimentação 100% Orgânica Certificada",
                            description = "Proibição de açúcar comercial refinado ou xaropes químicos; utilizar somente mel do próprio apiário ou açúcar orgânico.",
                            requirementCategory = "Sanidade & Manejo",
                            isCompleted = true,
                            completedAt = now - (12 * oneDay),
                            verificationEvidence = "Notas fiscais de açúcar orgânico e estoque de mel",
                            displayOrder = 2
                        ),
                        QualitySealGoalEntity(
                            sealType = "ORGANICO",
                            title = "Cera Alveolada de Origem Orgânica",
                            description = "Cera 100% pura derivada de opérculos do próprio sistema orgânico, sem resíduos de parafina ou pesticidas.",
                            requirementCategory = "Sanidade & Manejo",
                            isCompleted = false,
                            completedAt = null,
                            verificationEvidence = "Laudo de pureza da cera",
                            displayOrder = 3
                        ),
                        QualitySealGoalEntity(
                            sealType = "ORGANICO",
                            title = "Controle Sanitário Exclusivo com Ácidos Orgânicos",
                            description = "Tratamento de ácaros e pragas restrito a ácido oxálico, fórmico, timol ou óleos essenciais autorizados (sem acaricidas sintéticos).",
                            requirementCategory = "Sanidade & Manejo",
                            isCompleted = true,
                            completedAt = now - (8 * oneDay),
                            verificationEvidence = "Caderno de campo ApisSmart com registro fitoterápico",
                            displayOrder = 4
                        ),
                        QualitySealGoalEntity(
                            sealType = "ORGANICO",
                            title = "Caixas e Tintas com Materiais Atóxicos",
                            description = "Madeira virgem sem tratamento químico com arsênico/cromo; impermeabilização apenas com óleo de linhaça ou cera.",
                            requirementCategory = "Boas Práticas & BPF",
                            isCompleted = true,
                            completedAt = now - (6 * oneDay),
                            verificationEvidence = "Declaração de marcenaria ecológica",
                            displayOrder = 5
                        ),
                        QualitySealGoalEntity(
                            sealType = "ORGANICO",
                            title = "Auditoria por Certificadora Credenciada (IBD/Ecocert) ou OCS",
                            description = "Contrato e auditoria documental e presencial com certificadora credenciada no MAPA ou registro em OCS.",
                            requirementCategory = "Legislação & Inspeção",
                            isCompleted = false,
                            completedAt = null,
                            verificationEvidence = "Contrato de certificação e plano de manejo orgânico",
                            displayOrder = 6
                        ),

                        // --- SELO DE INDICAÇÃO GEOGRÁFICA (IG / DENOMINAÇÃO DE ORIGEM - INPI) ---
                        QualitySealGoalEntity(
                            sealType = "IG",
                            title = "Comprovação de Localização na Delimitação da IG",
                            description = "Apiários situados dentro do polígono geográfico delimitado no registro de Indicação Geográfica concedido pelo INPI.",
                            requirementCategory = "Legislação & Inspeção",
                            isCompleted = true,
                            completedAt = now - (25 * oneDay),
                            verificationEvidence = "Coordenadas GPS no ApisSmart cruzadas com mapa do INPI",
                            displayOrder = 1
                        ),
                        QualitySealGoalEntity(
                            sealType = "IG",
                            title = "Filiação à Associação/Cooperativa Gestora da IG",
                            description = "Inscrição ativa como membro produtor da entidade detentora dos direitos de uso da Indicação de Procedência (IP) ou Denominação de Origem (DO).",
                            requirementCategory = "Legislação & Inspeção",
                            isCompleted = true,
                            completedAt = now - (18 * oneDay),
                            verificationEvidence = "Comprovante de filiação associativa anual",
                            displayOrder = 2
                        ),
                        QualitySealGoalEntity(
                            sealType = "IG",
                            title = "Adesão Integral ao Caderno de Normas Técnicas da IG",
                            description = "Cumprimento das exigências de colheita, época de floração e técnicas tradicionais consagradas na região.",
                            requirementCategory = "Boas Práticas & BPF",
                            isCompleted = true,
                            completedAt = now - (14 * oneDay),
                            verificationEvidence = "Termo de compromisso assinado",
                            displayOrder = 3
                        ),
                        QualitySealGoalEntity(
                            sealType = "IG",
                            title = "Análise Melissopalinológica (Perfil Polínico)",
                            description = "Laudo botânico de pólen demonstrando dominância floral representativa da vegetação nativa da região geográfica.",
                            requirementCategory = "Análise Laboratorial",
                            isCompleted = false,
                            completedAt = null,
                            verificationEvidence = "Laudo microscópico de laboratório credenciado",
                            displayOrder = 4
                        ),
                        QualitySealGoalEntity(
                            sealType = "IG",
                            title = "Perfil Sensorial e Físico-Químico Típico",
                            description = "Aferição de cor (Escala Pfund), acidez e condutividade elétrica alinhadas ao padrão de identidade da denominação.",
                            requirementCategory = "Análise Laboratorial",
                            isCompleted = false,
                            completedAt = null,
                            verificationEvidence = "Boletim de análise físico-química",
                            displayOrder = 5
                        ),
                        QualitySealGoalEntity(
                            sealType = "IG",
                            title = "Selo Numerado de Rastreabilidade e Autenticidade",
                            description = "Emissão e afixação de selo holográfico ou QR Code serializado fornecido pela entidade gestora da IG em cada pote.",
                            requirementCategory = "Rastreabilidade",
                            isCompleted = false,
                            completedAt = null,
                            verificationEvidence = "Controle de lotes e numerações fornecidas",
                            displayOrder = 6
                        )
                    )
                )
            }
        }
    }
}
