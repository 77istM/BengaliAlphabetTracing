package com.bengalialphabettracing.app

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [splitIntoStrokes].
 *
 * Note: [scalePath], [extractPartialPath], and [getPathEndPosition] rely on
 * android.graphics classes that are not available in plain JVM unit tests.
 * Those functions are covered by instrumented tests on-device.
 */
class PathUtilsTest {

    // ── splitIntoStrokes ─────────────────────────────────────────────────────

    @Test
    fun splitIntoStrokes_singleStroke_returnsOriginal() {
        val path = "M10,20 L30,40 C50,60 70,80 90,100"
        val result = splitIntoStrokes(path)
        assertEquals(1, result.size)
        assertEquals(path, result[0])
    }

    @Test
    fun splitIntoStrokes_twoAbsoluteMoves_returnsTwoSegments() {
        val path = "M10,20L30,40M50,60L70,80"
        val result = splitIntoStrokes(path)
        assertEquals(2, result.size)
        assertTrue(result[0].startsWith("M10,20"))
        assertTrue(result[1].startsWith("M50,60"))
    }

    @Test
    fun splitIntoStrokes_threeAbsoluteMoves_returnsThreeSegments() {
        val path = "M1,2L3,4M5,6L7,8M9,10L11,12"
        val result = splitIntoStrokes(path)
        assertEquals(3, result.size)
    }

    @Test
    fun splitIntoStrokes_relativeMoveOnly_returnsOriginal() {
        // Lowercase 'm' is relative; the regex only splits on uppercase 'M'
        // followed by a digit. So relative moves don't create split points.
        val path = "m10,20 l30,40 m50,60 l70,80"
        val result = splitIntoStrokes(path)
        assertEquals(1, result.size)
    }

    @Test
    fun splitIntoStrokes_blankString_returnsListWithBlank() {
        val result = splitIntoStrokes("")
        assertEquals(1, result.size)
    }

    @Test
    fun splitIntoStrokes_eachSegmentStartsWithM() {
        val path = "M0,0L10,10M20,20L30,30M40,40L50,50"
        val result = splitIntoStrokes(path)
        result.forEach { segment ->
            assertTrue(
                "Segment should start with M but was: $segment",
                segment.trimStart().startsWith("M")
            )
        }
    }

    @Test
    fun splitIntoStrokes_preservesDataContent() {
        val stroke1 = "M100,200L300,400"
        val stroke2 = "M500,600C700,800 900,1000 1100,1200"
        val path = stroke1 + stroke2
        val result = splitIntoStrokes(path)
        assertEquals(2, result.size)
        // Each segment should contain its original data
        assertTrue(result[0].contains("L300,400"))
        assertTrue(result[1].contains("C700,800"))
    }

    @Test
    fun splitIntoStrokes_mWithNegativeCoord_isRecognised() {
        val path = "M-10,-20L30,40M-50,-60L70,80"
        val result = splitIntoStrokes(path)
        assertEquals(2, result.size)
    }
}
