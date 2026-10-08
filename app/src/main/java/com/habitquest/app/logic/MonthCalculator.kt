package com.habitquest.app.logic

import com.habitquest.app.data.Habit
import java.time.LocalDate
import java.time.YearMonth

enum class CellState { CHECKED, EMPTY, NOT_YET_CREATED, FUTURE }

data class DayCol(val date: LocalDate, val percent: Int, val tracked: Boolean, val future: Boolean)

data class HabitRowStat(
    val habit: Habit,
    val cells: List<CellState>,
    val percent: Int,
    val streak: Int,
    val best: Int
)

data class WeekStat(val index: Int, val firstDay: Int, val lastDay: Int, val percent: Int)

data class MonthStats(
    val month: YearMonth,
    val days: List<DayCol>,
    val rows: List<HabitRowStat>,
    val weeks: List<WeekStat>,
    val overallPercent: Int,
    val weekdayPercent: List<Int?>   // Пн..Вс, null = данных нет
)

/** Считает всё, что нужно для экрана «Месяц». Только чтение — ничего не пишет в базу. */
object MonthCalculator {

    fun compute(habits: List<Habit>, checks: Set<String>, month: YearMonth, today: LocalDate): MonthStats {
        val created: Map<Long, LocalDate> = habits.associate { it.id to LocalDate.parse(it.createdDate) }
        val len = month.lengthOfMonth()

        val days = (1..len).map { n ->
            val date = month.atDay(n)
            val future = date.isAfter(today)
            val relevant = habits.filter { !created.getValue(it.id).isAfter(date) }
            val done = relevant.count { checks.contains("${it.id}|$date") }
            val tracked = relevant.isNotEmpty() && !future
            DayCol(date, if (tracked) done * 100 / relevant.size else 0, tracked, future)
        }

        val rows = habits.map { h ->
            val c = created.getValue(h.id)
            var tracked = 0
            var done = 0
            var run = 0
            var best = 0
            val cells = days.map { day ->
                when {
                    day.date.isBefore(c) -> {
                        run = 0
                        CellState.NOT_YET_CREATED
                    }
                    day.future -> {
                        run = 0
                        CellState.FUTURE
                    }
                    checks.contains("${h.id}|${day.date}") -> {
                        tracked++
                        done++
                        run++
                        if (run > best) best = run
                        CellState.CHECKED
                    }
                    else -> {
                        tracked++
                        run = 0
                        CellState.EMPTY
                    }
                }
            }

            // Текущая серия по этой привычке: подряд до сегодня (или до вчера, если сегодня ещё не отмечено)
            var streak = 0
            var d = if (checks.contains("${h.id}|$today")) today else today.minusDays(1)
            while (!d.isBefore(c) && checks.contains("${h.id}|$d")) {
                streak++
                d = d.minusDays(1)
            }

            HabitRowStat(h, cells, if (tracked == 0) 0 else done * 100 / tracked, streak, best)
        }

        val weeks = (0 until 5).mapNotNull { w ->
            val first = w * 7 + 1
            if (first > len) {
                null
            } else {
                val last = minOf(first + 6, len)
                val slice = days.subList(first - 1, last).filter { it.tracked }
                WeekStat(w, first, last, if (slice.isEmpty()) 0 else slice.sumOf { it.percent } / slice.size)
            }
        }

        val tr = days.filter { it.tracked }
        val overall = if (tr.isEmpty()) 0 else tr.sumOf { it.percent } / tr.size

        val weekday: List<Int?> = (1..7).map { dow ->
            val l = tr.filter { it.date.dayOfWeek.value == dow }
            if (l.isEmpty()) null else l.sumOf { it.percent } / l.size
        }

        return MonthStats(month, days, rows, weeks, overall, weekday)
    }
}
