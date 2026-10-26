package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.QualitySealGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QualitySealDao {
    @Query("SELECT * FROM quality_seal_goals ORDER BY displayOrder ASC, id ASC")
    fun getAllGoals(): Flow<List<QualitySealGoalEntity>>

    @Query("SELECT * FROM quality_seal_goals ORDER BY displayOrder ASC, id ASC")
    suspend fun getAllGoalsOnce(): List<QualitySealGoalEntity>

    @Query("SELECT * FROM quality_seal_goals WHERE sealType = :sealType ORDER BY displayOrder ASC, id ASC")
    fun getGoalsBySeal(sealType: String): Flow<List<QualitySealGoalEntity>>

    @Query("SELECT COUNT(*) FROM quality_seal_goals WHERE isCompleted = 1")
    fun getCompletedGoalsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: QualitySealGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<QualitySealGoalEntity>)

    @Update
    suspend fun updateGoal(goal: QualitySealGoalEntity)

    @Query("UPDATE quality_seal_goals SET isCompleted = :completed, completedAt = :completedAt WHERE id = :id")
    suspend fun toggleGoalCompletion(id: Long, completed: Boolean, completedAt: Long?)

    @Query("DELETE FROM quality_seal_goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)

    @Query("DELETE FROM quality_seal_goals")
    suspend fun deleteAllGoals()
}
