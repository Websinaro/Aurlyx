package com.auralyx.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * aD17 thumbnail extraction with persistent disk cache.
 * Cache key = filename + lastModified so re-extraction only happens when file changes.
 */
object ThumbnailUtils {

    private const val THUMB_DIR  = "ad17_thumbs"
    private const val THUMB_SIZE = 512

    /** Returns a Bitmap for an .aD17 file. Checks disk cache first. */
    suspend fun getAD17Thumbnail(context: Context, path: String): Bitmap? =
        withContext(Dispatchers.IO) {
            val src = File(path)
            if (!src.exists()) return@withContext null

            val cacheKey  = "${src.nameWithoutExtension}_${src.lastModified()}.png"
            val thumbDir  = File(context.cacheDir, THUMB_DIR).also { it.mkdirs() }
            val cacheFile = File(thumbDir, cacheKey)

            // Return cached version if available
            if (cacheFile.exists()) {
                return@withContext try { BitmapFactory.decodeFile(cacheFile.absolutePath) } catch (_: Exception) { null }
            }

            // Extract frame from video
            val raw = extractFrame(path) ?: return@withContext null

            // Scale to manageable size
            val scaled = Bitmap.createScaledBitmap(
                raw,
                THUMB_SIZE,
                (THUMB_SIZE * raw.height.toFloat() / raw.width).toInt().coerceAtLeast(1),
                true
            )
            if (raw !== scaled) raw.recycle()

            // Save to disk cache
            try { FileOutputStream(cacheFile).use { scaled.compress(Bitmap.CompressFormat.PNG, 85, it) } } catch (_: Exception) {}

            scaled
        }

    private fun extractFrame(path: String): Bitmap? {
        val r = MediaMetadataRetriever()
        return try {
            r.setDataSource(path)
            r.getFrameAtTime(1_000_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: r.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (_: Exception) { null }
        finally { try { r.release() } catch (_: Exception) {} }
    }

    /**
     * Copies .aD17 to a temp .mp4 in cache so ExoPlayer can play it natively.
     * Reuses existing copy unless source has changed.
     */
    suspend fun resolveAD17Path(context: Context, path: String): String =
        withContext(Dispatchers.IO) {
            val src  = File(path)
            if (!src.exists()) return@withContext path
            val dest = File(context.cacheDir, "play_${src.nameWithoutExtension}.mp4")
            if (dest.exists() && dest.lastModified() >= src.lastModified()) return@withContext dest.absolutePath
            src.copyTo(dest, overwrite = true)
            dest.absolutePath
        }
}
