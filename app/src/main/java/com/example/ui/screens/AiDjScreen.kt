package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.MusicViewModel

@Composable
fun AiDjScreen(
    viewModel: MusicViewModel
) {
    val recommendations by viewModel.geminiRecommendations.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    val moods = listOf("Chill Lofi Beats", "Energetic Workout", "Late Night Jazz", "Focus Deep Work", "Party Anthems", "Happy Acoustic")
    var selectedMood by remember { mutableStateOf("Chill Lofi Beats") }

    LaunchedEffect(selectedMood) {
        viewModel.loadAiRecommendations(selectedMood)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI DJ",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Gemini AI Music DJ",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Choose your mood and let AI curate your playlist",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(moods) { mood ->
                FilterChip(
                    selected = selectedMood == mood,
                    onClick = { selectedMood = mood },
                    label = { Text(mood) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.loadAiRecommendations(selectedMood)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Playlist with AI")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isAiLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("AI is curating your vibe...")
                }
            }
        } else {
            Text(
                text = "AI Suggested Queries for '$selectedMood':",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recommendations) { query ->
                    AssistChip(
                        onClick = { viewModel.searchSongs(query) },
                        label = { Text(query) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Curated Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(searchResults) { song ->
                    SongCard(
                        song = song,
                        onPlayClick = {
                            val url = song.previewUrl ?: ""
                            val title = song.trackName ?: "Unknown"
                            val artist = song.artistName ?: "Unknown"
                            val artwork = song.getHighResArtwork()
                            val id = song.trackId ?: System.currentTimeMillis()
                            viewModel.playSong(id, title, artist, artwork, url)
                        },
                        onDownloadClick = {
                            viewModel.downloadSong(song)
                        }
                    )
                }
            }
        }
    }
}
