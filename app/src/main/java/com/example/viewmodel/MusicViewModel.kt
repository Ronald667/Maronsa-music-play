package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BeatStreamDatabase
import com.example.data.local.DownloadedSongEntity
import com.example.data.remote.ItunesSong
import com.example.data.repository.MusicRepository
import com.example.player.MusicPlayerManager
import com.example.player.PlaybackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BeatStreamDatabase.getDatabase(application)
    private val repository = MusicRepository(application, database.songDao())
    private val playerManager = MusicPlayerManager(application)

    val playbackState: StateFlow<PlaybackState> = playerManager.playbackState

    val downloadedSongs: StateFlow<List<DownloadedSongEntity>> = repository.downloadedSongs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchResults = MutableStateFlow<List<ItunesSong>>(emptyList())
    val searchResults: StateFlow<List<ItunesSong>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _geminiRecommendations = MutableStateFlow<List<String>>(emptyList())
    val geminiRecommendations: StateFlow<List<String>> = _geminiRecommendations.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        searchSongs("Top Hits")
    }

    fun searchSongs(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isSearching.value = true
            val results = repository.searchOnlineSongs(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun downloadSong(song: ItunesSong) {
        viewModelScope.launch {
            val success = repository.downloadSong(song)
            if (success) {
                _toastMessage.value = "Saved '${song.trackName}' offline!"
            } else {
                _toastMessage.value = "Failed to download song."
            }
        }
    }

    fun deleteDownloadedSong(trackId: Long) {
        viewModelScope.launch {
            repository.deleteDownloadedSong(trackId)
            _toastMessage.value = "Removed from offline library."
        }
    }

    fun playSong(trackId: Long, title: String, artist: String, artworkUrl: String, audioUrl: String) {
        playerManager.playSong(trackId, title, artist, artworkUrl, audioUrl)
    }

    fun playDownloadedSong(song: DownloadedSongEntity) {
        playerManager.playSong(song.trackId, song.title, song.artist, song.artworkUrl, song.localFilePath)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun loadAiRecommendations(mood: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val queries = repository.getGeminiMusicRecommendations(mood)
            _geminiRecommendations.value = queries
            _isAiLoading.value = false
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
