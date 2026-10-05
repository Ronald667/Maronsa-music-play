package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ItunesSearchResponse(
    @Json(name = "resultCount") val resultCount: Int,
    @Json(name = "results") val results: List<ItunesSong>
)

@JsonClass(generateAdapter = true)
data class ItunesSong(
    @Json(name = "trackId") val trackId: Long?,
    @Json(name = "trackName") val trackName: String?,
    @Json(name = "artistName") val artistName: String?,
    @Json(name = "collectionName") val collectionName: String?,
    @Json(name = "artworkUrl100") val artworkUrl100: String?,
    @Json(name = "previewUrl") val previewUrl: String?,
    @Json(name = "trackTimeMillis") val trackTimeMillis: Long?
) {
    fun getHighResArtwork(): String {
        return artworkUrl100?.replace("100x100bb.jpg", "600x600bb.jpg") ?: ""
    }
}
