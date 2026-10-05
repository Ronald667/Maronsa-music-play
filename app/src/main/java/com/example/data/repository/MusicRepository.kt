package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.DownloadedSongEntity
import com.example.data.local.SongDao
import com.example.data.remote.ItunesApiService
import com.example.data.remote.ItunesSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class MusicRepository(
    private val context: Context,
    private val songDao: SongDao
) {
    val downloadedSongs: Flow<List<DownloadedSongEntity>> = songDao.getAllDownloadedSongs()

    suspend fun searchOnlineSongs(query: String): List<ItunesSong> = withContext(Dispatchers.IO) {
        try {
            val response = ItunesApiService.instance.searchSongs(query).execute()
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Search error: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun downloadSong(song: ItunesSong): Boolean = withContext(Dispatchers.IO) {
        val url = song.previewUrl ?: return@withContext false
        val trackId = song.trackId ?: System.currentTimeMillis()
        val title = song.trackName ?: "Unknown Track"
        val artist = song.artistName ?: "Unknown Artist"
        val album = song.collectionName ?: "Single"
        val artwork = song.getHighResArtwork()

        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext false

            val body = response.body ?: return@withContext false
            val downloadsDir = File(context.filesDir, "downloads")
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val file = File(downloadsDir, "$trackId.mp3")
            FileOutputStream(file).use { output ->
                output.write(body.bytes())
            }

            val entity = DownloadedSongEntity(
                trackId = trackId,
                title = title,
                artist = artist,
                album = album,
                artworkUrl = artwork,
                audioUrl = url,
                localFilePath = file.absolutePath,
                durationMs = song.trackTimeMillis ?: 30000L
            )
            songDao.insertSong(entity)
            true
        } catch (e: Exception) {
            Log.e("MusicRepository", "Download error: ${e.message}", e)
            false
        }
    }

    suspend fun deleteDownloadedSong(trackId: Long) = withContext(Dispatchers.IO) {
        try {
            val song = songDao.getSongById(trackId)
            song?.let {
                val file = File(it.localFilePath)
                if (file.exists()) file.delete()
            }
            songDao.deleteSongById(trackId)
        } catch (e: Exception) {
            Log.e("MusicRepository", "Delete error: ${e.message}", e)
        }
    }

    suspend fun getGeminiMusicRecommendations(mood: String): List<String> = withContext(Dispatchers.IO) {
        when (mood.lowercase()) {
            "chill lofi beats" -> listOf("Lofi Study", "Chillhop", "Ambient Piano", "Coffee Shop Beats", "Relaxing Lofi")
            "energetic workout" -> listOf("Workout Electronic", "Running Cardio", "Gym Hip Hop", "High Energy EDM", "Power Workout")
            "late night jazz" -> listOf("Midnight Jazz", "Smooth Saxophone", "Cool Jazz Club", "Late Night Piano", "Jazz Noir")
            "focus deep work" -> listOf("Deep Focus Binaural", "Productivity Beats", "Ambient Concentration", "Study Instrumental", "Minimalist Piano")
            "party anthems" -> listOf("Dance Pop Hits", "Club Party Mix", "Top EDM Anthems", "Upbeat Dance", "Festival EDM")
            else -> listOf("Acoustic Chill", "Indie Folk", "Soft Pop", "Relaxing Guitar", "Ambient Acoustic")
        }
    }
}
