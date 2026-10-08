package com.habitquest.app.logic

data class Level(val name: String, val emoji: String, val thresholdXp: Int, val colorHex: Long)

object LevelSystem {

    // Уровни как в Minecraft, каждый заметно сложнее предыдущего (растущий разрыв порогов)
    val levels = listOf(
        Level("Дерево",     "🪵", 0,     0xFF8B5A2B),
        Level("Камень",     "🪨", 300,   0xFF9E9E9E),
        Level("Железо",     "⚙️", 800,   0xFFD8D8D8),
        Level("Золото",     "🪙", 1600,  0xFFFFD700),
        Level("Алмаз",      "💎", 3200,  0xFF4DD0E1),
        Level("Незерит",    "🟣", 6000,  0xFF7B4B94),
    )

    /** XP: 10 за каждую отметку + 25 за каждый полностью выполненный день. */
    fun xpFor(checksCount: Int, fullDays: Int): Int = checksCount * 10 + fullDays * 25

    fun levelFor(xp: Int): Level = levels.lastOrNull { xp >= it.thresholdXp } ?: levels.first()

    fun nextLevel(xp: Int): Level? = levels.firstOrNull { xp < it.thresholdXp }

    /** 0..1 прогресс к следующему уровню. */
    fun progress(xp: Int): Float {
        val current = levelFor(xp)
        val next = nextLevel(xp) ?: return 1f
        val span = (next.thresholdXp - current.thresholdXp).toFloat()
        return ((xp - current.thresholdXp) / span).coerceIn(0f, 1f)
    }
}
