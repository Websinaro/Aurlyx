package com.auralyx.domain.usecase

import com.auralyx.domain.model.MediaItem
import com.auralyx.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMusicVideosUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(): Flow<List<MediaItem>> = repository.getAllMusicVideos()
}
