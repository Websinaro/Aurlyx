package com.auralyx.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.auralyx.domain.model.MediaItem
import com.auralyx.domain.model.PlayerState
import com.auralyx.domain.model.RepeatMode
import com.auralyx.utils.ThumbnailUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuralyxPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
    val exoPlayer: ExoPlayer,
    private val trackSelector: DefaultTrackSelector
) {
    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val scope       = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob : Job? = null
    private var resolveJob  : Job? = null
    private var sleepJob    : Job? = null

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _state.update { it.copy(isPlaying = playing) }
                if (playing) startProgressLoop() else stopProgressLoop()
            }

            override fun onPlaybackStateChanged(s: Int) {
                _state.update {
                    it.copy(
                        isBuffering = s == Player.STATE_BUFFERING,
                        duration    = exoPlayer.duration.coerceAtLeast(0)
                    )
                }
            }

            override fun onMediaItemTransition(item: ExoMediaItem?, reason: Int) {
                val idx = exoPlayer.currentMediaItemIndex
                val q   = _state.value.queue
                if (idx in q.indices) _state.update { it.copy(currentItem = q[idx], queueIndex = idx) }
            }

            override fun onPlayerError(e: androidx.media3.common.PlaybackException) {
                _state.update { it.copy(error = e.message, isBuffering = false) }
            }
        })
    }

    fun playQueue(items: List<MediaItem>, startIndex: Int = 0, videoEnabled: Boolean = false) {
        resolveJob?.cancel()
        resolveJob = scope.launch {
            val resolved = items.map { item ->
                async(Dispatchers.IO) {
                    if (item.isAD17) item.copy(path = ThumbnailUtils.resolveAD17Path(context, item.path))
                    else item
                }
            }.awaitAll()
            _state.update { it.copy(queue = resolved, queueIndex = startIndex, isVideoEnabled = videoEnabled) }
            exoPlayer.setMediaItems(resolved.map { buildItem(it) }, startIndex, 0L)
            exoPlayer.prepare()
            applyVideoMode(videoEnabled)
            exoPlayer.play()
        }
    }

    fun playSingle(item: MediaItem, videoEnabled: Boolean = false) = playQueue(listOf(item), 0, videoEnabled)

    fun togglePlayPause() { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() }

    fun seekTo(ms: Long) {
        exoPlayer.seekTo(ms)
        _state.update { it.copy(progress = ms) }
    }

    fun seekToFraction(f: Float) {
        val d = exoPlayer.duration.takeIf { it > 0 } ?: return
        seekTo((f * d).toLong())
    }

    fun skipToNext() { if (exoPlayer.hasNextMediaItem()) exoPlayer.seekToNextMediaItem() }

    fun skipToPrev() {
        if (exoPlayer.currentPosition > 3000) exoPlayer.seekTo(0)
        else if (exoPlayer.hasPreviousMediaItem()) exoPlayer.seekToPreviousMediaItem()
    }

    fun setVideoEnabled(enabled: Boolean) {
        _state.update { it.copy(isVideoEnabled = enabled) }
        applyVideoMode(enabled)
    }

    fun setRepeatMode(mode: RepeatMode) {
        _state.update { it.copy(repeatMode = mode) }
        exoPlayer.repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    fun toggleShuffle() {
        val s = !_state.value.shuffleEnabled
        _state.update { it.copy(shuffleEnabled = s) }
        exoPlayer.shuffleModeEnabled = s
    }

    /** Start sleep timer; durationMs <= 0 cancels it. */
    fun setSleepTimer(durationMs: Long) {
        sleepJob?.cancel()
        if (durationMs <= 0) {
            _state.update { it.copy(sleepTimerEndsAt = 0L) }
            return
        }
        val endsAt = System.currentTimeMillis() + durationMs
        _state.update { it.copy(sleepTimerEndsAt = endsAt) }
        sleepJob = scope.launch {
            delay(durationMs)
            exoPlayer.pause()
            _state.update { it.copy(sleepTimerEndsAt = 0L) }
        }
    }

    fun cancelSleepTimer() = setSleepTimer(0)

    private fun buildItem(item: MediaItem): ExoMediaItem {
        val uri = if (File(item.path).exists()) Uri.fromFile(File(item.path)) else Uri.parse(item.path)
        return ExoMediaItem.Builder().setUri(uri).setMediaId(item.id.toString()).build()
    }

    private fun applyVideoMode(enabled: Boolean) {
        val p = trackSelector.buildUponParameters()
        if (enabled) p.clearVideoSizeConstraints() else p.setMaxVideoSize(0, 0)
        trackSelector.setParameters(p)
    }

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                _state.update {
                    it.copy(
                        progress = exoPlayer.currentPosition.coerceAtLeast(0),
                        duration = exoPlayer.duration.coerceAtLeast(0)
                    )
                }
                delay(500)
            }
        }
    }

    private fun stopProgressLoop() { progressJob?.cancel() }
}
