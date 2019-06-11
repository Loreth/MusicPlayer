package com.example.musicplayer.model

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.MediaController
import com.example.musicplayer.MainActivity
import com.example.musicplayer.MediaPlaybackService
import com.example.musicplayer.R

class MusicController(context: Context) :
    MediaController.MediaPlayerControl, MediaController(context) {

    init {
        setPrevNextListeners({ mediaService.setNext() },
            { mediaService.setPrevious() })
        setAnchorView((context as MainActivity).findViewById(R.id.activity_main_layout))
        setMediaPlayer(this)
        isEnabled = true
    }

    constructor(context: Context, songs: List<Song>) : this(context) {
        this.songs = songs
    }

    lateinit var mediaService: MediaPlaybackService
    private var isServiceBound: Boolean = false
    private lateinit var songs: List<Song>

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            connectService((service as MediaPlaybackService.MusicBinder).getService(), songs)
            mediaService.musicController = this@MusicController
        }

        override fun onServiceDisconnected(name: ComponentName) {
            disconnectService()
        }
    }

    fun songPicked(index: Int) {
        mediaService.setSong(index)
        mediaService.prepareSong()
        show()
    }

    override fun hide() {
    }

    override fun isPlaying(): Boolean {

        return if (isServiceBound) {
            mediaService.isPlaying()
        } else {
            false
        }
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getDuration(): Int {
        return if (isServiceBound && mediaService.isPlaying()) {
            mediaService.getDuration()
        } else {
            0
        }
    }

    override fun pause() {
        mediaService.pausePlayer()
    }

    override fun getBufferPercentage(): Int {
        return if (isServiceBound && mediaService.isPlaying()) {
            mediaService.getDuration()
        } else {
            0
        }
    }

    override fun seekTo(pos: Int) {
        mediaService.seek(pos)
    }

    override fun getCurrentPosition(): Int {
        return if (isServiceBound && mediaService.isPlaying()) {
            mediaService.getPosition()
        } else {
            0
        }
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun start() {
        mediaService.start()
    }

    override fun getAudioSessionId(): Int {
        return AUDIO_SESSION_ID_KEY
    }

    override fun canPause(): Boolean {
        return true
    }

    fun connectService(musicService: MediaPlaybackService, songs: List<Song>) {
        this.mediaService = musicService
        mediaService.songs = songs
        isServiceBound = true
    }

    fun disconnectService() {
        isServiceBound = false
    }


    companion object {
        private const val AUDIO_SESSION_ID_KEY = 652346
    }
}