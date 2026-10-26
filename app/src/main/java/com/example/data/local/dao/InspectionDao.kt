package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.InspectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Query("SELECT * FROM inspections ORDER BY date DESC")
    fun getAllInspections(): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections ORDER BY date DESC")
    suspend fun getAllInspectionsOnce(): List<InspectionEntity>

    @Query("SELECT * FROM inspections WHERE hiveId = :hiveId ORDER BY date DESC")
    fun getInspectionsByHive(hiveId: Long): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections WHERE apiaryId = :apiaryId ORDER BY date DESC")
    fun getInspectionsByApiary(apiaryId: Long): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections WHERE id = :id")
    fun getInspectionById(id: Long): Flow<InspectionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspections(inspections: List<InspectionEntity>)

    @Update
    suspend fun updateInspection(inspection: InspectionEntity)

    @Delete
    suspend fun deleteInspection(inspection: InspectionEntity)

    @Query("DELETE FROM inspections")
    suspend fun deleteAllInspections()
}
