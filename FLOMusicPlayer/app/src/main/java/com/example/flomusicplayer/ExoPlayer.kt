package com.example.flomusicplayer

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExoPlayer(context: Context) {

    private val _currentPosition = MutableLiveData<Long>()
    val currentPosition: LiveData<Long>
        get() = _currentPosition

    private var exoPlayer: SimpleExoPlayer? = SimpleExoPlayer.Builder(context).build()

    init {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(1000)
                _currentPosition.value = exoPlayer?.currentPosition

            }
        }
    }

    fun prepare(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        val mediaSource = ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory())
            .createMediaSource(mediaItem)

        exoPlayer?.apply {
            setMediaSource(mediaSource)
            prepare()

        }
    }

    fun play() {
        exoPlayer?.play()
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    fun pauseMusic() {
        exoPlayer?.pause()
    }

    fun getDuration() = exoPlayer?.duration
}