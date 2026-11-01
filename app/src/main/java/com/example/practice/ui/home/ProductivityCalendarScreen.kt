package com.example.practice.ui.home

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.practice.data.CalendarDeadline
import com.example.practice.data.ProductivityCalendarData
import com.example.practice.data.UserData
import com.example.practice.data.UserPreferencesRepository
import com.example.practice.data.WeeklyTarget
import com.example.practice.ui.theme.AppPageHorizontalMargin
import com.example.practice.ui.theme.DescintBlack
import com.example.practice.ui.theme.DescintOrange
import com.example.practice.ui.theme.DescintWhite
import com.example.practice.ui.theme.albertsans
import com.example.practice.ui.theme.inter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

private val CalendarSurface = Color(0xFF101010)
private val CalendarMuted = Color(0xFF8B8B8B)
private val CalendarPurple = Color(0xFF7846E8)
private val CalendarGreen = Color(0xFF4D9C55)
private val CalendarBlue = Color(0xFF2458DF)
private val CalendarDeadlineGray = Color(0xFF929292)
private val CalendarMonthLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

private data class ProductivityCalendarCell(
    val dateKey: String,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean
)

@Composable
fun ProductivityCalendarScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember { UserPreferencesRepository(context) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val userData by repository.userDataFlow.collectAsState(initial = UserData())

    val todayDateKey = remember { productivityDateKey(Calendar.getInstance()) }
    val initialMonth = remember { monthIndexFor(productivityCalendarFromKey(todayDateKey)) }
    var monthIndex by rememberSaveable { mutableIntStateOf(initialMonth) }
    var selectedDateKey by rememberSaveable { mutableStateOf(todayDateKey) }
    var calendarData by remember { mutableStateOf(userData.productivityCalendar) }
    var noteText by rememberSaveable { mutableStateOf("") }
    var deadlineTitle by rememberSaveable { mutableStateOf("") }
    var deadlineDateDraft by rememberSaveable { mutableStateOf(todayDateKey) }
    var deadlineError by rememberSaveable { mutableStateOf<String?>(null) }
    var showLegend by rememberSaveable { mutableStateOf(false) }
    var showMonthPicker by rememberSaveable { mutableStateOf(false) }
    var monthPickerYear by rememberSaveable { mutableIntStateOf(initialMonth / 12) }
    var showTargetEditor by rememberSaveable { mutableStateOf(false) }
    var editorWeekStart by rememberSaveable { mutableStateOf(weekStartFor(todayDateKey)) }
    var editorSelectedDateKey by rememberSaveable { mutableStateOf(todayDateKey) }
    var editorTitle by rememberSaveable { mutableStateOf("") }
    var editorNotifications by rememberSaveable { mutableStateOf(false) }
    var editorError by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(userData.productivityCalendar) {
        calendarData = userData.productivityCalendar
    }
    LaunchedEffect(selectedDateKey, calendarData.notesByDate[selectedDateKey]) {
        val storedNote = calendarData.notesByDate[selectedDateKey].orEmpty()
        if (noteText != storedNote) noteText = storedNote
    }
    LaunchedEffect(selectedDateKey, noteText) {
        val storedNote = calendarData.notesByDate[selectedDateKey].orEmpty()
        if (storedNote != noteText) {
            delay(450)
            val notes = calendarData.notesByDate.toMutableMap()
            if (noteText.isBlank()) notes.remove(selectedDateKey) else notes[selectedDateKey] = noteText
            persistCalendarData(calendarData.copy(notesByDate = notes), repository, coroutineScope) { calendarData = it }
        }
    }

    val displayedMonth = calendarFromMonthIndex(monthIndex)
    val displayedYear = displayedMonth.get(Calendar.YEAR)
    val displayedMonthIndex = displayedMonth.get(Calendar.MONTH)
    val monthCells = remember(monthIndex) {
        buildProductivityMonthGrid(displayedYear, displayedMonthIndex)
    }
    val activeWeekStart = weekStartFor(selectedDateKey)
    val weeklyTarget = calendarData.weeklyTargets.firstOrNull { it.weekStartDateKey == activeWeekStart }
    var lastWeeklyTargetForDisplay by remember { mutableStateOf(weeklyTarget) }
    LaunchedEffect(weeklyTarget) {
        if (weeklyTarget != null) lastWeeklyTargetForDisplay = weeklyTarget
    }
    val selectedDeadlines = calendarData.deadlines.filter { it.dateKey == selectedDateKey }
    val closestDeadlineDate = calendarData.deadlines
        .filter { !it.cleared && it.dateKey >= todayDateKey }
        .minByOrNull { it.dateKey }
        ?.dateKey
    val calendarStageTargetHeight = 26.dp + 40.dp * (monthCells.size / 7).toFloat()
    val calendarStageHeight by animateDpAsState(
        targetValue = calendarStageTargetHeight,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "calendarStageHeight"
    )
    val monthTitleColor by animateColorAsState(
        targetValue = if (showLegend) CalendarMuted else DescintWhite,
        animationSpec = tween(220),
        label = "calendarMonthTitleColor"
    )
    val infoIconColor by animateColorAsState(
        targetValue = if (showLegend) DescintWhite else CalendarMuted,
        animationSpec = tween(220),
        label = "calendarInfoIconColor"
    )

    fun saveCalendar(updated: ProductivityCalendarData) {
        calendarData = updated
        coroutineScope.launch { repository.saveProductivityCalendar(updated) }
    }

    fun selectDate(dateKey: String) {
        if (dateKey == selectedDateKey) return
        val notes = calendarData.notesByDate.toMutableMap()
        if (noteText.isBlank()) notes.remove(selectedDateKey) else notes[selectedDateKey] = noteText
        saveCalendar(calendarData.copy(notesByDate = notes))
        selectedDateKey = dateKey
        deadlineDateDraft = dateKey
        noteText = notes[dateKey].orEmpty()
        monthIndex = monthIndexFor(productivityCalendarFromKey(dateKey))
    }

    fun saveDeadline(dateKey: String) {
        val cleanTitle = deadlineTitle.trim()
        if (cleanTitle.isEmpty()) {
            deadlineError = "Enter a deadline first"
            return
        }
        val deadline = CalendarDeadline(dateKey = dateKey, title = cleanTitle)
        saveCalendar(calendarData.copy(deadlines = calendarData.deadlines + deadline))
        deadlineTitle = ""
        deadlineError = null
        selectDate(dateKey)
    }

    fun openDeadlinePicker() {
        val initialDate = productivityCalendarFromKey(deadlineDateDraft)
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val dateKey = productivityDateKey(year, month + 1, day)
                deadlineDateDraft = dateKey
                if (deadlineTitle.isNotBlank()) {
                    saveDeadline(dateKey)
                } else {
                    selectDate(dateKey)
                }
            },
            initialDate.get(Calendar.YEAR),
            initialDate.get(Calendar.MONTH),
            initialDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun openTargetEditor(existingTarget: WeeklyTarget? = null) {
        val targetWeek = existingTarget?.weekStartDateKey ?: weekStartFor(selectedDateKey)
        val existingForWeek = existingTarget
            ?: calendarData.weeklyTargets.firstOrNull { it.weekStartDateKey == targetWeek }
        editorWeekStart = targetWeek
        editorSelectedDateKey = if (weekStartFor(selectedDateKey) == targetWeek) {
            selectedDateKey
        } else {
            targetWeek
        }
        editorTitle = existingForWeek?.title.orEmpty()
        editorNotifications = existingForWeek?.notificationsEnabled ?: false
        editorError = null
        showTargetEditor = true
    }

    fun loadEditorWeek(newWeekStart: String) {
        editorWeekStart = newWeekStart
        val target = calendarData.weeklyTargets.firstOrNull { it.weekStartDateKey == newWeekStart }
        editorTitle = target?.title.orEmpty()
        editorNotifications = target?.notificationsEnabled ?: false
        editorError = null
    }

    fun chooseEditorDate(dateKey: String) {
        editorSelectedDateKey = dateKey
        loadEditorWeek(weekStartFor(dateKey))
    }

    fun saveTarget() {
        val cleanTitle = editorTitle.trim()
        if (cleanTitle.isEmpty()) {
            editorError = "Create a target before saving"
            return
        }
        val existing = calendarData.weeklyTargets.firstOrNull { it.weekStartDateKey == editorWeekStart }
        val target = WeeklyTarget(
            id = existing?.id ?: java.util.UUID.randomUUID().toString(),
            weekStartDateKey = editorWeekStart,
            title = cleanTitle,
            notificationsEnabled = editorNotifications,
            completed = existing?.completed ?: false
        )
        val notes = calendarData.notesByDate.toMutableMap()
        if (noteText.isBlank()) notes.remove(selectedDateKey) else notes[selectedDateKey] = noteText
        val updated = calendarData.copy(
            weeklyTargets = calendarData.weeklyTargets
                .filterNot { it.weekStartDateKey == editorWeekStart } + target,
            notesByDate = notes
        )
        saveCalendar(updated)
        selectedDateKey = editorSelectedDateKey
        deadlineDateDraft = editorSelectedDateKey
        noteText = notes[editorSelectedDateKey].orEmpty()
        monthIndex = monthIndexFor(productivityCalendarFromKey(editorSelectedDateKey))
        showTargetEditor = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CalendarSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppPageHorizontalMargin)
            .padding(top = 56.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Your personal calendar",
                    color = DescintWhite,
                    fontFamily = albertsans,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light
                )
                Text(
                    text = "Edit your goals, Set your task",
                    color = CalendarMuted,
                    fontFamily = inter,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            CalendarMenuGlyph()
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            if (showLegend) {
                                showLegend = false
                                showMonthPicker = false
                            } else {
                                monthPickerYear = displayedYear
                                showMonthPicker = true
                            }
                        }
                        .semantics {
                            contentDescription = if (showLegend) {
                                "Show calendar"
                            } else {
                                "Choose month, ${monthTitle(displayedMonth)}"
                            }
                        }
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthTitle(displayedMonth),
                        color = monthTitleColor,
                        fontFamily = inter,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(3.dp))
                    Icon(
                        Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = monthTitleColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMonthPicker,
                    onDismissRequest = { showMonthPicker = false },
                    modifier = Modifier
                        .width(252.dp)
                        .background(Color(0xFF151515), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF343434), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CalendarIconAction(
                            icon = Icons.Filled.ChevronLeft,
                            description = "Previous year",
                            onClick = { monthPickerYear -= 1 },
                            size = 28.dp,
                            iconSize = 18.dp,
                            tint = DescintWhite
                        )
                        Text(
                            text = monthPickerYear.toString(),
                            color = DescintWhite,
                            fontFamily = inter,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        CalendarIconAction(
                            icon = Icons.Filled.ChevronRight,
                            description = "Next year",
                            onClick = { monthPickerYear += 1 },
                            size = 28.dp,
                            iconSize = 18.dp,
                            tint = DescintWhite
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    CalendarMonthLabels.chunked(3).forEachIndexed { rowIndex, monthRow ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            monthRow.forEachIndexed { columnIndex, label ->
                                val selectedMonth = rowIndex * 3 + columnIndex
                                val isDisplayedMonth = monthPickerYear == displayedYear && selectedMonth == displayedMonthIndex
                                val monthTileColor by animateColorAsState(
                                    targetValue = if (isDisplayedMonth) DescintOrange else Color(0xFF202020),
                                    animationSpec = tween(180),
                                    label = "monthPickerTile"
                                )
                                val monthLabelColor by animateColorAsState(
                                    targetValue = if (isDisplayedMonth) DescintWhite else CalendarMuted,
                                    animationSpec = tween(180),
                                    label = "monthPickerLabel"
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(monthTileColor)
                                        .clickable {
                                            monthIndex = monthPickerYear * 12 + selectedMonth
                                            showMonthPicker = false
                                        }
                                        .semantics { contentDescription = "$label $monthPickerYear" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, color = monthLabelColor, fontFamily = inter, fontSize = 12.sp)
                                }
                            }
                        }
                        if (rowIndex < 3) Spacer(Modifier.height(4.dp))
                    }
                }
            }
            CalendarIconAction(
                icon = Icons.Filled.ChevronLeft,
                description = "Previous month",
                onClick = { showMonthPicker = false; monthIndex -= 1 },
                iconSize = 19.dp
            )
            CalendarIconAction(
                icon = Icons.Filled.ChevronRight,
                description = "Next month",
                onClick = { showMonthPicker = false; monthIndex += 1 },
                iconSize = 19.dp
            )
            Spacer(Modifier.width(10.dp))
            CalendarIconAction(
                icon = Icons.Filled.Info,
                description = if (showLegend) "Show calendar" else "Calendar legend",
                onClick = { showMonthPicker = false; showLegend = !showLegend },
                iconSize = 16.dp,
                tint = infoIconColor
            )
        }

        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(calendarStageHeight)
                .clipToBounds()
        ) {
            AnimatedContent(
                targetState = showLegend,
                transitionSpec = {
                    (fadeIn(tween(230)) + scaleIn(initialScale = 0.98f, animationSpec = tween(230))) togetherWith
                        (fadeOut(tween(160)) + scaleOut(targetScale = 0.98f, animationSpec = tween(160)))
                },
                contentAlignment = Alignment.TopStart,
                label = "calendarDisplayMode"
            ) { showingLegend ->
                if (showingLegend) {
                    CalendarLegendContent(modifier = Modifier.fillMaxWidth())
                } else {
                    AnimatedContent(
                        targetState = monthIndex,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { width -> width / 14 }) togetherWith
                                    (fadeOut(tween(170)) + slideOutHorizontally(tween(170)) { width -> -width / 14 })
                            } else {
                                (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { width -> -width / 14 }) togetherWith
                                    (fadeOut(tween(170)) + slideOutHorizontally(tween(170)) { width -> width / 14 })
                            }
                        },
                        label = "calendarMonthGrid"
                    ) { animatedMonthIndex ->
                        val animatedMonth = remember(animatedMonthIndex) {
                            calendarFromMonthIndex(animatedMonthIndex)
                        }
                        val animatedCells = remember(animatedMonthIndex) {
                            buildProductivityMonthGrid(
                                animatedMonth.get(Calendar.YEAR),
                                animatedMonth.get(Calendar.MONTH)
                            )
                        }
                        CalendarMonthGrid(
                            modifier = Modifier.fillMaxWidth(),
                            cells = animatedCells,
                            todayDateKey = todayDateKey,
                            selectedDateKey = selectedDateKey,
                            deadlines = calendarData.deadlines,
                            closestDeadlineDate = closestDeadlineDate,
                            onDateClick = ::selectDate
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(0.43f)
                    .height(44.dp)
                    .shadow(5.dp, RoundedCornerShape(7.dp), clip = false)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFFF8848), DescintOrange)))
                    .clickable { openTargetEditor() }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Add weekly\ntargets →",
                    color = DescintWhite,
                    fontFamily = inter,
                    fontSize = 11.sp,
                    lineHeight = 12.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .shadow(4.dp, RoundedCornerShape(7.dp), clip = false)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFF0B0B0B))
                    .border(1.dp, Color(0xFF303030), RoundedCornerShape(7.dp))
                    .padding(start = 10.dp, end = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = deadlineTitle,
                    onValueChange = {
                        deadlineTitle = it
                        deadlineError = null
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = TextStyle(color = DescintWhite, fontFamily = inter, fontSize = 12.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { saveDeadline(deadlineDateDraft) }),
                    decorationBox = { innerTextField ->
                        Box {
                            if (deadlineTitle.isEmpty()) {
                                Text(
                                    text = "Mark a deadline…",
                                    color = CalendarMuted,
                                    fontFamily = inter,
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                CalendarIconAction(
                    icon = Icons.Filled.CalendarMonth,
                    description = "Choose deadline date",
                    onClick = ::openDeadlinePicker,
                    size = 31.dp,
                    iconSize = 15.dp
                )
            }
        }
        val deadlineCaption = deadlineError ?: if (deadlineDateDraft != selectedDateKey) {
            "Deadline date: ${shortDateLabel(deadlineDateDraft)}"
        } else {
            null
        }
        AnimatedVisibility(
            visible = deadlineCaption != null,
            enter = fadeIn(tween(180)) + expandVertically(tween(220, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(140)) + shrinkVertically(tween(180, easing = FastOutSlowInEasing))
        ) {
            Text(
                text = deadlineCaption.orEmpty(),
                color = if (deadlineError != null) Color(0xFFFF7777) else CalendarMuted,
                fontFamily = inter,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 5.dp, start = 2.dp)
            )
        }

        AnimatedVisibility(
            visible = selectedDeadlines.isNotEmpty(),
            enter = fadeIn(tween(180)) + expandVertically(tween(240, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(140)) + shrinkVertically(tween(210, easing = FastOutSlowInEasing))
        ) {
            Column {
                Spacer(Modifier.height(10.dp))
                SelectedDayDeadlines(
                    deadlines = selectedDeadlines,
                    onToggle = { id ->
                        val updatedDeadlines = calendarData.deadlines.map { deadline ->
                            if (deadline.id == id) deadline.copy(cleared = !deadline.cleared) else deadline
                        }
                        saveCalendar(calendarData.copy(deadlines = updatedDeadlines))
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = weeklyTarget != null,
            enter = fadeIn(tween(180)) + expandVertically(tween(230, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(220, easing = FastOutSlowInEasing))
        ) {
            val targetForDisplay = weeklyTarget ?: lastWeeklyTargetForDisplay
            if (targetForDisplay != null) {
                Column {
                    Spacer(Modifier.height(14.dp))
                    WeeklyTargetCard(
                        target = targetForDisplay,
                        onEdit = { target -> openTargetEditor(target) },
                        onMarkDone = {
                            val updatedTargets = calendarData.weeklyTargets.map { current ->
                                if (current.id == targetForDisplay.id) current.copy(completed = true) else current
                            }
                            saveCalendar(calendarData.copy(weeklyTargets = updatedTargets))
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = "Add your notes",
            color = DescintWhite,
            fontFamily = inter,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .shadow(7.dp, RoundedCornerShape(10.dp), clip = false)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF171717), Color(0xFF111111))))
                .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(10.dp))
                .padding(13.dp),
            contentAlignment = Alignment.TopStart
        ) {
            BasicTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(color = DescintWhite, fontFamily = inter, fontSize = 12.sp, lineHeight = 17.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                decorationBox = { innerTextField ->
                    Box {
                        if (noteText.isEmpty()) {
                            Text("How was your day !!", color = CalendarMuted, fontFamily = inter, fontSize = 12.sp)
                        }
                        innerTextField()
                    }
                }
            )
        }
    }

    if (showTargetEditor) {
        WeeklyTargetEditorDialog(
            weekStartDateKey = editorWeekStart,
            selectedDateKey = editorSelectedDateKey,
            onDateSelected = ::chooseEditorDate,
            title = editorTitle,
            onTitleChange = { editorTitle = it; editorError = null },
            notificationsEnabled = editorNotifications,
            onNotificationsChange = { editorNotifications = it },
            errorMessage = editorError,
            onDismiss = { showTargetEditor = false },
            onSave = ::saveTarget
        )
    }
}

@Composable
private fun CalendarMonthGrid(
    modifier: Modifier = Modifier,
    cells: List<ProductivityCalendarCell>,
    todayDateKey: String,
    selectedDateKey: String,
    deadlines: List<CalendarDeadline>,
    closestDeadlineDate: String?,
    onDateClick: (String) -> Unit
) {
    val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            weekdays.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    color = CalendarMuted,
                    fontFamily = inter,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                week.forEach { cell ->
                    val isToday = cell.dateKey == todayDateKey
                    val isSelected = cell.dateKey == selectedDateKey
                    val deadlineColor = deadlineIndicatorColor(cell.dateKey, deadlines, closestDeadlineDate)
                    val marker = deadlineColor.takeIf { isToday || isSelected }
                    val targetFillColor = when {
                        isToday -> DescintOrange
                        isSelected -> CalendarPurple
                        cell.isCurrentMonth && deadlineColor != null -> deadlineColor
                        else -> Color.Transparent
                    }
                    val fillColor by animateColorAsState(
                        targetValue = targetFillColor,
                        animationSpec = tween(220),
                        label = "calendarDateFill"
                    )
                    val dateTextColor by animateColorAsState(
                        targetValue = when {
                            !cell.isCurrentMonth -> Color(0xFF555555)
                            isToday || isSelected || deadlineColor != null -> DescintWhite
                            else -> Color(0xFFD0D0D0)
                        },
                        animationSpec = tween(190),
                        label = "calendarDateText"
                    )
                    val markerColor by animateColorAsState(
                        targetValue = marker ?: Color.Transparent,
                        animationSpec = tween(220),
                        label = "calendarDeadlineMarker"
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable { onDateClick(cell.dateKey) }
                            .semantics { contentDescription = shortDateLabel(cell.dateKey) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(fillColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cell.dayOfMonth.toString(),
                                color = dateTextColor,
                                fontFamily = inter,
                                fontSize = 12.sp,
                                fontWeight = if (isToday || isSelected) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Box(Modifier.size(4.dp).clip(CircleShape).background(markerColor))
                    }
                }
            }
        }
    }
}

private fun deadlineIndicatorColor(
    dateKey: String,
    deadlines: List<CalendarDeadline>,
    closestDeadlineDate: String?
): Color? {
    val onDate = deadlines.filter { it.dateKey == dateKey }
    if (onDate.isEmpty()) return null
    if (onDate.any { !it.cleared } && dateKey == closestDeadlineDate) return CalendarBlue
    if (onDate.any { !it.cleared }) return CalendarDeadlineGray
    return CalendarGreen
}

@Composable
private fun SelectedDayDeadlines(
    deadlines: List<CalendarDeadline>,
    onToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(tween(220, easing = FastOutSlowInEasing))
            .shadow(6.dp, RoundedCornerShape(11.dp), clip = false)
            .clip(RoundedCornerShape(11.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF171717), CalendarSurface)))
            .border(1.dp, Color(0xFF292929), RoundedCornerShape(11.dp))
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Deadlines", color = DescintWhite, fontFamily = inter, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        deadlines.forEach { deadline ->
            val titleColor by animateColorAsState(
                targetValue = if (deadline.cleared) CalendarMuted else DescintWhite,
                animationSpec = tween(220),
                label = "deadlineTitleColor"
            )
            val actionColor by animateColorAsState(
                targetValue = if (deadline.cleared) CalendarGreen else DescintWhite,
                animationSpec = tween(220),
                label = "deadlineActionColor"
            )
            val actionBackground by animateColorAsState(
                targetValue = if (deadline.cleared) CalendarGreen.copy(alpha = 0.14f) else Color(0xFF252525),
                animationSpec = tween(220),
                label = "deadlineActionBackground"
            )
            Row(
                modifier = Modifier.fillMaxWidth().animateContentSize(tween(200)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = deadline.title,
                    color = titleColor,
                    fontFamily = inter,
                    fontSize = 12.sp,
                    textDecoration = if (deadline.cleared) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (deadline.cleared) "Cleared" else "Mark done",
                    color = actionColor,
                    fontFamily = inter,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .animateContentSize(tween(180))
                        .clip(RoundedCornerShape(6.dp))
                        .background(actionBackground)
                        .clickable { onToggle(deadline.id) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun WeeklyTargetCard(
    target: WeeklyTarget,
    onEdit: (WeeklyTarget) -> Unit,
    onMarkDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(tween(240, easing = FastOutSlowInEasing))
            .shadow(8.dp, RoundedCornerShape(13.dp), clip = false)
            .clip(RoundedCornerShape(13.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF191919), Color(0xFF111111))))
            .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(13.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("This weeks target :", color = DescintWhite, fontFamily = inter, fontSize = 12.sp, modifier = Modifier.weight(1f))
            CalendarIconAction(
                icon = Icons.Filled.Edit,
                description = "Edit weekly target",
                onClick = { onEdit(target) },
                size = 27.dp,
                iconSize = 15.dp,
                tint = DescintOrange
            )
        }
        Spacer(Modifier.height(4.dp))
        AnimatedContent(
            targetState = target,
            transitionSpec = {
                (fadeIn(tween(190)) + slideInVertically(tween(220)) { height -> height / 10 }) togetherWith
                    (fadeOut(tween(140)) + slideOutVertically(tween(170)) { height -> -height / 10 })
            },
            label = "weeklyTargetBody"
        ) { displayedTarget ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                CalendarSmallPill(
                    text = if (displayedTarget.completed) "Completed" else "Click to mark done",
                    enabled = !displayedTarget.completed,
                    completed = displayedTarget.completed,
                    onClick = onMarkDone
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = displayedTarget.title,
                    color = DescintWhite,
                    fontFamily = inter,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CalendarSmallPill(
    text: String,
    enabled: Boolean,
    completed: Boolean = false,
    onClick: () -> Unit
) {
    val pillBackground by animateColorAsState(
        targetValue = if (completed) CalendarGreen.copy(alpha = 0.18f) else Color(0xFFE6E6E6),
        animationSpec = tween(220),
        label = "weeklyTargetPillBackground"
    )
    val pillTextColor by animateColorAsState(
        targetValue = if (completed) CalendarGreen else Color(0xFF151515),
        animationSpec = tween(220),
        label = "weeklyTargetPillText"
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(pillBackground)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = pillTextColor,
            fontFamily = inter,
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun CalendarIconAction(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 30.dp,
    iconSize: androidx.compose.ui.unit.Dp = 16.dp,
    tint: Color = CalendarMuted
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
private fun CalendarMenuGlyph() {
    Column(
        modifier = Modifier
            .size(width = 28.dp, height = 28.dp)
            .clip(CircleShape)
            .clickable { /* Keep the existing panel affordance; panel content is not in this calendar scope. */ },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(3) {
            Row(
                modifier = Modifier.padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(2.dp).clip(CircleShape).background(DescintWhite))
                Spacer(Modifier.width(3.dp))
                Box(Modifier.width(10.dp).height(1.dp).background(DescintWhite))
            }
        }
    }
}

@Composable
private fun CalendarLegendContent(modifier: Modifier = Modifier) {
    val entries = listOf(
        DescintOrange to "Present day",
        CalendarPurple to "Selected day",
        CalendarGreen to "Cleared deadline",
        CalendarBlue to "Closest deadline",
        CalendarDeadlineGray to "Any other deadline"
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(tween(220, easing = FastOutSlowInEasing))
            .padding(start = 12.dp, top = 9.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        entries.forEachIndexed { index, entry ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(180, delayMillis = index * 35)) +
                    expandVertically(tween(200, delayMillis = index * 35, easing = FastOutSlowInEasing))
            ) {
                CalendarLegendRow(entry.first, entry.second)
            }
        }
    }
}

@Composable
private fun CalendarLegendRow(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(11.dp))
        Text(label, color = DescintWhite, fontFamily = inter, fontSize = 13.sp)
    }
}

@Composable
private fun WeeklyTargetEditorDialog(
    weekStartDateKey: String,
    selectedDateKey: String,
    onDateSelected: (String) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val editorVisibility = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) { editorVisibility.targetState = true }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visibleState = editorVisibility,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.985f, animationSpec = tween(260)),
            label = "weeklyTargetEditorEnter"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DescintBlack)
                    .systemBarsPadding()
                    .padding(horizontal = AppPageHorizontalMargin)
                    .padding(top = 28.dp, bottom = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Just some Fields", color = DescintWhite, fontFamily = albertsans, fontSize = 26.sp)
                        Text(
                            "make sure you complete it till the weekend :)",
                            color = DescintWhite.copy(alpha = 0.8f),
                            fontFamily = inter,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                    CalendarIconAction(
                        icon = Icons.Filled.Close,
                        description = "Close target editor",
                        onClick = onDismiss,
                        iconSize = 18.dp
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 22.dp)
                ) {
                    CalendarWeekStrip(
                        weekStartDateKey = weekStartDateKey,
                        selectedDateKey = selectedDateKey,
                        onDateSelected = onDateSelected
                    )
                    Spacer(Modifier.height(18.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Selected week", color = DescintWhite, fontFamily = inter, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color(0xFF252525))
                                .padding(horizontal = 11.dp, vertical = 8.dp)
                        ) {
                            Text(weekLabel(weekStartDateKey), color = DescintWhite, fontFamily = inter, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .shadow(5.dp, RoundedCornerShape(23.dp), clip = false)
                            .clip(RoundedCornerShape(23.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFF202020), Color(0xFF171717))))
                            .border(1.dp, Color(0xFF282828), RoundedCornerShape(23.dp))
                            .clickable { onNotificationsChange(!notificationsEnabled) }
                            .padding(horizontal = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notifications", color = DescintWhite, fontFamily = inter, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        CalendarToggle(checked = notificationsEnabled, onCheckedChange = onNotificationsChange)
                    }
                    Spacer(Modifier.height(22.dp))
                    Text("Create a target", color = DescintWhite, fontFamily = inter, fontSize = 13.sp)
                    Spacer(Modifier.height(9.dp))
                    CalendarTextInput(
                        value = title,
                        onValueChange = onTitleChange,
                        placeholder = "Complete assignments"
                    )
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn(tween(180)) + expandVertically(tween(220, easing = FastOutSlowInEasing)),
                        exit = fadeOut(tween(140)) + shrinkVertically(tween(180, easing = FastOutSlowInEasing))
                    ) {
                        Text(
                            errorMessage.orEmpty(),
                            color = Color(0xFFFF7777),
                            fontFamily = inter,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(5.dp, RoundedCornerShape(9.dp), clip = false)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFFFF8848), DescintOrange)))
                        .clickable(onClick = onSave),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Save", color = DescintWhite, fontFamily = inter, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun CalendarWeekStrip(
    weekStartDateKey: String,
    selectedDateKey: String,
    onDateSelected: (String) -> Unit
) {
    val stripStart = shiftDateKey(weekStartDateKey, -7)
    val todayDateKey = productivityDateKey(Calendar.getInstance())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(tween(220, easing = FastOutSlowInEasing))
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFF080808))
            .padding(vertical = 5.dp, horizontal = 5.dp)
    ) {
        repeat(3) { weekIndex ->
            val rowStartDateKey = shiftDateKey(stripStart, weekIndex * 7)
            val isSelectedWeek = rowStartDateKey == weekStartDateKey
            val weekBackground by animateColorAsState(
                targetValue = if (isSelectedWeek) Color.Transparent else Color(0xFF0C0C0C),
                animationSpec = tween(220),
                label = "targetWeekBackground"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(weekBackground),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(7) { dayOffset ->
                    val dateKey = shiftDateKey(rowStartDateKey, dayOffset)
                    val date = productivityCalendarFromKey(dateKey)
                    val isToday = dateKey == todayDateKey
                    val isSelected = dateKey == selectedDateKey
                    val dayTextColor by animateColorAsState(
                        targetValue = if (isSelectedWeek) DescintWhite else CalendarMuted.copy(alpha = 0.58f),
                        animationSpec = tween(220),
                        label = "targetWeekDateText"
                    )
                    val targetCircleColor = when {
                        isToday -> DescintOrange
                        isSelected -> CalendarPurple
                        else -> Color.Transparent
                    }
                    val circleColor by animateColorAsState(
                        targetValue = targetCircleColor,
                        animationSpec = tween(210),
                        label = "targetDateCircle"
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clickable { onDateSelected(dateKey) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(27.dp)
                                .clip(CircleShape)
                                .background(circleColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.get(Calendar.DAY_OF_MONTH).toString(),
                                color = dayTextColor,
                                fontFamily = inter,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) DescintOrange else Color(0xFF3A3A3A),
        animationSpec = tween(200),
        label = "notificationToggleTrack"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 15.dp else 0.dp,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "notificationToggleThumb"
    )
    Box(
        modifier = Modifier
            .size(width = 34.dp, height = 19.dp)
            .clip(CircleShape)
            .background(trackColor)
            .clickable { onCheckedChange(!checked) }
            .semantics { contentDescription = "Notifications ${if (checked) "on" else "off"}" },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            Modifier
                .offset(x = thumbOffset)
                .padding(2.dp)
                .size(15.dp)
                .clip(CircleShape)
                .background(Color(0xFFE6E6E6))
        )
    }
}

@Composable
private fun CalendarTextInput(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .shadow(6.dp, RoundedCornerShape(8.dp), clip = false)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF171717))
            .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
            .padding(horizontal = 13.dp, vertical = 13.dp),
        contentAlignment = Alignment.TopStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxSize(),
            textStyle = TextStyle(color = DescintWhite, fontFamily = inter, fontSize = 14.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) Text(placeholder, color = CalendarMuted, fontFamily = inter, fontSize = 13.sp)
                    innerTextField()
                }
            }
        )
    }
}

