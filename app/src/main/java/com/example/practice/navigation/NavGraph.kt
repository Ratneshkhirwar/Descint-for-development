package com.example.practice.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.practice.data.UserPreferencesRepository
import com.example.practice.ui.home.HomeScreen
import com.example.practice.ui.onboarding.FeaturesScreen
import com.example.practice.ui.onboarding.NameScreen
import com.example.practice.ui.onboarding.ScreenTimeScreen
import com.example.practice.ui.onboarding.WelcomeScreen
import com.example.practice.ui.splash.SplashScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private object Routes {
    const val WELCOME = "welcome"
    const val NAME = "name"
    const val SCREEN_TIME = "screen_time"
    const val FEATURES = "features"
    const val SPLASH = "splash"
    const val HOME = "home"
}

// Timing constants
private const val WELCOME_TRANSITION_MS = 1100
private const val STEP_TRANSITION_MS = 500
private val standardEasing = FastOutSlowInEasing

// Named parameters fix the type mismatch errors completely
private val fadeAndZoomOut: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(animationSpec = tween(WELCOME_TRANSITION_MS, easing = standardEasing)) +
            scaleOut(
                animationSpec = tween(WELCOME_TRANSITION_MS, easing = standardEasing),
                targetScale = 1.03f
            )
}

private val fadeAndZoomIn: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    fadeIn(animationSpec = tween(WELCOME_TRANSITION_MS, easing = standardEasing)) +
            scaleIn(
                animationSpec = tween(WELCOME_TRANSITION_MS, easing = standardEasing),
                initialScale = 0.97f
            )
}

private val slideInRight: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(
        animationSpec = tween(STEP_TRANSITION_MS, easing = standardEasing),
        initialOffsetX = { fullWidth -> (fullWidth * 0.18f).toInt() }
    ) + fadeIn(animationSpec = tween(STEP_TRANSITION_MS, easing = standardEasing))
}

private val slideOutLeft: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(
        animationSpec = tween(STEP_TRANSITION_MS, easing = standardEasing),
        targetOffsetX = { fullWidth -> (-fullWidth * 0.08f).toInt() }
    ) + fadeOut(animationSpec = tween(STEP_TRANSITION_MS, easing = standardEasing))
}

private val slideInLeft: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(
        animationSpec = tween(STEP_TRANSITION_MS, easing = standardEasing),
        initialOffsetX = { fullWidth -> (-fullWidth * 0.18f).toInt() }
    ) + fadeIn(animationSpec = tween(STEP_TRANSITION_MS, easing = standardEasing))
}

private val slideOutRight: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(
        animationSpec = tween(STEP_TRANSITION_MS, easing = standardEasing),
        targetOffsetX = { fullWidth -> (fullWidth * 0.08f).toInt() }
    ) + fadeOut(animationSpec = tween(STEP_TRANSITION_MS, easing = standardEasing))
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = remember { UserPreferencesRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    var draftName by rememberSaveable { mutableStateOf("") }
    var draftHours by rememberSaveable { mutableStateOf("") }
    var draftMinutes by rememberSaveable { mutableStateOf("") }

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        // WELCOME
        composable(
            route = Routes.WELCOME,
            enterTransition = { fadeIn(animationSpec = tween(WELCOME_TRANSITION_MS, easing = standardEasing)) },
            exitTransition = fadeAndZoomOut
        ) {
            LaunchedEffect(Unit) {
                delay(1400.milliseconds)
                val destination = if (repository.isOnboardingComplete()) Routes.HOME else Routes.NAME
                navController.navigate(destination) {
                    popUpTo(Routes.WELCOME) { inclusive = true }
                }
            }
            WelcomeScreen()
        }

        // NAME
        composable(
            route = Routes.NAME,
            enterTransition = fadeAndZoomIn,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInLeft,
            popExitTransition = slideOutRight
        ) {
            NameScreen(
                name = draftName,
                onNameChange = { draftName = it },
                onNext = { name ->
                    coroutineScope.launch { repository.saveName(name) }
                    navController.navigate(Routes.SCREEN_TIME)
                }
            )
        }

        // SCREEN TIME
        composable(
            route = Routes.SCREEN_TIME,
            enterTransition = slideInRight,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInLeft,
            popExitTransition = slideOutRight
        ) {
            ScreenTimeScreen(
                hours = draftHours,
                onHoursChange = { draftHours = it },
                minutes = draftMinutes,
                onMinutesChange = { draftMinutes = it },
                onNext = { hours, minutes ->
                    coroutineScope.launch {
                        repository.saveScreenTime(
                            hours = hours.toIntOrNull() ?: 0,
                            minutes = minutes.toIntOrNull() ?: 0
                        )
                    }
                    navController.navigate(Routes.FEATURES)
                }
            )
        }

        // FEATURES
        composable(
            route = Routes.FEATURES,
            enterTransition = slideInRight,
            exitTransition = slideOutLeft,
            popEnterTransition = slideInLeft,
            popExitTransition = slideOutRight
        ) {
            FeaturesScreen(
                onNext = {
                    coroutineScope.launch { repository.markOnboardingCompletedIfNeeded() }
                    navController.navigate(Routes.SPLASH) {
                        popUpTo(Routes.NAME) { inclusive = true }
                    }
                }
            )
        }

        // SPLASH
        composable(
            route = Routes.SPLASH,
            enterTransition = { fadeIn(animationSpec = tween(WELCOME_TRANSITION_MS, easing = standardEasing)) },
            exitTransition = fadeAndZoomOut
        ) {
            LaunchedEffect(Unit) {
                delay(1200.milliseconds)
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
            SplashScreen()
        }

        // HOME
        composable(
            route = Routes.HOME,
            enterTransition = fadeAndZoomIn,
            exitTransition = slideOutLeft
        ) {
            HomeScreen()
        }
    }
}