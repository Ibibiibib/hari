package com.habitquest.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val emoji: String = "✅",
    val createdDate: String,   // LocalDate.toString() — привычка "существует" только с этой даты
    val isActive: Boolean = true
)
