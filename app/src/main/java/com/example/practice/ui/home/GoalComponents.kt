package com.example.practice.ui.home

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practice.data.DailyGoal
import com.example.practice.data.DailyGoalProgress
import com.example.practice.data.DailyGoalType
import com.example.practice.ui.theme.AppPageHorizontalMargin
import com.example.practice.ui.theme.DescintOrange
import com.example.practice.ui.theme.DescintWhite
import com.example.practice.ui.theme.albertsans
import com.example.practice.ui.theme.inter
import kotlinx.coroutines.delay
import java.util.Locale

private val GoalRowColor = Color(0xFF181818)
private val GoalMuted = Color(0xFF8D8D8D)
private val GoalGreen = Color(0xFF3E963F)
private val GoalYellow = Color(0xFFFFB800)

@Composable
fun DailyGoalsSection(
    goals: List<DailyGoal>,
    dateKey: String,
    dayIndex: Int,
    nowMillis: Long,
    onProgressChange: (DailyGoal, DailyGoalProgress) -> Unit,
    onDelete: (DailyGoal) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todaysGoals = goals.filter { dayIndex in it.repeatDays }
    val completionCreditedCount = todaysGoals.count { it.progressFor(dateKey).completionCredited }
    val completionSummaryColor by animateColorAsState(
        targetValue = if (todaysGoals.isNotEmpty() && completionCreditedCount == todaysGoals.size) GoalGreen else DescintWhite,
        animationSpec = tween(220),
        label = "goalCompletionSummaryColor"
    )
    var deletingGoalIds by remember { mutableStateOf(emptySet<String>()) }
    val currentOnDelete by rememberUpdatedState(onDelete)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(260, easing = FastOutSlowInEasing))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Today's goal",
                color = DescintWhite,
                fontFamily = inter,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "$completionCreditedCount/${todaysGoals.size} Completed",
                color = completionSummaryColor,
                fontFamily = inter,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 225.dp, max = 280.dp)
                    .animateContentSize(animationSpec = tween(260, easing = FastOutSlowInEasing))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF111111), Color(0xFF080808))
                        )
                    )
                    .border(1.dp, Color(0xFF292929), RoundedCornerShape(18.dp))
                    .padding(7.dp)
                    .then(
                        if (todaysGoals.isNotEmpty()) {
                            Modifier.verticalScroll(rememberScrollState())
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (todaysGoals.isEmpty()) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(220)) + expandVertically(tween(260, easing = FastOutSlowInEasing))
                    ) {
                        EmptyGoalsPrompt(onClick = onAddClick)
                    }
                } else {
                    todaysGoals.forEach { goal ->
                        key(goal.id) {
                            val isDeleting = goal.id in deletingGoalIds
                            LaunchedEffect(isDeleting) {
                                if (isDeleting) {
                                    delay(280)
                                    currentOnDelete(goal)
                                    deletingGoalIds = deletingGoalIds - goal.id
                                }
                            }
                            AnimatedVisibility(
                                visible = !isDeleting,
                                enter = fadeIn(tween(180)) + expandVertically(tween(260, easing = FastOutSlowInEasing)),
                                exit = fadeOut(tween(170)) + shrinkVertically(tween(250, easing = FastOutSlowInEasing))
                            ) {
                                GoalRow(
                                    goal = goal,
                                    dateKey = dateKey,
                                    nowMillis = nowMillis,
                                    onProgressChange = { progress -> onProgressChange(goal, progress) },
                                    onDelete = {
                                        if (goal.id !in deletingGoalIds) {
                                            deletingGoalIds = deletingGoalIds + goal.id
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyGoalsPrompt(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(205.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.height(38.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    Modifier
                        .width(7.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF626262))
                )
                Box(
                    Modifier
                        .width(7.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF626262))
                )
                Box(
                    Modifier
                        .width(44.dp)
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF626262))
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Set up your daily goals\nright now",
                color = Color(0xFF929292),
                fontFamily = inter,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
fun HomeAddGoalButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 520f),
        label = "addGoalButtonScale"
    )
    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(if (isPressed) 3.dp else 9.dp, CircleShape)
            .clip(CircleShape)
            .background(DescintOrange)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add a goal",
            tint = DescintWhite,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun GoalRow(
    goal: DailyGoal,
    dateKey: String,
    nowMillis: Long,
    onProgressChange: (DailyGoalProgress) -> Unit,
    onDelete: () -> Unit
) {
    val progress = goal.progressFor(dateKey)
    val elapsedSeconds = remember(progress.elapsedSeconds, progress.timerStartedAtMillis, nowMillis) {
        if (progress.timerStartedAtMillis == null) {
            progress.elapsedSeconds
        } else {
            progress.elapsedSeconds + ((nowMillis - progress.timerStartedAtMillis) / 1000L).coerceAtLeast(0L)
        }
    }
    val goalNameColor by animateColorAsState(
        targetValue = if (progress.completed) Color(0xFFD0D0D0) else DescintWhite,
        animationSpec = tween(220),
        label = "goalNameColor"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .animateContentSize(animationSpec = tween(180, easing = FastOutSlowInEasing))
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(GoalRowColor, Color(0xFF111111))
                )
            )
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GoalCompletionMark(
            completed = progress.completed,
            onClick = if (progress.completed) null else when (goal.type) {
                DailyGoalType.BINARY, DailyGoalType.TIMER -> {
                    {
                        onProgressChange(
                            progress.copy(
                                completed = true,
                                completionCredited = true,
                                timerStartedAtMillis = null,
                                elapsedSeconds = elapsedSeconds
                            )
                        )
                    }
                }
                DailyGoalType.COUNTER -> null
            }
        )

        Spacer(Modifier.width(9.dp))

        Text(
            text = goal.name,
            modifier = Modifier.weight(1f),
            color = goalNameColor,
            fontFamily = inter,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.width(5.dp))

        when (goal.type) {
            DailyGoalType.BINARY -> {
                AnimatedContent(
                    targetState = progress.completed,
                    contentAlignment = Alignment.Center,
                    label = "binaryGoalAction"
                ) { isCompleted ->
                    if (isCompleted) {
                        GoalIconAction(
                            description = "Restart goal",
                            onClick = { onProgressChange(progress.copy(completed = false)) },
                            modifier = Modifier.size(27.dp),
                            background = GoalYellow,
                            border = null
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Replay,
                                contentDescription = null,
                                tint = Color(0xFF171717),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        GoalActionPill(
                            text = "Mark done",
                            color = GoalGreen,
                            onClick = {
                                onProgressChange(progress.copy(completed = true, completionCredited = true))
                            }
                        )
                    }
                }
            }

            DailyGoalType.TIMER -> {
                val elapsedColorTarget = when {
                    progress.completed -> GoalGreen
                    elapsedSeconds > 0L || progress.timerStartedAtMillis != null -> DescintOrange
                    else -> Color(0xFF4B4B4B)
                }
                val elapsedColor by animateColorAsState(
                    targetValue = elapsedColorTarget,
                    animationSpec = tween(240),
                    label = "timerChipColor"
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(elapsedColor)
                        .clickable(enabled = !progress.completed) {
                            if (progress.timerStartedAtMillis == null) {
                                onProgressChange(progress.copy(timerStartedAtMillis = nowMillis))
                            } else {
                                onProgressChange(
                                    progress.copy(
                                        elapsedSeconds = elapsedSeconds,
                                        timerStartedAtMillis = null
                                    )
                                )
                            }
                        }
                        .padding(horizontal = 9.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatElapsedTime(elapsedSeconds),
                        color = DescintWhite,
                        fontFamily = inter,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.width(5.dp))
                GoalIconAction(
                    icon = { Icon(Icons.Filled.Replay, contentDescription = null, tint = Color(0xFF171717), modifier = Modifier.size(18.dp)) },
                    description = "Reset timer",
                    background = GoalYellow,
                    border = null,
                    onClick = {
                        onProgressChange(
                            progress.copy(
                                completed = false,
                                count = 0,
                                elapsedSeconds = 0L,
                                timerStartedAtMillis = null
                            )
                        )
                    }
                )
            }

            DailyGoalType.COUNTER -> {
                val countTarget = (goal.targetCount ?: 1).coerceAtLeast(1)
                val countLabel = "${progress.count}/$countTarget"
                val countFontSize = when {
                    countLabel.length >= 10 -> 7.sp
                    countLabel.length >= 8 -> 8.sp
                    countLabel.length >= 6 -> 9.sp
                    else -> 10.sp
                }
                val plusBackground by animateColorAsState(
                    targetValue = if (progress.completed) Color(0xFF252525) else Color(0xFF454545),
                    animationSpec = tween(220),
                    label = "counterPlusBackground"
                )
                val plusColor by animateColorAsState(
                    targetValue = if (progress.completed) Color(0xFF777777) else DescintWhite,
                    animationSpec = tween(220),
                    label = "counterPlusColor"
                )
                GoalIconAction(
                    modifier = Modifier.size(27.dp),
                    background = plusBackground,
                    description = "Add one",
                    enabled = !progress.completed,
                    onClick = {
                        val target = (goal.targetCount ?: 1).coerceAtLeast(1)
                        val newCount = (progress.count + 1).coerceAtMost(target)
                        onProgressChange(
                            progress.copy(
                                count = newCount,
                                completed = newCount >= target,
                                completionCredited = progress.completionCredited || newCount >= target
                            )
                        )
                    }
                ) {
                    GoalPlusGlyph(color = plusColor)
                }
                Spacer(Modifier.width(5.dp))
                val countColor by animateColorAsState(
                    targetValue = if (progress.completed) GoalGreen else Color(0xFF4B4B4B),
                    animationSpec = tween(240),
                    label = "counterChipColor"
                )
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(countColor)
                        .padding(horizontal = 2.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = countLabel,
                        color = DescintWhite,
                        fontFamily = inter,
                        fontSize = countFontSize,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                }
                Spacer(Modifier.width(5.dp))
                GoalIconAction(
                    icon = { Icon(Icons.Filled.Replay, contentDescription = null, tint = Color(0xFF171717), modifier = Modifier.size(18.dp)) },
                    description = "Reset count",
                    background = GoalYellow,
                    border = null,
                    onClick = {
                        onProgressChange(
                            progress.copy(
                                completed = false,
                                count = 0,
                                elapsedSeconds = 0L,
                                timerStartedAtMillis = null
                            )
                        )
                    }
                )
            }
        }

        Spacer(Modifier.width(3.dp))
        GoalIconAction(
            icon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFB8B8B8), modifier = Modifier.size(18.dp)) },
            description = "Delete goal",
            onClick = onDelete,
            background = Color.Transparent,
            border = null
        )
    }
}

@Composable
private fun GoalCompletionMark(completed: Boolean, onClick: (() -> Unit)?) {
    val ringColor by animateColorAsState(
        targetValue = if (completed) GoalGreen else Color(0xFFB0B0B0),
        animationSpec = tween(220),
        label = "goalCheckColor"
    )
    Box(
        modifier = Modifier
            .size(23.dp)
            .clip(CircleShape)
            .border(1.5.dp, ringColor, CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics {
                contentDescription = if (completed) "Completed" else "Mark complete"
            },
        contentAlignment = Alignment.Center
    )
}

@Composable
private fun GoalPlusGlyph(color: Color) {
    Canvas(modifier = Modifier.size(14.dp)) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val halfLength = size.minDimension * 0.34f
        val strokeWidth = size.minDimension * 0.12f
        drawLine(
            color = color,
            start = Offset(centerX - halfLength, centerY),
            end = Offset(centerX + halfLength, centerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(centerX, centerY - halfLength),
            end = Offset(centerX, centerY + halfLength),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun GoalActionPill(text: String, color: Color, onClick: () -> Unit) {
    val animatedColor by animateColorAsState(color, tween(240), label = "goalActionColor")
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(animatedColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = DescintWhite,
            fontFamily = inter,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun GoalIconAction(
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier.size(27.dp),
    background: Color = Color(0xFF0D0D0D),
    border: BorderStroke? = BorderStroke(1.dp, Color(0xFF3D3D3D)),
    icon: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .then(if (border != null) Modifier.border(border, CircleShape) else Modifier)
            .graphicsLayer { alpha = if (enabled) 1f else 0.62f }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
            icon()
        }
    }
}

private fun formatElapsedTime(seconds: Long): String {
    val totalSeconds = seconds.coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val remainingSeconds = totalSeconds % 60L
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
}

@Composable
fun AddDailyGoalSheet(
    onClose: () -> Unit,
    onSave: (DailyGoal) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedType by rememberSaveable { mutableStateOf(DailyGoalType.BINARY) }
    var name by rememberSaveable { mutableStateOf("") }
    var durationHours by rememberSaveable { mutableStateOf("00") }
    var durationMinutes by rememberSaveable { mutableStateOf("00") }
    var targetCount by rememberSaveable { mutableStateOf("") }
    var reminderMinutes by rememberSaveable { mutableIntStateOf(-1) }
    var selectedDaysMask by rememberSaveable { mutableIntStateOf(0b0001111) }
    var showValidation by rememberSaveable { mutableStateOf(false) }

    val selectedDays = (0..6).filter { (selectedDaysMask and (1 shl it)) != 0 }.toSet()
    val hoursValue = durationHours.toIntOrNull() ?: 0
    val minutesValue = durationMinutes.toIntOrNull() ?: 0
    val durationSeconds = (hoursValue.coerceIn(0, 23) * 3600) + (minutesValue.coerceIn(0, 59) * 60)
    val countValue = targetCount.toIntOrNull() ?: 0
    val hasName = name.trim().isNotEmpty()
    val typeIsValid = when (selectedType) {
        DailyGoalType.BINARY -> true
        DailyGoalType.TIMER -> durationSeconds > 0
        DailyGoalType.COUNTER -> countValue > 0
    }
    val canSave = hasName && typeIsValid && selectedDays.isNotEmpty()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color(0xFF090909))
            .border(
                width = 1.dp,
                color = Color(0xFF181818),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .padding(horizontal = AppPageHorizontalMargin)
            .padding(bottom = 100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF555555))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add a goal",
                    color = DescintWhite,
                    fontFamily = albertsans,
                    fontSize = 23.sp
                )
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = DescintWhite, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.height(10.dp))
            GoalTypeProgress(selectedType = selectedType)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GoalTypeOption(
                    type = DailyGoalType.BINARY,
                    selected = selectedType == DailyGoalType.BINARY,
                    onClick = { selectedType = DailyGoalType.BINARY },
                    modifier = Modifier.weight(1f)
                )
                GoalTypeOption(
                    type = DailyGoalType.TIMER,
                    selected = selectedType == DailyGoalType.TIMER,
                    onClick = { selectedType = DailyGoalType.TIMER },
                    modifier = Modifier.weight(1f)
                )
                GoalTypeOption(
                    type = DailyGoalType.COUNTER,
                    selected = selectedType == DailyGoalType.COUNTER,
                    onClick = { selectedType = DailyGoalType.COUNTER },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(22.dp))
            FormLabel("Give a name")
            Spacer(Modifier.height(7.dp))
            GoalTextField(
                value = name,
                onValueChange = { name = it; showValidation = false },
                placeholder = "e.g. Drink Water, Study 25 mins",
                keyboardType = KeyboardType.Text
            )

            when (selectedType) {
                DailyGoalType.BINARY -> Unit
                DailyGoalType.TIMER -> {
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FormLabel("Set The Duration")
                        DurationInput(
                            hours = durationHours,
                            minutes = durationMinutes,
                            onHoursChange = { durationHours = it; showValidation = false },
                            onMinutesChange = { durationMinutes = it; showValidation = false }
                        )
                    }
                }
                DailyGoalType.COUNTER -> {
                    Spacer(Modifier.height(14.dp))
                    FormLabel("Counts")
                    Spacer(Modifier.height(7.dp))
                    GoalTextField(
                        value = targetCount,
                        onValueChange = { value ->
                            targetCount = value.filter(Char::isDigit).take(5)
                            showValidation = false
                        },
                        placeholder = "Specify number of counts",
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            ReminderInputRow(
                selectedMinutes = reminderMinutes.takeIf { it >= 0 },
                onPickTime = {
                    val current = if (reminderMinutes >= 0) reminderMinutes else 0
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> reminderMinutes = hour * 60 + minute },
                        current / 60,
                        current % 60,
                        true
                    ).show()
                },
                onClear = { reminderMinutes = -1 }
            )

            Spacer(Modifier.height(22.dp))
            Text(
                text = "Light up the day you want to repeat!",
                color = DescintWhite.copy(alpha = 0.82f),
                fontFamily = inter,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(14.dp))
            RepeatDaySelector(
                selectedDaysMask = selectedDaysMask,
                onToggle = { index ->
                    selectedDaysMask = selectedDaysMask xor (1 shl index)
                    showValidation = false
                }
            )

            if (showValidation && !canSave) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = when {
                        !hasName -> "Give your goal a name"
                        !typeIsValid && selectedType == DailyGoalType.TIMER -> "Set a duration greater than 00:00"
                        !typeIsValid -> "Enter a number of counts"
                        else -> "Choose at least one day"
                    },
                    color = Color(0xFFFF7777),
                    fontFamily = inter,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (canSave) DescintOrange else Color(0xFF6E3925))
                .clickable {
                    if (!canSave) {
                        showValidation = true
                    } else {
                        val goal = DailyGoal(
                            name = name.trim(),
                            type = selectedType,
                            repeatDays = selectedDays,
                            reminderMinutesOfDay = reminderMinutes.takeIf { it >= 0 },
                            durationSeconds = if (selectedType == DailyGoalType.TIMER) durationSeconds else null,
                            targetCount = if (selectedType == DailyGoalType.COUNTER) countValue else null
                        )
                        onSave(goal)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Save",
                color = DescintWhite,
                fontFamily = inter,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun GoalTypeProgress(selectedType: DailyGoalType) {
    val selectedIndex = DailyGoalType.entries.indexOf(selectedType)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        DailyGoalType.entries.forEachIndexed { index, _ ->
            val segmentColor = when {
                index < selectedIndex -> DescintWhite.copy(alpha = 0.8f)
                index == selectedIndex -> DescintOrange
                else -> Color(0xFF4A4A4A)
            }
            Box(
                Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(segmentColor)
            )
        }
    }
}

@Composable
private fun GoalTypeOption(
    type: DailyGoalType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) DescintOrange else Color(0xFF505050)
    Row(
        modifier = modifier
            .height(66.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF111111))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(25.dp)
                .clip(CircleShape)
                .border(1.3.dp, if (selected) DescintOrange else Color(0xFF979797), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when (type) {
                DailyGoalType.BINARY -> Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = if (selected) DescintOrange else Color(0xFFBEBEBE),
                    modifier = Modifier.size(16.dp)
                )
                DailyGoalType.TIMER -> Icon(
                    Icons.Filled.AccessTime,
                    contentDescription = null,
                    tint = if (selected) DescintOrange else Color(0xFFBEBEBE),
                    modifier = Modifier.size(17.dp)
                )
                DailyGoalType.COUNTER -> Text(
                    text = "123",
                    color = if (selected) DescintOrange else Color(0xFFBEBEBE),
                    fontFamily = inter,
                    fontSize = 9.sp
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (type) {
                    DailyGoalType.BINARY -> "Binary"
                    DailyGoalType.TIMER -> "Timer"
                    DailyGoalType.COUNTER -> "Counter"
                },
                color = DescintWhite,
                fontFamily = inter,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = when (type) {
                    DailyGoalType.BINARY -> "just check a box :)"
                    DailyGoalType.TIMER -> "Adds a timer to goal"
                    DailyGoalType.COUNTER -> "Just a simple counter"
                },
                color = GoalMuted,
                fontFamily = inter,
                fontSize = 9.sp,
                lineHeight = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(text = text, color = DescintWhite.copy(alpha = 0.88f), fontFamily = inter, fontSize = 13.sp)
}

@Composable
private fun GoalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0B0B0B))
            .border(1.dp, Color(0xFF292929), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(color = DescintWhite, fontFamily = inter, fontSize = 14.sp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFF707070),
                            fontFamily = inter,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun DurationInput(
    hours: String,
    minutes: String,
    onHoursChange: (String) -> Unit,
    onMinutesChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .width(112.dp)
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0B0B0B))
            .border(1.dp, Color(0xFF292929), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        DurationPart(hours, onHoursChange, label = "Hours")
        Text(text = ":", color = DescintWhite, fontFamily = inter, fontSize = 18.sp)
        DurationPart(minutes, onMinutesChange, label = "Minutes")
    }
}

@Composable
private fun DurationPart(value: String, onValueChange: (String) -> Unit, label: String) {
    BasicTextField(
        value = value,
        onValueChange = { input ->
            val digits = input.filter(Char::isDigit).take(2)
            val number = digits.toIntOrNull()
            onValueChange(if (number != null && label == "Hours") number.coerceAtMost(23).toString().padStart(digits.length, '0')
            else if (number != null) number.coerceAtMost(59).toString().padStart(digits.length, '0')
            else digits)
        },
        modifier = Modifier.width(38.dp),
        singleLine = true,
        textStyle = TextStyle(color = DescintWhite, fontFamily = inter, fontSize = 17.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
private fun ReminderInputRow(
    selectedMinutes: Int?,
    onPickTime: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF202020))
            .clickable(onClick = onPickTime)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Add a reminder", color = DescintWhite, fontFamily = inter, fontSize = 13.sp)
        Spacer(Modifier.width(5.dp))
        Text("(optional)", color = GoalMuted, fontFamily = inter, fontSize = 11.sp)
        Spacer(Modifier.weight(1f))
        Text(
            text = selectedMinutes?.let(::formatClockTime) ?: "00 : 00",
            color = DescintWhite,
            fontFamily = inter,
            fontSize = 18.sp
        )
        if (selectedMinutes != null) {
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.size(22.dp).clip(CircleShape).clickable(onClick = onClear),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Clear reminder", tint = GoalMuted, modifier = Modifier.size(15.dp))
            }
        }
    }
}

