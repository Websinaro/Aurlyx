package com.auralyx.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralyx.domain.model.*
import com.auralyx.domain.repository.MediaRepository
import com.auralyx.player.AuralyxPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab { SONGS, ALBUMS, ARTISTS, FOLDERS, VIDEOS }

data class LibraryUiState(
    val tab: LibraryTab = LibraryTab.SONGS,
    val songs: List<MediaItem> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val videos: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repo: MediaRepository, 
    private val player: AuralyxPlayer
) : ViewModel() {
    
    private val _s = MutableStateFlow(LibraryUiState())
    val state = _s.asStateFlow()
    val playerState = player.state

    init {
        viewModelScope.launch {
            combine(
                repo.getAllSongs(),
                repo.getAllAlbums(),
                repo.getAllArtists(),
                repo.getAllFolders(),
                repo.getAllMusicVideos()
            ) { s, a, ar, f, v ->
                // This is the transform block
                _s.value.copy(songs = s, albums = a, artists = ar, folders = f, videos = v, isLoading = false)
            }.collect { newState ->
                // This is the collection block
                _s.value = newState
            }
        }
    }

    fun selectTab(t: LibraryTab) { _s.update { it.copy(tab = t) } }

    fun play(item: MediaItem, queue: List<MediaItem>, video: Boolean = false) {
        viewModelScope.launch {
            player.playQueue(queue, queue.indexOf(item).coerceAtLeast(0), video)
            repo.updateLastPlayed(item.id, System.currentTimeMillis())
        }
    }
}
