package com.auralyx.data.local.entity

import androidx.room.*
import com.auralyx.domain.model.MediaItem

@Entity(
    tableName = "media_items",
    indices = [
        Index(value = ["path"], unique = true),
        Index(value = ["is_ad17"]),
        Index(value = ["last_played"]),
        Index(value = ["play_count"]),
        Index(value = ["is_favorite"])
    ]
)
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    @ColumnInfo(name = "album_art_uri")  val albumArtUri: String? = null,
    @ColumnInfo(name = "is_ad17")        val isAD17: Boolean = false,
    @ColumnInfo(name = "folder_id")      val folderId: Long = 0,
    @ColumnInfo(name = "folder_name")    val folderName: String = "",
    @ColumnInfo(name = "date_added")     val dateAdded: Long = 0,
    val size: Long = 0,
    @ColumnInfo(name = "track_number")   val trackNumber: Int = 0,
    @ColumnInfo(name = "last_played")    val lastPlayed: Long = 0,
    @ColumnInfo(name = "play_count")     val playCount: Int = 0,
    @ColumnInfo(name = "is_favorite")    val isFavorite: Boolean = false
) {
    fun toDomain() = MediaItem(
        id, title, artist, album, duration, path,
        albumArtUri, isAD17, folderId, folderName,
        dateAdded, size, trackNumber, isFavorite, playCount
    )
}
