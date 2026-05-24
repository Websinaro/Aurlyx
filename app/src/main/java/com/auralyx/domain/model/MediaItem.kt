package com.auralyx.domain.model

data class MediaItem(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumArtUri: String? = null,
    val isAD17: Boolean = false,
    val folderId: Long = 0,
    val folderName: String = "",
    val dateAdded: Long = 0,
    val size: Long = 0,
    val trackNumber: Int = 0,
    val isFavorite: Boolean = false,
    val playCount: Int = 0
) {
    val durationFormatted: String get() {
        val s = duration / 1000
        return if (s >= 3600) "%d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60)
        else "%d:%02d".format(s / 60, s % 60)
    }
    val displayArtist: String get() = artist.ifBlank { "Unknown Artist" }
    val displayAlbum: String  get() = album.ifBlank { "Unknown Album" }
}
