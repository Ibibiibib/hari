package com.habitquest.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.habitquest.app.ui.screens.*

@Composable
fun AppRoot(vm: MainViewModel) {
    val nav = rememberNavController()
    HabitQuestTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val backStack by nav.currentBackStackEntryAsState()
                    val route = backStack?.destination?.route
                    listOf("today" to "🔥 Сегодня", "stats" to "📊 Статистика", "levels" to "🎮 Уровни").forEach { (r, label) ->
                        NavigationBarItem(
                            selected = route == r,
                            onClick = { nav.navigate(r) { popUpTo("today") } },
                            label = { Text(label) },
                            icon = {}
                        )
                    }
                }
            },
            floatingActionButton = {
                val backStack by nav.currentBackStackEntryAsState()
                if (backStack?.destination?.route == "today") {
                    FloatingActionButton(onClick = { vm.setAddDialogVisible(true) }) {
                        Text("+", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        ) { padding ->
            NavHost(nav, startDestination = "today", modifier = Modifier.padding(padding)) {
                composable("today") { TodayScreen(vm) }
                composable("stats") { StatsScreen(vm) }
                composable("levels") { LevelsScreen(vm) }
            }
        }
    }
}
