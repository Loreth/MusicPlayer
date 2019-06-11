package com.example.musicplayer

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.musicplayer.model.Song
import android.view.LayoutInflater
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior.setTag
import android.R
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.song.view.*
import java.util.ArrayList


class SongAdapter(private val songs: MutableList<Song>) : RecyclerView.Adapter<SongAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(com.example.musicplayer.R.layout.song, parent, false)

        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]

        holder.title.text = song.title
        holder.artist.text = song.artist
        setCover(song.albumCover, holder.albumCover)
    }

    fun setCover(cover: ByteArray?, imageView: ImageView) {
        if (cover != null) {
            val coverImage = BitmapFactory.decodeByteArray(cover, 0, cover.size)
            imageView.setImageBitmap(coverImage)
        } else {
            imageView.setImageResource(com.example.musicplayer.R.drawable.music_note)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                (itemView.context as MainActivity).musicController.songPicked(adapterPosition)
            }
        }

        val title = itemView.song_name
        val artist = itemView.song_artist
        val albumCover = itemView.album_cover
    }
}