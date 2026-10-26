package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apiaries")
data class ApiaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val predominantFlora: String,
    val capacity: Int,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
