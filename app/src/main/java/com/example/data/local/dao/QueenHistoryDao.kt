package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.QueenHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QueenHistoryDao {
    @Query("SELECT * FROM queen_history WHERE hiveId = :hiveId ORDER BY date DESC")
    fun getQueenHistoryByHive(hiveId: Long): Flow<List<QueenHistoryEntity>>

    @Query("SELECT * FROM queen_history WHERE hiveId = :hiveId ORDER BY date DESC")
    suspend fun getQueenHistoryByHiveOnce(hiveId: Long): List<QueenHistoryEntity>

    @Query("SELECT * FROM queen_history ORDER BY date DESC")
    fun getAllQueenHistory(): Flow<List<QueenHistoryEntity>>

    @Query("SELECT * FROM queen_history ORDER BY id ASC")
    suspend fun getAllQueenHistoryOnce(): List<QueenHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueenEvent(event: QueenHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueenEvents(events: List<QueenHistoryEntity>)

    @Delete
    suspend fun deleteQueenEvent(event: QueenHistoryEntity)

    @Query("DELETE FROM queen_history")
    suspend fun deleteAllQueenHistory()
}
