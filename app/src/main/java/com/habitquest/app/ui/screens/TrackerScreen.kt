package com.habitquest.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitquest.app.logic.CellState
import com.habitquest.app.logic.MonthCalculator
import com.habitquest.app.logic.MonthStats
import com.habitquest.app.logic.WeekStat
import com.habitquest.app.ui.MainViewModel
import com.habitquest.app.ui.UiState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// Палитра как в таблице из видео
private val Cyan = Color(0xFF14B8F0)
private val Green = Color(0xFF12C583)
private val DarkGreen = Color(0xFF0B4A38)
private val Pink = Color(0xFFD56BF0)
private val Track = Color(0xFF111A2A)
private val CheckedBg = Color(0xFF0B3B33)
private val CellBg = Color(0xFF04070C)
private val HeaderBg = Color(0xFF16233A)
private val GreyArc = Color(0xFF5C6878)
private val Muted = Color(0xFF8A96A6)

// Недели: голубая, фиолетовая, жёлтая, зелёная, красная (29–31)
private val WeekColors = listOf(
    Cyan,
    Color(0xFF8E5CF7),
    Color(0xFFF5B400),
    Green,
    Color(0xFFE5174F)
)

private val WeekdayRu = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

private val CellW = 30.dp
private val RowH = 36.dp
private val HeaderH = 46.dp
private val FooterH = 28.dp
private val NameW = 118.dp
private val PctW = 48.dp
private val BarW = 96.dp
private val FireW = 40.dp
private val BoltW = 40.dp

@Composable
fun TrackerScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val month = vm.viewMonth
    val today = state.todayDate
    val stats = remember(state.habits, state.checkSet, month, today) {
        MonthCalculator.compute(state.habits, state.checkSet, month, today)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MonthHeader(vm, state)

        if (state.habits.isEmpty()) {
            Panel {
                Text(
                    "Привычек пока нет.\nНажми + и добавь первую.",
                    color = Muted,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            DashboardPanel(stats)
            DailyBarsPanel(stats)
            LinePanel(stats)
            WeekdayPanel(stats)
            GridPanel(vm, stats, today)
        }
        Spacer(Modifier.height(72.dp))
    }
}

@Composable
private fun MonthHeader(vm: MainViewModel, state: UiState) {
    val month = vm.viewMonth
    val title = month.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru")))
        .replaceFirstChar { it.uppercase() }
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { vm.shiftMonth(-1) }) { Text("‹", fontSize = 24.sp) }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.clickable { vm.goToCurrentMonth() }
        )
        TextButton(onClick = { vm.shiftMonth(1) }) { Text("›", fontSize = 24.sp) }
        Spacer(Modifier.weight(1f))
        if (state.fireVisible) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Text(
                    text = "🔥 ${state.streak}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun Panel(
    title: String? = null,
    trailing: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (title != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Muted,
                        modifier = Modifier.weight(1f)
                    )
                    if (trailing != null) {
                        Text(text = trailing, fontWeight = FontWeight.Bold, color = Cyan)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            content()
        }
    }
}

// ───────────── Кольцо + недели ─────────────

@Composable
private fun DashboardPanel(stats: MonthStats) {
    Panel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Donut(stats.overallPercent, Modifier.size(112.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                stats.weeks.forEach { w -> WeekRow(w) }
            }
        }
    }
}

@Composable
private fun Donut(percent: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 16.dp.toPx()
            val d = size.minDimension - stroke
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            val arcSize = Size(d, d)
            drawArc(
                color = GreyArc,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke)
            )
            drawArc(
                color = Green,
                startAngle = -90f,
                sweepAngle = 360f * percent / 100f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke)
            )
        }
        Text(text = "$percent%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Green)
    }
}

@Composable
private fun WeekRow(w: WeekStat) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Нед ${w.index + 1}",
            fontSize = 12.sp,
            color = Muted,
            modifier = Modifier.width(48.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Track)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((w.percent / 100f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(Cyan)
            )
        }
        Text(
            text = "${w.percent}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Cyan,
            textAlign = TextAlign.End,
            modifier = Modifier.width(44.dp)
        )
    }
}

// ───────────── Столбики по дням месяца ─────────────

