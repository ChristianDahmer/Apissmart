package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.BluetoothTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BluetoothTagDao {
    @Query("SELECT * FROM bluetooth_tags ORDER BY lastSeenTimestamp DESC")
    fun getAllTags(): Flow<List<BluetoothTagEntity>>

    @Query("SELECT * FROM bluetooth_tags ORDER BY id ASC")
    suspend fun getAllTagsOnce(): List<BluetoothTagEntity>

    @Query("SELECT * FROM bluetooth_tags WHERE id = :id LIMIT 1")
    fun getTagById(id: Long): Flow<BluetoothTagEntity?>

    @Query("SELECT * FROM bluetooth_tags WHERE id = :id LIMIT 1")
    suspend fun getTagByIdOnce(id: Long): BluetoothTagEntity?

    @Query("SELECT * FROM bluetooth_tags WHERE tagMacAddress = :macAddress LIMIT 1")
    suspend fun getTagByMac(macAddress: String): BluetoothTagEntity?

    @Query("SELECT * FROM bluetooth_tags WHERE hiveId = :hiveId LIMIT 1")
    fun getTagByHiveId(hiveId: Long): Flow<BluetoothTagEntity?>

    @Query("SELECT * FROM bluetooth_tags WHERE hiveId = :hiveId LIMIT 1")
    suspend fun getTagByHiveIdOnce(hiveId: Long): BluetoothTagEntity?

    @Query("SELECT * FROM bluetooth_tags WHERE apiaryId = :apiaryId ORDER BY tagName ASC")
    fun getTagsByApiary(apiaryId: Long): Flow<List<BluetoothTagEntity>>

    @Query("SELECT COUNT(*) FROM bluetooth_tags WHERE isOutOfRangeAlert = 1")
    fun getOutOfRangeAlertCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: BluetoothTagEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<BluetoothTagEntity>)

    @Update
    suspend fun updateTag(tag: BluetoothTagEntity)

    @Delete
    suspend fun deleteTag(tag: BluetoothTagEntity)

    @Query("DELETE FROM bluetooth_tags WHERE id = :id")
    suspend fun deleteTagById(id: Long)

    @Query("DELETE FROM bluetooth_tags")
    suspend fun deleteAllTags()

    @Query("""
        UPDATE bluetooth_tags 
        SET lastSeenTimestamp = :timestamp,
            lastRssi = :rssi,
            lastDistanceMeters = :distance,
            lastLatitude = COALESCE(:latitude, lastLatitude),
            lastLongitude = COALESCE(:longitude, lastLongitude),
            batteryPercent = COALESCE(:battery, batteryPercent),
            temperatureC = COALESCE(:temperature, temperatureC),
            humidityPercent = COALESCE(:humidity, humidityPercent),
            isOutOfRangeAlert = :isOutOfRange
        WHERE tagMacAddress = :macAddress
    """)
    suspend fun updateTelemetry(
        macAddress: String,
        timestamp: Long,
        rssi: Int,
        distance: Double,
        latitude: Double?,
        longitude: Double?,
        battery: Int?,
        temperature: Double?,
        humidity: Double?,
        isOutOfRange: Boolean
    )
}
