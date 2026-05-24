package com.auralyx.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.auralyx.R
import com.auralyx.player.AuralyxPlayer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Media3 MediaSessionService.
 *
 * ColorOS / MIUI / EMUI aggressive-kill mitigations:
 *  - START_STICKY so the system recreates the service after process kill
 *  - stopWithTask=false in manifest so playback survives task removal
 *  - onTaskRemoved keeps alive if actively playing
 *  - IMPORTANCE_LOW channel avoids heads-up interruptions while still
 *    posting a persistent notification that prevents background kills
 */
@UnstableApi
@AndroidEntryPoint
class AuralyxPlaybackService : MediaSessionService() {

    @Inject lateinit var auralyxPlayer: AuralyxPlayer
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        mediaSession = MediaSession.Builder(this, auralyxPlayer.exoPlayer)
            .setId("auralyx_session")
            .build()

        val provider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(CHANNEL_ID)
            .setNotificationId(NOTIF_ID)
            .build()
            .also { it.setSmallIcon(android.R.drawable.ic_media_play) }

        setMediaNotificationProvider(provider)
    }

    override fun onGetSession(info: MediaSession.ControllerInfo) = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY   // ColorOS: ensure service is restarted
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player ?: run { stopSelf(); return }
        // Keep alive if playing; stop cleanly otherwise
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "Auralyx Playback", NotificationManager.IMPORTANCE_LOW)
                .apply {
                    description = "Music playback controls"
                    setShowBadge(false)
                    setBypassDnd(false)
                }
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    companion object {
        const val CHANNEL_ID = "auralyx_playback"
        const val NOTIF_ID   = 1001
    }
}
