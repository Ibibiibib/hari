package com.habitquest.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitquest.app.ui.MainViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StatsScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Неделя", style = MaterialTheme.typography.headlineSmall) }

        // График по дням: столбик = % выполнения дня
        item {
            Card {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        state.weekStats.forEach { day ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("${day.percent}%", style = MaterialTheme.typography.labelSmall)
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .height((140 * day.percent / 100).coerceAtLeast(2).dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (day.isFull) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                        )
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        state.weekStats.forEach { day ->
                            Text(
                                day.date.format(DateTimeFormatter.ofPattern("EE", Locale("ru"))),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        item { Text("Привычки за неделю", style = MaterialTheme.typography.titleMedium) }

        items(state.habitWeekStats, key = { it.habit.id }) { stat ->
            Card {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${stat.habit.emoji} ${stat.habit.title}", modifier = Modifier.weight(1f))
                    LinearProgressIndicator(
                        progress = { stat.percent / 100f },
                        modifier = Modifier.width(90.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("${stat.percent}%", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