private fun persistCalendarData(
    updated: ProductivityCalendarData,
    repository: UserPreferencesRepository,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onLocalUpdate: (ProductivityCalendarData) -> Unit
) {
    onLocalUpdate(updated)
    coroutineScope.launch { repository.saveProductivityCalendar(updated) }
}

private fun buildProductivityMonthGrid(year: Int, month: Int): List<ProductivityCalendarCell> {
    val firstOfMonth = Calendar.getInstance().apply {
        clear()
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstOffset = (firstOfMonth.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val start = (firstOfMonth.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -firstOffset) }
    val cellCount = kotlin.math.ceil((firstOffset + firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)) / 7.0).toInt() * 7
    return (0 until cellCount).map { index ->
        val date = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, index) }
        ProductivityCalendarCell(
            dateKey = productivityDateKey(date),
            dayOfMonth = date.get(Calendar.DAY_OF_MONTH),
            isCurrentMonth = date.get(Calendar.YEAR) == year && date.get(Calendar.MONTH) == month
        )
    }
}

private fun monthIndexFor(calendar: Calendar): Int =
    calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH)

private fun calendarFromMonthIndex(monthIndex: Int): Calendar {
    val year = monthIndex / 12
    val month = monthIndex % 12
    return Calendar.getInstance().apply {
        clear()
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
}

private fun productivityCalendarFromKey(dateKey: String): Calendar {
    val parts = dateKey.split('-')
    return Calendar.getInstance().apply {
        clear()
        set(Calendar.YEAR, parts.getOrNull(0)?.toIntOrNull() ?: 2000)
        set(Calendar.MONTH, (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1)
        set(Calendar.DAY_OF_MONTH, parts.getOrNull(2)?.toIntOrNull() ?: 1)
    }
}

private fun productivityDateKey(calendar: Calendar): String = productivityDateKey(
    calendar.get(Calendar.YEAR),
    calendar.get(Calendar.MONTH) + 1,
    calendar.get(Calendar.DAY_OF_MONTH)
)

private fun productivityDateKey(year: Int, month: Int, day: Int): String =
    String.format(Locale.US, "%04d-%02d-%02d", year, month, day)

private fun weekStartFor(dateKey: String): String {
    val calendar = productivityCalendarFromKey(dateKey)
    val mondayOffset = mondayFirstWeekdayOffset(dateKey)
    calendar.add(Calendar.DAY_OF_YEAR, -mondayOffset)
    return productivityDateKey(calendar)
}

private fun mondayFirstWeekdayOffset(dateKey: String): Int {
    val calendar = productivityCalendarFromKey(dateKey)
    return (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
}

private fun shiftDateKey(dateKey: String, days: Int): String =
    productivityCalendarFromKey(dateKey).apply { add(Calendar.DAY_OF_YEAR, days) }.let(::productivityDateKey)

private fun monthTitle(calendar: Calendar): String =
    java.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

private fun weekLabel(weekStartDateKey: String): String {
    val calendar = productivityCalendarFromKey(weekStartDateKey).apply {
        firstDayOfWeek = Calendar.MONDAY
        minimalDaysInFirstWeek = 4
    }
    return "Week ${calendar.get(Calendar.WEEK_OF_YEAR)}, ${calendar.getWeekYear()}"
}

private fun shortDateLabel(dateKey: String): String {
    val date = productivityCalendarFromKey(dateKey)
    return java.text.SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(date.time)
}
