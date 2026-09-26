package com.example.practice.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val DescintColorScheme = darkColorScheme(
    primary = DescintOrange,
    background = DescintBlack,
    surface = DescintBlack,
    onPrimary = DescintWhite,
    onBackground = DescintWhite,
    onSurface = DescintWhite
)

@Composable
fun PracticeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DescintColorScheme,
        typography = Typography,
    ) {
        Surface(
            color = BlackGray,
            contentColor = BlackGray,
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }
}