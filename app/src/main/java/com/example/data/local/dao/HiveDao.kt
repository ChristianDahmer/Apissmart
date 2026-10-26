package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.HiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HiveDao {
    @Query("SELECT * FROM hives ORDER BY hiveNumber ASC")
    fun getAllHives(): Flow<List<HiveEntity>>

    @Query("SELECT * FROM hives ORDER BY hiveNumber ASC")
    suspend fun getAllHivesOnce(): List<HiveEntity>

    @Query("SELECT * FROM hives WHERE apiaryId = :apiaryId ORDER BY hiveNumber ASC")
    fun getHivesByApiary(apiaryId: Long): Flow<List<HiveEntity>>

    @Query("SELECT * FROM hives WHERE id = :id")
    fun getHiveById(id: Long): Flow<HiveEntity?>

    @Query("SELECT * FROM hives WHERE id = :id")
    suspend fun getHiveByIdOnce(id: Long): HiveEntity?

    @Query("SELECT COUNT(*) FROM hives WHERE healthStatus = 'Alerta Sanitário'")
    fun getSanitaryAlertHivesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHive(hive: HiveEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHives(hives: List<HiveEntity>)

    @Update
    suspend fun updateHive(hive: HiveEntity)

    @Delete
    suspend fun deleteHive(hive: HiveEntity)

    @Query("DELETE FROM hives")
    suspend fun deleteAllHives()
}
