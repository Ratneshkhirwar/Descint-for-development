package com.example.practice.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.practice.R
import com.example.practice.ui.theme.DescintOrange
import com.example.practice.ui.theme.DescintWhite
import com.example.practice.ui.theme.PracticeTheme
import com.example.practice.ui.theme.albertsans
import com.example.practice.ui.theme.inter

private data class Feature(
    val number: Int,
    val description: String
)

private val features = listOf(
    Feature(1, "Create daily task and make sure you complete it daily"),
    Feature(2, "Design your time table"),
    Feature(3, "Set deadlines, priorities and many more"),
    Feature(4, "Limit your screen time")
)

// Breakpoints for adapting padding/type to phone width. Everything below is
// derived from the available width at runtime via BoxWithConstraints, so this
// scales smoothly across small phones, large phones, and tablets/foldables.
private data class FeaturesScreenDimens(
    val horizontalPadding: Dp,
    val headingSize: TextUnit,
    val bodySize: TextUnit,
    val badgeSize: Dp
)

private fun featuresDimensFor(maxWidth: Dp): FeaturesScreenDimens = when {
    maxWidth < 340.dp -> FeaturesScreenDimens(
        horizontalPadding = 20.dp,
        headingSize = 28.sp,
        bodySize = 13.sp,
        badgeSize = 24.dp
    )
    maxWidth < 400.dp -> FeaturesScreenDimens(
        horizontalPadding = 28.dp,
        headingSize = 31.sp,
        bodySize = 13.5.sp,
        badgeSize = 26.dp
    )
    maxWidth < 600.dp -> FeaturesScreenDimens(
        horizontalPadding = 40.dp,
        headingSize = 34.sp,
        bodySize = 14.sp,
        badgeSize = 28.dp
    )
    else -> FeaturesScreenDimens( // tablets / large-screen devices
        horizontalPadding = 64.dp,
        headingSize = 40.sp,
        bodySize = 16.sp,
        badgeSize = 32.dp
    )
}

@Composable
fun FeaturesScreen(
    onNext: () -> Unit = {}
) {

    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        val dimens = featuresDimensFor(maxWidth)
        // Cap content width on very wide screens (tablets) so cards/text
        // don't stretch edge-to-edge and become hard to read.
        val contentMaxWidth = if (maxWidth > 600.dp) 480.dp else maxWidth

        Image(
            painter = painterResource(id = R.drawable.background_features_screen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.horizontalPadding)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Text(
                text = "Features",
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

            Column(
                modifier = Modifier.widthIn(max = contentMaxWidth)
            ) {
                features.forEachIndexed { index, feature ->
                    FeatureRow(feature = feature, dimens = dimens)
                    if (index != features.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = contentMaxWidth)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(DescintWhite.copy(alpha = 0.15f))
                )
                Text(
                    text = "and many more",
                    color = DescintWhite.copy(alpha = 0.4f),
                    fontFamily = inter,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(DescintWhite.copy(alpha = 0.15f))
                )
            }

            Button(
                onClick = {
                    onNext()
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
                    text = "Continue  →",
                    fontFamily = inter,
                    fontSize = 18.sp,
                    color = DescintWhite
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Pinned independently of the scrollable content above, so it always
        // sits at the true bottom of the screen regardless of content length.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = contentMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = dimens.horizontalPadding)
                .padding(bottom = 30.dp)
                .height(4.dp)
                .background(DescintOrange)
        )
    }
}

@Composable
private fun FeatureRow(feature: Feature, dimens: FeaturesScreenDimens) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = DescintWhite.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(dimens.badgeSize)
                .background(color = DescintOrange, shape = CircleShape)
        ) {
            Text(
                text = feature.number.toString(),
                color = DescintWhite,
                fontFamily = inter,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = feature.description,
            color = DescintWhite,
            fontFamily = inter,
            fontSize = dimens.bodySize,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891, name = "Standard phone")
@Composable
fun FeaturesScreenPreview() {
    PracticeTheme {
        FeaturesScreen()
    }
}