package com.example.musicplayer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import com.example.musicplayer.model.Song
import com.example.musicplayer.model.MusicController
import java.io.File


class MainActivity : AppCompatActivity() {
    lateinit var musicController: MusicController
    private val songs: MutableList<Song> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()
        setSongList()
        songs_view.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = SongAdapter(songs)
        }

        musicController = MusicController(this, songs)
    }

    public override fun onStart() {
        super.onStart()
        Intent(this, MediaPlaybackService::class.java).also { playIntent ->
            bindService(playIntent, musicController.serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(musicController.serviceConnection)
        musicController.disconnectService()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_READ_EXTERNAL_STORAGE
            )
        } else {
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_READ_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                } else {
                    finish()
                }
                return
            }
            else -> {
            }
        }
    }

    fun setSongList() {
        val musicResolver = contentResolver
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//        val musicUri = Uri.fromFile(
//            File(Environment.getExternalStorageDirectory().getPath() + "/Music/Torches/")
//        )
        val musicCursor = musicResolver.query(musicUri, null, null, null, null)
        val dataRetriever = MediaMetadataRetriever()
        var songsToLoad = 50

        if (musicCursor != null && musicCursor.moveToFirst()) {
            val titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val dataColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA)

            do {
                val id = musicCursor.getLong(idColumn)
                val title = musicCursor.getString(titleColumn)
                val artist = musicCursor.getString(artistColumn)
                val path = musicCursor.getString(dataColumn)
                dataRetriever.setDataSource(path)
                val cover = dataRetriever.embeddedPicture

                songs.add(Song(id, title, artist, cover))
            } while (musicCursor.moveToNext() && songsToLoad-- > 1)

            musicCursor.close()
        }
    }

//    //play next
//    private fun playNext() {
//        musicSrv.playNext()
//        controller.show(0)
//    }
//
//    //play previous
//    private fun playPrev() {
//        musicSrv.playPrev()
//        controller.show(0)
//    }
//
    companion object {
        private const val PERMISSION_READ_EXTERNAL_STORAGE = 62137
        const val NOTIFICATION_CHANNEL_ID_KEY = "notification_channel_id"
        const val NOTIFICATION_CHANNEL_NAME = "notification_channel"
    }
}
