package com.example.englishtobengaliphonix

import android.app.Application
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
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
    private val progressRepository = ProgressRepository(application)

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

    // ── Accuracy scoring ─────────────────────────────────────────────────────
    /** Most-recently computed score (0–100), or null if not yet scored this session. */
    private val _lastScore = MutableStateFlow<Float?>(null)
    val lastScore: StateFlow<Float?> = _lastScore.asStateFlow()

    /** True once the user has drawn at least one stroke since the last clear. */
    private val _hasUserDrawn = MutableStateFlow(false)
    val hasUserDrawn: StateFlow<Boolean> = _hasUserDrawn.asStateFlow()

    // ── Progress tracking ────────────────────────────────────────────────────
    /**
     * (practicedCount, totalCount) for the letters in the currently selected category.
     * "Practiced" means the letter has been scored at least once.
     */
    private val _categoryProgress = MutableStateFlow(0 to 0)
    val categoryProgress: StateFlow<Pair<Int, Int>> = _categoryProgress.asStateFlow()

    /** All-time best score for the currently displayed letter, or [ProgressRepository.UNPLAYED]. */
    private val _currentBestScore = MutableStateFlow(ProgressRepository.UNPLAYED)
    val currentBestScore: StateFlow<Float> = _currentBestScore.asStateFlow()

    private var animationJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    init {
        refreshProgress()
    }

    val currentLetter get() = letters[_currentLetterIndex.value]

    /** Guide paths to animate/hint; falls back to mainPath when guidePaths is empty. */
    val effectiveGuidePaths get() =
        currentLetter.guidePaths.ifEmpty { listOf(currentLetter.mainPath) }

    // ── Category selection ───────────────────────────────────────────────────
    fun selectCategory(category: LetterCategory) {
        _selectedCategory.value = category
        _currentLetterIndex.value = rangeFor[category]!!.first
        clearPath()
        refreshProgress()
    }

    // ── Audio ────────────────────────────────────────────────────────────────
    fun playSound() {
        currentLetter.audioResId?.let { resId ->
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(getApplication(), resId)
            if (mediaPlayer == null) {
                Log.e(TAG, "MediaPlayer.create() returned null for resId=$resId")
                _isPlaying.value = false
                return
            }
            _isPlaying.value = true
            mediaPlayer?.setOnCompletionListener {
                _isPlaying.value = false
            }
            mediaPlayer?.setOnErrorListener { _, _, _ ->
                _isPlaying.value = false
                true
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
        refreshProgress()
    }

    fun previousLetter() {
        val range = rangeFor[_selectedCategory.value]!!
        val prev = _currentLetterIndex.value - 1
        _currentLetterIndex.value = if (prev < range.first) range.last else prev
        clearPath()
        refreshProgress()
    }

    // ── Drawing ──────────────────────────────────────────────────────────────
    fun clearPath() {
        resetGuidanceState()
        _userPath.value = Path()
        _hasUserDrawn.value = false
        _lastScore.value = null
    }

    fun startPath(offset: Offset) {
        val newPath = Path().apply {
            addPath(_userPath.value)
            moveTo(offset.x, offset.y)
        }
        _userPath.value = newPath
        _hasUserDrawn.value = true
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

    // ── Accuracy scoring ─────────────────────────────────────────────────────
    /**
     * Computes how closely the user's drawn strokes follow the guide paths,
     * stores the result in [lastScore] and persists the all-time best to
     * [ProgressRepository].
     *
     * Algorithm:
     *  1. Sample the user path and all (scaled) guide paths at a fixed pixel interval.
     *  2. For every guide sample point, check whether any user sample point lies
     *     within [SCORE_THRESHOLD_PX] pixels.
     *  3. Score = (covered guide points / total guide points) × 100.
     */
    fun scoreUserPath() {
        val androidUserPath = _userPath.value.asAndroidPath()
        val userPoints = sampleAndroidPath(androidUserPath)

        if (userPoints.isEmpty()) {
            _lastScore.value = 0f
            return
        }

        val guidePoints = effectiveGuidePaths
            .flatMap { svgData -> sampleAndroidPath(scalePath(svgData).asAndroidPath()) }

        if (guidePoints.isEmpty()) {
            _lastScore.value = 100f
            return
        }

        val thresholdSq = SCORE_THRESHOLD_PX * SCORE_THRESHOLD_PX
        val coveredCount = guidePoints.count { gp ->
            userPoints.any { up ->
                val dx = up[0] - gp[0]
                val dy = up[1] - gp[1]
                dx * dx + dy * dy <= thresholdSq
            }
        }

        val score = (coveredCount.toFloat() / guidePoints.size * 100f).coerceIn(0f, 100f)
        _lastScore.value = score
        progressRepository.saveBestScore(currentLetter.name, score)
        refreshProgress()
    }

    // ── Progress tracking ────────────────────────────────────────────────────
    /** Clears all stored best scores and resets the in-memory progress state. */
    fun clearAllProgress() {
        progressRepository.clearAll()
        refreshProgress()
    }

    // ── Private helpers ──────────────────────────────────────────────────────
    /**
     * Updates [_categoryProgress] and [_currentBestScore] from the repository.
     * Called whenever the letter, category, or stored scores may have changed.
     */
    private fun refreshProgress() {
        val range = rangeFor[_selectedCategory.value]!!
        val categoryLetters = letters.slice(range)
        val practiced = categoryLetters.count {
            progressRepository.getBestScore(it.name) >= 0f
        }
        _categoryProgress.value = practiced to categoryLetters.size
        _currentBestScore.value = progressRepository.getBestScore(currentLetter.name)
    }

    /**
     * Samples all contours of [path] at every [intervalPx] pixels.
     * Returns a list of [x, y] float pairs.
     */
    private fun sampleAndroidPath(
        path: android.graphics.Path,
        intervalPx: Float = SAMPLE_INTERVAL_PX
    ): List<FloatArray> {
        val points = mutableListOf<FloatArray>()
        val measure = android.graphics.PathMeasure(path, false)
        do {
            val length = measure.length
            if (length > 0f) {
                val count = maxOf(MIN_SAMPLES_PER_CONTOUR, (length / intervalPx).toInt())
                val pos = FloatArray(2)
                for (i in 0..count) {
                    if (measure.getPosTan(length * i / count, pos, null)) {
                        points.add(pos.copyOf())
                    }
                }
            }
        } while (measure.nextContour())
        return points
    }

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
        private const val TAG = "TracingViewModel"
        /** Number of interpolation steps per stroke during animation. */
        private const val ANIM_STEPS = 100
        /** Milliseconds between each animation step — 100 steps × 9 ms ≈ 900 ms per stroke. */
        private const val ANIM_STEP_DELAY_MS = 9L
        /**
         * Distance between consecutive sample points when walking a path.
         * 8 px gives ≈ 60-150 points per typical letter stroke — enough
         * resolution for accurate scoring while keeping the O(n×m) check fast
         * (at most a few hundred guide × a few thousand user points = < 1 ms).
         */
        private const val SAMPLE_INTERVAL_PX = 8f
        /**
         * A guide sample point is considered "covered" when any user sample point
         * falls within this radius.  At 80 px the threshold corresponds to roughly
         * 27 dp on a common 3× device (350 dp canvas → ~1 050 px, 80/1050 × 350 ≈ 27 dp),
         * which is forgiving enough for natural handwriting variation while still
         * penalising strokes that miss the letter shape entirely.
         */
        private const val SCORE_THRESHOLD_PX = 80f
        /** Minimum sample points extracted per path contour. */
        private const val MIN_SAMPLES_PER_CONTOUR = 5
    }
}