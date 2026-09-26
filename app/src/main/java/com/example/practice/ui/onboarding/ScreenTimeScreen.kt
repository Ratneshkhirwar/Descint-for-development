package com.example.practice.ui.onboarding

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.practice.R
import com.example.practice.ui.theme.DescintOrange
import com.example.practice.ui.theme.DescintWhite
import com.example.practice.ui.theme.PracticeTheme
import com.example.practice.ui.theme.inter
import androidx.compose.ui.tooling.preview.Preview
import com.example.practice.ui.theme.albertsans
import kotlinx.coroutines.delay

private val FieldErrorRed = Color(0xFFFF5252)

// Breakpoints for adapting padding/type to phone width — same pattern used
// on FeaturesScreen and NameScreen, so all onboarding screens scale consistently.
private data class ScreenTimeScreenDimens(
    val horizontalPadding: Dp,
    val headingSize: TextUnit,
    val buttonFontSize: TextUnit,
    val timeBoxHeight: Dp,
    val timeBoxFontSize: TextUnit,
    val colonFontSize: TextUnit
)

private fun screenTimeDimensFor(maxWidth: Dp): ScreenTimeScreenDimens = when {
    maxWidth < 340.dp -> ScreenTimeScreenDimens(
        horizontalPadding = 20.dp,
        headingSize = 26.sp,
        buttonFontSize = 16.sp,
        timeBoxHeight = 68.dp,
        timeBoxFontSize = 26.sp,
        colonFontSize = 24.sp
    )
    maxWidth < 400.dp -> ScreenTimeScreenDimens(
        horizontalPadding = 28.dp,
        headingSize = 28.sp,
        buttonFontSize = 17.sp,
        timeBoxHeight = 74.dp,
        timeBoxFontSize = 29.sp,
        colonFontSize = 26.sp
    )
    maxWidth < 600.dp -> ScreenTimeScreenDimens(
        horizontalPadding = 40.dp,
        headingSize = 30.sp,
        buttonFontSize = 18.sp,
        timeBoxHeight = 80.dp,
        timeBoxFontSize = 32.sp,
        colonFontSize = 28.sp
    )
    else -> ScreenTimeScreenDimens( // tablets / large-screen devices
        horizontalPadding = 64.dp,
        headingSize = 36.sp,
        buttonFontSize = 20.sp,
        timeBoxHeight = 92.dp,
        timeBoxFontSize = 38.sp,
        colonFontSize = 32.sp
    )
}

@Composable
fun ScreenTimeScreen(
    hours: String = "",
    onHoursChange: (String) -> Unit = {},
    minutes: String = "",
    onMinutesChange: (String) -> Unit = {},
    onNext: (hours: String, minutes: String) -> Unit = { _, _ -> }
) {

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(2200)
            errorMessage = null
        }
    }

    val hoursInteractionSource = remember { MutableInteractionSource() }
    val isHoursFocused by hoursInteractionSource.collectIsFocusedAsState()

    val minutesInteractionSource = remember { MutableInteractionSource() }
    val isMinutesFocused by minutesInteractionSource.collectIsFocusedAsState()

    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()

    val focusManager = LocalFocusManager.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
            .navigationBarsPadding()
    ) {
        val dimens = screenTimeDimensFor(maxWidth)
        val contentMaxWidth = if (maxWidth > 600.dp) 480.dp else maxWidth

        Image(
            painter = painterResource(id = R.drawable.background_name_screen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimens.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Text(
                text = "What's your average screen time",
                color = DescintWhite,
                fontFamily = albertsans,
                fontSize = dimens.headingSize,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = contentMaxWidth)
                    .padding(top = 60.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Fixed-height slot — always occupies this space so the boxes
            // and button below never shift when the message appears/disappears.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = contentMaxWidth)
                    .height(26.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                this@Column.AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(animationSpec = tween(250)),
                    exit = fadeOut(animationSpec = tween(150))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = DescintWhite,
                            fontFamily = inter,
                            fontSize = 13.sp
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    color = DescintWhite.copy(alpha = 0.18f),
                                    shape = CircleShape
                                )
                        ) {
                            Text(
                                text = "i",
                                color = DescintWhite,
                                fontFamily = inter,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 11.sp,
                                style = androidx.compose.ui.text.TextStyle(
                                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = contentMaxWidth)
            ) {
                TimeUnitBox(
                    value = hours,
                    onValueChange = { input ->
                        onHoursChange(
                            input.filter { it.isDigit() }.take(2).let {
                                if ((it.toIntOrNull() ?: 0) > 12) "12" else it
                            }
                        )
                    },
                    isFocused = isHoursFocused,
                    isError = errorMessage != null && hours.isBlank(),
                    interactionSource = hoursInteractionSource,
                    boxHeight = dimens.timeBoxHeight,
                    fontSize = dimens.timeBoxFontSize,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = ":",
                    color = DescintWhite.copy(alpha = 0.6f),
                    fontFamily = inter,
                    fontSize = dimens.colonFontSize,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                TimeUnitBox(
                    value = minutes,
                    onValueChange = { input ->
                        onMinutesChange(
                            input
                                .filter { it.isDigit() }
                                .take(2)
                                .let {
                                    if ((it.toIntOrNull() ?: 0) > 59) "59" else it
                                }
                        )
                    },
                    isFocused = isMinutesFocused,
                    isError = errorMessage != null && minutes.isBlank(),
                    interactionSource = minutesInteractionSource,
                    boxHeight = dimens.timeBoxHeight,
                    fontSize = dimens.timeBoxFontSize,
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (hours.isBlank() || minutes.isBlank()) {
                        errorMessage = "This field is necessary"
                    } else {
                        onNext(hours, minutes)
                    }
                },
                interactionSource = buttonInteractionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = contentMaxWidth)
                    .padding(top = 30.dp)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isButtonPressed) {
                        DescintOrange.copy(alpha = 0.7f)
                    } else {
                        DescintOrange
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Proceed",
                    fontFamily = inter,
                    fontSize = dimens.buttonFontSize,
                    color = DescintWhite
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = contentMaxWidth)
                    .padding(bottom = 30.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(4.dp)
                        .background(DescintOrange)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(DescintWhite)
                )
            }
        }
    }
}

@Composable
private fun TimeUnitBox(
    value: String,
    onValueChange: (String) -> Unit,
    isFocused: Boolean,
    isError: Boolean,
    interactionSource: MutableInteractionSource,
    boxHeight: androidx.compose.ui.unit.Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> FieldErrorRed
            isFocused -> DescintOrange
            else -> DescintWhite.copy(alpha = 0.2f)
        },
        animationSpec = tween(300),
        label = "time_box_border_color"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(boxHeight)
            .background(
                color = DescintWhite.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        if (value.isEmpty()) {
            Text(
                text = "00",
                color = DescintWhite.copy(alpha = 0.4f),
                fontFamily = inter,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = DescintWhite,
                fontFamily = inter,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(DescintOrange),
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 568, name = "Small phone")
@Composable
fun ScreenTimeScreenPreviewSmall() {
    PracticeTheme {
        ScreenTimeScreen()
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891, name = "Standard phone")
@Composable
fun ScreenTimeScreenPreview() {
    PracticeTheme {
        ScreenTimeScreen()
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1280, name = "Tablet")
@Composable
fun ScreenTimeScreenPreviewTablet() {
    PracticeTheme {
        ScreenTimeScreen()
    }
}
