package com.auralyx.data.local.dao

import androidx.room.*
import com.auralyx.data.local.entity.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items WHERE is_ad17=0 ORDER BY title ASC")
    fun getAllSongs(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE is_ad17=1 ORDER BY title ASC")
    fun getAllMusicVideos(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE last_played>0 ORDER BY last_played DESC LIMIT 30")
    fun getRecentlyPlayed(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE play_count>0 ORDER BY play_count DESC LIMIT 30")
    fun getMostPlayed(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE is_favorite=1 ORDER BY title ASC")
    fun getFavorites(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE folder_id=:folderId ORDER BY title ASC")
    fun getSongsByFolder(folderId: Long): Flow<List<MediaEntity>>

    @Query("""
        SELECT * FROM media_items
        WHERE title LIKE '%'||:q||'%'
           OR artist LIKE '%'||:q||'%'
           OR album LIKE '%'||:q||'%'
        ORDER BY
            CASE WHEN title LIKE :q||'%' THEN 0 ELSE 1 END,
            play_count DESC,
            title ASC
        LIMIT 200
    """)
    fun search(q: String): Flow<List<MediaEntity>>

    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun getSongCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaEntity>)

    @Query("DELETE FROM media_items")
    suspend fun deleteAll()

    @Query("UPDATE media_items SET last_played=:timestamp, play_count=play_count+1 WHERE id=:id")
    suspend fun updateLastPlayed(id: Long, timestamp: Long)

    @Query("UPDATE media_items SET is_favorite=:fav WHERE id=:id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("SELECT * FROM media_items WHERE album=:album ORDER BY track_number ASC, title ASC")
    fun getSongsByAlbum(album: String): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE artist=:artist ORDER BY album ASC, track_number ASC")
    fun getSongsByArtist(artist: String): Flow<List<MediaEntity>>
}
