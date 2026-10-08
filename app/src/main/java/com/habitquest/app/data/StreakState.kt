package com.habitquest.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak_state")
data class StreakState(
    @PrimaryKey val id: Int = 1,
    val reviveBonus: Int = 0,          // 3 после воскрешения (1 раз в месяц)
    val lastReviveMonth: String? = null // "2026-10"
)
