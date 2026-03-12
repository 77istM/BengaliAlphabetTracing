package com.example.englishtobengaliphonix


import android.app.Application
import android.media.MediaPlayer
import androidx.compose.ui.graphics.Path
import androidx.lifecycle.AndroidViewModel
import com.example.englishtobengaliphonix.AlphabetPaths
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TracingViewModel(application: Application) : AndroidViewModel(application) {

    private val letters = AlphabetPaths.letters

    private val _currentLetterIndex = MutableStateFlow(0)
    val currentLetterIndex: StateFlow<Int> = _currentLetterIndex.asStateFlow()

    private val _userPath = MutableStateFlow(Path())
    val userPath: StateFlow<Path> = _userPath.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    val currentLetter get() = letters[_currentLetterIndex.value]

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

    fun nextLetter() {
        _currentLetterIndex.value = (_currentLetterIndex.value + 1) % letters.size
        clearPath()
    }

    fun previousLetter() {
        _currentLetterIndex.value = (_currentLetterIndex.value - 1 + letters.size) % letters.size
        clearPath()
    }

    fun clearPath() {
        _userPath.value = Path()
    }

    fun updatePath(newPath: Path) {
        _userPath.value = newPath
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}