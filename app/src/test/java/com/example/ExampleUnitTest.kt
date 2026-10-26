package com.example

import com.example.data.local.entities.BeeTrapEntity
import com.example.data.local.entities.HarvestEntity
import com.example.data.local.entities.HiveEntity
import com.example.data.local.entities.HivePhotoEntity
import com.example.data.local.entities.HiveWeightEntity
import com.example.data.local.entities.QueenHistoryEntity
import com.example.ui.theme.AppThemeConfig
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.FontColorOption
import com.example.ui.theme.FontSizeScale
import com.example.ui.theme.PrimaryPalette
import com.example.ui.theme.SubtitleColorOption
import com.example.util.LocationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testLocationFormatting() {
        val lat = -22.7394
        val lon = -45.5913
        val formatted = LocationHelper.formatCoordinates(lat, lon)
        assertEquals("-22.73940, -45.59130", formatted)

        val dms = LocationHelper.formatDms(lat, lon)
        assertTrue(dms.contains("S") && dms.contains("W"))
    }

    @Test
    fun testYieldCalculation() {
        val supers = 2
        val framesPerSuper = 9
        val yieldPerFrame = 2.4
        val operculation = 0.90

        val totalFrames = supers * framesPerSuper
        val totalYieldKg = totalFrames * yieldPerFrame * operculation
        val expected = 18 * 2.4 * 0.90

        assertEquals(18, totalFrames)
        assertEquals(expected, totalYieldKg, 0.001)
    }

    @Test
    fun testBeeTrapModel() {
        val trap = BeeTrapEntity(
            name = "Isca PET 2L #01",
            trapType = "Garrafa PET 2L",
            targetSpecies = "Tetragonisca angustula (Jataí)",
            attractantUsed = "Extrato Alcoólico de Própolis de Jataí",
            status = "Capturada",
            latitude = -22.73912,
            longitude = -45.59080
        )

        assertEquals("Capturada", trap.status)
        assertEquals("Tetragonisca angustula (Jataí)", trap.targetSpecies)
        assertNotNull(trap.latitude)
        assertNotNull(trap.longitude)
    }

    @Test
    fun testHiveSpeciesAndFramesPerSuper() {
        val hive = HiveEntity(
            apiaryId = 1L,
            hiveNumber = "Mandaçaia M-01",
            beeSpecies = "Melipona quadrifasciata (Mandaçaia MQA)",
            speciesCategory = "Meliponíneo (Sem Ferrão / ASF)",
            hiveType = "Caixa INPA Modular (ASF)",
            framesCount = 4,
            supersCount = 2,
            framesPerSuper = 6,
            estimatedYieldPerFrameKg = 0.4
        )

        assertEquals("Melipona quadrifasciata (Mandaçaia MQA)", hive.beeSpecies)
        assertEquals("Meliponíneo (Sem Ferrão / ASF)", hive.speciesCategory)
        assertEquals(6, hive.framesPerSuper)
        assertEquals(2, hive.supersCount)
    }

    @Test
    fun testHoneyStockEstimationFromHiveWeight() {
        val totalWeight = 42.5
        val tareWeight = 22.0
        val honeyStock = (totalWeight - tareWeight).coerceAtLeast(0.0)

        assertEquals(20.5, honeyStock, 0.001)

        val weightEntity = HiveWeightEntity(
            hiveId = 1L,
            date = System.currentTimeMillis(),
            totalWeightKg = totalWeight,
            tareWeightKg = tareWeight,
            estimatedHoneyKg = honeyStock,
            weightDeltaKg = 2.5,
            weightStatus = "Ganhando Peso (Florada Ativa)",
            notes = "Pesagem pós-florada do eucalipto"
        )

        assertEquals(42.5, weightEntity.totalWeightKg, 0.001)
        assertEquals(20.5, weightEntity.estimatedHoneyKg, 0.001)
        assertEquals(2.5, weightEntity.weightDeltaKg ?: 0.0, 0.001)
        assertEquals("Ganhando Peso (Florada Ativa)", weightEntity.weightStatus)
    }

    @Test
    fun testQueenHistoryRecord() {
        val queenEvent = QueenHistoryEntity(
            hiveId = 1L,
            date = System.currentTimeMillis(),
            previousQueenLineage = "Carnica F1",
            previousQueenColor = "Amarelo (2022)",
            newQueenLineage = "Buckfast Selecionada",
            newQueenColor = "Branco (2026)",
            reason = "Idade Avançada / Queda de Postura",
            introductionMethod = "Gaiola Jenter com liberação indireta após 72h",
            acceptanceConfirmed = true,
            notes = "Rainha aceita, postura uniforme com mais de 7 quadros de cria"
        )

        assertEquals(1L, queenEvent.hiveId)
        assertEquals("Buckfast Selecionada", queenEvent.newQueenLineage)
        assertEquals("Branco (2026)", queenEvent.newQueenColor)
        assertEquals("Idade Avançada / Queda de Postura", queenEvent.reason)
        assertTrue(queenEvent.acceptanceConfirmed)
    }

    @Test
    fun testThemeConfigurationOptions() {
        val config = AppThemeConfig(
            themeMode = AppThemeMode.DARK,
            primaryPalette = PrimaryPalette.MEADOW_GREEN,
            fontColorOption = FontColorOption.GOLDEN_AMBER,
            subtitleColorOption = SubtitleColorOption.MINT_GREEN,
            fontSizeScale = FontSizeScale.LARGE
        )

        assertEquals(AppThemeMode.DARK, config.themeMode)
        assertEquals(PrimaryPalette.MEADOW_GREEN, config.primaryPalette)
        assertEquals(FontColorOption.GOLDEN_AMBER, config.fontColorOption)
        assertEquals(SubtitleColorOption.MINT_GREEN, config.subtitleColorOption)
        assertEquals(FontSizeScale.LARGE, config.fontSizeScale)
        assertEquals(1.15f, config.fontSizeScale.scaleFactor, 0.001f)
    }

    @Test
    fun testHivePhotoEntity() {
        val photo = HivePhotoEntity(
            hiveId = 1L,
            photoUri = "content://media/external/images/media/102",
            dateTaken = System.currentTimeMillis(),
            category = "Ninho & Postura",
            caption = "Postura espiral perfeita",
            inspectorName = "Inspetor Silva",
            notes = "Cria operculada saudável, sem sinais de loque ou varroa"
        )

        assertEquals(1L, photo.hiveId)
        assertEquals("content://media/external/images/media/102", photo.photoUri)
        assertEquals("Ninho & Postura", photo.category)
        assertEquals("Postura espiral perfeita", photo.caption)
        assertEquals("Inspetor Silva", photo.inspectorName)
    }
}
