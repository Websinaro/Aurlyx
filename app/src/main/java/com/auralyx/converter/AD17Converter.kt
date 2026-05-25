package com.auralyx.converter

import android.content.Context
import android.media.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import com.auralyx.ui.converter.ConvertQuality
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AD17Converter
 *
 * Converts any video file into the .aD17 format by:
 *  1. Extracting & re-encoding the video track via MediaCodec (H.264)
 *  2. Extracting & re-encoding the audio track via MediaCodec (AAC)
 *  3. Muxing both into a temporary MP4 container
 *  4. XOR-encoding the file with the AD17 key → writes final .aD17 file
 *
 * The XOR key is the same lightweight obfuscation used by the existing
 * ThumbnailUtils/resolveAD17Path mechanism (copy → .mp4), so ExoPlayer
 * can play back after the reverse operation.
 */
@Singleton
class AD17Converter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentJob: Job? = null

    fun cancel() { currentJob?.cancel() }

    suspend fun convert(
        context: Context,
        uri: Uri,
        quality: ConvertQuality,
        onProgress: (Float) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        currentJob = coroutineContext[Job]
        try {
            // ── 0. Open source ──────────────────────────────────────────
            val afd = context.contentResolver.openAssetFileDescriptor(uri, "r")
                ?: return@withContext Result.failure(Exception("Cannot open file"))

            val srcPath = copyToCache(context, afd)
            afd.close()

            onProgress(0.05f)

            // ── 1. Extract tracks ────────────────────────────────────────
            val extractor = MediaExtractor()
            extractor.setDataSource(srcPath)
            val videoTrackIdx = findTrack(extractor, "video/")
            val audioTrackIdx = findTrack(extractor, "audio/")
            if (videoTrackIdx < 0 && audioTrackIdx < 0) {
                extractor.release()
                return@withContext Result.failure(Exception("No media tracks found"))
            }

            // ── 2. Re-encode to a temp MP4 ───────────────────────────────
            val tempMp4 = File(context.cacheDir, "ad17_tmp_${System.currentTimeMillis()}.mp4")
            val muxer   = MediaMuxer(tempMp4.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            if (videoTrackIdx >= 0) {
                encodeVideoTrack(extractor, videoTrackIdx, muxer, quality, srcPath) { p -> onProgress(0.05f + p * 0.55f) }
            }
            if (audioTrackIdx >= 0) {
                encodeAudioTrack(extractor, audioTrackIdx, muxer, quality, srcPath) { p -> onProgress(0.60f + p * 0.30f) }
            }

            muxer.stop(); muxer.release(); extractor.release()
            File(srcPath).delete()

            ensureNotCancelled()

            // ── 3. XOR-obfuscate → .aD17 ────────────────────────────────
            onProgress(0.92f)
            val outName = uri.lastPathSegment?.substringAfterLast("/")?.substringBeforeLast(".")
                ?: "converted_${System.currentTimeMillis()}"
            val outDir  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .also { it.mkdirs() }
            val outFile = uniqueFile(outDir, outName, "aD17")

            xorFile(tempMp4, outFile)
            tempMp4.delete()

            onProgress(1f)
            Result.success(outFile.absolutePath)
        } catch (e: CancellationException) {
            Result.failure(Exception("Cancelled"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Video encode ─────────────────────────────────────────────────────────
    private fun encodeVideoTrack(
        extractor: MediaExtractor,
        trackIdx: Int,
        muxer: MediaMuxer,
        quality: ConvertQuality,
        srcPath: String,
        onProgress: (Float) -> Unit
    ) {
        val ex2 = MediaExtractor().also { it.setDataSource(srcPath); it.selectTrack(trackIdx) }
        val fmt = ex2.getTrackFormat(trackIdx)
        val mime = fmt.getString(MediaFormat.KEY_MIME) ?: return

        val w0    = fmt.getInt(MediaFormat.KEY_WIDTH, 1280)
        val h0    = fmt.getInt(MediaFormat.KEY_HEIGHT, 720)
        val ratio = w0.toFloat() / h0
        val maxDim = quality.scale
        val (outW, outH) = if (w0 > h0) {
            val w = minOf(w0, maxDim); Pair(w, (w / ratio).toInt().roundToEven())
        } else {
            val h = minOf(h0, maxDim); Pair((h * ratio).toInt().roundToEven(), h)
        }

        val outFmt = MediaFormat.createVideoFormat("video/avc", outW, outH).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, quality.videoBitrate)
            setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        }

        val encoder = MediaCodec.createEncoderByType("video/avc")
        val decoder = MediaCodec.createDecoderByType(mime)

        val surface = encoder.apply { configure(outFmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE) }.createInputSurface()
        encoder.start()
        decoder.configure(fmt, surface, null, 0); decoder.start()

        val dur = if (fmt.containsKey(MediaFormat.KEY_DURATION)) fmt.getLong(MediaFormat.KEY_DURATION) else 1L
        var muxTrack = -1
        val bufInfo  = MediaCodec.BufferInfo()
        var inDone   = false; var outDone = false

        while (!outDone) {
            if (!inDone) {
                val idx = decoder.dequeueInputBuffer(10_000)
                if (idx >= 0) {
                    val buf  = decoder.getInputBuffer(idx)!!
                    val size = ex2.readSampleData(buf, 0)
                    if (size < 0) {
                        decoder.queueInputBuffer(idx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM); inDone = true
                    } else {
                        decoder.queueInputBuffer(idx, 0, size, ex2.sampleTime, ex2.sampleFlags); ex2.advance()
                    }
                }
            }
            // drain decoder → surface → encoder
            val decIdx = decoder.dequeueOutputBuffer(bufInfo, 5_000)
            if (decIdx >= 0) {
                decoder.releaseOutputBuffer(decIdx, true)
                if (bufInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) encoder.signalEndOfInputStream()
                if (dur > 0) onProgress(bufInfo.presentationTimeUs.toFloat() / dur)
            }

            val encIdx = encoder.dequeueOutputBuffer(bufInfo, 5_000)
            if (encIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                muxTrack = muxer.addTrack(encoder.outputFormat); muxer.start()
            } else if (encIdx >= 0) {
                if (muxTrack >= 0 && bufInfo.size > 0) muxer.writeSampleData(muxTrack, encoder.getOutputBuffer(encIdx)!!, bufInfo)
                encoder.releaseOutputBuffer(encIdx, false)
                if (bufInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outDone = true
            }
        }
        decoder.stop(); decoder.release(); encoder.stop(); encoder.release()
        surface.release(); ex2.release()
    }

    // ── Audio encode ─────────────────────────────────────────────────────────
    private fun encodeAudioTrack(
        extractor: MediaExtractor,
        trackIdx: Int,
        muxer: MediaMuxer,
        quality: ConvertQuality,
        srcPath: String,
        onProgress: (Float) -> Unit
    ) {
        val ex2 = MediaExtractor().also { it.setDataSource(srcPath); it.selectTrack(trackIdx) }
        val fmt = ex2.getTrackFormat(trackIdx)
        val sampleRate  = fmt.getInt(MediaFormat.KEY_SAMPLE_RATE, 44100)
        val channelCount = fmt.getInt(MediaFormat.KEY_CHANNEL_COUNT, 2)
        val mime = fmt.getString(MediaFormat.KEY_MIME) ?: "audio/mp4a-latm"

        val outFmt = MediaFormat.createAudioFormat("audio/mp4a-latm", sampleRate, channelCount).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, quality.audioBitrate)
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        }

        val encoder = MediaCodec.createEncoderByType("audio/mp4a-latm").apply { configure(outFmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE); start() }
        val decoder = MediaCodec.createDecoderByType(mime).apply { configure(fmt, null, null, 0); start() }

        val dur = if (fmt.containsKey(MediaFormat.KEY_DURATION)) fmt.getLong(MediaFormat.KEY_DURATION) else 1L
        var muxTrack = -1; val bufInfo = MediaCodec.BufferInfo()
        var inDone = false; var decDone = false; var outDone = false

        while (!outDone) {
            if (!inDone) {
                val idx = decoder.dequeueInputBuffer(10_000)
                if (idx >= 0) {
                    val buf  = decoder.getInputBuffer(idx)!!
                    val size = ex2.readSampleData(buf, 0)
                    if (size < 0) { decoder.queueInputBuffer(idx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM); inDone = true }
                    else { decoder.queueInputBuffer(idx, 0, size, ex2.sampleTime, ex2.sampleFlags); ex2.advance() }
                }
            }
            val decIdx = decoder.dequeueOutputBuffer(bufInfo, 5_000)
            if (decIdx >= 0) {
                if (!decDone) {
                    val pcm = decoder.getOutputBuffer(decIdx)!!
                    val encIdx = encoder.dequeueInputBuffer(10_000)
                    if (encIdx >= 0) {
                        val encBuf = encoder.getInputBuffer(encIdx)!!
                        val copy   = minOf(pcm.remaining(), encBuf.capacity())
                        val slice  = pcm.duplicate().also { it.limit(it.position() + copy) }
                        encBuf.clear(); encBuf.put(slice)
                        val flags = if (bufInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
                        encoder.queueInputBuffer(encIdx, 0, copy, bufInfo.presentationTimeUs, flags)
                        if (flags != 0) decDone = true
                    }
                }
                decoder.releaseOutputBuffer(decIdx, false)
                if (dur > 0) onProgress(bufInfo.presentationTimeUs.toFloat() / dur)
            }
            val encIdx = encoder.dequeueOutputBuffer(bufInfo, 5_000)
            if (encIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                muxTrack = muxer.addTrack(encoder.outputFormat)
                if (muxTrack >= 0 && muxer.toString().contains("started").not()) { /* already started by video */ }
            } else if (encIdx >= 0) {
                if (muxTrack >= 0 && bufInfo.size > 0) muxer.writeSampleData(muxTrack, encoder.getOutputBuffer(encIdx)!!, bufInfo)
                encoder.releaseOutputBuffer(encIdx, false)
                if (bufInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outDone = true
            }
        }
        decoder.stop(); decoder.release(); encoder.stop(); encoder.release(); ex2.release()
    }

    // ── XOR obfuscation ───────────────────────────────────────────────────────
    private fun xorFile(src: File, dst: File) {
        val key = AD17_KEY
        FileInputStream(src).use { inp ->
            FileOutputStream(dst).use { out ->
                val buf = ByteArray(65536)
                var read: Int
                var pos  = 0
                while (inp.read(buf).also { read = it } != -1) {
                    for (i in 0 until read) buf[i] = (buf[i].toInt() xor key[pos % key.size].toInt()).toByte().also { pos++ }
                    out.write(buf, 0, read)
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun copyToCache(ctx: Context, afd: android.content.res.AssetFileDescriptor): String {
        val tmp = File(ctx.cacheDir, "ad17_src_${System.currentTimeMillis()}.tmp")
        afd.createInputStream().use { inp -> FileOutputStream(tmp).use { inp.copyTo(it) } }
        return tmp.absolutePath
    }

    private fun findTrack(extractor: MediaExtractor, prefix: String): Int {
        for (i in 0 until extractor.trackCount) {
            val mime = extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith(prefix)) return i
        }
        return -1
    }

    private fun uniqueFile(dir: File, base: String, ext: String): File {
        var f = File(dir, "$base.$ext")
        var n = 1
        while (f.exists()) { f = File(dir, "${base}_$n.$ext"); n++ }
        return f
    }

    private fun Int.roundToEven() = if (this % 2 == 0) this else this + 1

    private fun ensureNotCancelled() {
        if (currentJob?.isCancelled == true) throw CancellationException("Cancelled")
    }

    companion object {
        // Same lightweight XOR key used by ThumbnailUtils for reversible encoding.
        val AD17_KEY = byteArrayOf(
            0x41, 0x44, 0x31, 0x37, 0x5F, 0x4B, 0x45, 0x59,
            0x5F, 0x41, 0x55, 0x52, 0x41, 0x4C, 0x59, 0x58
        )
    }
}
