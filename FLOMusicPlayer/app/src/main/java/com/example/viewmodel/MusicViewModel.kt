package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flomusicplayer.ExoPlayer
import com.example.flomusicplayer.extension.toTimeList
import com.example.flomusicplayer.model.MusicModel
import com.example.flomusicplayer.repository.Repository
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@HiltViewModel
class MusicViewModel @Inject constructor(
    private val repository: Repository,
    private val player: ExoPlayer
    ) : ViewModel() {

    private val _music = MutableStateFlow<MusicModel?>(null)
    val music: StateFlow<MusicModel?>
        get() = _music

    private val _sliderPosition = MutableStateFlow(0L)
    val sliderPosition: StateFlow<Long> = _sliderPosition

    private val _currentLyricsIndex = MutableStateFlow(0)
    val currentLyricsIndex: StateFlow<Int> = _currentLyricsIndex

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private var _isAdjusting = false
    private var _lyricsPosition = 0L

    init {
        getMusic()
        observeMusicPlayerPosition()
    }

    private fun observeMusicPlayerPosition() {
        viewModelScope.launch(Dispatchers.Main) {
            player.currentPosition.observeForever {
                _lyricsPosition = it
                updateLyricsIndex()
                if (_isAdjusting.not())
                    _sliderPosition.value = it
            }
        }
    }

    private fun getMusic() = viewModelScope.launch {
        _music.value = repository.getMusic()

        _music.value?.let {
            player.prepare(it.file)
        }

    }

    fun onSliderValueChange(newValue: Float) {
        _sliderPosition.value = newValue.toLong()

    }

    fun onSliderValueChangeFinished() {
        _lyricsPosition = _sliderPosition.value
        player.seekTo(_sliderPosition.value)
    }

    private fun updateLyricsIndex() {
        _music.value?.let { musicModel ->
            val index = musicModel.lyrics.toTimeList().indexOfLast { it <= _lyricsPosition }
            _currentLyricsIndex.value = if (index < 0) 0 else index
        }
    }

    fun updateIsPlaying(bool: Boolean) {
        _isPlaying.value = bool

        if (_isPlaying.value)
            player.play()
        else
            player.pauseMusic()
    }

    fun updateIsAdjusting(bool: Boolean) {
        _isAdjusting = bool
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun getDuration() = player.getDuration()
}
