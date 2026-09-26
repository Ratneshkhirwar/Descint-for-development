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

private data class NameScreenDimens(
    val horizontalPadding: Dp,
    val headingSize: TextUnit,
    val inputFontSize: TextUnit,
    val buttonFontSize: TextUnit
)

// Converted to explicit if-else blocks for better readability
private fun nameDimensFor(maxWidth: Dp): NameScreenDimens {
    val horizontalPadding: Dp
    val headingSize: TextUnit
    val inputFontSize: TextUnit
    val buttonFontSize: TextUnit

    if (maxWidth < 340.dp) {
        horizontalPadding = 20.dp
        headingSize = 26.sp
        inputFontSize = 17.sp
        buttonFontSize = 16.sp
    } else if (maxWidth < 400.dp) {
        horizontalPadding = 28.dp
        headingSize = 28.sp
        inputFontSize = 18.sp
        buttonFontSize = 17.sp
    } else if (maxWidth < 600.dp) {
        horizontalPadding = 40.dp
        headingSize = 30.sp
        inputFontSize = 20.sp
        buttonFontSize = 18.sp
    } else {
        horizontalPadding = 64.dp
        headingSize = 36.sp
        inputFontSize = 22.sp
        buttonFontSize = 20.sp
    }

    return NameScreenDimens(
        horizontalPadding = horizontalPadding,
        headingSize = headingSize,
        inputFontSize = inputFontSize,
        buttonFontSize = buttonFontSize
    )
}

@Composable
fun NameScreen(
    name: String = "",
    onNameChange: (String) -> Unit = {},
    onNext: (name: String) -> Unit = {}
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

    val textFieldInteractionSource = remember { MutableInteractionSource() }
    val isTextFieldFocused by textFieldInteractionSource.collectIsFocusedAsState()

    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()

    val focusManager = LocalFocusManager.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        val dimens = nameDimensFor(maxWidth)

        val contentMaxWidth = if (maxWidth > 600.dp) {
            480.dp
        } else {
            maxWidth
        }

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
                text = "What's your name?",
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

            // Expanded color selection into a readable if-else block
            val targetBorderColor = if (errorMessage != null) {
                FieldErrorRed
            } else if (isTextFieldFocused) {
                DescintOrange
            } else {
                DescintWhite.copy(alpha = 0.2f)
            }

            val fieldBorderColor by animateColorAsState(
                targetValue = targetBorderColor,
                animationSpec = tween(300),
                label = "name_field_border_color"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = contentMaxWidth)
                    .background(
                        color = DescintWhite.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = fieldBorderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(
                        horizontal = 20.dp,
                        vertical = 16.dp
                    )
            ) {
                if (name.isEmpty()) {
                    Text(
                        text = "Enter your name here",
                        color = DescintWhite.copy(alpha = 0.4f),
                        fontFamily = inter,
                        fontSize = dimens.inputFontSize
                    )
                }
                BasicTextField(
                    value = name,
                    onValueChange = { newValue ->
                        // Broke down the string filter into clear steps
                        val filteredValue = newValue
                            .filter { char -> char.isLetter() || char == ' ' }
                            .replace(Regex(" {2,}"), " ")

                        onNameChange(filteredValue)
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = DescintWhite,
                        fontFamily = inter,
                        fontSize = dimens.inputFontSize
                    ),
                    cursorBrush = SolidColor(DescintOrange),
                    interactionSource = textFieldInteractionSource,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Broke down button click logic into an explicit if-else
            val buttonContainerColor = if (isButtonPressed) {
                DescintOrange.copy(alpha = 0.7f)
            } else {
                DescintOrange
            }

            Button(
                onClick = {
                    focusManager.clearFocus()

                    val isBlank = name.isBlank()
                    val hasDigit = name.any { char -> char.isDigit() }

                    if (isBlank) {
                        errorMessage = "This field is necessary"
                    } else if (hasDigit) {
                        errorMessage = "Invalid username"
                    } else {
                        onNext(name)
                    }
                },
                interactionSource = buttonInteractionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = contentMaxWidth)
                    .padding(top = 25.dp)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor
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
                        .weight(1f)
                        .height(4.dp)
                        .background(DescintOrange)
                )

                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(4.dp)
                        .background(DescintWhite)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891, name = "Standard phone")
@Composable
fun NameScreenPreview() {
    PracticeTheme {
        NameScreen()
    }
}