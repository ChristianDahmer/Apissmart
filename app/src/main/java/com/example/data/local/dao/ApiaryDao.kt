package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ApiaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiaryDao {
    @Query("SELECT * FROM apiaries ORDER BY name ASC")
    fun getAllApiaries(): Flow<List<ApiaryEntity>>

    @Query("SELECT * FROM apiaries ORDER BY name ASC")
    suspend fun getAllApiariesOnce(): List<ApiaryEntity>

    @Query("SELECT * FROM apiaries WHERE id = :id")
    fun getApiaryById(id: Long): Flow<ApiaryEntity?>

    @Query("SELECT * FROM apiaries WHERE id = :id")
    suspend fun getApiaryByIdOnce(id: Long): ApiaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiary(apiary: ApiaryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiaries(apiaries: List<ApiaryEntity>)

    @Update
    suspend fun updateApiary(apiary: ApiaryEntity)

    @Delete
    suspend fun deleteApiary(apiary: ApiaryEntity)

    @Query("DELETE FROM apiaries")
    suspend fun deleteAllApiaries()
}
