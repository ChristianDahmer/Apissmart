package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.HarvestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HarvestDao {
    @Query("SELECT * FROM harvests ORDER BY date DESC")
    fun getAllHarvests(): Flow<List<HarvestEntity>>

    @Query("SELECT * FROM harvests ORDER BY date DESC")
    suspend fun getAllHarvestsOnce(): List<HarvestEntity>

    @Query("SELECT * FROM harvests WHERE apiaryId = :apiaryId ORDER BY date DESC")
    fun getHarvestsByApiary(apiaryId: Long): Flow<List<HarvestEntity>>

    @Query("SELECT SUM(quantityKg) FROM harvests WHERE productType = 'Mel'")
    fun getTotalHoneyHarvested(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHarvest(harvest: HarvestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHarvests(harvests: List<HarvestEntity>)

    @Update
    suspend fun updateHarvest(harvest: HarvestEntity)

    @Delete
    suspend fun deleteHarvest(harvest: HarvestEntity)

    @Query("DELETE FROM harvests")
    suspend fun deleteAllHarvests()
}
