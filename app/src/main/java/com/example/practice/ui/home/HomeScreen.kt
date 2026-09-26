package com.example.practice.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.example.practice.R
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practice.data.UserData
import com.example.practice.data.UserPreferencesRepository
import com.example.practice.ui.theme.DescintBlack
import com.example.practice.ui.theme.DescintWhite
import com.example.practice.ui.theme.PracticeTheme
import com.example.practice.ui.theme.albertsans
import com.example.practice.ui.theme.inter
import java.util.Calendar
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// Rotates to the next one every 24 hours (see UserPreferencesRepository.getRotatingQuote).
// Add/reorder freely — the stored index just wraps to list size.
private val homeQuotes = listOf(
    "Consistency beats intensity...",
    "Small steps, every single day.",
    "Progress, not perfection.",
    "One distraction at a time.",
    "Your future self is watching.",
    "Focus is a muscle — train it daily.",
    "Keep showing up.",
    "Do it even when you don't feel like it.",
    "A little better every day.",
    "Stay focused. Keep moving.",
    "Start small. Stay consistent.",
    "Make today count.",
    "Less scrolling. More doing.",
    "Protect your focus.",
    "One task. Full focus.",
    "Build, don't rush.",
    "Keep going. You're building.",
    "Win the next hour.",
    "Focus on what matters.",
    "Progress happens quietly.",
    "Don't break the streak.",
    "Your habits shape your future.",
    "Be consistent, not perfect.",
    "Show up for yourself.",
    "Slow progress is still progress.",
    "Do less. Do it well.",
    "Focus now. Relax later.",
    "Stay patient. Stay focused.",
    "Today is another chance.",
    "Discipline creates freedom."
)

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val repository = remember { UserPreferencesRepository(context) }

    val userData by repository.userDataFlow.collectAsState(initial = UserData())

    var quote by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        quote = repository.getRotatingQuote(homeQuotes)
    }

    val today = remember { Calendar.getInstance() }
    val todayDateKey = remember { dateKeyFor(today) }
    val todayStartMillis = remember { startOfDayMillis(today) }
    val cells = remember { buildMonthGrid(today) }

    val onboardingStartDateKey = if (userData.onboardingCompletedAtMillis > 0L) {
        dateKeyFromMillis(userData.onboardingCompletedAtMillis)
    } else {
        null
    }

    val streak = remember(userData.completedDates) {
        calculateStreak(userData.completedDates, today)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DescintBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 60.dp)
        ) {
            HomeHeader(
                name = userData.name,
                quote = quote,
                onPanelClick = { /* TODO: open panel */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            StreakCalendarCard(
                streakDays = streak,
                cells = cells,
                dayStateFor = { cell ->
                    resolveDayState(
                        cell = cell,
                        completedDates = userData.completedDates,
                        onboardingStartDateKey = onboardingStartDateKey,
                        todayDateKey = todayDateKey,
                        todayStartMillis = todayStartMillis,
                        cellStartMillis = parseDateKeyToStartMillis(cell.dateKey)
                    )
                }
            )
        }
    }
}

@Composable
private fun HomeHeader(
    name: String,
    quote: String,
    onPanelClick: () -> Unit
) {
    val nameFontSize = when {
        name.length > 22 -> 14.sp
        name.length > 14 -> 15.sp
        else -> 17.sp
    }

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Changed verticalAlignment to CenterVertically here
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            AvatarPlaceholder()

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${greetingForCurrentTime()},",
                        color = DescintWhite,
                        fontFamily = albertsans,
                        fontSize = 17.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = "$name 👋",
                        color = DescintWhite,
                        fontFamily = albertsans,
                        fontSize = nameFontSize,
                        lineHeight = nameFontSize * 1.15f,
                        fontWeight = FontWeight.Normal
                    )
                }

                if (quote.isNotBlank()) {
                    Text(
                        text = quote,
                        color = DescintWhite.copy(alpha = .7f),
                        fontFamily = inter,
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        PanelButton(onClick = onPanelClick)
    }
}

@Composable
private fun AvatarPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.profile_icon),
            contentDescription = "Profile",
            tint = DescintWhite,
            modifier = Modifier.size(32.dp)
        )
    }
}

// Simple hand-drawn bullet-list glyph (3 rows of dot + line) — avoids pulling
// in the material-icons-extended dependency for one icon. Does nothing on
// click yet; onPanelClick is wired up for whenever the panel itself exists.
@Composable
private fun PanelButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(3) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(2.5.dp)
                            .background(DescintWhite, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 11.dp, height = 1.5.dp)
                            .background(DescintWhite)
                    )
                }
            }
        }
    }
}

private fun greetingForCurrentTime(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..20 -> "Good Evening"
        else -> "Good Night"
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(){
    PracticeTheme {
        HomeScreen()
    }
}