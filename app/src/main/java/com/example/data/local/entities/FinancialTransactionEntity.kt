package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_transactions")
data class FinancialTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "RECEITA" or "DESPESA"
    val category: String, // e.g., "Venda de Mel", "Insumos/Alimentação", "Equipamentos", "Tratamento Sanitário", etc.
    val amount: Double,
    val dateMillis: Long,
    val description: String,
    val apiaryId: Long? = null,
    val paymentStatus: String = "PAGO", // "PAGO", "PENDENTE"
    val paymentMethod: String = "PIX", // "PIX", "Dinheiro", "Cartão de Crédito", "Transferência", "Boleto"
    val quantityKgOrUnits: Double? = null
)
