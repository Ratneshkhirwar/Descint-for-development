package com.example.practice.ui.onboarding

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.practice.R
import com.example.practice.ui.theme.PracticeTheme
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practice.ui.theme.Jura
import com.example.practice.ui.theme.albertsans
import com.example.practice.ui.theme.inter

@Composable
fun WelcomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_welcome_screen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp)
                    .navigationBarsPadding()
                ) {
            Text(
                text = "Descint",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 100.dp),
                fontSize = 35.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontFamily = Jura
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "“Every distraction\nsteals a little piece\nof your future”",
                    color = Color.White,
                    fontFamily = albertsans,
                    fontWeight = FontWeight.Light,
                    fontSize = 28.sp,
                    lineHeight = 40.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                text = "Descint helps you take it back.",
                color = Color.White,
                fontFamily = inter,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    PracticeTheme {
        WelcomeScreen()
    }
}
