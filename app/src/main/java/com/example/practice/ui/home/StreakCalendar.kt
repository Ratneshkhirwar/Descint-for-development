package com.example.practice.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practice.ui.theme.DescintOrange
import com.example.practice.ui.theme.DescintWhite
import com.example.practice.ui.theme.inter
import java.util.Calendar
import java.util.Locale

// ─────────────────────────────────────────────
// Data
// ─────────────────────────────────────────────

data class CalendarDayCell(
    val dayOfMonth: Int,
    val dateKey: String,
    val isCurrentMonth: Boolean
)

enum class DayVisualState { COMPLETED, TODAY, MISSED, NEUTRAL }

// ─────────────────────────────────────────────
// Date-key helpers (yyyy-MM-dd, always via Calendar — no java.time, since
// minSdk 24 doesn't have it without desugaring)
// ─────────────────────────────────────────────

fun dateKeyFor(cal: Calendar): String {
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-indexed
    val day = cal.get(Calendar.DAY_OF_MONTH)
    return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
}

fun dateKeyFromMillis(millis: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    return dateKeyFor(cal)
}

fun parseDateKeyToStartMillis(dateKey: String): Long {
    val parts = dateKey.split("-")
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, parts[0].toInt())
    cal.set(Calendar.MONTH, parts[1].toInt() - 1)
    cal.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
    return startOfDayMillis(cal)
}

// Midnight-normalized millis, so "before/after" comparisons ignore time-of-day.
fun startOfDayMillis(cal: Calendar): Long {
    val c = cal.clone() as Calendar
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

// ─────────────────────────────────────────────
// Month grid — full weeks (Mon-first), including padding days from the
// previous/next month so every row has 7 cells.
// ─────────────────────────────────────────────

fun buildMonthGrid(reference: Calendar = Calendar.getInstance()): List<CalendarDayCell> {
    val targetMonth = reference.get(Calendar.MONTH)
    val targetYear = reference.get(Calendar.YEAR)

    val firstOfMonth = (reference.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }

    // Calendar.DAY_OF_WEEK: SUNDAY=1 ... SATURDAY=7. Convert to Monday-first
    // index: MONDAY=0 ... SUNDAY=6.
    val firstDayOfWeek = firstOfMonth.get(Calendar.DAY_OF_WEEK)
    val mondayIndexed = (firstDayOfWeek + 5) % 7

    val gridStart = (firstOfMonth.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, -mondayIndexed)
    }

    val daysInMonth = firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val totalCellsNeeded = mondayIndexed + daysInMonth
    val weeks = kotlin.math.ceil(totalCellsNeeded / 7.0).toInt()
    val totalCells = weeks * 7

    val cursor = gridStart.clone() as Calendar
    return (0 until totalCells).map {
        val cell = CalendarDayCell(
            dayOfMonth = cursor.get(Calendar.DAY_OF_MONTH),
            dateKey = dateKeyFor(cursor),
            isCurrentMonth = cursor.get(Calendar.MONTH) == targetMonth &&
                    cursor.get(Calendar.YEAR) == targetYear
        )
        cursor.add(Calendar.DAY_OF_YEAR, 1)
        cell
    }
}

// ─────────────────────────────────────────────
// Color-priority logic, exactly as specified:
// 1. completed == true   -> COMPLETED (green)
// 2. else, date == today -> TODAY (orange)
// 3. else, date is past  -> MISSED (red)
// 4. otherwise (future, before tracking started, or outside this month) -> NEUTRAL
// ─────────────────────────────────────────────

fun resolveDayState(
    cell: CalendarDayCell,
    completedDates: Set<String>,
    onboardingStartDateKey: String?,
    todayDateKey: String,
    todayStartMillis: Long,
    cellStartMillis: Long
): DayVisualState {
    if (!cell.isCurrentMonth) return DayVisualState.NEUTRAL

    // If onboarding date is recorded, any day *before* onboarding started should be neutral (normal), not missed (red).
    // If onboarding is somehow not recorded yet, default to today as the start so past days before app usage aren't marked red.
    val effectiveStartDateKey = onboardingStartDateKey ?: todayDateKey
    val trackingStarted = cell.dateKey >= effectiveStartDateKey

    if (!trackingStarted) return DayVisualState.NEUTRAL

    return when {
        completedDates.contains(cell.dateKey) -> DayVisualState.COMPLETED
        cell.dateKey == todayDateKey -> DayVisualState.TODAY
        cellStartMillis < todayStartMillis -> DayVisualState.MISSED
        else -> DayVisualState.NEUTRAL // future day
    }
}

// ─────────────────────────────────────────────
// Streak — consecutive COMPLETED days counting backward from today
// (includes today if today is already marked completed).
// ─────────────────────────────────────────────

fun calculateStreak(completedDates: Set<String>, today: Calendar = Calendar.getInstance()): Int {
    val cursor = today.clone() as Calendar
    var streak = 0

    if (completedDates.contains(dateKeyFor(cursor))) {
        streak++
    }
    cursor.add(Calendar.DAY_OF_YEAR, -1)

    while (completedDates.contains(dateKeyFor(cursor))) {
        streak++
        cursor.add(Calendar.DAY_OF_YEAR, -1)
    }

    return streak
}

// ─────────────────────────────────────────────
// UI
// ─────────────────────────────────────────────

private val completedGreen = Color(0xFF3E7A42)
private val missedRed = Color(0xFF7A2E2E)
private val weekdayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

@Composable
fun StreakCalendarCard(
    streakDays: Int,
    cells: List<CalendarDayCell>,
    dayStateFor: (CalendarDayCell) -> DayVisualState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = DescintWhite.copy(alpha = 0.1f), shape = RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "\uD83D\uDD25 $streakDays ${if (streakDays == 1) "day" else "days"} streak",
            color = DescintWhite,
            fontFamily = inter,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            weekdayLabels.forEach { label ->
                Text(
                    text = label,
                    color = DescintWhite.copy(alpha = 0.4f),
                    fontFamily = inter,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val weeks = remember(cells) { cells.chunked(7) }

        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { cell ->
                    val state = dayStateFor(cell)
                    DayCell(
                        cell = cell,
                        state = state,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    cell: CalendarDayCell,
    state: DayVisualState,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (state) {
        DayVisualState.COMPLETED -> completedGreen
        DayVisualState.TODAY -> DescintOrange
        DayVisualState.MISSED -> missedRed
        DayVisualState.NEUTRAL -> Color.Transparent
    }

    val textColor = when {
        !cell.isCurrentMonth -> DescintWhite.copy(alpha = 0.25f)
        state == DayVisualState.NEUTRAL -> DescintWhite.copy(alpha = 0.6f)
        else -> DescintWhite
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .padding(2.dp)
                .background(color = backgroundColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cell.dayOfMonth.toString(),
                color = textColor,
                fontFamily = inter,
                fontSize = 12.sp,
                fontWeight = if (state == DayVisualState.NEUTRAL) FontWeight.Normal else FontWeight.Medium
            )
        }
    }
}
