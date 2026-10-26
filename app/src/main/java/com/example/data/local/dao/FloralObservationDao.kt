package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.FloralObservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FloralObservationDao {
    @Query("SELECT * FROM floral_observations ORDER BY createdAt DESC")
    fun getAllObservations(): Flow<List<FloralObservationEntity>>

    @Query("SELECT * FROM floral_observations ORDER BY id ASC")
    suspend fun getAllObservationsOnce(): List<FloralObservationEntity>

    @Query("SELECT * FROM floral_observations WHERE apiaryId = :apiaryId ORDER BY startMonth ASC, createdAt DESC")
    fun getObservationsByApiary(apiaryId: Long): Flow<List<FloralObservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(observation: FloralObservationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservations(observations: List<FloralObservationEntity>)

    @Update
    suspend fun updateObservation(observation: FloralObservationEntity)

    @Delete
    suspend fun deleteObservation(observation: FloralObservationEntity)

    @Query("DELETE FROM floral_observations WHERE id = :id")
    suspend fun deleteObservationById(id: Long)

    @Query("DELETE FROM floral_observations")
    suspend fun deleteAllObservations()
}
