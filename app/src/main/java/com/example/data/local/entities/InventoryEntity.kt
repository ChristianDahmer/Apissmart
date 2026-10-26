package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Madeiramento, Quadros e Cera, Alimentação, Equipamentos/EPI, Sanidade/Tratamentos, Ferramentas
    val quantity: Int,
    val unit: String = "un", // un, kg, L, doses, conj
    val minThreshold: Int = 5,
    val lastRestockDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)
