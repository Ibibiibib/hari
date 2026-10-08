package com.habitquest.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitquest.app.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    var habitToDelete by remember { mutableStateOf<com.habitquest.app.data.Habit?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Шапка: дата + огонь серии в углу
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(state.dateHeader, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Выполнено: ${state.todayPercent}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (state.fireVisible) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "🔥 ${state.streak}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        // Воскрешение серии (1 раз в месяц)
        if (state.canRevive) {
            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Серия сгорела…", modifier = Modifier.weight(1f))
                    TextButton(onClick = { vm.revive() }) { Text("Воскресить 🔥 (1 раз в месяц)") }
                }
            }
        }
        if (state.penaltyTriggered) {
            Spacer(Modifier.height(8.dp))
            Text(
                "В этом месяце 2+ неполных дня — серия сброшена.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(12.dp))

        if (state.habits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Привычек пока нет.\nНажми + и добавь первую.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.habits, key = { it.id }) { habit ->
                    val checked = state.todayChecks.contains(habit.id)
                    Card {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { vm.toggle(habit.id, it) } // только сегодня
                            )
                            Text(
                                text = "${habit.emoji} ${habit.title}",
                                modifier = Modifier.weight(1f),
                                style = if (checked) MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough)
                                        else MaterialTheme.typography.bodyLarge
                            )
                            TextButton(onClick = { habitToDelete = habit }) { Text("🗑", fontSize = 16.sp) }
                        }
                    }
                }
            }
        }
    }

    habitToDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("Удалить привычку?") },
            text = { Text("«${habit.title}» и её история будут удалены.") },
            confirmButton = {
                TextButton(onClick = { vm.deleteHabit(habit); habitToDelete = null }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { habitToDelete = null }) { Text("Отмена") } }
        )
    }

    if (vm.showAddDialog) {
        AddHabitDialog(
            onDismiss = { vm.setAddDialogVisible(false) },
            onAdd = { title, emoji -> vm.addHabit(title, emoji); vm.setAddDialogVisible(false) }
        )
    }
}

@Composable
private fun AddHabitDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("✅") }
    val emojis = listOf("✅", "🚿", "🚫", "💧", "😴", "🏃", "💪", "🥩", "✍️", "📵", "🧘", "📚")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая привычка") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text("Иконка:", style = MaterialTheme.typography.bodySmall)
                FlowRowEmoji(emojis, selected = emoji, onSelect = { emoji = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onAdd(title, emoji) }, enabled = title.isNotBlank()) {
                Text("Добавить")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowEmoji(emojis: List<String>, selected: String, onSelect: (String) -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        emojis.forEach { e ->
            Surface(
                onClick = { onSelect(e) },
                shape = MaterialTheme.shapes.small,
                color = if (e == selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(e, modifier = Modifier.padding(8.dp), fontSize = 20.sp)
            }
        }
    }
}
