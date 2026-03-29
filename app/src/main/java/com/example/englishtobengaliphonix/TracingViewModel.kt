package com.example.englishtobengaliphonix

import android.app.Application
import android.media.MediaPlayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TracingViewModel(application: Application) : AndroidViewModel(application) {

    private val letters = AlphabetPaths.letters

    // ── Category ranges (match order in AlphabetPaths.letters) ──────────────
    // Vowels:     অ → ঔ  (indices 0–10,  11 letters)
    // Consonants: ক → ঁ  (indices 11–49, 39 letters)
    // Numbers:    ০ → ১০ (indices 50–60, 11 items)
    private val rangeFor = mapOf(
        LetterCategory.ALL         to (0..letters.lastIndex),
        LetterCategory.VOWELS      to (0..10),
        LetterCategory.CONSONANTS  to (11..49),
        LetterCategory.NUMBERS     to (50..letters.lastIndex)
    )

    // ── State ────────────────────────────────────────────────────────────────
    private val _selectedCategory = MutableStateFlow(LetterCategory.ALL)
    val selectedCategory: StateFlow<LetterCategory> = _selectedCategory.asStateFlow()

    private val _currentLetterIndex = MutableStateFlow(0)
    val currentLetterIndex: StateFlow<Int> = _currentLetterIndex.asStateFlow()

    private val _userPath = MutableStateFlow(Path())
    val userPath: StateFlow<Path> = _userPath.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // ── Animated guidance ────────────────────────────────────────────────────
    private val _isAnimating = MutableStateFlow(false)
    val isAnimating: StateFlow<Boolean> = _isAnimating.asStateFlow()

    private val _animStrokeIndex = MutableStateFlow(0)
    val animStrokeIndex: StateFlow<Int> = _animStrokeIndex.asStateFlow()

    private val _animProgress = MutableStateFlow(0f)
    val animProgress: StateFlow<Float> = _animProgress.asStateFlow()

    // ── Stroke-by-stroke hints ───────────────────────────────────────────────
    private val _visibleHintCount = MutableStateFlow(0)
    val visibleHintCount: StateFlow<Int> = _visibleHintCount.asStateFlow()

    private var animationJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    val currentLetter get() = letters[_currentLetterIndex.value]

    /** Guide paths to animate/hint; falls back to mainPath when guidePaths is empty. */
    val effectiveGuidePaths get() =
        currentLetter.guidePaths.ifEmpty { listOf(currentLetter.mainPath) }

    // ── Category selection ───────────────────────────────────────────────────
    fun selectCategory(category: LetterCategory) {
        _selectedCategory.value = category
        _currentLetterIndex.value = rangeFor[category]!!.first
        clearPath()
    }

    // ── Audio ────────────────────────────────────────────────────────────────
    fun playSound() {
        currentLetter.audioResId?.let { resId ->
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(getApplication(), resId)
            _isPlaying.value = true
            mediaPlayer?.setOnCompletionListener {
                _isPlaying.value = false
            }
            mediaPlayer?.start()
        }
    }

    // ── Navigation (bounded to active category) ──────────────────────────────
    fun nextLetter() {
        val range = rangeFor[_selectedCategory.value]!!
        val next = _currentLetterIndex.value + 1
        _currentLetterIndex.value = if (next > range.last) range.first else next
        clearPath()
    }

    fun previousLetter() {
        val range = rangeFor[_selectedCategory.value]!!
        val prev = _currentLetterIndex.value - 1
        _currentLetterIndex.value = if (prev < range.first) range.last else prev
        clearPath()
    }

    // ── Drawing ──────────────────────────────────────────────────────────────
    fun clearPath() {
        resetGuidanceState()
        _userPath.value = Path()
    }

    fun startPath(offset: Offset) {
        val newPath = Path().apply {
            addPath(_userPath.value)
            moveTo(offset.x, offset.y)
        }
        _userPath.value = newPath
    }

    fun dragTo(position: Offset) {
        val newPath = Path().apply {
            addPath(_userPath.value)
            lineTo(position.x, position.y)
        }
        _userPath.value = newPath
    }

    // ── Animated guidance ────────────────────────────────────────────────────
    fun startAnimation() {
        animationJob?.cancel()
        animationJob = viewModelScope.launch {
            _isAnimating.value = true
            _visibleHintCount.value = 0
            val strokes = effectiveGuidePaths
            for (i in strokes.indices) {
                _animStrokeIndex.value = i
                // Animate each stroke over ANIM_STEPS × ANIM_STEP_DELAY_MS ≈ 900 ms
                for (step in 0..ANIM_STEPS) {
                    _animProgress.value = step / ANIM_STEPS.toFloat()
                    delay(ANIM_STEP_DELAY_MS)
                }
                _visibleHintCount.value = i + 1
            }
            _isAnimating.value = false
            _animStrokeIndex.value = 0
            _animProgress.value = 0f
        }
    }

    fun stopAnimation() {
        animationJob?.cancel()
        _isAnimating.value = false
    }

    // ── Stroke-by-stroke hints ───────────────────────────────────────────────
    fun showNextHint() {
        val max = effectiveGuidePaths.size
        if (_visibleHintCount.value < max) {
            _visibleHintCount.value++
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────
    private fun resetGuidanceState() {
        stopAnimation()
        _visibleHintCount.value = 0
        _animStrokeIndex.value = 0
        _animProgress.value = 0f
    }

    override fun onCleared() {
        super.onCleared()
        animationJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        /** Number of interpolation steps per stroke during animation. */
        private const val ANIM_STEPS = 100
        /** Milliseconds between each animation step — 100 steps × 9 ms ≈ 900 ms per stroke. */
        private const val ANIM_STEP_DELAY_MS = 9L
    }
}