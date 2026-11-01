package com.example.practice.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** One productivity target for a Monday-starting calendar week. */
data class WeeklyTarget(
    val id: String = UUID.randomUUID().toString(),
    val weekStartDateKey: String,
    val title: String,
    val notificationsEnabled: Boolean = false,
    val completed: Boolean = false
)

/** A dated deadline shown on the monthly productivity calendar. */
data class CalendarDeadline(
    val id: String = UUID.randomUUID().toString(),
    val dateKey: String,
    val title: String,
    val cleared: Boolean = false
)

/** User-created calendar content, persisted locally in DataStore. */
data class ProductivityCalendarData(
    val weeklyTargets: List<WeeklyTarget> = emptyList(),
    val deadlines: List<CalendarDeadline> = emptyList(),
    val notesByDate: Map<String, String> = emptyMap()
)

internal object ProductivityCalendarJson {
    fun encode(data: ProductivityCalendarData): String = JSONObject().apply {
        put("weeklyTargets", JSONArray().apply {
            data.weeklyTargets.forEach { target ->
                put(JSONObject().apply {
                    put("id", target.id)
                    put("weekStartDateKey", target.weekStartDateKey)
                    put("title", target.title)
                    put("notificationsEnabled", target.notificationsEnabled)
                    put("completed", target.completed)
                })
            }
        })
        put("deadlines", JSONArray().apply {
            data.deadlines.forEach { deadline ->
                put(JSONObject().apply {
                    put("id", deadline.id)
                    put("dateKey", deadline.dateKey)
                    put("title", deadline.title)
                    put("cleared", deadline.cleared)
                })
            }
        })
        put("notesByDate", JSONObject().apply {
            data.notesByDate.forEach { (dateKey, note) -> put(dateKey, note) }
        })
    }.toString()

    fun decode(json: String): ProductivityCalendarData = runCatching {
        val root = JSONObject(json)
        val targets = buildList {
            val array = root.optJSONArray("weeklyTargets") ?: JSONArray()
            for (index in 0 until array.length()) {
                val value = array.optJSONObject(index) ?: continue
                val title = value.optString("title").trim()
                val weekStart = value.optString("weekStartDateKey")
                if (title.isNotEmpty() && weekStart.isNotEmpty()) {
                    add(
                        WeeklyTarget(
                            id = value.optString("id").ifBlank { UUID.randomUUID().toString() },
                            weekStartDateKey = weekStart,
                            title = title,
                            notificationsEnabled = value.optBoolean("notificationsEnabled", false),
                            completed = value.optBoolean("completed", false)
                        )
                    )
                }
            }
        }
        val deadlines = buildList {
            val array = root.optJSONArray("deadlines") ?: JSONArray()
            for (index in 0 until array.length()) {
                val value = array.optJSONObject(index) ?: continue
                val title = value.optString("title").trim()
                val dateKey = value.optString("dateKey")
                if (title.isNotEmpty() && dateKey.isNotEmpty()) {
                    add(
                        CalendarDeadline(
                            id = value.optString("id").ifBlank { UUID.randomUUID().toString() },
                            dateKey = dateKey,
                            title = title,
                            cleared = value.optBoolean("cleared", false)
                        )
                    )
                }
            }
        }
        val notes = mutableMapOf<String, String>()
        val storedNotes = root.optJSONObject("notesByDate") ?: JSONObject()
        val keys = storedNotes.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val note = storedNotes.optString(key).trimEnd()
            if (note.isNotBlank()) notes[key] = note
        }
        ProductivityCalendarData(
            weeklyTargets = targets,
            deadlines = deadlines,
            notesByDate = notes
        )
    }.getOrDefault(ProductivityCalendarData())
}
