package com.auralyx.domain.usecase
import com.auralyx.domain.repository.MediaRepository
import javax.inject.Inject
class ScanStorageUseCase @Inject constructor(private val r: MediaRepository) {
    suspend operator fun invoke() = r.scanStorage()
}
