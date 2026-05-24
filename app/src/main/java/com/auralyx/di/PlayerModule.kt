package com.auralyx.di
import android.content.Context
import androidx.media3.common.AudioAttributes; import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import dagger.Module; import dagger.Provides; import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module @InstallIn(SingletonComponent::class)
object PlayerModule {
    @Provides @Singleton
    fun provideTrackSelector(@ApplicationContext ctx:Context) = DefaultTrackSelector(ctx)
    @Provides @Singleton
    fun provideExoPlayer(@ApplicationContext ctx:Context, ts:DefaultTrackSelector): ExoPlayer =
        ExoPlayer.Builder(ctx).setTrackSelector(ts)
            .setAudioAttributes(AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(), true)
            .build()
}
