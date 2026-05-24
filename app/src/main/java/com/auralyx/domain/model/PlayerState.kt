package com.auralyx.domain.model

data class PlayerState(
    val currentItem: MediaItem? = null,
    val isPlaying: Boolean = false,
    val progress: Long = 0L,
    val duration: Long = 0L,
    val isVideoEnabled: Boolean = false,
    val isBuffering: Boolean = false,
    val queue: List<MediaItem> = emptyList(),
    val queueIndex: Int = 0,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleEnabled: Boolean = false,
    val error: String? = null,
    val sleepTimerEndsAt: Long = 0L   // epoch ms; 0 = disabled
) {
    val progressFraction: Float get() = if (duration > 0) progress.toFloat() / duration else 0f
    val hasNext: Boolean get() = queueIndex < queue.size - 1
    val hasPrev: Boolean get() = queueIndex > 0
    val sleepTimerActive: Boolean get() = sleepTimerEndsAt > System.currentTimeMillis()
}

enum class RepeatMode { OFF, ONE, ALL }
