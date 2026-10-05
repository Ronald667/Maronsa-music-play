package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_songs")
data class DownloadedSongEntity(
    @PrimaryKey val trackId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String,
    val audioUrl: String,
    val localFilePath: String,
    val durationMs: Long,
    val downloadedAt: Long = System.currentTimeMillis()
)
