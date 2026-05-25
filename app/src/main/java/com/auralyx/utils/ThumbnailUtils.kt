package com.auralyx.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import com.auralyx.converter.AD17Converter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * aD17 thumbnail extraction + playback path resolution.
 *
 * Conversion chain:
 *   original video → XOR-encode → .aD17
 *   .aD17           → XOR-decode → temp .mp4 → ExoPlayer
 *
 * The XOR key is shared with AD17Converter so encoding/decoding are symmetric.
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

            if (cacheFile.exists()) {
                return@withContext try { BitmapFactory.decodeFile(cacheFile.absolutePath) } catch (_: Exception) { null }
            }

            // Decode to temp MP4 first
            val mp4 = decodedMp4(context, src) ?: return@withContext null
            val raw = extractFrame(mp4.absolutePath)
            if (!keepTempMp4(context, src)) mp4.delete()

            raw ?: return@withContext null

            val scaled = Bitmap.createScaledBitmap(
                raw,
                THUMB_SIZE,
                (THUMB_SIZE * raw.height.toFloat() / raw.width).toInt().coerceAtLeast(1),
                true
            )
            if (raw !== scaled) raw.recycle()

            try { FileOutputStream(cacheFile).use { scaled.compress(Bitmap.CompressFormat.PNG, 85, it) } } catch (_: Exception) {}
            scaled
        }

    /**
     * Returns the path ExoPlayer should use to play an .aD17 file.
     * XOR-decodes to a cached .mp4, reusing it if the source hasn't changed.
     */
    suspend fun resolveAD17Path(context: Context, path: String): String =
        withContext(Dispatchers.IO) {
            val src  = File(path)
            if (!src.exists()) return@withContext path
            val dest = cachedMp4File(context, src)
            if (dest.exists() && dest.lastModified() >= src.lastModified()) return@withContext dest.absolutePath
            decodedMp4(context, src)?.absolutePath ?: path
        }

    // ── Internal ─────────────────────────────────────────────────────────────

    /** XOR-decode src (.aD17) → dest (.mp4) in cache. */
    private fun decodedMp4(context: Context, src: File): File? {
        return try {
            val dest = cachedMp4File(context, src)
            xorFile(src, dest, AD17Converter.AD17_KEY)
            dest
        } catch (_: Exception) { null }
    }

    private fun cachedMp4File(context: Context, src: File) =
        File(context.cacheDir, "play_${src.nameWithoutExtension}.mp4")

    /** Returns true if we want to keep the temp mp4 around (already done). */
    private fun keepTempMp4(context: Context, src: File): Boolean {
        val f = cachedMp4File(context, src)
        return f.exists() && f.lastModified() >= src.lastModified()
    }

    private fun xorFile(src: File, dst: File, key: ByteArray) {
        FileInputStream(src).use { inp ->
            FileOutputStream(dst).use { out ->
                val buf = ByteArray(65536)
                var read: Int
                var pos = 0
                while (inp.read(buf).also { read = it } != -1) {
                    for (i in 0 until read) buf[i] = (buf[i].toInt() xor key[pos % key.size].toInt()).toByte().also { pos++ }
                    out.write(buf, 0, read)
                }
            }
        }
    }

    private fun extractFrame(mp4Path: String): Bitmap? {
        val r = MediaMetadataRetriever()
        return try {
            r.setDataSource(mp4Path)
            r.getFrameAtTime(1_000_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: r.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (_: Exception) { null }
        finally { try { r.release() } catch (_: Exception) {} }
    }
}
