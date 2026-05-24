package com.auralyx.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralyx.domain.model.MediaItem
import com.auralyx.domain.repository.MediaRepository
import com.auralyx.domain.usecase.SearchMediaUseCase
import com.auralyx.player.AuralyxPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel @Inject constructor(
    private val search: SearchMediaUseCase,
    private val player: AuralyxPlayer,
    private val repo: MediaRepository
) : ViewModel() {

    private val _q = MutableStateFlow("")
    val query = _q.asStateFlow()
    val playerState = player.state

    val results: StateFlow<List<MediaItem>> = _q
        .debounce(300)
        .flatMapLatest { search(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onQueryChange(q: String) { _q.value = q }

    fun play(item: MediaItem, queue: List<MediaItem>) {
        viewModelScope.launch {
            player.playQueue(queue, queue.indexOf(item).coerceAtLeast(0), item.isAD17)
            repo.updateLastPlayed(item.id, System.currentTimeMillis())
        }
    }
}
