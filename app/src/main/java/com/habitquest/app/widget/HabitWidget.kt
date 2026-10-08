package com.habitquest.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.habitquest.app.data.AppDatabase
import com.habitquest.app.data.HabitRepository
import java.time.LocalDate

class HabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = HabitWidget()
}

class HabitWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.get(context)
        val habits = db.habitDao().getActiveHabits()
        val today = LocalDate.now().toString()
        val checked = db.habitDao().getChecksForDate(today).map { it.habitId }.toSet()

        provideContent {
            WidgetContent(habits, checked)
        }
    }
}

@Composable
private fun WidgetContent(habits: List<com.habitquest.app.data.Habit>, checked: Set<Long>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(androidx.compose.ui.graphics.Color(0xFF131A22)))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "Сегодня",
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorProvider(androidx.compose.ui.graphics.Color.White))
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        if (habits.isEmpty()) {
            Text(
                text = "Добавь привычки в приложении",
                style = TextStyle(fontSize = 12.sp, color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF9AA5B1)))
            )
        } else {
            habits.take(5).forEach { habit ->
                val isChecked = checked.contains(habit.id)
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable(actionRunCallback<ToggleHabitAction>(
                            parameters = actionParametersOf(
                                ToggleHabitAction.KEY_HABIT_ID to habit.id,
                                ToggleHabitAction.KEY_CHECKED to isChecked
                            )
                        )),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isChecked) "☑" else "☐",
                        style = TextStyle(fontSize = 16.sp, color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF2EE6A8)))
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = "${habit.emoji} ${habit.title}",
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = ColorProvider(if (isChecked) androidx.compose.ui.graphics.Color(0xFF7A8794) else androidx.compose.ui.graphics.Color.White)
                        )
                    )
                }
            }
            if (habits.size > 5) {
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "… ещё ${habits.size - 5}",
                    style = TextStyle(fontSize = 11.sp, color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF7A8794)))
                )
            }
        }
    }
}

class ToggleHabitAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val habitId = parameters[KEY_HABIT_ID] ?: return
        val checked = parameters[KEY_CHECKED] ?: false
        val repo = HabitRepository(AppDatabase.get(context))
        repo.toggleToday(habitId, checked = !checked)
        HabitWidget().updateAll(context)
    }

    companion object {
        val KEY_HABIT_ID = ActionParameters.Key<Long>("habit_id")
        val KEY_CHECKED = ActionParameters.Key<Boolean>("checked")
    }
}
