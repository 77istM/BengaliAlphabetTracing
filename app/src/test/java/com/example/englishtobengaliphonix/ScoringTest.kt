package com.bengalialphabettracing.app

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for scoring-related logic extracted from [TracingViewModel].
 *
 * The core "coverage" scoring algorithm is pure arithmetic — no Android
 * dependencies — so it can be tested on the JVM directly.
 */
class ScoringTest {

    // Helper: run the same coverage algorithm used in TracingViewModel
    private fun computeCoverageScore(
        guidePoints: List<FloatArray>,
        userPoints: List<FloatArray>,
        thresholdSq: Float = 80f * 80f
    ): Float {
        if (userPoints.isEmpty()) return 0f
        if (guidePoints.isEmpty()) return 100f
        val covered = guidePoints.count { gp ->
            userPoints.any { up ->
                val dx = up[0] - gp[0]
                val dy = up[1] - gp[1]
                dx * dx + dy * dy <= thresholdSq
            }
        }
        return (covered.toFloat() / guidePoints.size * 100f).coerceIn(0f, 100f)
    }

    @Test
    fun score_perfectTrace_returns100() {
        // User points exactly match guide points
        val points = listOf(
            floatArrayOf(10f, 20f),
            floatArrayOf(30f, 40f),
            floatArrayOf(50f, 60f)
        )
        val score = computeCoverageScore(points, points)
        assertEquals(100f, score, 0.01f)
    }

    @Test
    fun score_emptyUserPath_returns0() {
        val guide = listOf(floatArrayOf(10f, 20f))
        val score = computeCoverageScore(guide, emptyList())
        assertEquals(0f, score, 0.01f)
    }

    @Test
    fun score_emptyGuidePath_returns100() {
        val user = listOf(floatArrayOf(10f, 20f))
        val score = computeCoverageScore(emptyList(), user)
        assertEquals(100f, score, 0.01f)
    }

    @Test
    fun score_noOverlap_returns0() {
        val guide = listOf(floatArrayOf(0f, 0f))
        // User point is far away (distance = sqrt(10000^2+10000^2) >> threshold)
        val user = listOf(floatArrayOf(10000f, 10000f))
        val score = computeCoverageScore(guide, user)
        assertEquals(0f, score, 0.01f)
    }

    @Test
    fun score_partialCoverage_isProportional() {
        // 2 out of 4 guide points covered → expect ~50 %
        val guide = listOf(
            floatArrayOf(0f, 0f),    // covered by user
            floatArrayOf(1f, 0f),    // covered by user
            floatArrayOf(5000f, 0f), // not covered
            floatArrayOf(6000f, 0f)  // not covered
        )
        val user = listOf(
            floatArrayOf(0f, 0f),
            floatArrayOf(1f, 0f)
        )
        val score = computeCoverageScore(guide, user)
        assertEquals(50f, score, 0.01f)
    }

    @Test
    fun score_pointWithinThreshold_isCovered() {
        val threshold = 80f
        val guide = listOf(floatArrayOf(0f, 0f))
        // User point exactly at threshold distance
        val user = listOf(floatArrayOf(threshold, 0f))
        val score = computeCoverageScore(guide, user, threshold * threshold)
        assertEquals(100f, score, 0.01f)
    }

    @Test
    fun score_pointJustBeyondThreshold_isNotCovered() {
        val threshold = 80f
        val guide = listOf(floatArrayOf(0f, 0f))
        // User point one pixel beyond threshold
        val user = listOf(floatArrayOf(threshold + 1f, 0f))
        val score = computeCoverageScore(guide, user, threshold * threshold)
        assertEquals(0f, score, 0.01f)
    }

    @Test
    fun score_resultClamped_neverExceeds100() {
        // Even with many user points covering few guide points
        val guide = listOf(floatArrayOf(0f, 0f))
        val user = (1..1000).map { floatArrayOf(0f, 0f) }
        val score = computeCoverageScore(guide, user)
        assertTrue(score <= 100f)
    }

    @Test
    fun score_resultClamped_neverBelow0() {
        val guide = listOf(floatArrayOf(0f, 0f))
        val user = listOf(floatArrayOf(999f, 999f))
        val score = computeCoverageScore(guide, user)
        assertTrue(score >= 0f)
    }
}
