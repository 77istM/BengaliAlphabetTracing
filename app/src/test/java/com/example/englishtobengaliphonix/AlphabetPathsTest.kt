package com.bengalialphabettracing.app

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [AlphabetPaths] data integrity.
 */
class AlphabetPathsTest {

    @Test
    fun letters_count_is61() {
        assertEquals(61, AlphabetPaths.letters.size)
    }

    @Test
    fun every_letter_has_non_blank_name() {
        AlphabetPaths.letters.forEachIndexed { i, letter ->
            assertFalse("Letter at index $i has blank name", letter.name.isBlank())
        }
    }

    @Test
    fun every_letter_has_non_blank_mainPath() {
        AlphabetPaths.letters.forEachIndexed { i, letter ->
            assertFalse("Letter '${letter.name}' at index $i has blank mainPath",
                letter.mainPath.isBlank())
        }
    }

    @Test
    fun every_letter_has_non_blank_romanized() {
        AlphabetPaths.letters.forEachIndexed { i, letter ->
            assertFalse("Letter '${letter.name}' at index $i has blank romanized",
                letter.romanized.isBlank())
        }
    }

    @Test
    fun vowels_range_has_11_letters() {
        val vowels = AlphabetPaths.letters.subList(0, 11)
        assertEquals(11, vowels.size)
    }

    @Test
    fun consonants_range_has_39_letters() {
        val consonants = AlphabetPaths.letters.subList(11, 50)
        assertEquals(39, consonants.size)
    }

    @Test
    fun numbers_range_has_11_items() {
        val numbers = AlphabetPaths.letters.subList(50, 61)
        assertEquals(11, numbers.size)
    }

    @Test
    fun first_vowel_is_o() {
        val firstVowel = AlphabetPaths.letters[0]
        assertEquals("অ", firstVowel.name)
        assertEquals("o", firstVowel.romanized)
    }

    @Test
    fun first_consonant_is_ko() {
        val firstConsonant = AlphabetPaths.letters[11]
        assertEquals("ক", firstConsonant.name)
        assertEquals("ko", firstConsonant.romanized)
    }

    @Test
    fun first_number_is_zero() {
        val firstNumber = AlphabetPaths.letters[50]
        assertEquals("০", firstNumber.name)
        assertEquals("0", firstNumber.romanized)
    }

    @Test
    fun all_mainPaths_contain_at_least_one_move_command() {
        AlphabetPaths.letters.forEachIndexed { i, letter ->
            val hasMove = letter.mainPath.contains('M') || letter.mainPath.contains('m')
            assertTrue("Letter '${letter.name}' at index $i mainPath has no move command",
                hasMove)
        }
    }

    @Test
    fun splitIntoStrokes_on_each_mainPath_returns_non_empty() {
        AlphabetPaths.letters.forEachIndexed { i, letter ->
            val strokes = splitIntoStrokes(letter.mainPath)
            assertTrue("Letter '${letter.name}' at index $i produced 0 strokes",
                strokes.isNotEmpty())
        }
    }
}
