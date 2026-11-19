package com.rustduo.app

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.progressData by preferencesDataStore("rust_duo_progress")
class ProgressStore(context: Context) {
    private val store = context.progressData
    private val completed = stringSetPreferencesKey("completed")
    private val xp = intPreferencesKey("xp")
    private val hearts = intPreferencesKey("hearts")
    private val streak = intPreferencesKey("streak")
    private val last = stringPreferencesKey("last")
    private val refill = stringPreferencesKey("refill")
    val progress = store.data.map { p ->
        Progress(p[completed].orEmpty().mapNotNull { it.toIntOrNull() }.toSet(), p[xp] ?: 0,
            p[hearts] ?: 5, p[streak] ?: 0, p[last]?.let(LocalDate::parse), p[refill]?.let(LocalDate::parse))
    }
    suspend fun save(value: Progress) {
        store.edit { p ->
            p[completed] = value.completed.map { it.toString() }.toSet()
            p[xp] = value.xp; p[hearts] = value.hearts; p[streak] = value.streak
            value.lastCompleted?.let { p[last] = it.toString() }
            value.refillDate?.let { p[refill] = it.toString() }
        }
    }
}
