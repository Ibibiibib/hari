package com.habitquest.app.logic

import com.habitquest.app.data.Habit
import com.habitquest.app.data.HabitCheck
import java.time.LocalDate

object StreakCalculator {

    private fun key(habitId: Long, date: LocalDate) = "$habitId|$date"

    fun isFullDay(habits: List<Habit>, checks: Set<String>, date: LocalDate): Boolean {
        val relevant = habits.filter { LocalDate.parse(it.createdDate) <= date }
        if (relevant.isEmpty()) return false
        return relevant.all { checks.contains(key(it.id, date)) }
    }

    /** Неполные дни текущего месяца (вчера и раньше — сегодня ещё идёт). */
    fun incompleteDaysInMonth(habits: List<Habit>, checks: Set<String>, today: LocalDate): Int {
        if (habits.isEmpty()) return 0
        var count = 0
        var d = today.withDayOfMonth(1)
        val last = today.minusDays(1)
        while (!d.isAfter(last)) {
            val relevant = habits.filter { LocalDate.parse(it.createdDate) <= d }
            if (relevant.isNotEmpty() && !isFullDay(habits, checks, d)) count++
            d = d.plusDays(1)
        }
        return count
    }

    /** Сколько дней подряд (заканчивая сегодня или вчера) выполнены ВСЕ привычки. */
    fun consecutiveFullDays(habits: List<Habit>, checks: Set<String>, today: LocalDate): Int {
        var streak = 0
        var d = if (isFullDay(habits, checks, today)) today else today.minusDays(1)
        while (isFullDay(habits, checks, d)) {
            streak++
            d = d.minusDays(1)
        }
        return streak
    }

    fun toCheckSet(checks: List<HabitCheck>): Set<String> =
        checks.map { "${it.habitId}|${it.date}" }.toSet()
}
