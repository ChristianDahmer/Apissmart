package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.bluetooth.BluetoothTagTracker
import com.example.data.local.ApisSmartDatabase
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.BluetoothTagEntity
import com.example.data.local.entities.HiveEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BluetoothTagTrackingTest {

    private lateinit var db: ApisSmartDatabase
    private lateinit var context: Context
    private lateinit var tracker: BluetoothTagTracker

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, ApisSmartDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        tracker = BluetoothTagTracker(context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testDistanceEstimationCalculation() {
        // Quando RSSI é igual ao TxPower (-59 dBm), a distância calculada deve ser aproximadamente 1.0 metro
        val distAt1m = tracker.calculateDistanceMeters(rssi = -59, txPower = -59)
        assertEquals(1.0, distAt1m, 0.2)

        // Sinal mais forte (-45 dBm) indica proximidade imediata (< 1m)
        val distClose = tracker.calculateDistanceMeters(rssi = -45, txPower = -59)
        assertTrue("Distância de sinal forte deve ser menor que 1.0m", distClose < 1.0)

        // Sinal atenuado (-85 dBm) indica afastamento no campo (> 5m)
        val distFar = tracker.calculateDistanceMeters(rssi = -85, txPower = -59)
        assertTrue("Distância de sinal fraco deve ser maior que 5.0m", distFar > 5.0)
    }

    @Test
    fun testBluetoothTagDatabasePersistenceAndTelemetry() = runBlocking {
        val now = System.currentTimeMillis()

        // 1. Cadastra apiário e colmeia
        val apiaryId = db.apiaryDao().insertApiary(
            ApiaryEntity(
                name = "Apiário Teste",
                location = "Campo Experimental",
                predominantFlora = "Silvestre",
                capacity = 20
            )
        )
        val hiveId = db.hiveDao().insertHive(
            HiveEntity(apiaryId = apiaryId, hiveNumber = "Colmeia 01")
        )

        // 2. Insere Tag Bluetooth vinculada à colmeia
        val tagMac = "AA:BB:CC:DD:EE:01"
        val tag = BluetoothTagEntity(
            tagMacAddress = tagMac,
            tagName = "SmartTag Colmeia 01",
            tagType = "SmartTag Apis Pro",
            hiveId = hiveId,
            apiaryId = apiaryId,
            assignedItemName = "Colmeia 01",
            lastSeenTimestamp = now,
            lastRssi = -58,
            lastDistanceMeters = 1.2,
            batteryPercent = 95,
            temperatureC = 35.1,
            humidityPercent = 54.0,
            isAntiTheftEnabled = true
        )
        val insertedId = db.bluetoothTagDao().insertTag(tag)
        assertTrue(insertedId > 0)

        // 3. Consulta a tag pelo endereço MAC
        val fetchedTag = db.bluetoothTagDao().getTagByMac(tagMac)
        assertNotNull(fetchedTag)
        assertEquals("SmartTag Colmeia 01", fetchedTag?.tagName)
        assertEquals(hiveId, fetchedTag?.hiveId)
        assertEquals(35.1, fetchedTag?.temperatureC ?: 0.0, 0.01)

        // 4. Atualiza a telemetria via beacon scanning
        db.bluetoothTagDao().updateTelemetry(
            macAddress = tagMac,
            timestamp = now + 1000L,
            rssi = -48,
            distance = 0.6,
            latitude = -22.7394,
            longitude = -45.5913,
            battery = 94,
            temperature = 35.4,
            humidity = 53.5,
            isOutOfRange = false
        )

        val updated = db.bluetoothTagDao().getTagByMac(tagMac)
        assertNotNull(updated)
        assertEquals(-48, updated?.lastRssi)
        assertEquals(0.6, updated?.lastDistanceMeters ?: 0.0, 0.01)
        assertEquals(35.4, updated?.temperatureC ?: 0.0, 0.01)
        assertEquals(94, updated?.batteryPercent)

        // 5. Verifica listagem de tags via Flow
        val allTags = db.bluetoothTagDao().getAllTags().first()
        assertEquals(1, allTags.size)
    }
}
