package com.auralyx.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralyx.domain.repository.MediaRepository
import com.auralyx.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val darkTheme: Boolean       = true,
    val defaultVideoMode: Boolean = false,
    val scanOnLaunch: Boolean    = true,
    val dynamicColor: Boolean    = false,
    val isScanning: Boolean      = false,
    val scanMsg: String?         = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesManager,
    private val repo: MediaRepository
) : ViewModel() {

    private val _s = MutableStateFlow(SettingsUiState())
    val state = _s.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefs.isDarkTheme,
                prefs.isDefaultVideoMode,
                prefs.scanOnLaunch,
                prefs.dynamicColor
            ) { dark, video, scan, dynColor ->
                SettingsUiState(dark, video, scan, dynColor)
            }.collect { _s.value = it }
        }
    }

    fun toggleDarkTheme(v: Boolean)        = viewModelScope.launch { prefs.setDarkTheme(v) }
    fun toggleDefaultVideoMode(v: Boolean) = viewModelScope.launch { prefs.setDefaultVideoMode(v) }
    fun toggleScanOnLaunch(v: Boolean)     = viewModelScope.launch { prefs.setScanOnLaunch(v) }
    fun toggleDynamicColor(v: Boolean)     = viewModelScope.launch { prefs.setDynamicColor(v) }

    fun scanStorage() = viewModelScope.launch {
        _s.update { it.copy(isScanning = true, scanMsg = null) }
        try {
            repo.scanStorage()
            val c = repo.getSongCount()
            _s.update { it.copy(isScanning = false, scanMsg = "Found $c items") }
        } catch (e: Exception) {
            _s.update { it.copy(isScanning = false, scanMsg = "Failed: ${e.message}") }
        }
    }
}
