package com.example.practice.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** The three goal types shown in the Home screen's Add a goal sheet. */
enum class DailyGoalType(val storageKey: String) {
    BINARY("binary"),
    TIMER("timer"),
    COUNTER("counter");

    companion object {
        fun fromStorageKey(value: String): DailyGoalType =
            entries.firstOrNull { it.storageKey == value } ?: BINARY
    }
}

/** Progress for one goal on one local calendar date. */
data class DailyGoalProgress(
    /** Whether this goal's current run is complete; Restart clears this flag. */
    val completed: Boolean = false,
    /** Sticky daily credit that survives restarting the goal's active progress. */
    val completionCredited: Boolean = false,
    val count: Int = 0,
    val elapsedSeconds: Long = 0L,
    /** Non-null while a timer is running; elapsed time is continued from this instant. */
    val timerStartedAtMillis: Long? = null
)

/**
 * A repeating goal. Repeat-day indexes are Monday=0 through Sunday=6.
 * Progress is keyed by yyyy-MM-dd so each scheduled day starts fresh without
 * overwriting the previous day's completion.
 */
data class DailyGoal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: DailyGoalType,
    val repeatDays: Set<Int> = setOf(0, 1, 2, 3),
    val reminderMinutesOfDay: Int? = null,
    val durationSeconds: Int? = null,
    val targetCount: Int? = null,
    val progressByDate: Map<String, DailyGoalProgress> = emptyMap()
) {
    fun progressFor(dateKey: String): DailyGoalProgress =
        progressByDate[dateKey] ?: DailyGoalProgress()

    fun withProgress(dateKey: String, progress: DailyGoalProgress): DailyGoal = copy(
        progressByDate = if (progress == DailyGoalProgress()) {
            progressByDate - dateKey
        } else {
            progressByDate + (dateKey to progress)
        }
    )
}

/** Small, version-tolerant JSON codec so goal data stays in the existing DataStore. */
internal object DailyGoalJson {
    fun encode(goals: List<DailyGoal>): String = JSONArray().apply {
        goals.forEach { goal ->
            put(JSONObject().apply {
                put("id", goal.id)
                put("name", goal.name)
                put("type", goal.type.storageKey)
                put("repeatDays", JSONArray().apply {
                    goal.repeatDays.sorted().forEach { day -> put(day) }
                })
                putNullable("reminderMinutesOfDay", goal.reminderMinutesOfDay)
                putNullable("durationSeconds", goal.durationSeconds)
                putNullable("targetCount", goal.targetCount)
                put("progressByDate", JSONObject().apply {
                    goal.progressByDate.forEach { (dateKey, progress) ->
                        put(dateKey, JSONObject().apply {
                            put("completed", progress.completed)
                            put("completionCredited", progress.completionCredited)
                            put("count", progress.count)
                            put("elapsedSeconds", progress.elapsedSeconds)
                            putNullable("timerStartedAtMillis", progress.timerStartedAtMillis)
                        })
                    }
                })
            })
        }
    }.toString()

    fun decode(json: String): List<DailyGoal> = runCatching {
        val array = JSONArray(json)
        buildList {
            for (index in 0 until array.length()) {
                val value = array.optJSONObject(index) ?: continue
                val repeatDays = buildSet {
                    val days = value.optJSONArray("repeatDays") ?: JSONArray()
                    for (dayIndex in 0 until days.length()) {
                        val day = days.optInt(dayIndex, -1)
                        if (day in 0..6) add(day)
                    }
                }
                val progressByDate = mutableMapOf<String, DailyGoalProgress>()
                val storedProgress = value.optJSONObject("progressByDate") ?: JSONObject()
                val dateKeys = storedProgress.keys()
                while (dateKeys.hasNext()) {
                    val dateKey = dateKeys.next()
                    val progress = storedProgress.optJSONObject(dateKey) ?: continue
                    val startedValue = progress.opt("timerStartedAtMillis")
                    val startedAt = when (startedValue) {
                        null, JSONObject.NULL -> null
                        is Number -> startedValue.toLong()
                        else -> startedValue.toString().toLongOrNull()
                    }
                    val wasCompleted = progress.optBoolean("completed", false)
                    progressByDate[dateKey] = DailyGoalProgress(
                        completed = wasCompleted,
                        completionCredited = progress.optBoolean("completionCredited", wasCompleted),
                        count = progress.optInt("count", 0).coerceAtLeast(0),
                        elapsedSeconds = progress.optLong("elapsedSeconds", 0L).coerceAtLeast(0L),
                        timerStartedAtMillis = startedAt
                    )
                }
                add(
                    DailyGoal(
                        id = value.optString("id").ifBlank { UUID.randomUUID().toString() },
                        name = value.optString("name"),
                        type = DailyGoalType.fromStorageKey(value.optString("type")),
                        repeatDays = repeatDays,
                        reminderMinutesOfDay = value.optNullableInt("reminderMinutesOfDay"),
                        durationSeconds = value.optNullableInt("durationSeconds"),
                        targetCount = value.optNullableInt("targetCount"),
                        progressByDate = progressByDate
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

    private fun JSONObject.putNullable(key: String, value: Any?) {
        put(key, value ?: JSONObject.NULL)
    }

    private fun JSONObject.optNullableInt(key: String): Int? =
        if (isNull(key)) null else optInt(key)
}
