package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.AiConsultationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiConsultationDao {
    @Query("SELECT * FROM ai_consultations ORDER BY timestamp ASC")
    fun getAllConsultations(): Flow<List<AiConsultationEntity>>

    @Query("SELECT * FROM ai_consultations ORDER BY id ASC")
    suspend fun getAllConsultationsOnce(): List<AiConsultationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsultation(consultation: AiConsultationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsultations(consultations: List<AiConsultationEntity>)

    @Query("DELETE FROM ai_consultations")
    suspend fun clearHistory()

    @Query("DELETE FROM ai_consultations")
    suspend fun deleteAllConsultations()
}
