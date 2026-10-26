package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.TaskScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskScheduleDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDate ASC")
    fun getAllTasks(): Flow<List<TaskScheduleEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDate ASC")
    fun getPendingTasks(): Flow<List<TaskScheduleEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    fun getPendingTasksCount(): Flow<Int>

    @Query("SELECT * FROM tasks ORDER BY id ASC")
    suspend fun getAllTasksOnce(): List<TaskScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskScheduleEntity>)

    @Update
    suspend fun updateTask(task: TaskScheduleEntity)

    @Delete
    suspend fun deleteTask(task: TaskScheduleEntity)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}
