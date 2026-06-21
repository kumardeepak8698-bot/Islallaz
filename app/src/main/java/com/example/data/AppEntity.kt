package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_configurations")
data class AppConfigEntity(
    @PrimaryKey val packageName: String,
    val customLabel: String? = null,
    val isAutoFreeze: Boolean = false,
    val lastActionTimestamp: Long = System.currentTimeMillis(),
    val category: String = "General",
    val notes: String = ""
)
