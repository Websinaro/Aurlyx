package com.auralyx.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralyx.domain.model.RepeatMode
import com.auralyx.domain.repository.MediaRepository
import com.auralyx.player.AuralyxPlayer
import com.auralyx.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val player: AuralyxPlayer,
    private val repository: MediaRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    val state            = player.state
    val defaultVideoMode = prefs.isDefaultVideoMode

    fun togglePlayPause()        = player.togglePlayPause()
    fun skipToNext()             = player.skipToNext()
    fun skipToPrev()             = player.skipToPrev()
    fun seekTo(ms: Long)         = player.seekTo(ms)
    fun seekToFraction(f: Float) = player.seekToFraction(f)
    fun toggleShuffle()          = player.toggleShuffle()

    fun cycleRepeatMode() {
        val next = when (player.state.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        player.setRepeatMode(next)
    }

    fun toggleVideo() = player.setVideoEnabled(!player.state.value.isVideoEnabled)

    fun toggleFavorite() {
        val item = player.state.value.currentItem ?: return
        viewModelScope.launch {
            repository.setFavorite(item.id, !item.isFavorite)
        }
    }

    /** durationMs = 0 cancels */
    fun setSleepTimer(durationMs: Long) = player.setSleepTimer(durationMs)
    fun cancelSleepTimer()              = player.cancelSleepTimer()

    fun updateLastPlayed(id: Long) {
        viewModelScope.launch { repository.updateLastPlayed(id, System.currentTimeMillis()) }
    }
}
