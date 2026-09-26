package com.example.practice.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// One DataStore instance per app, backed by a file named "user_prefs.preferences_pb"
// under the app's private data directory.
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

data class UserData(
    val name: String = "",
    val screenTimeHours: Int = 0,
    val screenTimeMinutes: Int = 0,
    val blockedApps: Set<String> = emptySet(), // app package names, e.g. "com.instagram.android"
    // Epoch millis of the moment the user finished onboarding (Features screen
    // "Continue"). 0L means onboarding hasn't been completed yet. Used to tell
    // "today is the user's first day" apart from "there's a real yesterday to
    // compare against" for the improvement calculation, and as the start date
    // for the streak calendar.
    val onboardingCompletedAtMillis: Long = 0L,
    // Dates (as "yyyy-MM-dd") on which the user completed at least one task.
    // Presence in the set = completed that day; absence = not completed.
    val completedDates: Set<String> = emptySet()
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val NAME = stringPreferencesKey("user_name")
        val SCREEN_TIME_HOURS = intPreferencesKey("screen_time_hours")
        val SCREEN_TIME_MINUTES = intPreferencesKey("screen_time_minutes")
        val BLOCKED_APPS = stringSetPreferencesKey("blocked_apps")
        val QUOTE_INDEX = intPreferencesKey("quote_index")
        val QUOTE_LAST_ROTATED_AT = longPreferencesKey("quote_last_rotated_at")
        val ONBOARDING_COMPLETED_AT = longPreferencesKey("onboarding_completed_at")
        val COMPLETED_DATES = stringSetPreferencesKey("completed_task_dates")
    }

    val userDataFlow: Flow<UserData> = context.dataStore.data.map { prefs ->
        UserData(
            name = prefs[Keys.NAME] ?: "",
            screenTimeHours = prefs[Keys.SCREEN_TIME_HOURS] ?: 0,
            screenTimeMinutes = prefs[Keys.SCREEN_TIME_MINUTES] ?: 0,
            blockedApps = prefs[Keys.BLOCKED_APPS] ?: emptySet(),
            onboardingCompletedAtMillis = prefs[Keys.ONBOARDING_COMPLETED_AT] ?: 0L,
            completedDates = prefs[Keys.COMPLETED_DATES] ?: emptySet()
        )
    }

    suspend fun saveName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NAME] = name
        }
    }

    suspend fun saveScreenTime(hours: Int, minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SCREEN_TIME_HOURS] = hours
            prefs[Keys.SCREEN_TIME_MINUTES] = minutes
        }
    }

    suspend fun saveBlockedApps(packageNames: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BLOCKED_APPS] = packageNames
        }
    }

    suspend fun addBlockedApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.BLOCKED_APPS] ?: emptySet()
            prefs[Keys.BLOCKED_APPS] = current + packageName
        }
    }

    suspend fun removeBlockedApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.BLOCKED_APPS] ?: emptySet()
            prefs[Keys.BLOCKED_APPS] = current - packageName
        }
    }

    // Marks (or unmarks) a specific date as "completed at least one task".
    // dateKey format: "yyyy-MM-dd". This is the hook a real task-completion
    // system calls later — for now it's also used by the calendar's own
    // tap-to-toggle-today behavior so the states are visually testable
    // before that system exists.
    suspend fun setDateCompleted(dateKey: String, completed: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.COMPLETED_DATES] ?: emptySet()
            prefs[Keys.COMPLETED_DATES] = if (completed) current + dateKey else current - dateKey
        }
    }

    // Called once, right when onboarding finishes (Features screen's Continue).
    // Only ever sets the value if it isn't already set, so re-navigating
    // through onboarding somehow later can't overwrite the original date.
    suspend fun markOnboardingCompletedIfNeeded() {
        context.dataStore.edit { prefs ->
            if (prefs[Keys.ONBOARDING_COMPLETED_AT] == null) {
                prefs[Keys.ONBOARDING_COMPLETED_AT] = System.currentTimeMillis()
            }
        }
    }

    // One-shot read for the splash screen's startup check — was onboarding
    // ever completed? (We treat "has a name" as the signal.)
    suspend fun isOnboardingComplete(): Boolean {
        return userDataFlow.first().name.isNotBlank()
    }

    // Picks a quote from the given list. The SAME quote is returned for every
    // call within a 24-hour window (so it doesn't feel random/flickery within
    // a day) — once 24 hours have passed since it was last rotated, the next
    // call advances to the next quote in the list and persists that as the
    // new starting point. Wraps back to the start at the end of the list.
    suspend fun getRotatingQuote(quotes: List<String>): String {
        if (quotes.isEmpty()) return ""

        val prefs = context.dataStore.data.first()
        val storedIndex = prefs[Keys.QUOTE_INDEX] ?: 0
        val lastRotatedAt = prefs[Keys.QUOTE_LAST_ROTATED_AT] ?: 0L
        val now = System.currentTimeMillis()

        val index = if (lastRotatedAt == 0L || now - lastRotatedAt >= ONE_DAY_MS) {
            val nextIndex = (storedIndex + 1) % quotes.size
            context.dataStore.edit { prefs ->
                prefs[Keys.QUOTE_INDEX] = nextIndex
                prefs[Keys.QUOTE_LAST_ROTATED_AT] = now
            }
            nextIndex
        } else {
            storedIndex % quotes.size // guards against the list shrinking between app versions
        }

        return quotes[index]
    }

    companion object {
        private const val ONE_DAY_MS = 24 * 60 * 60 * 1000L
    }
}