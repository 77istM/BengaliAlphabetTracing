package com.example.englishtobengaliphonix

import android.content.Context

/**
 * Persists per-letter best scores in SharedPreferences.
 * A score of [UNPLAYED] (-1) means the letter has never been scored.
 */
class ProgressRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Returns the stored best score, or [UNPLAYED] if the letter has never been scored. */
    fun getBestScore(letterName: String): Float =
        prefs.getFloat(keyFor(letterName), UNPLAYED)

    /**
     * Saves [score] only when it is higher than the existing best
     * (or when the letter has never been scored before).
     */
    fun saveBestScore(letterName: String, score: Float) {
        val current = getBestScore(letterName)
        if (score > current) {
            prefs.edit().putFloat(keyFor(letterName), score).apply()
        }
    }

    /** Returns a map of letter name → best score for the supplied list of letter names. */
    fun getScoresFor(letterNames: List<String>): Map<String, Float> =
        letterNames.associateWith { getBestScore(it) }

    /** Removes all stored progress. */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "tracing_progress"
        private const val KEY_PREFIX  = "score_"
        const val UNPLAYED = -1f
        private fun keyFor(name: String) = "$KEY_PREFIX$name"
    }
}
