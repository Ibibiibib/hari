package com.habitquest.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "checks",
    primaryKeys = ["habitId", "date"],
    foreignKeys = [ForeignKey(
        entity = Habit::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("date")]
)
data class HabitCheck(
    val habitId: Long,
    val date: String   // LocalDate.toString()
)
