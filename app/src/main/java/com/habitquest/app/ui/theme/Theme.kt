package com.habitquest.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Тёмная тема в палитре таблицы-трекера: почти чёрный синий фон, голубой акцент.
// Светлой темы нет намеренно.
private val DarkColors = darkColorScheme(
    primary = Color(0xFF14B8F0),
    onPrimary = Color(0xFF00212E),
    primaryContainer = Color(0xFF0B3B54),
    onPrimaryContainer = Color(0xFFD6F1FF),
    secondary = Color(0xFFD56BF0),
    onSecondary = Color(0xFF2A0033),
    tertiary = Color(0xFF12C583),
    background = Color(0xFF070B12),
    onBackground = Color(0xFFE6EDF3),
    surface = Color(0xFF0C1320),
    onSurface = Color(0xFFE6EDF3),
    surfaceVariant = Color(0xFF142033),
    onSurfaceVariant = Color(0xFF9AA5B1),
    surfaceContainerLowest = Color(0xFF05080D),
    surfaceContainerLow = Color(0xFF0A111C),
    surfaceContainer = Color(0xFF0E1726),
    surfaceContainerHigh = Color(0xFF121D2F),
    surfaceContainerHighest = Color(0xFF111B2B),
    outline = Color(0xFF2A3850),
    outlineVariant = Color(0xFF1B283B),
)

@Composable
fun HabitQuestTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
