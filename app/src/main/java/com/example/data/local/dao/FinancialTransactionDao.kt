package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.FinancialTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinancialTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<FinancialTransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: FinancialTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: FinancialTransactionEntity)

    @Query("SELECT * FROM financial_transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions ORDER BY id ASC")
    suspend fun getAllTransactionsOnce(): List<FinancialTransactionEntity>

    @Query("SELECT * FROM financial_transactions WHERE apiaryId = :apiaryId ORDER BY dateMillis DESC")
    fun getTransactionsByApiary(apiaryId: Long): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions WHERE type = :type ORDER BY dateMillis DESC")
    fun getTransactionsByType(type: String): Flow<List<FinancialTransactionEntity>>

    @Query("DELETE FROM financial_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM financial_transactions")
    suspend fun deleteAllTransactions()
}
