package com.habitquest.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitquest.app.logic.LevelSystem
import com.habitquest.app.ui.MainViewModel

@Composable
fun LevelsScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val level = LevelSystem.levelFor(state.xp)
    val next = LevelSystem.nextLevel(state.xp)
    val progress = LevelSystem.progress(state.xp)

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Твой уровень", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${level.emoji} ${level.name}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(level.colorHex)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Опыт: ${state.xp} XP", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    if (next != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "До «${next.name}»: ${next.thresholdXp - state.xp} XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text("Максимальный уровень! 🏆", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item { Text("Все уровни", style = MaterialTheme.typography.titleMedium) }

        items(LevelSystem.levels) { l ->
            val reached = state.xp >= l.thresholdXp
            Card(colors = CardDefaults.cardColors(
                containerColor = if (reached) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
            )) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${l.emoji} ${l.name}", modifier = Modifier.weight(1f), color = Color(l.colorHex))
                    Text(
                        if (reached) "✅ открыт" else "${l.thresholdXp} XP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                "XP начисляется: +10 за каждую отметку, +25 за полностью выполненный день.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
