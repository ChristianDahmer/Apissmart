package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.BeeTrapEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BeeTrapDao {
    @Query("SELECT * FROM bee_traps ORDER BY installationDate DESC")
    fun getAllTraps(): Flow<List<BeeTrapEntity>>

    @Query("SELECT * FROM bee_traps ORDER BY installationDate DESC")
    suspend fun getAllTrapsOnce(): List<BeeTrapEntity>

    @Query("SELECT * FROM bee_traps WHERE status = :status ORDER BY installationDate DESC")
    fun getTrapsByStatus(status: String): Flow<List<BeeTrapEntity>>

    @Query("SELECT * FROM bee_traps WHERE id = :id")
    fun getTrapById(id: Long): Flow<BeeTrapEntity?>

    @Query("SELECT * FROM bee_traps WHERE id = :id")
    suspend fun getTrapByIdOnce(id: Long): BeeTrapEntity?

    @Query("SELECT COUNT(*) FROM bee_traps WHERE status = 'Capturada'")
    fun getCapturedTrapsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM bee_traps WHERE status = 'Armada'")
    fun getActiveTrapsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrap(trap: BeeTrapEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraps(traps: List<BeeTrapEntity>)

    @Update
    suspend fun updateTrap(trap: BeeTrapEntity)

    @Delete
    suspend fun deleteTrap(trap: BeeTrapEntity)

    @Query("DELETE FROM bee_traps")
    suspend fun deleteAllTraps()
}
