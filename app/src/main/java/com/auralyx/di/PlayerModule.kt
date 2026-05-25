package com.auralyx.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun provideTrackSelector(@ApplicationContext ctx: Context) = DefaultTrackSelector(ctx)

    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext ctx: Context,
        ts: DefaultTrackSelector
    ): ExoPlayer {
        // Premium audio attributes — handle audio focus, use MUSIC content type
        val audioAttr = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        // Buffering tuned for smooth gapless playback and fast start
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs         */ 15_000,
                /* maxBufferMs         */ 60_000,
                /* bufferForPlaybackMs */ 1_500,   // fast first-play
                /* bufferForPlaybackAfterRebuffer */ 5_000
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        return ExoPlayer.Builder(ctx)
            .setTrackSelector(ts)
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttr, /* handleAudioFocus= */ true)
            .setHandleAudioBecomingNoisy(true)   // pause on headphone unplug
            .build()
            .also { player ->
                player.skipSilenceEnabled = false   // preserve dynamics
            }
    }
}