private fun formatClockTime(minutesOfDay: Int): String {
    val hour = (minutesOfDay / 60).coerceIn(0, 23)
    val minute = (minutesOfDay % 60).coerceIn(0, 59)
    return String.format(Locale.getDefault(), "%02d : %02d", hour, minute)
}

@Composable
private fun RepeatDaySelector(selectedDaysMask: Int, onToggle: (Int) -> Unit) {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { index, label ->
            val selected = (selectedDaysMask and (1 shl index)) != 0
            Box(
                modifier = Modifier
                    .size(33.dp)
                    .clip(CircleShape)
                    .background(if (selected) DescintWhite else Color.Transparent)
                    .border(1.dp, if (selected) DescintWhite else Color(0xFF555555), CircleShape)
                    .clickable { onToggle(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selected) Color(0xFF1A1A1A) else Color(0xFFB0B0B0),
                    fontFamily = inter,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun AddGoalScrim(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismiss)
    )
}

enum class HomeTab(val title: String, val label: String) {
    TIME_TABLE("Time Table", "Time Table"),
    CALENDAR("Calendar", "Calendar"),
    HOME("Home", "Home"),
    BLOCKER("Blocker", "Blocker"),
    SETTINGS("Settings", "Settings")
}

@Composable
fun HomeBottomNavigation(
    selectedTab: HomeTab,
    onSelectTab: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth().height(70.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(62.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF121212), Color(0xFF080808))
                    )
                )
        )
        Row(
            modifier = Modifier.fillMaxWidth().height(70.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            NavigationTextTab(
                HomeTab.TIME_TABLE,
                selectedTab,
                onSelectTab,
                Modifier.weight(1f).height(62.dp)
            )
            NavigationDivider(Modifier.padding(bottom = 19.dp))
            NavigationTextTab(
                HomeTab.CALENDAR,
                selectedTab,
                onSelectTab,
                Modifier.weight(0.9f).height(62.dp)
            )
            HomeNavigationTab(
                selected = selectedTab == HomeTab.HOME,
                onClick = { onSelectTab(HomeTab.HOME) },
                modifier = Modifier.weight(0.85f).height(70.dp)
            )
            NavigationDivider(Modifier.padding(bottom = 19.dp))
            NavigationTextTab(
                HomeTab.BLOCKER,
                selectedTab,
                onSelectTab,
                Modifier.weight(0.9f).height(62.dp)
            )
            NavigationDivider(Modifier.padding(bottom = 19.dp))
            NavigationTextTab(
                HomeTab.SETTINGS,
                selectedTab,
                onSelectTab,
                Modifier.weight(1f).height(62.dp)
            )
        }
    }
}

