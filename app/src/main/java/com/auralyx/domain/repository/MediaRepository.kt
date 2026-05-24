package com.auralyx.domain.repository

import com.auralyx.domain.model.*
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getAllSongs(): Flow<List<MediaItem>>
    fun getAllMusicVideos(): Flow<List<MediaItem>>
    fun getRecentlyPlayed(): Flow<List<MediaItem>>
    fun getMostPlayed(): Flow<List<MediaItem>>
    fun getFavorites(): Flow<List<MediaItem>>
    fun getAllAlbums(): Flow<List<Album>>
    fun getAllArtists(): Flow<List<Artist>>
    fun getAllFolders(): Flow<List<Folder>>
    fun getSongsByAlbum(albumId: Long): Flow<List<MediaItem>>
    fun getSongsByArtist(artistId: Long): Flow<List<MediaItem>>
    fun getSongsByFolder(folderId: Long): Flow<List<MediaItem>>
    fun searchAll(query: String): Flow<List<MediaItem>>
    suspend fun scanStorage()
    suspend fun updateLastPlayed(id: Long, timestamp: Long)
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
    suspend fun getSongCount(): Int
}
