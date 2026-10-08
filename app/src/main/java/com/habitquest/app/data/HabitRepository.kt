package com.habitquest.app.data

import java.time.LocalDate

class HabitRepository(private val db: AppDatabase) {

    val habits = db.habitDao().observeActiveHabits()
    val checks = db.habitDao().observeAllChecks()
    val streakState = db.streakDao().observe()

    suspend fun addHabit(title: String, emoji: String) {
        db.habitDao().insertHabit(
            Habit(title = title.trim(), emoji = emoji, createdDate = LocalDate.now().toString())
        )
    }

    suspend fun deleteHabit(habit: Habit) {
        db.habitDao().deleteChecksForHabit(habit.id)
        db.habitDao().deleteHabit(habit)
    }

    /** Можно менять ТОЛЬКО сегодняшний день — UI вызывает это только с LocalDate.now(). */
    suspend fun toggleToday(habitId: Long, checked: Boolean) {
        val check = HabitCheck(habitId = habitId, date = LocalDate.now().toString())
        if (checked) db.habitDao().insertCheck(check) else db.habitDao().deleteCheck(check)
    }

    suspend fun revive(monthKey: String) {
        db.streakDao().upsert(StreakState(reviveBonus = 3, lastReviveMonth = monthKey))
    }

    /** Штраф: сброс бонуса воскрешения. Считается в ViewModel. */
    suspend fun resetReviveBonus(monthKey: String) {
        db.streakDao().upsert(StreakState(reviveBonus = 0, lastReviveMonth = monthKey))
    }
}
