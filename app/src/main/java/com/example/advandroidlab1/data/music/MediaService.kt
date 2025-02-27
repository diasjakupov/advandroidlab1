package com.example.advandroidlab1.data.music

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.advandroidlab1.R
import com.example.advandroidlab1.domain.AudioFile

class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var playlist: List<AudioFile> = arrayListOf()
    private var currentIndex: Int = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                intent.getParcelableArrayListExtra<AudioFile>(EXTRA_PLAYLIST)?.let { files ->
                    if (files.isNotEmpty()) {
                        playlist = files
                        currentIndex = 0
                    }
                }
                playMusic()
            }
            ACTION_PAUSE -> pauseMusic()
            ACTION_STOP -> stopMusic()
            ACTION_NEXT -> nextTrack()
            ACTION_PREVIOUS -> previousTrack()
        }
        return START_STICKY
    }

    private fun playMusic() {
        if (playlist.isEmpty()) return

        if (mediaPlayer?.isPlaying == true) {
            updateNotification()
            return
        }

        if (mediaPlayer == null) {
            startTrack()
        } else {
            mediaPlayer?.start()
            updateNotification()
            broadcastCurrentSong(playlist[currentIndex])
        }
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    private fun pauseMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                updateNotification()
            }
        }
    }

    private fun stopMusic() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
            mediaPlayer = null
        }
        sendStopBroadcast()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun nextTrack() {
        if (playlist.isEmpty()) return
        currentIndex++
        if (currentIndex >= playlist.size) {
            stopMusic()
        } else {
            startTrack()
        }
    }

    private fun previousTrack() {
        if (playlist.isEmpty()) return
        currentIndex = if (currentIndex > 0) currentIndex - 1 else 0
        startTrack()
    }

    private fun startTrack() {
        val audioFile = playlist[currentIndex]
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@MusicService, audioFile.uri)
            setOnPreparedListener { mp ->
                mp.start()
                updateNotification()
                broadcastCurrentSong(audioFile)
            }
            setOnCompletionListener {
                nextTrack()
            }
            prepareAsync()
        }
    }

    private fun buildNotification(): android.app.Notification {
        val channelId = "MusicServiceChannel"
        val channelName = "Music Playback"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val playPauseActionIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, MusicService::class.java).apply {
                action = if (mediaPlayer?.isPlaying == true) ACTION_PAUSE else ACTION_PLAY
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val nextActionIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, MusicService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_IMMUTABLE
        )

        val previousActionIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, MusicService::class.java).apply { action = ACTION_PREVIOUS },
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopActionIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, MusicService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        val isPlaying = mediaPlayer?.isPlaying == true
        val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val playPauseTitle = if (isPlaying) "Pause" else "Play"

        val audioTitle = if (playlist.isNotEmpty() && currentIndex < playlist.size) {
            playlist[currentIndex].title
        } else {
            "Music Player"
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(audioTitle)
            .setContentText("Playing music")
            .setSmallIcon(R.drawable.ic_note)
            .setOngoing(true)
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_previous,
                    "Previous",
                    previousActionIntent
                )
            )
            .addAction(
                NotificationCompat.Action(
                    playPauseIcon,
                    playPauseTitle,
                    playPauseActionIntent
                )
            )
            .addAction(NotificationCompat.Action(R.drawable.ic_next, "Next", nextActionIntent))
            .addAction(NotificationCompat.Action(R.drawable.ic_stop, "Stop", stopActionIntent))
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun broadcastCurrentSong(audioFile: AudioFile) {
        val intent = Intent(ACTION_UPDATE_SONG).apply {
            putExtra(EXTRA_CURRENT_SONG, audioFile.title)
        }
        sendBroadcast(intent)
    }

    private fun sendStopBroadcast() {
        sendBroadcast(Intent(ACTION_NO_SONG))
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        const val ACTION_PLAY = "com.example.advandroidlab1.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.advandroidlab1.ACTION_PAUSE"
        const val ACTION_STOP = "com.example.advandroidlab1.ACTION_STOP"
        const val ACTION_NEXT = "com.example.advandroidlab1.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.advandroidlab1.ACTION_PREVIOUS"
        const val ACTION_UPDATE_SONG = "com.example.advandroidlab1.ACTION_UPDATE_SONG"
        const val ACTION_NO_SONG = "com.example.advandroidlab1.ACTION_NO_SONG"
        const val NOTIFICATION_ID = 1
        const val EXTRA_PLAYLIST = "com.example.advandroidlab1.EXTRA_PLAYLIST"
        const val EXTRA_CURRENT_SONG = "com.example.advandroidlab1.EXTRA_CURRENT_SONG"
    }
}