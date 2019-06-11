package com.example.musicplayer

import android.app.Notification
import android.content.Intent
import android.media.MediaPlayer
import android.media.AudioManager
import android.os.PowerManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.musicplayer.model.MusicController
import com.example.musicplayer.model.Song


class MediaPlaybackService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener {

    lateinit var songs: List<Song>
    private lateinit var mediaPlayer: MediaPlayer
    lateinit var musicController: MusicController
    private var positon: Int = 0
    private val binder = MusicBinder()

    inner class MusicBinder : Binder() {
        fun getService(): MediaPlaybackService = this@MediaPlaybackService
    }

    override fun onCreate() {
        super.onCreate()

        mediaPlayer = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setOnPreparedListener(this@MediaPlaybackService)
            setOnErrorListener(this@MediaPlaybackService)
            setOnCompletionListener(this@MediaPlaybackService)
        }
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        mediaPlayer.start()

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification: Notification = NotificationCompat.Builder(this, MainActivity.NOTIFICATION_CHANNEL_ID_KEY)
            .setContentTitle(songs[positon].title)
            .setSmallIcon(R.drawable.music_note)
            .setContentText(songs[positon].artist)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(PLAYER_NOTIFICATION_ID, notification)
        musicController.show(0)
    }

    override fun onError(mediaPlayer: MediaPlayer?, what: Int, extra: Int): Boolean {
        mediaPlayer!!.reset()
        return false
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        if (mediaPlayer.currentPosition > 0) {
            mediaPlayer.reset()
            setNext()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        mediaPlayer.stop()
        mediaPlayer.release()
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        stopForeground(true)
    }

    fun prepareSong() {
        mediaPlayer.reset()

        val songToPlayId = songs[positon].id
        val trackUri = ContentUris.withAppendedId(
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            songToPlayId
        )

        try {
            mediaPlayer.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mediaPlayer.prepare()
    }

    fun setSong(songIndex: Int) {
        positon = songIndex
    }

    fun getPosition(): Int {
        return mediaPlayer.currentPosition
    }

    fun getDuration(): Int {
        return mediaPlayer.duration
    }

    fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    fun pausePlayer() {
        mediaPlayer.pause()
    }

    fun seek(position: Int) {
        mediaPlayer.seekTo(position)
    }

    fun start() {
        mediaPlayer.start()
    }

    fun setPrevious() {
        positon--
        if (positon < 0) {
            positon = songs.size - 1
        }
        prepareSong()
    }

    fun setNext() {
        positon++
        if (positon >= songs.size) {
            positon = 0
        }
        prepareSong()
    }


    companion object {
        const val PLAYER_NOTIFICATION_ID = 123321
    }
}
