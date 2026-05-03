package com.bengalialphabettracing.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tracing_progress")

/**
 * Persists per-letter best scores using Jetpack DataStore.
 * All write operations are suspend functions and are safe to call from a coroutine
 * (e.g. [androidx.lifecycle.ViewModel.viewModelScope]).
 *
 * A score of [UNPLAYED] (-1) means the letter has never been scored.
 */
class ProgressRepository(private val context: Context) {

    // Pre-built key cache to avoid recreating Preferences.Key objects on every access
    private val keyCache = mutableMapOf<String, Preferences.Key<Float>>()
    private fun keyFor(letterName: String): Preferences.Key<Float> =
        keyCache.getOrPut(letterName) { floatPreferencesKey("score_$letterName") }

    /** Returns the stored best score as a [Flow], emitting [UNPLAYED] if never scored. */
    fun getBestScoreFlow(letterName: String): Flow<Float> =
        context.dataStore.data.map { prefs ->
            prefs[keyFor(letterName)] ?: UNPLAYED
        }

    /**
     * Returns the current best score synchronously by collecting the first emission.
     * Must be called from a coroutine / suspend context.
     */
    suspend fun getBestScore(letterName: String): Float =
        getBestScoreFlow(letterName).first()

    /**
     * Saves [score] only when it is higher than the existing best
     * (or when the letter has never been scored before).
     */
    suspend fun saveBestScore(letterName: String, score: Float) {
        val key = keyFor(letterName)
        context.dataStore.edit { prefs ->
            val current = prefs[key] ?: UNPLAYED
            if (score > current) {
                prefs[key] = score
            }
        }
    }

    /** Removes all stored progress. */
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        const val UNPLAYED = -1f
    }
}