@Composable
private fun NavigationTextTab(
    tab: HomeTab,
    selectedTab: HomeTab,
    onSelectTab: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabColor by animateColorAsState(
        targetValue = when {
            selectedTab != tab -> Color(0xFFBDBDBD)
            tab == HomeTab.CALENDAR -> DescintOrange
            else -> DescintWhite
        },
        animationSpec = tween(180),
        label = "navTabColor"
    )
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onSelectTab(tab) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tab.label,
            color = tabColor,
            fontFamily = inter,
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun NavigationDivider(modifier: Modifier = Modifier) {
    Box(modifier.width(1.dp).height(24.dp).background(Color(0xFF444444)))
}

@Composable
private fun HomeNavigationTab(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val homeSurfaceTop by animateColorAsState(
        targetValue = if (selected) Color(0xFF783C16) else Color(0xFF1A1A1A),
        animationSpec = tween(220),
        label = "homeTabSurfaceTop"
    )
    val homeSurfaceBottom by animateColorAsState(
        targetValue = if (selected) Color(0xFF24160F) else Color(0xFF080808),
        animationSpec = tween(220),
        label = "homeTabSurfaceBottom"
    )
    val homeBorderColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF8F4416) else DescintWhite,
        animationSpec = tween(220),
        label = "homeTabBorderColor"
    )
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 3.dp)
            .clip(RoundedCornerShape(topStartPercent = 50, topEndPercent = 50))
            .background(Brush.verticalGradient(listOf(homeSurfaceTop, homeSurfaceBottom)))
            .border(
                width = 1.dp,
                color = homeBorderColor,
                shape = RoundedCornerShape(topStartPercent = 50, topEndPercent = 50)
            )
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.Home, contentDescription = "Home", tint = DescintWhite, modifier = Modifier.size(26.dp))
        Text("Home", color = DescintWhite, fontFamily = inter, fontSize = 8.sp, lineHeight = 9.sp)
    }
}

@Composable
fun PlaceholderTabScreen(tab: HomeTab, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = tab.title,
                color = DescintWhite,
                fontFamily = albertsans,
                fontSize = 24.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Coming soon",
                color = GoalMuted,
                fontFamily = inter,
                fontSize = 14.sp
            )
        }
    }
}
