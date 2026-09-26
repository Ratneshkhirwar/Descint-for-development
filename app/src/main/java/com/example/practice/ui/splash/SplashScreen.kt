package com.example.practice.ui.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practice.ui.theme.DescintBlack
import com.example.practice.ui.theme.DescintWhite
import com.example.practice.ui.theme.PracticeTheme
import com.example.practice.ui.theme.inter
import kotlin.math.PI
import kotlin.math.sin

// A continuous sine wave drives every bar's height, each offset in phase —
// this reads as one fluid ripple passing through the bars rather than
// separate bars each independently pulsing up and down.
private const val BAR_COUNT = 5
private const val WAVE_DURATION_MS = 1400
private const val MIN_SCALE = 0.3f
private const val MAX_SCALE = 1f

private val barWidth = 3.dp
private val barHeight = 26.dp
private val barSpacing = 7.dp

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DescintBlack)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LineWaveLoader()

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "This won't take much",
                color = DescintWhite,
                fontFamily = inter,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun LineWaveLoader() {
    val infiniteTransition = rememberInfiniteTransition(label = "line_wave")

    // One shared phase value, 0 -> 2π, looping linearly. Each bar reads this
    // same value but offset, so the wave visibly travels across the bars.
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = WAVE_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(barSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(BAR_COUNT) { index ->
            val phaseOffset = index * (PI.toFloat() / (BAR_COUNT - 1))
            val wave = (sin(phase + phaseOffset) + 1f) / 2f // normalize to 0..1
            val scale = MIN_SCALE + (MAX_SCALE - MIN_SCALE) * wave

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(barHeight)
                    .graphicsLayer {
                        scaleY = scale
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    }
                    .background(
                        color = DescintWhite,
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    PracticeTheme {
        SplashScreen()
    }
}