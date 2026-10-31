package com.idlix.tv.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubdlResponse(
    val status: Boolean = false,
    val results: List<SubdlItem> = emptyList(),
    val subtitles: List<SubdlSubtitle> = emptyList()
)

@Serializable
data class SubdlItem(
    val sd_id: Long? = null,
    val type: String? = null,
    val name: String? = null,
    val imdb_id: String? = null,
    val tmdb_id: Long? = null,
    val year: Int? = null
)

@Serializable
data class SubdlSubtitle(
    @SerialName("release_name") val releaseName: String? = null,
    val name: String? = null,
    val lang: String? = null,
    val author: String? = null,
    val url: String = "",
    val language: String? = null,
    val season: Int? = null,
    val episode: Int? = null
) {
    val fullDownloadUrl: String
        get() = if (url.startsWith("http")) url else "https://dl.subdl.com$url"
}
