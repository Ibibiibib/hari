package com.habitquest.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY id ASC")
    fun observeActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY id ASC")
    suspend fun getActiveHabits(): List<Habit>

    @Query("SELECT * FROM checks")
    fun observeAllChecks(): Flow<List<HabitCheck>>

    @Query("SELECT * FROM checks WHERE date = :date")
    suspend fun getChecksForDate(date: String): List<HabitCheck>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM checks WHERE habitId = :habitId")
    suspend fun deleteChecksForHabit(habitId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheck(check: HabitCheck)

    @Delete
    suspend fun deleteCheck(check: HabitCheck)
}

@Dao
interface StreakDao {

    @Query("SELECT * FROM streak_state WHERE id = 1")
    fun observe(): Flow<StreakState?>

    @Query("SELECT * FROM streak_state WHERE id = 1")
    suspend fun get(): StreakState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: StreakState)
}
