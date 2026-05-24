package com.auralyx.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    private val ds: DataStore<Preferences>
) {
    companion object {
        val KEY_DARK_THEME         = booleanPreferencesKey("dark_theme")
        val KEY_DEFAULT_VIDEO_MODE = booleanPreferencesKey("default_video_mode")
        val KEY_SCAN_ON_LAUNCH     = booleanPreferencesKey("scan_on_launch")
        val KEY_DYNAMIC_COLOR      = booleanPreferencesKey("dynamic_color")
    }

    val isDarkTheme:       Flow<Boolean> = ds.data.map { it[KEY_DARK_THEME]         ?: true }
    val isDefaultVideoMode:Flow<Boolean> = ds.data.map { it[KEY_DEFAULT_VIDEO_MODE] ?: false }
    val scanOnLaunch:      Flow<Boolean> = ds.data.map { it[KEY_SCAN_ON_LAUNCH]     ?: true }
    val dynamicColor:      Flow<Boolean> = ds.data.map { it[KEY_DYNAMIC_COLOR]      ?: false }

    suspend fun setDarkTheme(v: Boolean)        { ds.edit { it[KEY_DARK_THEME]         = v } }
    suspend fun setDefaultVideoMode(v: Boolean) { ds.edit { it[KEY_DEFAULT_VIDEO_MODE] = v } }
    suspend fun setScanOnLaunch(v: Boolean)     { ds.edit { it[KEY_SCAN_ON_LAUNCH]     = v } }
    suspend fun setDynamicColor(v: Boolean)     { ds.edit { it[KEY_DYNAMIC_COLOR]      = v } }
}
