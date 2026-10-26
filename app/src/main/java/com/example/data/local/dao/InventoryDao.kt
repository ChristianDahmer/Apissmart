package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.InventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory ORDER BY category ASC, name ASC")
    fun getAllInventory(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory ORDER BY category ASC, name ASC")
    suspend fun getAllInventoryOnce(): List<InventoryEntity>

    @Query("SELECT * FROM inventory WHERE quantity <= minThreshold ORDER BY quantity ASC")
    fun getLowStockInventory(): Flow<List<InventoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(items: List<InventoryEntity>)

    @Update
    suspend fun updateItem(item: InventoryEntity)

    @Delete
    suspend fun deleteItem(item: InventoryEntity)

    @Query("DELETE FROM inventory")
    suspend fun deleteAllInventory()
}
