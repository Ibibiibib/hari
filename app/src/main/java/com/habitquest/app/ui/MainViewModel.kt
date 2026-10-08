package com.habitquest.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitquest.app.data.Habit
import com.habitquest.app.data.HabitCheck
import com.habitquest.app.data.HabitRepository
import com.habitquest.app.data.StreakState
import com.habitquest.app.logic.LevelSystem
import com.habitquest.app.logic.StreakCalculator
import com.habitquest.app.widget.HabitWidget
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DayStat(val date: LocalDate, val percent: Int, val isFull: Boolean)
data class HabitWeekStat(val habit: Habit, val percent: Int)

data class UiState(
    val habits: List<Habit> = emptyList(),
    val todayChecks: Set<Long> = emptySet(),
    val todayDate: LocalDate = LocalDate.now(),
    val streak: Int = 0,                 // итоговая серия (с учётом воскрешения)
    val fireVisible: Boolean = false,    // огонь виден только при серии >= 3
    val canRevive: Boolean = false,      // серия сгорела, воскрешение ещё не использовано в этом месяце
    val penaltyTriggered: Boolean = false, // в этом месяце уже 2+ неполных дня
    val weekStats: List<DayStat> = emptyList(),
    val habitWeekStats: List<HabitWeekStat> = emptyList(),
    val xp: Int = 0,
    val todayPercent: Int = 0,
    val checkSet: Set<String> = emptySet()   // "habitId|yyyy-MM-dd" — для экрана «Месяц»
) {
    val dateHeader: String
        get() = todayDate.format(DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale("ru")))
}

class MainViewModel(app: Application, private val repo: HabitRepository) : AndroidViewModel(app) {

    var showAddDialog by androidx.compose.runtime.mutableStateOf(false)
        private set

    fun setAddDialogVisible(visible: Boolean) { showAddDialog = visible }

    // Месяц, который показан на экране «Месяц» (только просмотр; отмечать можно лишь сегодня)
    var viewMonth by androidx.compose.runtime.mutableStateOf(YearMonth.now())
        private set

    fun shiftMonth(delta: Long) { viewMonth = viewMonth.plusMonths(delta) }

    fun goToCurrentMonth() { viewMonth = YearMonth.now() }

    val state: StateFlow<UiState> =
        combine(repo.habits, repo.checks, repo.streakState) { habits, checks, streakState ->
            buildState(habits, checks, streakState ?: StreakState())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    init {
        // Если серия сгорела штрафом — сбрасываем бонус воскрешения (однократно за месяц)
        viewModelScope.launch {
            state.collect { s ->
                if (s.penaltyTriggered) {
                    val monthKey = LocalDate.now().toString().substring(0, 7)
                    repo.resetReviveBonus(monthKey)
                }
            }
        }
    }

    private fun buildState(habits: List<Habit>, checks: List<HabitCheck>, streakState: StreakState): UiState {
        val today = LocalDate.now()
        val checkSet = StreakCalculator.toCheckSet(checks)
        val todayStr = today.toString()
        val todayChecks = checks.filter { it.date == todayStr }.map { it.habitId }.toSet()

        val consecutive = StreakCalculator.consecutiveFullDays(habits, checkSet, today)
        val incomplete = StreakCalculator.incompleteDaysInMonth(habits, checkSet, today)
        val penalty = incomplete >= 2

        val monthKey = todayStr.substring(0, 7)
        val bonus = if (penalty) 0 else streakState.reviveBonus
        val streak = if (consecutive >= 3) consecutive else consecutive + bonus

        // Недельная статистика: последние 7 дней
        val weekStats = (0L..6L).map { offset ->
            val date = today.minusDays(6 - offset)
            val relevant = habits.filter { LocalDate.parse(it.createdDate) <= date }
            val done = relevant.count { checkSet.contains("${it.id}|$date") }
            val percent = if (relevant.isEmpty()) 0 else done * 100 / relevant.size
            DayStat(date, percent, StreakCalculator.isFullDay(habits, checkSet, date))
        }

        val habitWeekStats = habits.map { habit ->
            var done = 0
            for (offset in 0L..6L) {
                val date = today.minusDays(offset)
                if (LocalDate.parse(habit.createdDate) <= date && checkSet.contains("${habit.id}|$date")) done++
            }
            val existedDays = (0L..6L).count { LocalDate.parse(habit.createdDate) <= today.minusDays(it) }.coerceAtLeast(1)
            HabitWeekStat(habit, done * 100 / existedDays)
        }

        // Полные дни с момента создания первой привычки — для XP
        var fullDays = 0
        if (habits.isNotEmpty()) {
            val start = habits.minOf { LocalDate.parse(it.createdDate) }
            var d = start
            while (!d.isAfter(today)) {
                if (StreakCalculator.isFullDay(habits, checkSet, d)) fullDays++
                d = d.plusDays(1)
            }
        }
        val xp = LevelSystem.xpFor(checks.size, fullDays)

        val relevantToday = habits.filter { LocalDate.parse(it.createdDate) <= today }
        val todayPercent = if (relevantToday.isEmpty()) 0
        else relevantToday.count { todayChecks.contains(it.id) } * 100 / relevantToday.size

        return UiState(
            habits = habits,
            todayChecks = todayChecks,
            todayDate = today,
            streak = streak,
            fireVisible = streak >= 3,
            canRevive = !penalty && streak == 0 && streakState.lastReviveMonth != monthKey,
            penaltyTriggered = penalty,
            weekStats = weekStats,
            habitWeekStats = habitWeekStats,
            xp = xp,
            todayPercent = todayPercent,
            checkSet = checkSet
        )
    }

    fun toggle(habitId: Long, checked: Boolean) {
        viewModelScope.launch {
            repo.toggleToday(habitId, checked)
            HabitWidget().updateAll(getApplication<Application>())
        }
    }

    fun addHabit(title: String, emoji: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repo.addHabit(title, emoji)
            HabitWidget().updateAll(getApplication<Application>())
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repo.deleteHabit(habit)
            HabitWidget().updateAll(getApplication<Application>())
        }
    }

    fun revive() {
        viewModelScope.launch {
            val monthKey = LocalDate.now().toString().substring(0, 7)
            repo.revive(monthKey)
        }
    }
}
