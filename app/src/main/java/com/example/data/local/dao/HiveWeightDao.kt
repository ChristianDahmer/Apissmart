package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.HiveWeightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HiveWeightDao {
    @Query("SELECT * FROM hive_weights WHERE hiveId = :hiveId ORDER BY date DESC")
    fun getWeightsByHive(hiveId: Long): Flow<List<HiveWeightEntity>>

    @Query("SELECT * FROM hive_weights WHERE hiveId = :hiveId ORDER BY date DESC")
    suspend fun getWeightsByHiveOnce(hiveId: Long): List<HiveWeightEntity>

    @Query("SELECT * FROM hive_weights ORDER BY date DESC")
    fun getAllWeights(): Flow<List<HiveWeightEntity>>

    @Query("SELECT * FROM hive_weights ORDER BY id ASC")
    suspend fun getAllWeightsOnce(): List<HiveWeightEntity>

    @Query("SELECT * FROM hive_weights WHERE hiveId = :hiveId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestWeightForHive(hiveId: Long): HiveWeightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weight: HiveWeightEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeights(weights: List<HiveWeightEntity>)

    @Delete
    suspend fun deleteWeight(weight: HiveWeightEntity)

    @Query("DELETE FROM hive_weights")
    suspend fun deleteAllWeights()
}
