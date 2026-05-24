package com.auralyx.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.auralyx.data.local.dao.MediaDao
import com.auralyx.data.local.entity.MediaEntity
import com.auralyx.domain.model.*
import com.auralyx.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: MediaDao
) : MediaRepository {

    override fun getAllSongs()        = dao.getAllSongs().map { it.map { e -> e.toDomain() } }
    override fun getAllMusicVideos()  = dao.getAllMusicVideos().map { it.map { e -> e.toDomain() } }
    override fun getRecentlyPlayed() = dao.getRecentlyPlayed().map { it.map { e -> e.toDomain() } }
    override fun getMostPlayed()     = dao.getMostPlayed().map { it.map { e -> e.toDomain() } }
    override fun getFavorites()      = dao.getFavorites().map { it.map { e -> e.toDomain() } }
    override fun searchAll(q: String) = dao.search(q).map { it.map { e -> e.toDomain() } }
    override fun getSongsByAlbum(id: Long)   = dao.getSongsByAlbum(id.toString()).map { it.map { e -> e.toDomain() } }
    override fun getSongsByArtist(id: Long)  = dao.getSongsByArtist(id.toString()).map { it.map { e -> e.toDomain() } }
    override fun getSongsByFolder(id: Long)  = dao.getSongsByFolder(id).map { it.map { e -> e.toDomain() } }

    override fun getAllAlbums(): Flow<List<Album>> = dao.getAllSongs().map { entities ->
        entities.groupBy { it.album }.map { (name, songs) ->
            Album(
                name.hashCode().toLong(),
                name.ifBlank { "Unknown Album" },
                songs.first().artist.ifBlank { "Unknown Artist" },
                songs.first().albumArtUri,
                songs.size
            )
        }.sortedBy { it.name }
    }

    override fun getAllArtists(): Flow<List<Artist>> = dao.getAllSongs().map { entities ->
        entities.groupBy { it.artist }.map { (name, songs) ->
            Artist(
                name.hashCode().toLong(),
                name.ifBlank { "Unknown Artist" },
                songs.first().albumArtUri,
                songs.map { it.album }.distinct().size,
                songs.size
            )
        }.sortedBy { it.name }
    }

    override fun getAllFolders(): Flow<List<Folder>> = dao.getAllSongs().map { entities ->
        entities.groupBy { it.folderId }.map { (fid, songs) ->
            Folder(fid, songs.first().folderName.ifBlank { "Unknown" },
                File(songs.first().path).parent ?: "", songs.size)
        }.sortedBy { it.name }
    }

    override suspend fun scanStorage() = withContext(Dispatchers.IO) {
        val items = mutableListOf<MediaEntity>()
        items += scanAudio()
        items += scanAD17()
        dao.deleteAll()
        if (items.isNotEmpty()) dao.insertAll(items)
    }

    private fun scanAudio(): List<MediaEntity> {
        val proj = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.BUCKET_ID,
            MediaStore.Audio.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.TRACK
        )
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val items = mutableListOf<MediaEntity>()
        context.contentResolver.query(
            uri, proj,
            "${MediaStore.Audio.Media.IS_MUSIC}!=0 AND ${MediaStore.Audio.Media.DURATION}>10000",
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { c ->
            val idxTitle    = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val idxArtist   = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val idxAlbum    = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val idxDuration = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val idxData     = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val idxAlbumId  = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val idxBucketId = c.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_ID)
            val idxBucketNm = c.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)
            val idxDate     = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val idxSize     = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val idxTrack    = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)

            while (c.moveToNext()) {
                val path = c.getString(idxData) ?: continue
                val albumId = c.getLong(idxAlbumId)
                val artUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"), albumId
                ).toString()
                items += MediaEntity(
                    title       = c.getString(idxTitle) ?: "Unknown",
                    artist      = c.getString(idxArtist) ?: "",
                    album       = c.getString(idxAlbum) ?: "",
                    duration    = c.getLong(idxDuration),
                    path        = path,
                    albumArtUri = artUri,
                    isAD17      = false,
                    folderId    = c.getLong(idxBucketId),
                    folderName  = c.getString(idxBucketNm) ?: "",
                    dateAdded   = c.getLong(idxDate),
                    size        = c.getLong(idxSize),
                    trackNumber = c.getInt(idxTrack)
                )
            }
        }
        return items
    }

    private fun scanAD17(): List<MediaEntity> {
        val items = mutableListOf<MediaEntity>()
        val roots = mutableListOf<File>()
        android.os.Environment.getExternalStorageDirectory()?.let { roots += it }
        context.getExternalFilesDirs(null)
            .mapNotNull { it?.parentFile?.parentFile?.parentFile?.parentFile }
            .forEach { roots += it }
        roots.distinct().filter { it.exists() }.forEach { scanDir(it, items) }
        return items
    }

    private fun scanDir(dir: File, acc: MutableList<MediaEntity>) {
        if (!dir.canRead()) return
        dir.listFiles()?.forEach { f ->
            when {
                f.isDirectory -> scanDir(f, acc)
                f.extension.lowercase() == "ad17" -> {
                    val dur = try {
                        val r = android.media.MediaMetadataRetriever()
                        r.setDataSource(f.absolutePath)
                        val d = r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                        r.release(); d
                    } catch (_: Exception) { 0L }
                    acc += MediaEntity(
                        title      = f.nameWithoutExtension,
                        artist     = "",
                        album      = "",
                        duration   = dur,
                        path       = f.absolutePath,
                        albumArtUri = null,
                        isAD17     = true,
                        folderId   = f.parent.hashCode().toLong(),
                        folderName = f.parentFile?.name ?: "",
                        dateAdded  = f.lastModified(),
                        size       = f.length()
                    )
                }
            }
        }
    }

    override suspend fun updateLastPlayed(id: Long, timestamp: Long) = dao.updateLastPlayed(id, timestamp)
    override suspend fun setFavorite(id: Long, isFavorite: Boolean)  = dao.setFavorite(id, isFavorite)
    override suspend fun getSongCount() = dao.getSongCount()
}
