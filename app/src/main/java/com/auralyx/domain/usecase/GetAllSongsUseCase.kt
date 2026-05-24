package com.auralyx.domain.usecase
import com.auralyx.domain.model.MediaItem
import com.auralyx.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
class GetAllSongsUseCase @Inject constructor(private val r: MediaRepository) {
    operator fun invoke(): Flow<List<MediaItem>> = r.getAllSongs()
}
