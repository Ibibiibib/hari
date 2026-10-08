package com.habitquest.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Тёмная тема обязательна — светлой нет намеренно
private val DarkColors = darkColorScheme(
    primary = Color(0xFF2EE6A8),
    onPrimary = Color(0xFF003828),
    secondary = Color(0xFF7A5AF8),
    background = Color(0xFF0B0F14),
    surface = Color(0xFF131A22),
    surfaceVariant = Color(0xFF1B242F),
    onBackground = Color(0xFFE6EDF3),
    onSurface = Color(0xFFE6EDF3),
    onSurfaceVariant = Color(0xFF9AA5B1),
)

@Composable
fun HabitQuestTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
