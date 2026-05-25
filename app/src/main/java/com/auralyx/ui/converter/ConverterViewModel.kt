package com.auralyx.ui.converter

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralyx.converter.AD17Converter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class ConvertQuality(val label: String, val videoBitrate: Int, val audioBitrate: Int, val scale: Int) {
    STANDARD("Standard",  1_500_000, 192_000, 720),
    HIGH    ("High",       4_000_000, 256_000, 1080),
    ULTRA   ("Ultra",     8_000_000, 320_000, 1440)
}

data class ConversionJob(
    val id: Long,
    val name: String,
    val uri: Uri,
    val quality: ConvertQuality,
    val progress: Float = 0f,          // 0..1
    val status: ConversionStatus = ConversionStatus.QUEUED,
    val error: String? = null,
    val outputPath: String? = null
)

enum class ConversionStatus { QUEUED, CONVERTING, DONE, FAILED, CANCELLED }

data class ConverterUiState(
    val queue: List<ConversionJob> = emptyList(),
    val selectedQuality: ConvertQuality = ConvertQuality.HIGH,
    val isPickerOpen: Boolean = false
)

@HiltViewModel
class ConverterViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val converter: AD17Converter
) : ViewModel() {

    private val _ui = MutableStateFlow(ConverterUiState())
    val uiState = _ui.asStateFlow()

    fun selectQuality(q: ConvertQuality) = _ui.update { it.copy(selectedQuality = q) }

    fun enqueue(uri: Uri) {
        val name = resolveFileName(uri)
        val job  = ConversionJob(
            id      = System.currentTimeMillis(),
            name    = name,
            uri     = uri,
            quality = _ui.value.selectedQuality
        )
        _ui.update { it.copy(queue = it.queue + job) }
        processNext()
    }

    private fun processNext() {
        val job = _ui.value.queue.firstOrNull { it.status == ConversionStatus.QUEUED } ?: return
        // Mark as converting
        update(job.id) { copy(status = ConversionStatus.CONVERTING, progress = 0f) }

        viewModelScope.launch {
            converter.convert(
                context  = context,
                uri      = job.uri,
                quality  = job.quality,
                onProgress = { p -> update(job.id) { copy(progress = p) } }
            ).onSuccess { outPath ->
                update(job.id) { copy(status = ConversionStatus.DONE, progress = 1f, outputPath = outPath) }
                processNext()
            }.onFailure { e ->
                update(job.id) { copy(status = ConversionStatus.FAILED, error = e.message) }
                processNext()
            }
        }
    }

    fun cancel(id: Long) {
        val job = _ui.value.queue.find { it.id == id } ?: return
        if (job.status == ConversionStatus.CONVERTING) {
            converter.cancel()
        }
        update(id) { copy(status = ConversionStatus.CANCELLED) }
    }

    fun remove(id: Long) {
        _ui.update { it.copy(queue = it.queue.filter { j -> j.id != id }) }
    }

    fun retryFailed() {
        _ui.update { s ->
            s.copy(queue = s.queue.map { j ->
                if (j.status == ConversionStatus.FAILED) j.copy(status = ConversionStatus.QUEUED, error = null, progress = 0f) else j
            })
        }
        processNext()
    }

    private fun update(id: Long, block: ConversionJob.() -> ConversionJob) {
        _ui.update { s -> s.copy(queue = s.queue.map { if (it.id == id) it.block() else it }) }
    }

    private fun resolveFileName(uri: Uri): String {
        var name = "video"
        context.contentResolver.query(uri, arrayOf(android.provider.MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)
            ?.use { c -> if (c.moveToFirst()) name = c.getString(0) ?: "video" }
        return name.substringBeforeLast(".")
    }
}
