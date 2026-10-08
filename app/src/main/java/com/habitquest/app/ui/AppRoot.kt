package com.habitquest.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.habitquest.app.ui.screens.*
import com.habitquest.app.ui.theme.HabitQuestTheme

@Composable
fun AppRoot(vm: MainViewModel) {
    val nav = rememberNavController()
    HabitQuestTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val backStack by nav.currentBackStackEntryAsState()
                    val route = backStack?.destination?.route
                    listOf(
                        Triple("tracker", "📅", "Месяц"),
                        Triple("today", "✅", "Сегодня"),
                        Triple("stats", "📊", "Статистика"),
                        Triple("levels", "🎮", "Уровни")
                    ).forEach { (r, icon, label) ->
                        NavigationBarItem(
                            selected = route == r,
                            onClick = {
                                nav.navigate(r) {
                                    popUpTo("tracker")
                                    launchSingleTop = true
                                }
                            },
                            label = { Text(label, maxLines = 1) },
                            icon = { Text(icon) }
                        )
                    }
                }
            },
            floatingActionButton = {
                val backStack by nav.currentBackStackEntryAsState()
                val route = backStack?.destination?.route
                if (route == "today" || route == "tracker") {
                    FloatingActionButton(onClick = { vm.setAddDialogVisible(true) }) {
                        Text("+", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        ) { padding ->
            NavHost(nav, startDestination = "tracker", modifier = Modifier.padding(padding)) {
                composable("tracker") { TrackerScreen(vm) }
                composable("today") { TodayScreen(vm) }
                composable("stats") { StatsScreen(vm) }
                composable("levels") { LevelsScreen(vm) }
            }
        }

        if (vm.showAddDialog) {
            AddHabitDialog(
                onDismiss = { vm.setAddDialogVisible(false) },
                onAdd = { title, emoji ->
                    vm.addHabit(title, emoji)
                    vm.setAddDialogVisible(false)
                }
            )
        }
    }
}