@Composable
private fun DailyBarsPanel(stats: MonthStats) {
    Panel(title = "По дням", trailing = "${stats.overallPercent}%") {
        Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
            val n = stats.days.size
            val slot = size.width / n
            val barW = slot * 0.62f
            stats.days.forEachIndexed { i, d ->
                val x = i * slot + (slot - barW) / 2f
                drawRect(color = Track, topLeft = Offset(x, 0f), size = Size(barW, size.height))
                if (!d.future) {
                    val minH = if (d.tracked) 3.dp.toPx() else 0f
                    val h = (size.height * d.percent / 100f).coerceAtLeast(minH)
                    drawRect(
                        color = WeekColors[i / 7],
                        topLeft = Offset(x, size.height - h),
                        size = Size(barW, h)
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("1", fontSize = 10.sp, color = Muted)
            Spacer(Modifier.weight(1f))
            Text("${stats.days.size}", fontSize = 10.sp, color = Muted)
        }
    }
}

// ───────────── Линия месяца ─────────────

@Composable
private fun LinePanel(stats: MonthStats) {
    Panel(title = "Месяц") {
        Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
            val n = stats.days.size
            val pts = ArrayList<Offset>()
            stats.days.forEachIndexed { i, d ->
                if (d.tracked) {
                    val x = if (n > 1) size.width * i / (n - 1) else 0f
                    val y = size.height * (1f - d.percent / 100f)
                    pts.add(Offset(x, y))
                }
            }
            if (pts.size >= 2) {
                val line = Path()
                line.moveTo(pts[0].x, pts[0].y)
                for (i in 1 until pts.size) {
                    val p0 = pts[i - 1]
                    val p1 = pts[i]
                    val mx = (p0.x + p1.x) / 2f
                    line.cubicTo(mx, p0.y, mx, p1.y, p1.x, p1.y)
                }
                val fill = Path()
                fill.addPath(line)
                fill.lineTo(pts.last().x, size.height)
                fill.lineTo(pts.first().x, size.height)
                fill.close()
                drawPath(
                    path = fill,
                    brush = Brush.verticalGradient(listOf(Cyan.copy(alpha = 0.35f), Color.Transparent))
                )
                drawPath(path = line, color = Cyan, style = Stroke(width = 2.dp.toPx()))
            }
        }
    }
}

// ───────────── По дням недели ─────────────

@Composable
private fun WeekdayPanel(stats: MonthStats) {
    Panel(title = "По дням недели") {
        Row(
            modifier = Modifier.fillMaxWidth().height(84.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            stats.weekdayPercent.forEachIndexed { i, p ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    val h = (56f * ((p ?: 0) / 100f)).coerceAtLeast(4f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(h.dp)
                            .background(if (p == null) DarkGreen else Green)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(WeekdayRu[i], fontSize = 11.sp, color = Muted)
                }
            }
        }
    }
}

// ───────────── Сетка привычек × дни ─────────────

@Composable
private fun GridPanel(vm: MainViewModel, stats: MonthStats, today: LocalDate) {
    val scroll = rememberScrollState()
    val density = LocalDensity.current

    // Открываем сетку так, чтобы «сегодня» было на виду
    LaunchedEffect(stats.month) {
        val idx = if (YearMonth.from(today) == stats.month) today.dayOfMonth - 1 else 0
        val px = with(density) { (CellW * idx - 60.dp).toPx() }
        scroll.scrollTo(px.toInt().coerceAtLeast(0))
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            // Левая колонка с названиями — не прокручивается
            Column(modifier = Modifier.width(NameW).padding(start = 8.dp)) {
                Box(modifier = Modifier.height(HeaderH).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text("Привычка", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                stats.rows.forEach { r ->
                    Box(modifier = Modifier.height(RowH).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = "${r.habit.emoji} ${r.habit.title}",
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Box(modifier = Modifier.height(FooterH).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text("% дня", fontSize = 11.sp, color = Muted)
                }
            }

            // Дни + колонки статистики — прокручиваются по горизонтали
            Column(modifier = Modifier.weight(1f).horizontalScroll(scroll)) {
                Row(modifier = Modifier.height(HeaderH)) {
                    stats.days.forEachIndexed { i, d ->
                        val isToday = d.date == today
                        Column(
                            modifier = Modifier.width(CellW).fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 1.dp)
                                    .fillMaxWidth()
                                    .height(22.dp)
                                    .background(WeekColors[i / 7]),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${d.date.dayOfMonth}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = WeekdayRu[d.date.dayOfWeek.value - 1],
                                fontSize = 9.sp,
                                color = if (isToday) Cyan else Muted,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                    StatHeader("%", PctW)
                    StatHeader("📊", BarW)
                    StatHeader("🔥", FireW)
                    StatHeader("⚡", BoltW)
                }

                stats.rows.forEach { r ->
                    Row(modifier = Modifier.height(RowH)) {
                        r.cells.forEachIndexed { i, cs ->
                            val isToday = stats.days[i].date == today
                            CheckCell(
                                cs = cs,
                                isToday = isToday,
                                onClick = { vm.toggle(r.habit.id, cs != CellState.CHECKED) } // меняется только сегодня
                            )
                        }
                        StatText("${r.percent}%", PctW)
                        BarCell(r.percent)
                        StatText("${r.streak}", FireW)
                        StatText("${r.best}", BoltW)
                    }
                }

                Row(modifier = Modifier.height(FooterH)) {
                    stats.days.forEach { d ->
                        Box(modifier = Modifier.width(CellW).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (d.tracked) "${d.percent}" else "–",
                                fontSize = 10.sp,
                                color = Muted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckCell(cs: CellState, isToday: Boolean, onClick: () -> Unit) {
    val canClick = isToday && (cs == CellState.CHECKED || cs == CellState.EMPTY)
    Box(
        modifier = Modifier
            .width(CellW)
            .fillMaxHeight()
            .padding(1.dp)
            .background(if (cs == CellState.CHECKED) CheckedBg else CellBg)
            .then(if (isToday) Modifier.border(1.5.dp, Cyan) else Modifier)
            .clickable(enabled = canClick, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (cs != CellState.NOT_YET_CREATED) {
            val a = if (cs == CellState.FUTURE) 0.25f else 1f
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .border(1.5.dp, Color.White.copy(alpha = a), RoundedCornerShape(3.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (cs == CellState.CHECKED) {
                    Text("✓", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatHeader(label: String, width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .padding(1.dp)
            .background(HeaderBg),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatText(text: String, width: Dp) {
    Box(modifier = Modifier.width(width).fillMaxHeight(), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BarCell(percent: Int) {
    Box(
        modifier = Modifier
            .width(BarW)
            .fillMaxHeight()
            .padding(horizontal = 6.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth((percent / 100f).coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(Pink)
        )
    }
}
